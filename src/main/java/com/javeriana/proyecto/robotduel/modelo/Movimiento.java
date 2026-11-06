package com.javeriana.proyecto.robotduel.modelo;

public abstract class Movimiento {

    private final String nombre;

    protected Movimiento(String nombre) {
        this.nombre = nombre;
    }

    public abstract void ejecutar(EstadoCombate actor, EstadoCombate rival, ResultadoAtaque resultado);

    public boolean requiereRespuesta() {
        return false;
    }

    public String getNombre() {
        return nombre;
    }

    public static Movimiento desdeTexto(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("Movimiento vacío");
        }
        return switch (texto.trim().toUpperCase()) {
            case "ATACAR", "ATTACK" -> new Ataque();
            case "ESPECIAL", "SPECIAL" -> new Especial();
            case "ESQUIVAR", "DODGE" -> new Esquivar();
            case "CURAR", "HEAL" -> new Curar();
            case "CARGAR", "CHARGE" -> new Cargar();
            default -> throw new IllegalArgumentException("Movimiento desconocido: " + texto);
        };
    }

    @Override
    public String toString() {
        return nombre;
    }
}