package com.javeriana.proyecto.robotduel.vista;

import com.javeriana.proyecto.robotduel.archivos.GestorEstrategia;
import com.javeriana.proyecto.robotduel.modelo.Movimiento;
import com.javeriana.proyecto.robotduel.modelo.Robot;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.List;

// con el robot del rival a la vista se revisa la estrategia.
// El jugador elige: CON ARCHIVO (estrategia.txt se repite en ciclo) o A MANO (elige cada movimiento).
class PantallaEstrategia {

    private final MechaDuelApp app;
    private final Robot miRobot;
    private final Robot robotRival;
    private final GestorEstrategia gestor = new GestorEstrategia();

    private final ListView<String> lista = new ListView<>();
    private final PixelText modo = Componentes.texto("", 2).conAnchoMaximo(400);
    private final PixelBoton botonArchivo = new PixelBoton("CON ARCHIVO", 3).principal();
    private final PixelBoton botonManual = new PixelBoton("A MANO", 3).principal();
    private List<Movimiento> estrategia;

    PantallaEstrategia(MechaDuelApp app, Robot miRobot, Robot robotRival) {
        this.app = app;
        this.miRobot = miRobot;
        this.robotRival = robotRival;
    }

    Parent getVista() {
        lista.setPrefHeight(170);
        Componentes.celdasPixel(lista, 380, s -> s, s -> Componentes.TEXTO);
        lista.setPlaceholder(Componentes.texto("SIN ARCHIVO", 2, Componentes.TEXTO_SUAVE));

        PixelBoton recargar = new PixelBoton("VOLVER A LEER", 2);
        recargar.setOnAction(this::cargar);

        botonArchivo.setAyuda("Los movimientos de estrategia.txt se repiten en ciclo hasta que un robot sea destruido");
        botonArchivo.setOnAction(() -> app.iniciarCombate(estrategia, false));
        botonManual.setAyuda("Eliges cada movimiento con los botones durante el combate");
        botonManual.setOnAction(() -> app.iniciarCombate(null, estrategia == null));

        VBox cajaEstrategia = Componentes.caja(12,
                Componentes.texto("ESTRATEGIA.TXT", 3),
                Componentes.texto("UN MOVIMIENTO POR LINEA: ATTACK, SPECIAL, DODGE, HEAL O CHARGE", 2,
                        Componentes.TEXTO_SUAVE).conAnchoMaximo(400),
                lista, modo, new HBox(recargar),
                Componentes.texto("COMO QUIERES JUGAR?", 2, Componentes.TEXTO_SUAVE),
                new HBox(10, botonArchivo, botonManual));
        HBox.setHgrow(cajaEstrategia, Priority.ALWAYS);

        PixelText vs = new PixelText("VS", 5, Componentes.DORADO).conSombra(Color.web("#a8401c"));
        VBox centro = new VBox(10, vs, cajaEstrategia);
        centro.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(centro, Priority.ALWAYS);

        HBox fila = new HBox(16, Componentes.tarjetaRobot("TU MECHA", miRobot, false), centro,
                Componentes.tarjetaRobot("MECHA DEL RIVAL", robotRival, true));
        fila.setAlignment(Pos.TOP_CENTER);

        cargar();

        VBox raiz = new VBox(16, Componentes.encabezado("PASO 3 DE 4 - ESTRATEGIA"), fila);
        raiz.setAlignment(Pos.TOP_CENTER);
        raiz.getStyleClass().add("pantalla");
        return raiz;
    }

    private void cargar() {
        lista.getItems().clear();
        estrategia = null;

        botonArchivo.setDisable(true);

        if (!gestor.existe()) {
            modo.setColor(Componentes.TEXTO);
            modo.setTexto("NO EXISTE ESTRATEGIA.TXT: SOLO SE PUEDE JUGAR A MANO. "
                    + "EL ARCHIVO SE IRA CREANDO CON LOS MOVIMIENTOS QUE ELIJAS.");
            return;
        }
        try {
            estrategia = gestor.leer();
            for (int i = 0; i < estrategia.size(); i++) {
                lista.getItems().add((i + 1) + ". " + estrategia.get(i).getNombre());
            }
            botonArchivo.setDisable(false);
            modo.setColor(Componentes.VERDE);
            modo.setTexto("CON ARCHIVO: ESTOS " + estrategia.size() + " MOVIMIENTOS SE REPITEN EN CICLO "
                    + "HASTA QUE UN ROBOT SEA DESTRUIDO. A MANO: ELIGES CADA MOVIMIENTO (EL ARCHIVO NO SE TOCA).");
        } catch (IOException e) {
            modo.setColor(Componentes.ROJO);
            modo.setTexto(e.getMessage() + ". SOLO SE PUEDE JUGAR A MANO Y EL ARCHIVO SE REEMPLAZARA.");
        }
    }
}
