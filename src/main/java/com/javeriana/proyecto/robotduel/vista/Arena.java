package com.javeriana.proyecto.robotduel.vista;

import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.util.Random;

// Escenario del combate fondo en pixel art y los dos mechas sobre sus plataformas
class Arena extends Pane {

    static final int ANCHO = 760;
    static final int ALTO = 340;
    private static final int PIXEL = 4; // el fondo se dibuja a 190x85 y se amplía x4

    private final ImageView spriteMio;
    private final ImageView spriteRival;

    Arena(ModeloMecha mio, ModeloMecha rival) {
        setPrefSize(ANCHO, ALTO);
        setMinSize(ANCHO, ALTO);
        setMaxSize(ANCHO, ALTO);
        setClip(new Rectangle(ANCHO, ALTO));

        // Mi mecha más grande y abajo a la izquierda  mirando al rival; el rival arriba a la derecha
        spriteMio = mio.vista(230, 300, true);
        spriteRival = rival.vista(170, 240, false);
        ubicar(spriteMio, 48 * PIXEL, 78 * PIXEL);
        ubicar(spriteRival, 141 * PIXEL, 52 * PIXEL);

        getChildren().addAll(new ImageView(dibujarFondo()), spriteRival, spriteMio);
    }

    private static void ubicar(ImageView sprite, double centroX, double piso) {
        sprite.setLayoutX(Math.round(centroX - sprite.getFitWidth() / 2));
        sprite.setLayoutY(Math.round(piso - sprite.getFitHeight()));
    }

    // El robot destruido desaparece de su plataforma
    void derrotado(boolean mio) {
        (mio ? spriteMio : spriteRival).setVisible(false);
    }

    // Fondo: cielo oscuro con luces, muro y piso con cuadrícula en perspectiva, dos plataformas
    private static WritableImage dibujarFondo() {
        int w = ANCHO / PIXEL;
        int h = ALTO / PIXEL;
        int[] px = new int[w * h];
        int horizonte = 36;
        int[][] bayer = {{0, 8, 2, 10}, {12, 4, 14, 6}, {3, 11, 1, 9}, {15, 7, 13, 5}};
        int[] cielo = {0xFF0d1b2a, 0xFF13293d, 0xFF1b3a4b, 0xFF21495a};
        for (int y = 0; y < horizonte; y++) {
            double t = y / (double) horizonte * (cielo.length - 1);
            int banda = (int) t;
            double resto = t - banda;
            for (int x = 0; x < w; x++) {
                boolean siguiente = resto * 16 > bayer[y % 4][x % 4] && banda + 1 < cielo.length;
                px[y * w + x] = siguiente ? cielo[banda + 1] : cielo[banda];
            }
        }
        // luces del estadio
        Random azar = new Random(7);
        for (int i = 0; i < 40; i++) {
            int x = azar.nextInt(w);
            int y = 2 + azar.nextInt(horizonte - 10);
            px[y * w + x] = azar.nextBoolean() ? 0xFF5ad1e8 : 0xFF2e6f84;
        }
        // muro
        for (int y = horizonte - 6; y < horizonte; y++) {
            for (int x = 0; x < w; x++) {
                px[y * w + x] = (x / 6 + y) % 2 == 0 ? 0xFF16313d : 0xFF1a3a47;
            }
        }
        for (int x = 0; x < w; x++) {
            px[(horizonte - 6) * w + x] = 0xFF2e6f84;
            px[horizonte * w + x] = 0xFF7ee8f5;
        }
        // piso
        for (int y = horizonte + 1; y < h; y++) {
            int c = y < horizonte + 12 ? 0xFF12303a : (y < horizonte + 26 ? 0xFF153844 : 0xFF19404d);
            for (int x = 0; x < w; x++) {
                px[y * w + x] = c;
            }
        }
        int linea = 0xFF2a8fa3;
        double puntoX = w / 2.0;
        double puntoY = horizonte - 25;
        for (int x0 = -260; x0 <= w + 260; x0 += 22) {
            for (int y = horizonte + 1; y < h; y++) {
                double t = (y - puntoY) / (h - 1 - puntoY);
                int x = (int) Math.round(puntoX + (x0 - puntoX) * t);
                if (x >= 0 && x < w) {
                    px[y * w + x] = linea;
                }
            }
        }
        for (int k = 1; k < 9; k++) {
            int y = horizonte + (int) Math.round(k * k * 0.65);
            if (y >= h) {
                break;
            }
            for (int x = 0; x < w; x++) {
                px[y * w + x] = linea;
            }
        }
        plataforma(px, w, h, 48, 76, 36, 6);
        plataforma(px, w, h, 141, 50, 29, 5);

        WritableImage img = new WritableImage(ANCHO, ALTO);
        PixelWriter pw = img.getPixelWriter();
        for (int y = 0; y < ALTO; y++) {
            for (int x = 0; x < ANCHO; x++) {
                pw.setArgb(x, y, px[(y / PIXEL) * w + x / PIXEL]);
            }
        }
        return img;
    }

    private static void plataforma(int[] px, int w, int h, int cx, int cy, int rx, int ry) {
        for (int y = cy - ry - 1; y <= cy + ry + 1; y++) {
            for (int x = cx - rx - 1; x <= cx + rx + 1; x++) {
                if (x < 0 || y < 0 || x >= w || y >= h) {
                    continue;
                }
                double nx = (x + 0.5 - cx) / rx;
                double ny = (y + 0.5 - cy) / ry;
                double d = nx * nx + ny * ny;
                if (d <= 0.72) {
                    px[y * w + x] = ny < -0.2 ? 0xFF1f5a6b : 0xFF0e2a33;
                } else if (d <= 1.0) {
                    px[y * w + x] = 0xFF7ee8f5;
                }
            }
        }
    }
}
