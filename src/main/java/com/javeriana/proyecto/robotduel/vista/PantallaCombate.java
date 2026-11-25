package com.javeriana.proyecto.robotduel.vista;

import com.javeriana.proyecto.robotduel.archivos.ArchivoCPU;
import com.javeriana.proyecto.robotduel.archivos.GestorEstrategia;
import com.javeriana.proyecto.robotduel.archivos.GestorRobots;
import com.javeriana.proyecto.robotduel.archivos.RegistroPartida;
import com.javeriana.proyecto.robotduel.logica.Combate;
import com.javeriana.proyecto.robotduel.logica.CombateListener;
import com.javeriana.proyecto.robotduel.logica.EstrategiaArchivo;
import com.javeriana.proyecto.robotduel.logica.EstrategiaManual;
import com.javeriana.proyecto.robotduel.logica.FuenteMovimientos;
import com.javeriana.proyecto.robotduel.logica.ImpactoAleatorio;
import com.javeriana.proyecto.robotduel.logica.RegistroTurno;
import com.javeriana.proyecto.robotduel.logica.RivalCPU;
import com.javeriana.proyecto.robotduel.modelo.Efecto;
import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;
import com.javeriana.proyecto.robotduel.modelo.Movimiento;
import com.javeriana.proyecto.robotduel.modelo.ResultadoAtaque;
import com.javeriana.proyecto.robotduel.modelo.Robot;
import com.javeriana.proyecto.robotduel.red.Conexion;
import com.javeriana.proyecto.robotduel.red.PartidaEnRed;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.List;

// combate y fin de la partida
// El combate corre en su propio hilo
class PantallaCombate implements CombateListener {

    // Pausa entre movimientos en modo archivo para que se alcance a ver el combate
    private static final long PAUSA_ESTRATEGIA_MS = 900;
    // Tiempo que queda cada mensaje en la caja de diálogo antes de pasar al siguiente
    private static final double PAUSA_MENSAJE_MS = 1000;

    private static final Color ROJO = Color.web("#d03838");
    private static final Color AZUL = Color.web("#3058c8");
    private static final Color ORO = Color.web("#c89000");
    private static final Color VERDE = Color.web("#2f9e44");
    private static final Color NARANJA = Color.web("#e06810");

    private record Opcion(String movimiento, Color color, String ayuda) {
    }

    private static final List<Opcion> OPCIONES = List.of(
            new Opcion("ATTACK", ROJO, "Ataque normal: ATK² / (ATK + DEF del rival)"),
            new Opcion("SPECIAL", AZUL, "Ataque especial: (ATK + 0.5·NRG)² / (ATK + 0.5·NRG + DEF). La energía vuelve a 0"),
            new Opcion("DODGE", ORO, "Esquivar el siguiente ataque: una vez 50 %, dos veces 75 %"),
            new Opcion("HEAL", VERDE, "Recuperar 10 puntos de vida"),
            new Opcion("CHARGE", NARANJA, "Cargar 10 puntos de energía"));

    // Copia de los valores de un robot tomada en el hilo del combate
    private record Foto(int vida, int energia, int esquives) {
        static Foto de(EstadoCombate estado) {
            return new Foto(estado.getVida(), estado.getEnergia(), estado.getEsquivesActivos());
        }
    }

    private record Entrada(String texto, Color color) {
    }

    private final MechaDuelApp app;
    private final Conexion conexion;
    private final Robot miRobot;
    private final Robot robotRival;
    private final List<Movimiento> estrategia;
    private final boolean crearArchivo;
    private final String nombreMio;
    private final String nombreRival;
    private final ArchivoCPU.ConfiguracionCPU configuracionCPU; // null = partida en red

    // El combate y el estado de cada robot
    private Combate combate;
    private EstadoCombate miEstado;
    private EstadoCombate estadoRival;
    private EstrategiaManual manual;
    private RegistroPartida registro;

