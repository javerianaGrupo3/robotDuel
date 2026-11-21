package com.javeriana.proyecto.robotduel.modelo;

public class Curar extends Movimiento {

    private static final int CURACION = 10;

    public Curar() {
        super("HEAL");
    }

    @Override
    public void ejecutar(EstadoCombate actor, EstadoCombate rival, ResultadoAtaque resultado) {
        actor.curar(CURACION);
    }
}