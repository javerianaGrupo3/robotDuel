package com.javeriana.proyecto.robotduel.vista;

import com.javeriana.proyecto.robotduel.archivos.GestorRobots;
import com.javeriana.proyecto.robotduel.modelo.Efecto;
import com.javeriana.proyecto.robotduel.modelo.Robot;
import com.javeriana.proyecto.robotduel.red.Conexion;
import com.javeriana.proyecto.robotduel.red.PartidaEnRed;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

// se elige el mechase reparten las estadísticas (ATK + DEF + SPD + NRG = 200)
// y se envían al rival
class PantallaEnsamble {

    private static final String[] EFECTOS = {"NINGUNO", "QUEMADURA (Q)", "REGENERACION (R)", "NUCLEO ACTIVO (N)"};
    private static final String[] DESCRIPCIONES = {
            "SIN EFECTO DE ESTADO",
            "EL RIVAL PIERDE 3 HP AL INICIO DE CADA TURNO TUYO",
            "RECUPERAS 3 HP AL INICIO DE CADA TURNO TUYO",
            "GANAS 2 DE ENERGIA AL INICIO DE CADA TURNO TUYO"
    };
    private static final char[] LETRAS = {' ', 'Q', 'R', 'N'};
    private static final int COLUMNAS_GALERIA = 4;
    private static final double LADO_FICHA = 64;

    private final MechaDuelApp app;
    private final Conexion conexion;
    private final GestorRobots gestor = new GestorRobots();

    private final TextField campoNombre = new TextField("Mecha");
    private final Spinner<Integer> ataque = spinner();
    private final Spinner<Integer> defensa = spinner();
    private final Spinner<Integer> velocidad = spinner();
    private final Spinner<Integer> energia = spinner();
    private final ComboBox<String> efecto = new ComboBox<>();
    private final PixelText descripcionEfecto = Componentes.texto("", 2, Componentes.TEXTO_SUAVE).conAnchoMaximo(330);
    private final StackPane vistaPrevia = new StackPane();
    private final PixelText nombreModelo = Componentes.texto("", 3);
    private final GridPane galeria = new GridPane();
    private final Map<ModeloMecha, StackPane> fichas = new EnumMap<>(ModeloMecha.class);
    private final PixelText total = Componentes.texto("", 2);
    private final PixelText estado = Componentes.texto("", 2, Componentes.CLARO).conAnchoMaximo(1000);
    private final PixelBoton botonEnviar = new PixelBoton("ENVIAR ROBOT", 3).principal();
    private final ListView<Robot> guardados = new ListView<>();
    private final PixelBoton botonUsar = new PixelBoton("USAR", 2);
    private final PixelBoton botonEliminar = new PixelBoton("ELIMINAR", 2);

    private ModeloMecha modelo = ModeloMecha.MANGO;

    PantallaEnsamble(MechaDuelApp app, Conexion conexion) {
        this.app = app;
        this.conexion = conexion;
    }