    private final ColaAnimaciones cola = new ColaAnimaciones();
    private Arena arena;
    private PanelInfo infoMio;
    private PanelInfo infoRival;
    private final PixelText dialogo = new PixelText("", 3).conAnchoMaximo(330);
    private final VBox menu = new VBox(10);
    private final GridPane botones = new GridPane();
    private final VBox listaEstrategia = new VBox(6);
    private final ListView<Entrada> bitacora = new ListView<>();

    // Último estado conocido de cada robot
    private Foto previaMia;
    private Foto previaRival;
    private int turnoEstrategia;

    PantallaCombate(MechaDuelApp app, Conexion conexion, Robot miRobot, Robot robotRival, List<Movimiento> estrategia,
                    boolean crearArchivo, ArchivoCPU.ConfiguracionCPU configuracionCPU) {
        this.app = app;
        this.conexion = conexion;
        this.miRobot = miRobot;
        this.robotRival = robotRival;
        this.estrategia = estrategia;
        this.crearArchivo = crearArchivo;
        this.configuracionCPU = configuracionCPU;
        this.nombreMio = PixelFont.normalizar(miRobot.getNombre());
        this.nombreRival = PixelFont.normalizar(robotRival.getNombre());
    }

    Parent getVista() {
        arena = new Arena(ModeloMecha.de(miRobot), ModeloMecha.de(robotRival));
        StackPane marco = new StackPane(arena);
        marco.getStyleClass().add("marco-arena");
        marco.setMaxWidth(Arena.ANCHO + 16);

        infoMio = new PanelInfo(miRobot, "TU MECHA");
        infoRival = new PanelInfo(robotRival, "OPONENTE");
        HBox infos = new HBox(10, infoMio.caja, infoRival.caja);
        HBox.setHgrow(infoMio.caja, Priority.ALWAYS);
        HBox.setHgrow(infoRival.caja, Priority.ALWAYS);

        VBox cajaDialogo = Componentes.caja(0, dialogo);
        cajaDialogo.setPrefWidth(380);
        cajaDialogo.setMinWidth(380);
        cajaDialogo.setMinHeight(165);

        botones.setHgap(8);
        botones.setVgap(8);
        for (int i = 0; i < OPCIONES.size(); i++) {
            Opcion opcion = OPCIONES.get(i);
            PixelBoton boton = new PixelBoton(opcion.movimiento(), 3, opcion.color());
            boton.setAyuda(opcion.ayuda());
            boton.setOnAction(() -> elegir(opcion.movimiento()));
            boton.setPrefWidth(178);
            botones.add(boton, i % 2, i / 2);
        }
        habilitarBotones(false);
        if (estrategia == null) {
            menu.getChildren().setAll(botones);
        } else {
            pintarEstrategia();
            menu.getChildren().setAll(listaEstrategia);
        }
        VBox cajaMenu = Componentes.caja(0, menu);
        HBox.setHgrow(cajaMenu, Priority.ALWAYS);
        cajaMenu.setMinHeight(165);

        HBox abajo = new HBox(10, cajaDialogo, cajaMenu);
        VBox izquierda = new VBox(10, marco, infos, abajo);
        izquierda.setMaxWidth(Arena.ANCHO + 16);
        izquierda.setMinWidth(Arena.ANCHO + 16);

        bitacora.setPlaceholder(Componentes.texto("AUN NO HAY\nMOVIMIENTOS", 2, Componentes.TEXTO_SUAVE));
        Componentes.celdasPixel(bitacora, 205, Entrada::texto, Entrada::color);
        VBox.setVgrow(bitacora, Priority.ALWAYS);
        VBox derecha = Componentes.caja(10, Componentes.texto("REGISTRO", 3),
                Componentes.texto("AZUL: TU  ROJO: RIVAL", 2, Componentes.TEXTO_SUAVE), bitacora);
        derecha.setPrefWidth(270);
        derecha.setMinWidth(270);
        HBox.setHgrow(derecha, Priority.ALWAYS);

        HBox raiz = new HBox(12, izquierda, derecha);
        raiz.getStyleClass().add("pantalla");
        raiz.setStyle("-fx-padding: 12;");
        return raiz;
    }

