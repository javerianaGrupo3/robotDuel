package com.javeriana.proyecto.robotduel.vista;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.List;

// se partio el css
class PixelText extends Region {

    static final Color OSCURO = Color.web("#283040");

    private final Canvas canvas = new Canvas();
    private String texto = "";
    private int escala;
    private Color color;
    private Color sombra;
    private double anchoMaximo = -1;
    private int visibles = Integer.MAX_VALUE;

    PixelText(String texto, int escala, Color color) {
        this.escala = escala;
        this.color = color;
        getChildren().add(canvas);
        setTexto(texto);
    }

    PixelText(String texto, int escala) {
        this(texto, escala, OSCURO);
    }

    void setTexto(String nuevo) {
        this.texto = PixelFont.normalizar(nuevo);
        this.visibles = Integer.MAX_VALUE;
        redibujar();
    }

    String getTexto() {
        return texto;
    }

    void setColor(Color nuevo) {
        this.color = nuevo;
        redibujar();
    }

    PixelText conSombra(Color nueva) {
        this.sombra = nueva;
        redibujar();
        return this;
    }

    // Ancho en pixeles a partir del cual se parte el texto en varias líneas
    PixelText conAnchoMaximo(double ancho) {
        this.anchoMaximo = ancho;
        redibujar();
        return this;
    }

    // Para el efecto de máquina de escribir
    void setVisibles(int cantidad) {
        this.visibles = cantidad;
        redibujar();
    }

    int largo() {
        return texto.replace("\n", "").length();
    }

    private void redibujar() {
        int columnas = anchoMaximo > 0
                ? Math.max(1, (int) ((anchoMaximo + escala) / (PixelFont.AVANCE * escala)))
                : Integer.MAX_VALUE;
        List<String> lineas = PixelFont.envolver(texto, columnas);

        // Tamaño calculado con el texto completo para que no salte al escribir letra por letra
        int ancho = 0;
        for (String linea : lineas) {
            ancho = Math.max(ancho, PixelFont.ancho(linea, escala));
        }
        int extra = sombra != null ? escala : 0;
        double w = ancho + extra;
        double h = lineas.size() * PixelFont.INTERLINEA * escala - (PixelFont.INTERLINEA - PixelFont.ALTO) * escala + extra;
        canvas.setWidth(w);
        canvas.setHeight(h);
        setMinSize(w, h);
        setPrefSize(w, h);
        setMaxSize(w, h);

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.clearRect(0, 0, w, h);
        int restantes = visibles;
        for (int i = 0; i < lineas.size() && restantes > 0; i++) {
            double y = i * PixelFont.INTERLINEA * escala;
            if (sombra != null) {
                PixelFont.dibujar(g, lineas.get(i), escala, y + escala, escala, sombra, restantes);
            }
            restantes -= PixelFont.dibujar(g, lineas.get(i), 0, y, escala, color, restantes);
        }
    }
}
