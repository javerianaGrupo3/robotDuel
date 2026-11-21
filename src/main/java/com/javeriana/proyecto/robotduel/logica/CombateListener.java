package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;

public interface CombateListener {

    // Empieza el turno de actor (ya se aplicaron sus efectos de estado)
    default void turnoIniciado(EstadoCombate actor) {
    }

    default void turnoJugado(RegistroTurno registro) {
    }

    default void combateTerminado(EstadoCombate ganador) {
    }
}
