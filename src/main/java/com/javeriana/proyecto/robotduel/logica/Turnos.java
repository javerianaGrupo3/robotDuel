package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;

import java.util.Random;

public class Turnos {

    private final EstadoCombate jugadorA;
    private final EstadoCombate jugadorB;
    private final Random azar;

    public Turnos(EstadoCombate jugadorA, EstadoCombate jugadorB, Random azar) {
        if (jugadorA == null || jugadorB == null) {
            throw new IllegalArgumentException("Los dos jugadores son obligatorios");
        }
        if (azar == null) {
            throw new IllegalArgumentException("Falta el generador aleatorio");
        }
        this.jugadorA = jugadorA;
        this.jugadorB = jugadorB;
        this.azar = azar;
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
        // Empate: los dos llegaron a 100 al mismo tiempo. Decide el azar.
        // En red ambos computadores deben usar la misma semilla para obtener el mismo resultado.
        return azar.nextBoolean() ? jugadorA : jugadorB;
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