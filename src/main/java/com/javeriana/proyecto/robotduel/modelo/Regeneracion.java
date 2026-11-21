package com.javeriana.proyecto.robotduel.modelo;

public class Regeneracion extends Efecto {

    private static final int Curacion = 3;

    public Regeneracion() {
        super('R', "Regeneración");
    }

    @Override
    public void aplicar(EstadoCombate propio, EstadoCombate enemigo) {
        propio.curar(Curacion);
    }
}