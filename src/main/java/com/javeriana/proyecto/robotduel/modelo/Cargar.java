package com.javeriana.proyecto.robotduel.modelo;

public class Cargar extends Movimiento {

    private static final int ENERGIA = 10;

    public Cargar() {
        super("CHARGE");
    }

    @Override
    public void ejecutar(EstadoCombate actor, EstadoCombate rival, ResultadoAtaque resultado) {
        actor.cargarEnergia(ENERGIA);
    }
}