    // Arranca el combate en un hilo aparte y lo devuelve
    Thread comenzar() {
        FuenteMovimientos miFuente;
        if (estrategia != null) {
            EstrategiaArchivo archivo = new EstrategiaArchivo(estrategia);
            miFuente = () -> {
                Thread.sleep(PAUSA_ESTRATEGIA_MS);
                return archivo.siguienteMovimiento();
            };
        } else {
            // A man el jugador elige cada movimiento Si no había archivo de estrategia,
            // estrategia.txt se va creando durante el combate con lo que elige
            manual = crearArchivo ? new EstrategiaManual(new GestorEstrategia()) : new EstrategiaManual();
            miFuente = manual;
        }
        if (configuracionCPU == null) {
            PartidaEnRed partida = PartidaEnRed.crear(conexion, miRobot, robotRival, miFuente);
            combate = partida.getCombate();
            miEstado = partida.getMiEstado();
            estadoRival = partida.getEstadoRival();
        } else {
            // sus movimientos salen de cpu.txt y el HIT/DODGE se decid1e aquí mismo
            miEstado = new EstadoCombate(miRobot);
            estadoRival = new EstadoCombate(robotRival);
            FuenteMovimientos cpu = new RivalCPU(new EstrategiaArchivo(configuracionCPU.movimientos()),
                    PAUSA_ESTRATEGIA_MS);
            combate = new Combate(miEstado, miFuente, estadoRival, cpu, new ImpactoAleatorio(), true);
        }
        registro = new RegistroPartida(miRobot.getNombre());
        combate.agregarListener(registro);
        combate.agregarListener(this);
        previaMia = Foto.de(miEstado);
        previaRival = Foto.de(estadoRival);
        infoMio.mostrar(previaMia);
        infoRival.mostrar(previaRival);
        cola.agregar(listo -> escribir("COMIENZA EL DUELO! " + nombreMio + " VS " + nombreRival + "!", listo));

        Thread hilo = new Thread(() -> {
            try {
                combate.jugar();
            } catch (InterruptedException e) {
                // el jugador salió de la partida
            } catch (RuntimeException e) {
                Platform.runLater(() -> {
                    cola.vaciar();
                    app.errorDeRed(e.getMessage());
                });
            }
        }, "combate");
        hilo.setDaemon(true);
        hilo.start();
        return hilo;
    }

    // Eventos del combate

    @Override
    public void turnoIniciado(EstadoCombate actor) {
        boolean esMio = actor == miEstado;
        Foto mia = Foto.de(miEstado);
        Foto rival = Foto.de(estadoRival);
        Platform.runLater(() -> alIniciarTurno(esMio, mia, rival));
    }

    @Override
    public void turnoJugado(RegistroTurno turno) {
        Foto mia = Foto.de(miEstado);
        Foto rival = Foto.de(estadoRival);
        Platform.runLater(() -> alJugarTurno(turno, mia, rival));
    }

    @Override
    public void combateTerminado(EstadoCombate ganador) {
        boolean gane = ganador == miEstado;
        Foto mia = Foto.de(miEstado);
        Foto rival = Foto.de(estadoRival);
        Platform.runLater(() -> alTerminar(gane, mia, rival));
    }

    // Lo que se muestra en cada evento

    private void alIniciarTurno(boolean esMio, Foto mia, Foto rival) {
        efectosDeEstado(esMio, mia, rival);
        String nombre = esMio ? nombreMio : nombreRival;
        cola.agregar(listo -> {
            infoMio.marcarTurno(esMio);
            infoRival.marcarTurno(!esMio);
            if (!esMio) {
                escribir(nombreRival + " ESTA PENSANDO...", 0, listo);
            } else if (manual != null) {
                escribir("QUE HARA " + nombre + "?", 0, () -> {
                    habilitarBotones(true);
                    listo.run();
                });
            } else {
                escribir(nombre + " SIGUE SU ESTRATEGIA...", 0, listo);
            }
        });
    }

