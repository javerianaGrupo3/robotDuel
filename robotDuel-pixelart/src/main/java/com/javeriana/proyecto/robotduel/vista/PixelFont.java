package com.javeriana.proyecto.robotduel.vista;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Fuente de mapa de bits 5x7 para que todo el texto del juego se vea en pixel art
// Solo tiene mayúsculas: el texto se pasa a mayúsculas y sin tildes antes de dibujarlo.
final class PixelFont {

    static final int ANCHO = 5;
    static final int ALTO = 7;
    static final int AVANCE = 6;      // ancho de la letra + 1 de espacio
    static final int INTERLINEA = 10; // alto de la letra + 3 de espacio

    private static final Map<Character, boolean[][]> GLIFOS = new HashMap<>();

    private PixelFont() {
    }

    static {
        def('A', ".###.", "#...#", "#...#", "#####", "#...#", "#...#", "#...#");
        def('B', "####.", "#...#", "#...#", "####.", "#...#", "#...#", "####.");
        def('C', ".###.", "#...#", "#....", "#....", "#....", "#...#", ".###.");
        def('D', "####.", "#...#", "#...#", "#...#", "#...#", "#...#", "####.");
        def('E', "#####", "#....", "#....", "####.", "#....", "#....", "#####");
        def('F', "#####", "#....", "#....", "####.", "#....", "#....", "#....");
        def('G', ".###.", "#...#", "#....", "#.###", "#...#", "#...#", ".####");
        def('H', "#...#", "#...#", "#...#", "#####", "#...#", "#...#", "#...#");
        def('I', ".###.", "..#..", "..#..", "..#..", "..#..", "..#..", ".###.");
        def('J', "..###", "...#.", "...#.", "...#.", "#..#.", "#..#.", ".##..");
        def('K', "#...#", "#..#.", "#.#..", "##...", "#.#..", "#..#.", "#...#");
        def('L', "#....", "#....", "#....", "#....", "#....", "#....", "#####");
        def('M', "#...#", "##.##", "#.#.#", "#.#.#", "#...#", "#...#", "#...#");
        def('N', "#...#", "##..#", "#.#.#", "#..##", "#...#", "#...#", "#...#");
        def('O', ".###.", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.");
        def('P', "####.", "#...#", "#...#", "####.", "#....", "#....", "#....");
        def('Q', ".###.", "#...#", "#...#", "#...#", "#.#.#", "#..#.", ".##.#");
        def('R', "####.", "#...#", "#...#", "####.", "#.#..", "#..#.", "#...#");
        def('S', ".####", "#....", "#....", ".###.", "....#", "....#", "####.");
        def('T', "#####", "..#..", "..#..", "..#..", "..#..", "..#..", "..#..");
        def('U', "#...#", "#...#", "#...#", "#...#", "#...#", "#...#", ".###.");
        def('V', "#...#", "#...#", "#...#", "#...#", "#...#", ".#.#.", "..#..");
        def('W', "#...#", "#...#", "#...#", "#.#.#", "#.#.#", "#.#.#", ".#.#.");
        def('X', "#...#", "#...#", ".#.#.", "..#..", ".#.#.", "#...#", "#...#");
        def('Y', "#...#", "#...#", ".#.#.", "..#..", "..#..", "..#..", "..#..");
        def('Z', "#####", "....#", "...#.", "..#..", ".#...", "#....", "#####");
        def('0', ".###.", "#...#", "#..##", "#.#.#", "##..#", "#...#", ".###.");
        def('1', "..#..", ".##..", "..#..", "..#..", "..#..", "..#..", ".###.");
        def('2', ".###.", "#...#", "....#", "...#.", "..#..", ".#...", "#####");
        def('3', "####.", "....#", "....#", ".###.", "....#", "....#", "####.");
        def('4', "...#.", "..##.", ".#.#.", "#..#.", "#####", "...#.", "...#.");
        def('5', "#####", "#....", "####.", "....#", "....#", "#...#", ".###.");
        def('6', ".###.", "#....", "#....", "####.", "#...#", "#...#", ".###.");
        def('7', "#####", "....#", "...#.", "..#..", ".#...", ".#...", ".#...");
        def('8', ".###.", "#...#", "#...#", ".###.", "#...#", "#...#", ".###.");
        def('9', ".###.", "#...#", "#...#", ".####", "....#", "....#", ".###.");
        def(' ', ".....", ".....", ".....", ".....", ".....", ".....", ".....");
        def('.', ".....", ".....", ".....", ".....", ".....", ".##..", ".##..");
        def(',', ".....", ".....", ".....", ".....", ".##..", "..#..", ".#...");
        def(':', ".....", ".##..", ".##..", ".....", ".##..", ".##..", ".....");
        def(';', ".....", ".##..", ".##..", ".....", ".##..", "..#..", ".#...");
        def('!', "..#..", "..#..", "..#..", "..#..", "..#..", ".....", "..#..");
        def('?', ".###.", "#...#", "....#", "...#.", "..#..", ".....", "..#..");
        def('-', ".....", ".....", ".....", "#####", ".....", ".....", ".....");
        def('+', ".....", "..#..", "..#..", "#####", "..#..", "..#..", ".....");
        def('=', ".....", ".....", "#####", ".....", "#####", ".....", ".....");
        def('/', "....#", "...#.", "...#.", "..#..", ".#...", ".#...", "#....");
        def('(', "...#.", "..#..", ".#...", ".#...", ".#...", "..#..", "...#.");
        def(')', ".#...", "..#..", "...#.", "...#.", "...#.", "..#..", ".#...");
        def('[', ".###.", ".#...", ".#...", ".#...", ".#...", ".#...", ".###.");
        def(']', ".###.", "...#.", "...#.", "...#.", "...#.", "...#.", ".###.");
        def('%', "##..#", "##..#", "...#.", "..#..", ".#...", "#..##", "#..##");
        def('\'', "..#..", "..#..", ".....", ".....", ".....", ".....", ".....");
        def('"', ".#.#.", ".#.#.", ".....", ".....", ".....", ".....", ".....");
        def('#', ".#.#.", "#####", ".#.#.", ".#.#.", "#####", ".#.#.", ".....");
        def('*', ".....", "#.#.#", ".###.", "#####", ".###.", "#.#.#", ".....");
        def('_', ".....", ".....", ".....", ".....", ".....", ".....", "#####");
        def('|', "..#..", "..#..", "..#..", "..#..", "..#..", "..#..", "..#..");
        def('<', "...#.", "..#..", ".#...", "#....", ".#...", "..#..", "...#.");
        def('>', ".#...", "..#..", "...#.", "....#", "...#.", "..#..", ".#...");
        def('&', ".##..", "#..#.", "#.#..", ".#...", "#.#.#", "#..#.", ".##.#");
        def('►', "#....", "##...", "###..", "####.", "###..", "##...", "#....");
        def('▼', ".....", "#####", "#####", ".###.", ".###.", "..#..", ".....");
    }

    private static void def(char c, String... filas) {
        boolean[][] g = new boolean[ALTO][ANCHO];
        for (int y = 0; y < ALTO; y++) {
            for (int x = 0; x < ANCHO; x++) {
                g[y][x] = filas[y].charAt(x) == '#';
            }
        }
        GLIFOS.put(c, g);
    }

    // Mayusculas, sin tildes y sin signos de apertura
    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinTildes = Normalizer.normalize(texto.replace('·', '*'), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toUpperCase().replace("¡", "").replace("¿", "").replace('²', '2');
    }

    private static boolean[][] glifo(char c) {
        return GLIFOS.getOrDefault(c, GLIFOS.get('?'));
    }

    static int ancho(String linea, int escala) {
        return linea.isEmpty() ? 0 : (linea.length() * AVANCE - 1) * escala;
    }

    // Dibuja una línea ya normalizada. Devuelve cuántos caracteres dibujó.
    static int dibujar(GraphicsContext g, String linea, double x, double y, int escala, Color color, int maximo) {
        g.setFill(color);
        int dibujados = 0;
        for (int i = 0; i < linea.length() && dibujados < maximo; i++, dibujados++) {
            boolean[][] gl = glifo(linea.charAt(i));
            for (int fy = 0; fy < ALTO; fy++) {
                for (int fx = 0; fx < ANCHO; fx++) {
                    if (gl[fy][fx]) {
                        g.fillRect(x + (i * AVANCE + fx) * escala, y + fy * escala, escala, escala);
                    }
                }
            }
        }
        return dibujados;
    }

    // Parte el texto en líneas de máximo "columnas" caracteres sin cortar palabras
    static List<String> envolver(String texto, int columnas) {
        List<String> lineas = new ArrayList<>();
        for (String parrafo : texto.split("\n", -1)) {
            StringBuilder actual = new StringBuilder();
            for (String palabra : parrafo.split(" ")) {
                while (palabra.length() > columnas) {
                    if (!actual.isEmpty()) {
                        lineas.add(actual.toString());
                        actual.setLength(0);
                    }
                    lineas.add(palabra.substring(0, columnas));
                    palabra = palabra.substring(columnas);
                }
                if (actual.isEmpty()) {
                    actual.append(palabra);
                } else if (actual.length() + 1 + palabra.length() <= columnas) {
                    actual.append(' ').append(palabra);
                } else {
                    lineas.add(actual.toString());
                    actual.setLength(0);
                    actual.append(palabra);
                }
            }
            lineas.add(actual.toString());
        }
        return lineas;
    }
}
