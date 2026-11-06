package com.javeriana.proyecto.robotduel.modelo;
import java.io.Serializable;

public abstract class Efecto implements Serializable{

    private static final long serialVersionUID = 1L;

    private final char letra;
    private final String nombre;

    protected Efecto(char letra, String nombre) {
        this.letra = letra;
        this.nombre = nombre;
    }
    public abstract void aplicar(EstadoCombate propio, EstadoCombate enemigo);

    public char getLetra() {
        return letra;
    }

    public String getNombre() {
        return nombre;
    }

    public static Efecto desdeLetra(char letra) {
        return switch (Character.toUpperCase(letra)) {
            case 'Q' -> new Quemadura();
            case 'R' -> new Regeneracion();
            case 'N' -> new NucleoActivo();
            default -> throw new IllegalArgumentException("Efecto desconocido: " + letra);
        };
    }

    @Override
    public String toString() {
        return nombre;
    }
}