    // Mensajes de Quemadura / Regeneración / Núcleo Activo a partir de lo que cambió
    private void efectosDeEstado(boolean esMio, Foto mia, Foto rival) {
        Robot robotActor = esMio ? miRobot : robotRival;
        String actor = esMio ? nombreMio : nombreRival;
        String otro = esMio ? nombreRival : nombreMio;
        Foto actorAntes = esMio ? previaMia : previaRival;
        Foto actorAhora = esMio ? mia : rival;
        Foto otroAntes = esMio ? previaRival : previaMia;
        Foto otroAhora = esMio ? rival : mia;

        for (Efecto efecto : robotActor.getEfectos()) {
            switch (Character.toUpperCase(efecto.getLetra())) {
                case 'Q' -> {
                    int dano = otroAntes.vida() - otroAhora.vida();
                    if (dano > 0) {
                        int vida = otroAhora.vida();
                        anotar("QUEMADURA: " + otro + " -" + dano + " HP", Componentes.TEXTO_SUAVE);
                        cola.agregar(listo -> {
                            panel(!esMio).barra.actualizar(vida, () -> { });
                            escribir("LA QUEMADURA DE " + actor + " LE QUITA " + dano + " HP A " + otro + "!", listo);
                        });
                    }
                }
                case 'R' -> {
                    int cura = actorAhora.vida() - actorAntes.vida();
                    if (cura > 0) {
                        int vida = actorAhora.vida();
                        anotar("REGENERACION: " + actor + " +" + cura + " HP", Componentes.TEXTO_SUAVE);
                        cola.agregar(listo -> {
                            panel(esMio).barra.actualizar(vida, () -> { });
                            escribir(actor + " SE REGENERA " + cura + " HP.", listo);
                        });
                    }
                }
                default -> {
                    int carga = actorAhora.energia() - actorAntes.energia();
                    if (carga > 0) {
                        int energia = actorAhora.energia();
                        anotar("NUCLEO ACTIVO: " + actor + " +" + carga + " NRG", Componentes.TEXTO_SUAVE);
                        cola.agregar(listo -> {
                            panel(esMio).mostrarEnergia(energia);
                            escribir("EL NUCLEO ACTIVO DE " + actor + " CARGA " + carga + " DE ENERGIA.", listo);
                        });
                    }
                }
            }
        }
        previaMia = mia;
        previaRival = rival;
    }

