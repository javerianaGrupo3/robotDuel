package com.javeriana.proyecto.robotduel.modelo;

public class Esquivar extends Movimiento {

    public Esquivar() {
        super("DODGE");
    }

    @Override
    public void ejecutar(EstadoCombate actor, EstadoCombate rival, ResultadoAtaque resultado) {
        actor.agregarEsquive();
    }
}