    Parent getVista() {
        efecto.getItems().addAll(EFECTOS);
        efecto.getSelectionModel().selectFirst();
        efecto.setPrefWidth(220);
        efecto.valueProperty().addListener((obs, antes, ahora) -> actualizar());
        campoNombre.setPrefWidth(220);

        GridPane formulario = new GridPane();
        formulario.setHgap(16);
        formulario.setVgap(12);
        formulario.addRow(0, Componentes.texto("NOMBRE", 2), campoNombre);
        formulario.addRow(1, Componentes.texto("ATK ATAQUE", 2), ataque);
        formulario.addRow(2, Componentes.texto("DEF DEFENSA", 2), defensa);
        formulario.addRow(3, Componentes.texto("SPD VELOCIDAD", 2), velocidad);
        formulario.addRow(4, Componentes.texto("NRG ENERGIA", 2), energia);
        formulario.addRow(5, Componentes.texto("EFECTO", 2), efecto);

        for (Spinner<Integer> s : List.of(ataque, defensa, velocidad, energia)) {
            s.valueProperty().addListener((obs, antes, ahora) -> actualizar());
        }
        campoNombre.textProperty().addListener((obs, antes, ahora) -> actualizar());

        botonEnviar.setOnAction(this::enviar);
        VBox cajaFormulario = Componentes.caja(14, Componentes.texto("ARMA TU MECHA", 3), formulario, descripcionEfecto,
                Componentes.texto("HP SIEMPRE EMPIEZA EN 100", 2, Componentes.TEXTO_SUAVE), total, botonEnviar);

        // Galería para elegir la apariencia del mecha
        galeria.setHgap(6);
        galeria.setVgap(6);
        int i = 0;
        for (ModeloMecha m : ModeloMecha.values()) {
            StackPane ficha = new StackPane(m.vista(LADO_FICHA - 12, LADO_FICHA - 10, true));
            ficha.setPrefSize(LADO_FICHA, LADO_FICHA);
            ficha.setMinSize(LADO_FICHA, LADO_FICHA);
            ficha.getStyleClass().add("ficha");
            ficha.setCursor(Cursor.HAND);
            Tooltip.install(ficha, new Tooltip(m.getNombre()));
            ficha.setOnMouseClicked(e -> {
                if (!ficha.isDisabled()) {
                    elegirModelo(m);
                }
            });
            fichas.put(m, ficha);
            galeria.add(ficha, i % COLUMNAS_GALERIA, i / COLUMNAS_GALERIA);
            i++;
        }
        vistaPrevia.setPrefSize(290, 160);
        vistaPrevia.setMinSize(290, 160);
        vistaPrevia.setAlignment(Pos.BOTTOM_CENTER);
        VBox cajaGaleria = Componentes.caja(12, Componentes.texto("ELIGE TU MECHA", 3), vistaPrevia, nombreModelo, galeria);
        cajaGaleria.setAlignment(Pos.TOP_CENTER);

        // Robots guardados en robots.bin
        guardados.setPrefHeight(330);
        Componentes.celdasRobot(guardados, 170);
        guardados.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                usarGuardado();
            }
        });
        botonUsar.setOnAction(this::usarGuardado);
        botonEliminar.setOnAction(this::eliminarGuardado);
        VBox cajaGuardados = Componentes.caja(12, Componentes.texto("ROBOTS.BIN", 3), guardados,
                new HBox(10, botonUsar, botonEliminar));
        HBox.setHgrow(cajaGuardados, Priority.ALWAYS);
        cargarGuardados();

        HBox centro = new HBox(14, cajaFormulario, cajaGaleria, cajaGuardados);
        centro.setAlignment(Pos.TOP_CENTER);

        elegirModelo(modelo);
        Componentes.estado(estado, "CONECTADO CON " + conexion.getDireccionRival(), Componentes.VERDE_CLARO);
        VBox raiz = new VBox(16, Componentes.encabezado("PASO 2 DE 4 - ENSAMBLE"), centro, estado);
        raiz.setAlignment(Pos.TOP_CENTER);
        raiz.getStyleClass().add("pantalla");
        return raiz;
    }

    private static Spinner<Integer> spinner() {
        Spinner<Integer> s = new Spinner<>(0, Robot.TOTAL_PUNTOS, 50, 5);
        s.setEditable(true);
        s.setPrefWidth(120);
        // Tomar el valor mientras se escribe (por defecto solo se toma al presionar Enter)
        s.getEditor().textProperty().addListener((obs, antes, ahora) -> {
            try {
                int valor = Integer.parseInt(ahora.trim());
                if (valor >= 0 && valor <= Robot.TOTAL_PUNTOS) {
                    s.getValueFactory().setValue(valor);
                }
            } catch (NumberFormatException ignorada) {
                // se ignora mientras el texto no sea un número
            }
        });
        return s;
    }

    private void elegirModelo(ModeloMecha elegido) {
        modelo = elegido;
        vistaPrevia.getChildren().setAll(elegido.vista(150, 280, true));
        nombreModelo.setTexto(elegido.getNombre());
        fichas.forEach((m, ficha) -> {
            ficha.getStyleClass().remove("ficha-elegida");
            if (m == elegido) {
                ficha.getStyleClass().add("ficha-elegida");
            }
        });
    }

    private void actualizar() {
        int suma = ataque.getValue() + defensa.getValue() + velocidad.getValue() + energia.getValue();
        descripcionEfecto.setTexto(DESCRIPCIONES[Math.max(0, efecto.getSelectionModel().getSelectedIndex())]);

        boolean valido = suma == Robot.TOTAL_PUNTOS && velocidad.getValue() > 0 && !campoNombre.getText().isBlank();
        String detalle = "";
        if (suma != Robot.TOTAL_PUNTOS) {
            int falta = Robot.TOTAL_PUNTOS - suma;
            detalle = falta > 0 ? "  TE FALTAN " + falta : "  TE SOBRAN " + (-falta);
        } else if (velocidad.getValue() == 0) {
            detalle = "  SPD DEBE SER MAYOR A 0";
        } else if (campoNombre.getText().isBlank()) {
            detalle = "  FALTA EL NOMBRE";
        }
        total.setColor(valido ? Componentes.VERDE : Componentes.ROJO);
        total.setTexto("PUNTOS " + suma + "/" + Robot.TOTAL_PUNTOS + detalle);
        botonEnviar.setDisable(!valido);
    }

    private Robot construirRobot() {
        List<Efecto> efectos = new ArrayList<>();
        int indice = efecto.getSelectionModel().getSelectedIndex();
        if (indice > 0) {
            efectos.add(Efecto.desdeLetra(LETRAS[indice]));
        }
        return new Robot(campoNombre.getText(), ataque.getValue(), defensa.getValue(),
                velocidad.getValue(), energia.getValue(), efectos, modelo.name());
    }

    private void enviar() {
        Robot robot;
        try {
            robot = construirRobot();
        } catch (IllegalArgumentException e) {
            Componentes.estado(estado, e.getMessage(), Componentes.ROJO_CLARO);
            return;
        }
        bloquear(true);
        Componentes.estado(estado, "ESTADISTICAS ENVIADAS. ESPERANDO EL ROBOT DEL RIVAL...", Componentes.CLARO);

        MechaDuelApp.enSegundoPlano("ensamble", () -> {
            try {
                Robot rival = PartidaEnRed.intercambiarRobots(conexion, robot);
                Platform.runLater(() -> app.robotsListos(robot, rival));
            } catch (IOException e) {
                Platform.runLater(() -> app.errorDeRed(e.getMessage()));
            }
        });
    }

    private void bloquear(boolean bloqueado) {
        List<Node> controles = new ArrayList<>(List.of(campoNombre, ataque, defensa, velocidad, energia, efecto,
                botonEnviar, guardados, botonUsar, botonEliminar));
        controles.addAll(fichas.values());
        for (Node control : controles) {
            control.setDisable(bloqueado);
        }
    }

    private void cargarGuardados() {
        try {
            guardados.getItems().setAll(gestor.cargar());
        } catch (IOException e) {
            Componentes.estado(estado, "NO SE PUDO LEER ROBOTS.BIN: " + e.getMessage(), Componentes.ROJO_CLARO);
        }
    }

    private void usarGuardado() {
        Robot robot = guardados.getSelectionModel().getSelectedItem();
        if (robot == null) {
            Componentes.estado(estado, "SELECCIONA UN ROBOT DE LA LISTA", Componentes.ROJO_CLARO);
            return;
        }
        campoNombre.setText(robot.getNombre());
        ataque.getValueFactory().setValue(robot.getAtaque());
        defensa.getValueFactory().setValue(robot.getDefensa());
        velocidad.getValueFactory().setValue(robot.getVelocidad());
        energia.getValueFactory().setValue(robot.getEnergia());
        int indice = 0;
        if (!robot.getEfectos().isEmpty()) {
            char letra = robot.getEfectos().get(0).getLetra();
            for (int i = 1; i < LETRAS.length; i++) {
                if (LETRAS[i] == letra) {
                    indice = i;
                }
            }
        }
        efecto.getSelectionModel().select(indice);
        elegirModelo(ModeloMecha.de(robot));
        Componentes.estado(estado, "SE CARGO " + robot.getNombre(), Componentes.VERDE_CLARO);
    }

    private void eliminarGuardado() {
        Robot robot = guardados.getSelectionModel().getSelectedItem();
        if (robot == null) {
            Componentes.estado(estado, "SELECCIONA UN ROBOT DE LA LISTA", Componentes.ROJO_CLARO);
            return;
        }
        try {
            gestor.eliminar(robot.getNombre());
            cargarGuardados();
            Componentes.estado(estado, "SE ELIMINO " + robot.getNombre() + " DE ROBOTS.BIN", Componentes.VERDE_CLARO);
        } catch (IOException e) {
            Componentes.estado(estado, "NO SE PUDO MODIFICAR ROBOTS.BIN: " + e.getMessage(), Componentes.ROJO_CLARO);
        }
    }
}
