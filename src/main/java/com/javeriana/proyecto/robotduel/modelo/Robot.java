package com.javeriana.proyecto.robotduel.modelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Robot implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int VIDA_INICIAL = 100;
    public static final int TOTAL_PUNTOS = 200;

    private final String nombre;
    private final int ataque;
    private final int defensa;
    private final int velocidad;
    private final int energia;
    private final List<Efecto> efectos;

    public Robot(String nombre, int ataque, int defensa, int velocidad, int energia, List<Efecto> efectos) {
        validar(nombre, ataque, defensa, velocidad, energia, efectos);
        this.nombre = nombre.trim();
        this.ataque = ataque;
        this.defensa = defensa;
        this.velocidad = velocidad;
        this.energia = energia;
        this.efectos = new ArrayList<>(efectos);
    }

    private static void validar(String nombre, int ataque, int defensa, int velocidad, int energia, List<Efecto> efectos) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El robot debe tener un nombre");
        }
        if (ataque < 0 || defensa < 0 || velocidad < 0 || energia < 0) {
            throw new IllegalArgumentException("Las estadísticas no pueden ser negativas");
        }
        int suma = ataque + defensa + velocidad + energia;
        if (suma != TOTAL_PUNTOS) {
            throw new IllegalArgumentException(
                    "Las estadísticas deben sumar " + TOTAL_PUNTOS + " y suman " + suma);
        }
        if (efectos == null) {
            throw new IllegalArgumentException("La lista de efectos no puede ser nula");
        }
    }

    public String getNombre() {
        return nombre;
    }

    public int getAtaque() {
        return ataque;
    }

    public int getDefensa() {
        return defensa;
    }

    public int getVelocidad() {
        return velocidad;
    }

    public int getEnergia() {
        return energia;
    }

    public List<Efecto> getEfectos() {
        return Collections.unmodifiableList(efectos);
    }

    @Override
    public String toString() {
        return nombre + " [Ataque " + ataque + ", Defensa " + defensa + ", Velocidad " + velocidad + ", Energia " + energia + ", Efectos " + efectos + "]";
    }
}