    private void alJugarTurno(RegistroTurno turno, Foto mia, Foto rival) {
        boolean esMio = turno.robot().equals(miRobot.getNombre());
        String actor = esMio ? nombreMio : nombreRival;
        String otro = esMio ? nombreRival : nombreMio;
        String movimiento = turno.movimiento();
        Foto actorAntes = esMio ? previaMia : previaRival;
        Foto actorAhora = esMio ? mia : rival;
        Foto otroAntes = esMio ? previaRival : previaMia;
        Foto otroAhora = esMio ? rival : mia;
        previaMia = mia;
        previaRival = rival;

        String linea = "T" + turno.numero() + " " + actor + ": " + movimiento
                + (turno.resultado() == ResultadoAtaque.NO_APLICA ? "" : " (" + turno.resultado() + ")")
                + "\n  " + nombreMio + " " + mia.vida() + " / " + nombreRival + " " + rival.vida();
        Color color = esMio ? AZUL : ROJO;

        cola.agregar(listo -> {
            habilitarBotones(false);
            escribir(actor + " USO " + movimiento + "!", 700, listo);
        });

        switch (movimiento) {
            case "ATTACK", "SPECIAL" -> {
                cola.agregar(listo -> {
                    panel(esMio).mostrarEnergia(actorAhora.energia());
                    listo.run();
                });
                if (turno.resultado() == ResultadoAtaque.HIT) {
                    int dano = otroAntes.vida() - otroAhora.vida();
                    cola.agregar(listo -> {
                        panel(!esMio).mostrarEsquive(otroAhora.esquives());
                        panel(!esMio).barra.actualizar(otroAhora.vida(), listo);
                    });
                    cola.agregar(listo -> escribir("ACERTO! " + otro + " PIERDE " + dano + " HP.", listo));
                } else {
                    cola.agregar(listo -> {
                        panel(!esMio).mostrarEsquive(otroAhora.esquives());
                        listo.run();
                    });
                    cola.agregar(listo -> escribir(otro + " ESQUIVO EL ATAQUE!", listo));
                }
            }
            case "HEAL" -> {
                int cura = actorAhora.vida() - actorAntes.vida();
                cola.agregar(listo -> {
                    panel(esMio).barra.actualizar(actorAhora.vida(), () ->
                            escribir(cura > 0 ? actor + " RECUPERO " + cura + " HP." : actor + " YA TENIA LA VIDA LLENA.", listo));
                });
            }
            case "CHARGE" -> cola.agregar(listo -> {
                panel(esMio).mostrarEnergia(actorAhora.energia());
                escribir(actor + " CARGO 10 DE ENERGIA. (NRG " + actorAhora.energia() + ")", listo);
            });
            default -> cola.agregar(listo -> {
                int porcentaje = actorAhora.esquives() >= 2 ? 75 : 50;
                panel(esMio).mostrarEsquive(actorAhora.esquives());
                escribir(actor + " SE PREPARA PARA ESQUIVAR EL SIGUIENTE ATAQUE (" + porcentaje + "%).", listo);
            });
        }

        cola.agregar(listo -> {
            agregarBitacora(new Entrada(linea, color));
            if (esMio && estrategia != null) {
                turnoEstrategia++;
                pintarEstrategia();
            }
            listo.run();
        });
    }

    private void alTerminar(boolean gane, Foto mia, Foto rival) {
        // Si una quemadura remató al rival, el combate termina sin turno se muestra igual
        if (mia.vida() < previaMia.vida() || rival.vida() < previaRival.vida()) {
            boolean muereMio = mia.vida() < previaMia.vida();
            int vida = muereMio ? mia.vida() : rival.vida();
            int dano = (muereMio ? previaMia.vida() : previaRival.vida()) - vida;
            String victima = muereMio ? nombreMio : nombreRival;
            anotar("QUEMADURA: " + victima + " -" + dano + " HP", Componentes.TEXTO_SUAVE);
            cola.agregar(listo -> {
                panel(muereMio).barra.actualizar(vida, () ->
                        escribir("LA QUEMADURA LE QUITA " + dano + " HP A " + victima + "!", listo));
            });
        }
        previaMia = mia;
        previaRival = rival;
        String perdedor = gane ? nombreRival : nombreMio;
        String ganador = gane ? nombreMio : nombreRival;
        anotar("GANADOR: " + ganador, gane ? AZUL : ROJO);

        cola.agregar(listo -> {
            habilitarBotones(false);
            infoMio.marcarTurno(false);
            infoRival.marcarTurno(false);
            arena.derrotado(!gane);
            escribir(perdedor + " FUE DESTRUIDO!", listo);
        });
        cola.agregar(listo -> {
            escribir(gane ? "GANASTE! " + ganador + " ES EL CAMPEON DEL DUELO." : "PERDISTE... " + ganador + " GANO EL DUELO.", 0, listo);
            mostrarFinal();
        });
    }

    // Controles

    private void elegir(String movimiento) {
        habilitarBotones(false);
        dialogo.setTexto(nombreMio + " USARA " + movimiento + "...");
        manual.elegir(Movimiento.desdeTexto(movimiento));
    }

