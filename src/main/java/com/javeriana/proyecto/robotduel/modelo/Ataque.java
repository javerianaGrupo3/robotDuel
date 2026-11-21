package com.javeriana.proyecto.robotduel.modelo;

public class Ataque extends MovimientoOfensivo {

    public Ataque() {
        super("ATTACK");
    }

    @Override
    protected double calcularDmg(EstadoCombate actor, EstadoCombate rival) {
        int ataque = actor.getRobot().getAtaque();
        int defensa = rival.getRobot().getDefensa();
        return formulaDmg(ataque, defensa);
    }
}