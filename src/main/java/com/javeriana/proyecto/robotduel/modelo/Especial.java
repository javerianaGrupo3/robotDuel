package com.javeriana.proyecto.robotduel.modelo;

public class Especial extends MovimientoOfensivo {

    public Especial() {
        super("SPECIAL");
    }

    // Se puede usar con cualquier energía (con 0 pega igual que un ataque normal)
    @Override
    protected double calcularDmg(EstadoCombate actor, EstadoCombate rival) {
        double potencia = actor.getRobot().getAtaque() + 0.5 * actor.getEnergia();
        int defensa = rival.getRobot().getDefensa();
        return formulaDmg(potencia, defensa);
    }

    // Luego del ataque la energía vuelve a cero
    @Override
    protected void despuesDeAtacar(EstadoCombate actor) {
        actor.vaciarEnergia();
    }
}
