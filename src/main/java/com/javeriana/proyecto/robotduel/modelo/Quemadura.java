package com.javeriana.proyecto.robotduel.modelo;

public class Quemadura extends Efecto{

    private static final int Damage = 3;
    public Quemadura() {
        super('Q', "Quemadura");
    }

    @Override
    public void aplicar(EstadoCombate propio, EstadoCombate enemigo) {
        enemigo.recibirDmg(Damage);
    }
}
