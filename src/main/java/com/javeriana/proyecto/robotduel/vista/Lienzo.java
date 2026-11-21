package com.javeriana.proyecto.robotduel.vista;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;


final class Lienzo {

    private final int ancho;
    private final int alto;
    private final int[] px;

    Lienzo(int ancho, int alto) {
        this.ancho = ancho;
        this.alto = alto;
        this.px = new int[ancho * alto];
    }

    static int color(String hex) {
        return 0xFF000000 | Integer.parseInt(hex.substring(1), 16);
    }

    void punto(int x, int y, int c) {
        if (x >= 0 && y >= 0 && x < ancho && y < alto) {
            px[y * ancho + x] = c;
        }
    }

    int get(int x, int y) {
        return (x >= 0 && y >= 0 && x < ancho && y < alto) ? px[y * ancho + x] : 0;
    }

    void rect(int x0, int y0, int x1, int y1, int c) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                punto(x, y, c);
            }
        }
    }

    void elipse(double cx, double cy, double rx, double ry, int base, int sombra, int brillo) {
        for (int y = (int) Math.floor(cy - ry); y <= Math.ceil(cy + ry); y++) {
            for (int x = (int) Math.floor(cx - rx); x <= Math.ceil(cx + rx); x++) {
                double nx = (x + 0.5 - cx) / rx;
                double ny = (y + 0.5 - cy) / ry;
                if (nx * nx + ny * ny > 1.0) {
                    continue;
                }
                int c = base;
                if (nx * 0.45 + ny * 0.9 > 0.5) {
                    c = sombra;
                } else if (brillo != 0 && -nx * 0.5 - ny * 0.85 > 0.55) {
                    c = brillo;
                }
                punto(x, y, c);
            }
        }
    }

    void elipse(double cx, double cy, double rx, double ry, int c) {
        elipse(cx, cy, rx, ry, c, c, 0);
    }

    // Polígono relleno (pares x, y)
    void poligono(int c, double... xy) {
        int n = xy.length / 2;
        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        for (int i = 0; i < n; i++) {
            minX = Math.min(minX, xy[2 * i]);
            maxX = Math.max(maxX, xy[2 * i]);
            minY = Math.min(minY, xy[2 * i + 1]);
            maxY = Math.max(maxY, xy[2 * i + 1]);
        }
        for (int y = (int) Math.floor(minY); y <= Math.ceil(maxY); y++) {
            for (int x = (int) Math.floor(minX); x <= Math.ceil(maxX); x++) {
                if (dentro(x + 0.5, y + 0.5, xy, n)) {
                    punto(x, y, c);
                }
            }
        }
    }

    private static boolean dentro(double px, double py, double[] xy, int n) {
        boolean adentro = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = xy[2 * i];
            double yi = xy[2 * i + 1];
            double xj = xy[2 * j];
            double yj = xy[2 * j + 1];
            if ((yi > py) != (yj > py) && px < (xj - xi) * (py - yi) / (yj - yi) + xi) {
                adentro = !adentro;
            }
        }
        return adentro;
    }

    void linea(int x0, int y0, int x1, int y1, int c) {
        int dx = Math.abs(x1 - x0);
        int dy = -Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            punto(x0, y0, c);
            if (x0 == x1 && y0 == y1) {
                return;
            }
            int e2 = 2 * err;
            if (e2 >= dy) {
                err += dy;
                x0 += sx;
            }
            if (e2 <= dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    // Contorno oscuro de 1 pixel alrededor de toda la figura
    void contorno(int c) {
        int[] copia = px.clone();
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                if (copia[y * ancho + x] != 0) {
                    continue;
                }
                if (lleno(copia, x - 1, y) || lleno(copia, x + 1, y) || lleno(copia, x, y - 1) || lleno(copia, x, y + 1)) {
                    px[y * ancho + x] = c;
                }
            }
        }
    }

    private boolean lleno(int[] datos, int x, int y) {
        return x >= 0 && y >= 0 && x < ancho && y < alto && datos[y * ancho + x] != 0;
    }

    // Imagen ampliada sin suavizado (cada pixel se vuelve un cuadro de escala x escala)
    WritableImage imagen(int escala, boolean voltear) {
        WritableImage img = new WritableImage(ancho * escala, alto * escala);
        PixelWriter w = img.getPixelWriter();
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                int c = px[y * ancho + (voltear ? ancho - 1 - x : x)];
                if (c == 0) {
                    continue;
                }
                for (int dy = 0; dy < escala; dy++) {
                    for (int dx = 0; dx < escala; dx++) {
                        w.setArgb(x * escala + dx, y * escala + dy, c);
                    }
                }
            }
        }
        return img;
    }

    int getAncho() {
        return ancho;
    }

    int getAlto() {
        return alto;
    }
}
