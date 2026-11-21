package com.javeriana.proyecto.robotduel.modelo;

public abstract class Movimiento {

    private final String nombre;

    protected Movimiento(String nombre) {
        this.nombre = nombre;
    }

    public abstract void ejecutar(EstadoCombate actor, EstadoCombate rival, ResultadoAtaque resultado);

    // Los ataques necesitan que el rival responda HIT o DODGE
    public boolean requiereRespuesta() {
        return false;
    }

    public String getNombre() {
        return nombre;
    }

    // Entiende ingles y español para los sockets
    public static Movimiento desdeTexto(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("Movimiento vacío");
        }
        return switch (texto.trim().toUpperCase()) {
            case "ATTACK", "ATACAR" -> new Ataque();
            case "SPECIAL", "ESPECIAL" -> new Especial();
            case "DODGE", "ESQUIVAR" -> new Esquivar();
            case "HEAL", "CURAR" -> new Curar();
            case "CHARGE", "CARGAR" -> new Cargar();
            default -> throw new IllegalArgumentException("Movimiento desconocido: " + texto);
        };
    }

    @Override
    public String toString() {
        return nombre;
    }
}