    private void habilitarBotones(boolean habilitar) {
        botones.getChildren().forEach(b -> b.setDisable(!habilitar));
    }

    private void pintarEstrategia() {
        listaEstrategia.getChildren().clear();
        listaEstrategia.getChildren().add(Componentes.texto("ESTRATEGIA (ARCHIVO)", 2, Componentes.TEXTO_SUAVE));
        int siguiente = turnoEstrategia % estrategia.size();
        int desde = Math.max(0, Math.min(siguiente - 1, estrategia.size() - 4));
        for (int i = desde; i < Math.min(estrategia.size(), desde + 4); i++) {
            String nombre = estrategia.get(i).getNombre();
            Color color = OPCIONES.stream().filter(o -> o.movimiento().equals(nombre)).findFirst()
                    .map(Opcion::color).orElse(Componentes.TEXTO);
            listaEstrategia.getChildren().add(Componentes.texto((i == siguiente ? "► " : "  ") + nombre, 3, color));
        }
    }

    private void agregarBitacora(Entrada entrada) {
        bitacora.getItems().add(entrada);
        bitacora.scrollTo(bitacora.getItems().size() - 1);
    }

    // Línea de la bitácora que aparece cuando le llega su turno en la cola
    private void anotar(String texto, Color color) {
        cola.agregar(listo -> {
            agregarBitacora(new Entrada(texto, color));
            listo.run();
        });
    }

    private PanelInfo panel(boolean mio) {
        return mio ? infoMio : infoRival;
    }

    // Muestra el mensaje en la caja de diálogo y lo deja un momento antes de seguir con el siguiente
    private void escribir(String texto, Runnable fin) {
        escribir(texto, PAUSA_MENSAJE_MS, fin);
    }

    private void escribir(String texto, double pausa, Runnable fin) {
        dialogo.setTexto(texto);
        if (pausa <= 0) {
            fin.run();
            return;
        }
        PauseTransition espera = new PauseTransition(Duration.millis(pausa));
        espera.setOnFinished(e -> fin.run());
        espera.play();
    }

    // Fin de la partida

    private void mostrarFinal() {
        PixelBoton guardarRobot = new PixelBoton("GUARDAR ROBOT", 2);
        guardarRobot.setAyuda("Guarda la configuración de tu robot en robots.bin");
        guardarRobot.setOnAction(() -> {
            try {
                new GestorRobots().guardar(miRobot);
                guardarRobot.setDisable(true);
                dialogo.setTexto("ROBOT GUARDADO EN " + GestorRobots.NOMBRE_ARCHIVO + ".");
            } catch (IOException ex) {
                dialogo.setTexto("NO SE PUDO GUARDAR EL ROBOT: " + ex.getMessage());
            }
        });

        PixelBoton guardarMovimientos = new PixelBoton("GUARDAR MOVIMIENTOS", 2);
        guardarMovimientos.setAyuda("Archivo de texto con tus movimientos, para usarlo como estrategia");
        guardarMovimientos.setOnAction(() -> {
            File archivo = elegirArchivo("Guardar movimientos", "movimientos_partida.txt");
            if (archivo == null) {
                return;
            }
            try {
                registro.guardarComoEstrategia(new GestorEstrategia(archivo.toPath()));
                dialogo.setTexto("MOVIMIENTOS GUARDADOS EN " + archivo.getName() + ". RENOMBRALO A ESTRATEGIA.TXT PARA USARLO.");
            } catch (IOException | IllegalArgumentException ex) {
                dialogo.setTexto("NO SE PUDIERON GUARDAR: " + ex.getMessage());
            }
        });

        PixelBoton guardarHistorial = new PixelBoton("GUARDAR HISTORIAL", 2);
        guardarHistorial.setAyuda("Archivo de texto con todos los turnos de los dos robots");
        guardarHistorial.setOnAction(() -> {
            File archivo = elegirArchivo("Guardar historial", "historial_partida.txt");
            if (archivo == null) {
                return;
            }
            try {
                registro.guardarHistorial(archivo.toPath());
                dialogo.setTexto("HISTORIAL GUARDADO EN " + archivo.getName() + ".");
            } catch (IOException ex) {
                dialogo.setTexto("NO SE PUDO GUARDAR EL HISTORIAL: " + ex.getMessage());
            }
        });

        PixelBoton nueva = new PixelBoton("NUEVA PARTIDA", 2).principal();
        nueva.setOnAction(() -> {
            app.irAConexion();
        });

        for (PixelBoton b : List.of(guardarRobot, guardarMovimientos, guardarHistorial, nueva)) {
            b.setMaxWidth(Double.MAX_VALUE);
        }
        menu.getChildren().setAll(guardarRobot, guardarMovimientos, guardarHistorial, nueva);
        menu.setSpacing(6);
    }

