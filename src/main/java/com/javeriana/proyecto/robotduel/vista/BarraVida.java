package com.javeriana.proyecto.robotduel.vista;

import com.javeriana.proyecto.robotduel.modelo.Robot;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

// Barra de vida estilo clásico etiqueta HP y color verde / amarillo / rojo.
// Cambia de golpe al nuevo valor
class BarraVida extends Canvas {

    private static final int PIXEL = 3;
    private static final int LARGO = 64;    // pixeles de la barra (sin escalar)
    private static final int ETIQUETA = 14;

    private double mostrada = Robot.VIDA_INICIAL;
    private Runnable alCambiar = () -> { };

    BarraVida() {
        super((ETIQUETA + LARGO + 2) * PIXEL, 7 * PIXEL);
        dibujar();
    }


    void setAlCambiar(Runnable accion) {
        this.alCambiar = accion;
    }

    int getMostrada() {
        return (int) Math.round(mostrada);
    }

    void fijar(int vida) {
        mostrada = vida;
        dibujar();
        alCambiar.run();
    }

    // Actualiza la barra y el número al instante y luego sigue con fin
    void actualizar(int vida, Runnable fin) {
        fijar(vida);
        fin.run();
    }

    private void dibujar() {
        GraphicsContext g = getGraphicsContext2D();
        g.clearRect(0, 0, getWidth(), getHeight());
        // etiqueta "HP"
        g.setFill(Color.web("#283040"));
        g.fillRect(0, 0, ETIQUETA * PIXEL, 7 * PIXEL);
        g.setFill(Color.web("#f8c030"));
        g.fillRect(PIXEL, PIXEL, (ETIQUETA - 2) * PIXEL, 5 * PIXEL);
        dibujarHp(g, 2 * PIXEL, PIXEL);
        // barra
        double x0 = ETIQUETA * PIXEL;
        g.setFill(Color.web("#283040"));
        g.fillRect(x0, 0, (LARGO + 2) * PIXEL, 7 * PIXEL);
        g.setFill(Color.web("#506070"));
        g.fillRect(x0 + PIXEL, 2 * PIXEL, LARGO * PIXEL, 3 * PIXEL);
        double fraccion = Math.max(0, Math.min(1, mostrada / Robot.VIDA_INICIAL));
        int lleno = (int) Math.ceil(fraccion * LARGO);
        if (mostrada <= 0) {
            lleno = 0;
        }
        Color color = fraccion > 0.5 ? Color.web("#58d858") : (fraccion > 0.2 ? Color.web("#f8c030") : Color.web("#f05030"));
        Color brillo = fraccion > 0.5 ? Color.web("#a8f8a8") : (fraccion > 0.2 ? Color.web("#f8e8a0") : Color.web("#f8a080"));
        g.setFill(color);
        g.fillRect(x0 + PIXEL, 2 * PIXEL, lleno * PIXEL, 3 * PIXEL);
        g.setFill(brillo);
        g.fillRect(x0 + PIXEL, 2 * PIXEL, lleno * PIXEL, PIXEL);
    }

    // Letras "HP" de 3x5 dentro de la etiqueta
    private static void dibujarHp(GraphicsContext g, double x, double y) {
        String[] h = {"#.#", "#.#", "###", "#.#", "#.#"};
        String[] p = {"##.", "#.#", "##.", "#..", "#.."};
        g.setFill(Color.web("#283040"));
        for (int fy = 0; fy < 5; fy++) {
            for (int fx = 0; fx < 3; fx++) {
                if (h[fy].charAt(fx) == '#') {
                    g.fillRect(x + fx * PIXEL, y + fy * PIXEL, PIXEL, PIXEL);
                }
                if (p[fy].charAt(fx) == '#') {
                    g.fillRect(x + (fx + 4) * PIXEL, y + fy * PIXEL, PIXEL, PIXEL);
                }
            }
        }
    }
}
