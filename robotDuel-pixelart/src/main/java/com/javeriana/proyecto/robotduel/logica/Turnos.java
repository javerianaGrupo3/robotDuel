package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;

// Cada robot suma su SPD a un acumulador; al llegar a 100 juega y se le restan 100
public class Turnos {

    private final EstadoCombate jugadorA;
    private final EstadoCombate jugadorB;
    private final boolean empateParaA;

    // empateParaA: quién juega si los dos llegan a 100 con el mismo acumulador y la misma velocidad.
    // En red cada computador lo calcula de forma que los dos lleguen a la misma respuesta.
    public Turnos(EstadoCombate jugadorA, EstadoCombate jugadorB, boolean empateParaA) {
        if (jugadorA == null || jugadorB == null) {
            throw new IllegalArgumentException("Los dos jugadores son obligatorios");
        }
        this.jugadorA = jugadorA;
        this.jugadorB = jugadorB;
        this.empateParaA = empateParaA;
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
        // Los dos pueden jugar: primero el que tenga más acumulado
        int acumuladoA = jugadorA.getAcumuladorTurno();
        int acumuladoB = jugadorB.getAcumuladorTurno();
        if (acumuladoA != acumuladoB) {
            return acumuladoA > acumuladoB ? jugadorA : jugadorB;
        }
        // Luego el de más velocidad
        int velocidadA = jugadorA.getRobot().getVelocidad();
        int velocidadB = jugadorB.getRobot().getVelocidad();
        if (velocidadA != velocidadB) {
            return velocidadA > velocidadB ? jugadorA : jugadorB;
        }
        // Empate total
        return empateParaA ? jugadorA : jugadorB;
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
