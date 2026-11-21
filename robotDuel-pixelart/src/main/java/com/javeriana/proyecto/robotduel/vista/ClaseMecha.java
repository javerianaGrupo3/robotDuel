package com.javeriana.proyecto.robotduel.vista;

import com.javeriana.proyecto.robotduel.modelo.Robot;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

// Apariencia de cada mecha. Se deduce de sus estadísticas (la más alta, con 10 puntos de ventaja),
// así los dos computadores dibujan igual a cada robot sin enviar nada extra por la red.
enum ClaseMecha {

    GATLING("GATLING", "ATAQUE"),
    ORBE("ORB", "DEFENSA"),
    KOI("KOI", "VELOCIDAD"),
    ORCA("WHITE", "ENERGIA"),
    CONEJO("MANGO", "EQUILIBRADO");

    private static final int VENTAJA_MINIMA = 10;

    // Paleta
    private static final int CONTORNO = Lienzo.color("#1a1c2c");
    private static final int BLANCO = Lienzo.color("#e8ecf2");
    private static final int BLANCO_SOMBRA = Lienzo.color("#a7b0c0");
    private static final int BRILLO = Lienzo.color("#ffffff");
    private static final int GRIS = Lienzo.color("#8b93a5");
    private static final int GRIS_OSCURO = Lienzo.color("#4d5366");
    private static final int GRIS_CLARO = Lienzo.color("#c2c8d4");
    private static final int ROJO = Lienzo.color("#e43b44");
    private static final int ROJO_OSCURO = Lienzo.color("#9e2835");
    private static final int ROJO_CLARO = Lienzo.color("#ff7a7a");
    private static final int AZUL = Lienzo.color("#3b5dc9");
    private static final int AZUL_OSCURO = Lienzo.color("#29366f");
    private static final int AZUL_CLARO = Lienzo.color("#73a3ff");
    private static final int CIAN = Lienzo.color("#41a6f6");
    private static final int CIAN_OSCURO = Lienzo.color("#2a5f9e");
    private static final int AMARILLO = Lienzo.color("#ffd25a");
    private static final int AMARILLO_OSCURO = Lienzo.color("#c98d26");
    private static final int AMARILLO_CLARO = Lienzo.color("#fff1a8");
    private static final int ORO = Lienzo.color("#e0a83a");
    private static final int CAFE = Lienzo.color("#8a4b2c");
    private static final int VERDE = Lienzo.color("#38b764");
    private static final int CREMA = Lienzo.color("#f2e6cf");
    private static final int CREMA_OSCURO = Lienzo.color("#c4b08f");
    private static final int ROSA = Lienzo.color("#f49ac1");
    private static final int ROSA_OSCURO = Lienzo.color("#b8578a");
    private static final int ROSA_CLARO = Lienzo.color("#ffd0e4");
    private static final int NARANJA = Lienzo.color("#ff8c26");

    private static final Map<String, Image> CACHE = new HashMap<>();
    private static final Map<ClaseMecha, Lienzo> DIBUJOS = new EnumMap<>(ClaseMecha.class);

    private final String nombre;
    private final String especialidad;

