package com.javeriana.proyecto.robotduel.vista;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

// Botón de estilo retro: al pasar el mouse aparece la flecha ► como en los menús de los juegos clásicos
class PixelBoton extends StackPane {

    private static final Color DESHABILITADO = Color.web("#9aa0ad");

    private final String texto;
    private final PixelText etiqueta;
    private final Color color;
    private Runnable accion;

    PixelBoton(String texto, int escala, Color color) {
        this.texto = PixelFont.normalizar(texto);
        this.color = color;
        this.etiqueta = new PixelText("  " + this.texto, escala, color);
        getChildren().add(etiqueta);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("pixel-boton");
        setCursor(Cursor.HAND);

        setOnMouseEntered(e -> {
            if (!isDisabled()) {
                etiqueta.setTexto("► " + this.texto);
                getStyleClass().add("pixel-boton-activo");
            }
        });
        setOnMouseExited(e -> {
            etiqueta.setTexto("  " + this.texto);
            getStyleClass().remove("pixel-boton-activo");
        });
        setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                disparar();
            }
        });
        disabledProperty().addListener((obs, antes, ahora) -> {
            etiqueta.setColor(ahora ? DESHABILITADO : color);
            if (ahora) {
                etiqueta.setTexto("  " + this.texto);
                getStyleClass().remove("pixel-boton-activo");
            }
        });
    }

    PixelBoton(String texto, int escala) {
        this(texto, escala, PixelText.OSCURO);
    }

    PixelBoton principal() {
        getStyleClass().add("pixel-boton-principal");
        return this;
    }

    void setOnAction(Runnable nueva) {
        this.accion = nueva;
    }

    void disparar() {
        if (!isDisabled() && accion != null) {
            accion.run();
        }
    }

    String getTexto() {
        return texto;
    }

    void setAyuda(String ayuda) {
        Tooltip tip = new Tooltip(ayuda);
        tip.setShowDelay(Duration.millis(300));
        Tooltip.install(this, tip);
    }
}
