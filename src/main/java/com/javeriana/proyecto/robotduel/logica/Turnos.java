package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;

public class Turnos {

    private final EstadoCombate jugadorA;
    private final EstadoCombate jugadorB;

    public Turnos(EstadoCombate jugadorA, EstadoCombate jugadorB) {
        if (jugadorA == null || jugadorB == null) {
            throw new IllegalArgumentException("Los dos jugadores son obligatorios");
        }
        if (jugadorA.getRobot().getVelocidad() <= 0 && jugadorB.getRobot().getVelocidad() <= 0) {
            throw new IllegalArgumentException("Al menos un robot debe tener velocidad mayor a 0");
        }
        this.jugadorA = jugadorA;
        this.jugadorB = jugadorB;
    }

    public EstadoCombate siguienteTurno() {
        while (!jugadorA.puedeActuar() && !jugadorB.puedeActuar()) {
            jugadorA.avanzarAcumulador();
            jugadorB.avanzarAcumulador();
        }
        EstadoCombate turno = elegirQuienActua();
        turno.consumirTurno();
        return turno;
    }

    private EstadoCombate elegirQuienActua() {
        boolean puedeA = jugadorA.puedeActuar();
        boolean puedeB = jugadorB.puedeActuar();

        if (puedeA && !puedeB) {
            return jugadorA;
        }
        if (puedeB && !puedeA) {
            return jugadorB;
        }

        if (jugadorA.getAcumuladorTurno() != jugadorB.getAcumuladorTurno()) {
            return jugadorA.getAcumuladorTurno() > jugadorB.getAcumuladorTurno() ? jugadorA : jugadorB;
        }

        int velocidadA = jugadorA.getRobot().getVelocidad();
        int velocidadB = jugadorB.getRobot().getVelocidad();
        if (velocidadA != velocidadB) {
            return velocidadA > velocidadB ? jugadorA : jugadorB;
        }

        return jugadorA;
    }

    public EstadoCombate getRival(EstadoCombate actor) {
        return actor == jugadorA ? jugadorB : jugadorA;
    }

    public EstadoCombate getJugadorA() {
        return jugadorA;
    }

    public EstadoCombate getJugadorB() {
        return jugadorB;
    }
}