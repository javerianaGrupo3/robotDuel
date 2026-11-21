package com.javeriana.proyecto.robotduel.vista;

import com.javeriana.proyecto.robotduel.modelo.Robot;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

// Los 7 mechas que se pueden elegir en el ensamble. Las imágenes están en resources/.../vista/mechas
enum ModeloMecha {

    MANGO("MANGO", "conejo.png", true),
    ORB("ORB", "orbe.png", true),
    SHARKNADO("SHARKNADO", "tiburon.png", false),
    MANOTAS("MANOTAS", "garra.png", true),
    GATLING("GATLING", "gatling.png", true),
    KOI("KOI", "koi.png", true),
    WHITE("WHITE", "orca.png", true);

    private static final int VENTAJA_MINIMA = 10;
    private static final Map<ModeloMecha, Image> IMAGENES = new EnumMap<>(ModeloMecha.class);

    private final String nombre;
    private final String archivo;
    private final boolean miraDerecha;

    ModeloMecha(String nombre, String archivo, boolean miraDerecha) {
        this.nombre = nombre;
        this.archivo = archivo;
        this.miraDerecha = miraDerecha;
    }

    String getNombre() {
        return nombre;
    }

    // Apariencia de un robot: la que eligió el jugador, o si no viene (robot de otro programa
    // o guardado antes de existir la galería) se deduce de su estadística más alta
    static ModeloMecha de(Robot robot) {
        if (robot.getModelo() != null) {
            for (ModeloMecha m : values()) {
                if (m.name().equalsIgnoreCase(robot.getModelo())) {
                    return m;
                }
            }
        }
        return porEstadisticas(robot.getAtaque(), robot.getDefensa(), robot.getVelocidad(), robot.getEnergia());
    }

    static ModeloMecha porEstadisticas(int ataque, int defensa, int velocidad, int energia) {
        int[] valores = {ataque, defensa, velocidad, energia};
        ModeloMecha[] modelos = {GATLING, ORB, KOI, WHITE};
        int mayor = 0;
        for (int i = 1; i < valores.length; i++) {
            if (valores[i] > valores[mayor]) {
                mayor = i;
            }
        }
        for (int i = 0; i < valores.length; i++) {
            if (i != mayor && valores[mayor] - valores[i] < VENTAJA_MINIMA) {
                return MANGO;
            }
        }
        return modelos[mayor];
    }

    Image imagen() {
        return IMAGENES.computeIfAbsent(this, m -> {
            try (InputStream entrada = ModeloMecha.class.getResourceAsStream("mechas/" + m.archivo)) {
                if (entrada == null) {
                    throw new IllegalStateException("Falta la imagen mechas/" + m.archivo);
                }
                return new Image(entrada);
            } catch (java.io.IOException e) {
                throw new IllegalStateException("No se pudo leer mechas/" + m.archivo, e);
            }
        });
    }

    // Imagen que cabe en alto x anchoMaximo, mirando a la derecha o a la izquierda
    ImageView vista(double alto, double anchoMaximo, boolean mirarDerecha) {
        Image img = imagen();
        double escala = Math.min(alto / img.getHeight(), anchoMaximo / img.getWidth());
        ImageView vista = new ImageView(img);
        vista.setSmooth(true);
        vista.setFitWidth(Math.round(img.getWidth() * escala));
        vista.setFitHeight(Math.round(img.getHeight() * escala));
        vista.setScaleX(mirarDerecha == miraDerecha ? 1 : -1);
        return vista;
    }
}