    ClaseMecha(String nombre, String especialidad) {
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    String getNombre() {
        return nombre;
    }

    String getEspecialidad() {
        return especialidad;
    }

    static ClaseMecha de(Robot robot) {
        return de(robot.getAtaque(), robot.getDefensa(), robot.getVelocidad(), robot.getEnergia());
    }

    static ClaseMecha de(int ataque, int defensa, int velocidad, int energia) {
        int[] valores = {ataque, defensa, velocidad, energia};
        ClaseMecha[] clases = {GATLING, ORBE, KOI, ORCA};
        int mayor = 0;
        for (int i = 1; i < valores.length; i++) {
            if (valores[i] > valores[mayor]) {
                mayor = i;
            }
        }
        for (int i = 0; i < valores.length; i++) {
            if (i != mayor && valores[mayor] - valores[i] < VENTAJA_MINIMA) {
                return CONEJO;
            }
        }
        return clases[mayor];
    }

    // Sprite mirando a la derecha (voltear = mirando a la izquierda)
    Image sprite(int escala, boolean voltear) {
        return CACHE.computeIfAbsent(name() + escala + voltear, k -> dibujo().imagen(escala, voltear));
    }

    int anchoBase() {
        return dibujo().getAncho();
    }

    int altoBase() {
        return dibujo().getAlto();
    }

    private Lienzo dibujo() {
        return DIBUJOS.computeIfAbsent(this, ClaseMecha::dibujar);
    }

    private static Lienzo dibujar(ClaseMecha clase) {
        Lienzo l = switch (clase) {
            case KOI -> koi();
            case ORCA -> orca();
            case CONEJO -> conejo();
            case ORBE -> orbe();
            case GATLING -> gatling();
        };
        l.contorno(CONTORNO);
        return l;
    }

    // Pez koi mecánico rojo y blanco
    private static Lienzo koi() {
        Lienzo l = new Lienzo(54, 42);
        // aletas de la cola
        l.poligono(ROJO, 16, 19, 2, 6, 5, 14, 9, 20);
        l.poligono(BLANCO, 17, 20, 15, 25, 6, 36, 2, 36, 9, 24);
        l.poligono(ROJO, 2, 36, 5, 32, 9, 32, 6, 36);
        l.elipse(16, 21, 3, 3, GRIS, GRIS_OSCURO, GRIS_CLARO);
        l.elipse(13, 21, 2, 2, ORO, CAFE, 0);
        // aleta dorsal y aletas de abajo
        l.poligono(ROJO, 21, 13, 39, 5, 42, 7, 34, 13);
        l.poligono(BLANCO, 24, 27, 21, 39, 25, 39, 29, 28);
        l.poligono(ROJO, 21, 39, 22, 36, 26, 36, 25, 39);
        l.poligono(BLANCO, 34, 28, 35, 38, 38, 38, 38, 28);
        l.poligono(ROJO, 35, 38, 35, 36, 38, 36, 38, 38);
        // cuerpo mecánico con costillas y resorte dorado
        l.rect(17, 17, 31, 27, GRIS);
        for (int x = 19; x <= 30; x += 2) {
            l.linea(x, 17, x, 27, GRIS_OSCURO);
        }
        l.rect(24, 18, 25, 26, ORO);
        l.elipse(20, 16, 6, 3, ROJO, ROJO_OSCURO, ROJO_CLARO);
        // cabeza
        l.elipse(39, 21, 11, 8.5, BLANCO, BLANCO_SOMBRA, BRILLO);
        l.elipse(37, 14, 8.5, 3.5, ROJO, ROJO_OSCURO, ROJO_CLARO);
        l.elipse(46, 18, 2.5, 1.6, ROJO, ROJO_OSCURO, 0);
        // ojo-lente
        l.elipse(37, 20, 3.4, 3.4, GRIS, GRIS_OSCURO, GRIS_CLARO);
        l.elipse(37, 20, 1.8, 1.8, CIAN, CIAN_OSCURO, BRILLO);
        l.linea(43, 25, 48, 23, GRIS_OSCURO);
        l.punto(33, 14, VERDE);
        l.punto(33, 16, VERDE);
        return l;
    }

    // Orca mecánica azul y blanca
    private static Lienzo orca() {
        Lienzo l = new Lienzo(56, 42);
        // cola
        l.poligono(AZUL, 7, 28, 0, 22, 3, 30, 0, 38, 8, 32);
        l.elipse(11, 28, 4.5, 3.5, BLANCO, BLANCO_SOMBRA, BRILLO);
        l.elipse(17, 25, 5, 4.5, BLANCO, BLANCO_SOMBRA, BRILLO);
        l.elipse(13, 27, 1.3, 1.3, AZUL, AZUL, 0);
        // aletas
        l.poligono(AZUL, 24, 28, 18, 39, 22, 40, 29, 29);
        l.poligono(AZUL, 38, 27, 39, 39, 43, 39, 43, 27);
        // núcleo mecánico con resorte
        l.rect(20, 14, 31, 28, GRIS);
        for (int y = 15; y <= 27; y += 2) {
            l.linea(22, y, 29, y, ORO);
        }
        l.linea(21, 14, 21, 28, GRIS_OSCURO);
        // cabeza grande
        l.elipse(39, 18, 15, 9.5, BLANCO, BLANCO_SOMBRA, BRILLO);
        l.elipse(40, 24, 12, 4, BLANCO, BLANCO_SOMBRA, 0);
        // correa azul que rodea el cuerpo
        l.poligono(AZUL, 26, 8, 29, 7, 34, 31, 31, 32);
        l.poligono(AZUL, 21, 6, 27, 2, 29, 4, 24, 8);
        l.poligono(AZUL_CLARO, 31, 9, 34, 3, 35, 9);
        // ojo y marcas
        l.elipse(49, 18, 1.7, 1.7, CIAN, CIAN_OSCURO, BRILLO);
        l.linea(44, 15, 46, 13, AZUL_CLARO);
        l.linea(44, 18, 47, 18, AZUL_CLARO);
        l.linea(44, 21, 46, 23, AZUL_CLARO);
        l.punto(37, 12, AZUL);
        l.punto(39, 12, AZUL);
        return l;
    }

    // Conejo mecha amarillo
    private static Lienzo conejo() {
        Lienzo l = new Lienzo(46, 48);
        // orejas con ventanas
        l.elipse(17, 10, 3.2, 9.5, AMARILLO, AMARILLO_OSCURO, AMARILLO_CLARO);
        l.elipse(27, 9, 3.2, 9.5, AMARILLO, AMARILLO_OSCURO, AMARILLO_CLARO);
        l.elipse(17, 9, 1.4, 6, GRIS_OSCURO, GRIS_OSCURO, 0);
        l.elipse(27, 8, 1.4, 6, GRIS_OSCURO, GRIS_OSCURO, 0);
        // pierna de atrás
        l.elipse(14, 32, 4, 5, AMARILLO_OSCURO, AMARILLO_OSCURO, 0);
        l.rect(12, 34, 16, 41, AMARILLO_OSCURO);
        l.elipse(14, 43, 5, 2.3, GRIS_OSCURO, GRIS_OSCURO, 0);
        // cuerpo
        l.elipse(23, 23, 15, 10.5, AMARILLO, AMARILLO_OSCURO, AMARILLO_CLARO);
        l.elipse(21, 14, 5, 3, CIAN, CIAN_OSCURO, BRILLO);
        l.linea(9, 22, 12, 22, AMARILLO_OSCURO);
        l.linea(9, 24, 12, 24, AMARILLO_OSCURO);
        // brazos
        l.rect(20, 31, 21, 37, GRIS);
        l.rect(25, 31, 26, 37, GRIS);
        l.rect(19, 37, 21, 38, GRIS_OSCURO);
        l.rect(25, 37, 27, 38, GRIS_OSCURO);
        // pierna de adelante
        l.elipse(31, 32, 4, 5, AMARILLO, AMARILLO_OSCURO, AMARILLO_CLARO);
        l.rect(29, 34, 33, 41, AMARILLO);
        l.linea(33, 34, 33, 41, AMARILLO_OSCURO);
        l.elipse(32, 43, 5, 2.3, GRIS, GRIS_OSCURO, GRIS_CLARO);
        // cara
        l.elipse(32, 21, 2.4, 2.4, BLANCO, BLANCO_SOMBRA, 0);
        l.rect(32, 21, 33, 22, CONTORNO);
        l.elipse(37, 21, 1.8, 2, BLANCO, BLANCO_SOMBRA, 0);
        l.punto(38, 21, CONTORNO);
        l.elipse(37, 25, 1.6, 1.2, CAFE, CAFE, 0);
        l.rect(34, 27, 37, 28, BLANCO);
        l.punto(35, 28, BLANCO_SOMBRA);
        return l;
    }

    // Esfera blanca sobre patas de cangrejo
    private static Lienzo orbe() {
        Lienzo l = new Lienzo(50, 46);
        // pinza de atrás
        l.elipse(10, 29, 5, 4, BLANCO_SOMBRA, BLANCO_SOMBRA, 0);
        l.poligono(BLANCO_SOMBRA, 5, 29, 1, 36, 5, 33, 7, 32);
        l.poligono(BLANCO_SOMBRA, 6, 27, 2, 21, 8, 25);
        l.punto(1, 35, CONTORNO);
        l.punto(2, 22, CONTORNO);
        // articulaciones
        l.elipse(16, 28, 2.6, 2.6, GRIS_OSCURO, GRIS_OSCURO, GRIS);
        l.elipse(34, 28, 2.6, 2.6, GRIS_OSCURO, GRIS_OSCURO, GRIS);
        l.elipse(13, 36, 2.5, 4, BLANCO_SOMBRA, BLANCO_SOMBRA, 0);
        // esfera
        l.elipse(25, 16, 12.5, 12.5, BLANCO, BLANCO_SOMBRA, BRILLO);
        // ojos sensores
        l.rect(30, 13, 31, 14, CONTORNO);
        l.rect(33, 16, 34, 17, CONTORNO);
        l.rect(30, 19, 31, 20, CONTORNO);
        // pata delantera puntuda
        l.poligono(BLANCO, 21, 27, 29, 27, 25, 43);
        l.linea(23, 32, 27, 32, CONTORNO);
        l.poligono(CONTORNO, 24, 39, 26, 39, 25, 43);
        // pinza/escudo de adelante
        l.elipse(39, 31, 6, 6.5, BLANCO, BLANCO_SOMBRA, BRILLO);
        l.elipse(39, 31, 3, 3, GRIS_CLARO, BLANCO_SOMBRA, 0);
        l.poligono(BLANCO, 41, 37, 46, 42, 42, 42, 39, 38);
        l.punto(45, 42, CONTORNO);
        return l;
    }

    // Gata artillera con cuatro ametralladoras
    private static Lienzo gatling() {
        Lienzo l = new Lienzo(50, 48);
        // cañones (cada uno con cinta rosada)
        canon(l, 8, 2, 12, 16, true);
        canon(l, 37, 2, 41, 16, true);
        canon(l, 1, 25, 14, 29, false);
        canon(l, 36, 25, 48, 29, false);
        // hombros
        l.elipse(13, 18, 3.5, 3, CREMA, CREMA_OSCURO, 0);
        l.elipse(37, 18, 3.5, 3, CREMA, CREMA_OSCURO, 0);
        l.elipse(15, 27, 3, 3, CREMA, CREMA_OSCURO, 0);
        l.elipse(35, 27, 3, 3, CREMA, CREMA_OSCURO, 0);
        // piernas
        l.elipse(20, 41, 3.5, 4, CREMA, CREMA_OSCURO, 0);
        l.elipse(30, 41, 3.5, 4, CREMA, CREMA_OSCURO, 0);
        l.elipse(20, 45, 3, 1.6, ROSA, ROSA_OSCURO, 0);
        l.elipse(30, 45, 3, 1.6, ROSA, ROSA_OSCURO, 0);
        // cuerpo y panza
        l.elipse(25, 31, 9, 9, CREMA, CREMA_OSCURO, BRILLO);
        l.elipse(25, 32, 5.5, 6, ROSA, ROSA_OSCURO, ROSA_CLARO);
        // cabeza con orejas
        l.poligono(ROSA, 16, 11, 17, 2, 22, 8);
        l.poligono(ROSA, 28, 8, 33, 2, 34, 11);
        l.elipse(25, 15, 9.5, 8, CREMA, CREMA_OSCURO, BRILLO);
        // visor
        l.elipse(26, 16, 6.2, 4.6, CIAN, AZUL, BRILLO);
        l.punto(24, 16, AZUL_OSCURO);
        l.punto(28, 16, AZUL_OSCURO);
        l.linea(25, 18, 27, 18, AZUL_OSCURO);
        l.punto(32, 9, ROSA);
        l.punto(18, 20, NARANJA);
        return l;
    }

    private static void canon(Lienzo l, int x0, int y0, int x1, int y1, boolean vertical) {
        l.rect(x0, y0, x1, y1, GRIS_OSCURO);
        if (vertical) {
            l.linea(x0 + 2, y0, x0 + 2, y1, CONTORNO);
            l.rect(x0, y0 + 9, x1, y0 + 11, ROSA);
            l.rect(x0, y0, x1, y0, GRIS);
        } else {
            l.linea(x0, y0 + 2, x1, y0 + 2, CONTORNO);
            int cinta = x0 < 25 ? x1 - 4 : x0 + 2;
            l.rect(cinta, y0, cinta + 2, y1, ROSA);
            int punta = x0 < 25 ? x0 : x1;
            l.rect(punta, y0, punta, y1, GRIS);
        }
    }

    // Iconos de los efectos de estado (8x8)
    static Image icono(char efecto, int escala) {
        return CACHE.computeIfAbsent("icono" + efecto + escala, k -> {
            Lienzo l = new Lienzo(9, 9);
            switch (Character.toUpperCase(efecto)) {
                case 'Q' -> {
                    l.poligono(NARANJA, 4.5, 0, 8, 5, 7, 8, 1, 8, 0, 5, 2, 3);
                    l.poligono(AMARILLO, 4.5, 3, 6, 6, 5.5, 8, 3, 8, 2.5, 6);
                }
                case 'R' -> {
                    l.rect(3, 1, 5, 7, VERDE);
                    l.rect(1, 3, 7, 5, VERDE);
                }
                default -> l.poligono(AMARILLO, 5, 0, 1, 5, 4, 5, 3, 9, 8, 3, 5, 3, 6, 0);
            }
            l.contorno(CONTORNO);
            return l.imagen(escala, false);
        });
    }
}