    private File elegirArchivo(String titulo, String nombreInicial) {
        FileChooser selector = new FileChooser();
        selector.setTitle(titulo);
        selector.setInitialDirectory(new File(System.getProperty("user.dir")));
        selector.setInitialFileName(nombreInicial);
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Texto", "*.txt"));
        return selector.showSaveDialog(app.getVentana());
    }

    // Caja con nombre, barra de vida, energía, esquive y efectos de un robot
    private static final class PanelInfo {
        private final VBox caja;
        private final BarraVida barra = new BarraVida();
        private final PixelText numeros = new PixelText("100/100", 3);
        private final PixelText energia = new PixelText("", 2, Componentes.TEXTO_SUAVE);
        private final PixelText esquive = new PixelText("", 2, Componentes.TEXTO_SUAVE);
        private final PixelText turno = new PixelText("", 2, Color.web("#c06000"));

        PanelInfo(Robot robot, String titulo) {
            ModeloMecha modelo = ModeloMecha.de(robot);
            barra.setAlCambiar(() -> numeros.setTexto(barra.getMostrada() + "/" + Robot.VIDA_INICIAL));
            HBox cabecera = new HBox(10, new PixelText(robot.getNombre(), 3).conAnchoMaximo(190),
                    new PixelText(modelo.getNombre(), 2, Componentes.TEXTO_SUAVE), turno);
            cabecera.setAlignment(Pos.BASELINE_LEFT);
            HBox vida = new HBox(6, barra, numeros);
            vida.setAlignment(Pos.CENTER_LEFT);
            HBox efectos = new HBox(5);
            efectos.setAlignment(Pos.CENTER_LEFT);
            for (Efecto efecto : robot.getEfectos()) {
                efectos.getChildren().addAll(new ImageView(Iconos.efecto(efecto.getLetra(), 2)),
                        new PixelText(efecto.getNombre(), 2));
            }
            HBox datos = new HBox(12, energia, esquive, efectos);
            datos.setAlignment(Pos.CENTER_LEFT);
            caja = Componentes.caja(8, new PixelText(titulo, 2, Componentes.TEXTO_SUAVE), cabecera, vida, datos);
            caja.setPrefWidth(383);
            caja.setMaxWidth(383);
            caja.setStyle("-fx-padding: 14;");
        }

        void mostrar(Foto foto) {
            barra.fijar(foto.vida());
            mostrarEnergia(foto.energia());
            mostrarEsquive(foto.esquives());
        }

        void mostrarEnergia(int valor) {
            energia.setTexto("NRG " + valor);
        }

        void mostrarEsquive(int esquives) {
            esquive.setTexto(switch (esquives) {
                case 0 -> "ESQ --";
                case 1 -> "ESQ 50%";
                default -> "ESQ 75%";
            });
        }

        void marcarTurno(boolean activo) {
            turno.setTexto(activo ? "▼ TURNO" : "");
        }
    }
}
