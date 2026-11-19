package com.javeriana.proyecto.robotduel.modelo;

public class Especial extends MovimientoOfensivo {

    public Especial() {
        super("SPECIAL");
    }

    // No se puede usar con energia en 0
    @Override
    public boolean puedeEjecutar(EstadoCombate actor) {
        return actor.getEnergia() > 0;
    }

    @Override
    protected double calcularDmg(EstadoCombate actor, EstadoCombate rival) {
        double potencia = actor.getRobot().getAtaque() + 0.5 * actor.getEnergia();
        int defensa = rival.getRobot().getDefensa();
        return formulaDmg(potencia, defensa);
    }

    @Override
    protected void despuesDeAtacar(EstadoCombate actor) {
        actor.vaciarEnergia();
    }
}