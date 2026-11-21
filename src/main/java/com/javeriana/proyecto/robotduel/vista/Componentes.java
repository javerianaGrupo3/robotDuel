package com.javeriana.proyecto.robotduel.vista;

import com.javeriana.proyecto.robotduel.modelo.Efecto;
import com.javeriana.proyecto.robotduel.modelo.Robot;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.function.Function;

// Piezas de interfaz que se repiten en varias pantallas
final class Componentes {

    static final Color TEXTO = PixelText.OSCURO;
    static final Color TEXTO_SUAVE = Color.web("#687088");
    static final Color CLARO = Color.web("#f8f8f0");
    static final Color DORADO = Color.web("#ffcc33");
    static final Color AZUL_CLARO = Color.web("#a8c8f8");
    static final Color VERDE = Color.web("#2f9e44");
    static final Color VERDE_CLARO = Color.web("#88e888");
    static final Color ROJO = Color.web("#d03838");
    static final Color ROJO_CLARO = Color.web("#ff8070");

    private Componentes() {
    }

    static PixelText texto(String texto, int escala) {
        return new PixelText(texto, escala, TEXTO);
    }

    static PixelText texto(String texto, int escala, Color color) {
        return new PixelText(texto, escala, color);
    }

    // Caja de diálogo clásica: borde oscuro, borde azul y fondo claro
    static VBox caja(double espacio, Node... hijos) {
        VBox caja = new VBox(espacio, hijos);
        caja.getStyleClass().add("caja");
        return caja;
    }

    static VBox encabezado(String paso) {
        PixelText logo = new PixelText("MECHA DUEL", 6, DORADO).conSombra(Color.web("#a8401c"));
        PixelText sub = new PixelText(paso, 2, AZUL_CLARO);
        VBox caja = new VBox(10, logo, sub);
        caja.setAlignment(Pos.CENTER);
        return caja;
    }

    // Imagen del mecha centrada abajo dentro de un marco de alto fijo
    static HBox marcoMecha(ModeloMecha modelo, double alto, double anchoMaximo, boolean mirarDerecha) {
        HBox marco = new HBox(modelo.vista(alto, anchoMaximo, mirarDerecha));
        marco.setAlignment(Pos.BOTTOM_CENTER);
        marco.setMinHeight(alto);
        marco.setPrefHeight(alto);
        return marco;
    }

    // Tarjeta con la imagen y las estadísticas de un robot
    static VBox tarjetaRobot(String titulo, Robot robot, boolean mirarDerecha) {
        ModeloMecha modelo = ModeloMecha.de(robot);

        GridPane stats = new GridPane();
        stats.setHgap(18);
        stats.setVgap(8);
        stats.addRow(0, texto("HP", 2, TEXTO_SUAVE), texto(String.valueOf(Robot.VIDA_INICIAL), 2));
        stats.addRow(1, texto("ATK", 2, TEXTO_SUAVE), texto(String.valueOf(robot.getAtaque()), 2));
        stats.addRow(2, texto("DEF", 2, TEXTO_SUAVE), texto(String.valueOf(robot.getDefensa()), 2));
        stats.addRow(3, texto("SPD", 2, TEXTO_SUAVE), texto(String.valueOf(robot.getVelocidad()), 2));
        stats.addRow(4, texto("NRG", 2, TEXTO_SUAVE), texto(String.valueOf(robot.getEnergia()), 2));

        VBox tarjeta = caja(10,
                texto(titulo, 2, TEXTO_SUAVE),
                marcoMecha(modelo, 160, 200, mirarDerecha),
                texto(robot.getNombre(), 3).conAnchoMaximo(200),
                texto("MECHA " + modelo.getNombre(), 2, TEXTO_SUAVE),
                stats,
                efectos(robot));
        tarjeta.setPrefWidth(240);
        tarjeta.setMinWidth(240);
        return tarjeta;
    }

    // Iconos y nombres de los efectos de estado del robot
    static VBox efectos(Robot robot) {
        VBox lista = new VBox(6);
        if (robot.getEfectos().isEmpty()) {
            lista.getChildren().add(texto("SIN EFECTO", 2, TEXTO_SUAVE));
        }
        for (Efecto efecto : robot.getEfectos()) {
            HBox fila = new HBox(6, new ImageView(Iconos.efecto(efecto.getLetra(), 2)), texto(efecto.getNombre(), 2));
            fila.setAlignment(Pos.CENTER_LEFT);
            lista.getChildren().add(fila);
        }
        return lista;
    }

    static String nombresEfectos(Robot robot) {
        if (robot.getEfectos().isEmpty()) {
            return "ninguno";
        }
        StringBuilder texto = new StringBuilder();
        for (Efecto efecto : robot.getEfectos()) {
            if (!texto.isEmpty()) {
                texto.append(", ");
            }
            texto.append(efecto.getNombre());
        }
        return texto.toString();
    }

    // Las listas muestran cada elemento con la fuente de pixeles
    static <T> void celdasPixel(ListView<T> lista, double ancho, Function<T, String> texto, Function<T, Color> color) {
        lista.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean vacio) {
                super.updateItem(item, vacio);
                setText(null);
                setGraphic(vacio || item == null ? null
                        : new PixelText(texto.apply(item), 2, color.apply(item)).conAnchoMaximo(ancho));
            }
        });
    }

    // Lista de robots guardados: miniatura del mecha + nombre + estadísticas
    static void celdasRobot(ListView<Robot> lista, double anchoTexto) {
        lista.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(Robot r, boolean vacio) {
                super.updateItem(r, vacio);
                setText(null);
                if (vacio || r == null) {
                    setGraphic(null);
                    return;
                }
                HBox miniatura = new HBox(ModeloMecha.de(r).vista(52, 60, true));
                miniatura.setAlignment(Pos.CENTER);
                miniatura.setMinWidth(62);
                PixelText datos = new PixelText(r.getNombre() + "\nATK " + r.getAtaque() + " DEF " + r.getDefensa()
                        + "\nSPD " + r.getVelocidad() + " NRG " + r.getEnergia(), 2, TEXTO).conAnchoMaximo(anchoTexto);
                HBox fila = new HBox(8, miniatura, datos);
                fila.setAlignment(Pos.CENTER_LEFT);
                setGraphic(fila);
            }
        });
    }

    // Mensaje de estado sobre el fondo oscuro
    static void estado(PixelText etiqueta, String mensaje, Color color) {
        etiqueta.setColor(color);
        etiqueta.setTexto(mensaje);
    }
}
