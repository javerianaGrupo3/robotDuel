package com.javeriana.proyecto.robotduel.vista;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

// Iconos en pixel art de los efectos de estado llama (Quemadura), cruz (Regeneración), rayo (Núcleo Activo)
final class Iconos {

    private static final int CONTORNO = Lienzo.color("#1a1c2c");
    private static final int NARANJA = Lienzo.color("#ff8c26");
    private static final int AMARILLO = Lienzo.color("#ffd25a");
    private static final int VERDE = Lienzo.color("#38b764");

    private static final Map<String, Image> CACHE = new HashMap<>();

    private Iconos() {
    }

    static Image efecto(char letra, int escala) {
        return CACHE.computeIfAbsent("" + letra + escala, k -> {
            Lienzo l = new Lienzo(11, 11);
            switch (Character.toUpperCase(letra)) {
                case 'Q' -> {
                    l.poligono(NARANJA, 5.5, 1, 9, 6, 8, 9, 2, 9, 1, 6, 3, 4);
                    l.poligono(AMARILLO, 5.5, 4, 7, 7, 6.5, 9, 4, 9, 3.5, 7);
                }
                case 'R' -> {
                    l.rect(4, 2, 6, 8, VERDE);
                    l.rect(2, 4, 8, 6, VERDE);
                }
                default -> l.poligono(AMARILLO, 6, 1, 2, 6, 5, 6, 4, 10, 9, 4, 6, 4, 7, 1);
            }
            l.contorno(CONTORNO);
            return l.imagen(escala, false);
        });
    }
}
