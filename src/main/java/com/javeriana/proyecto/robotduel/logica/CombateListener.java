package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;

public interface CombateListener {

    default void turnoJugado(RegistroTurno registro) {
    }

    default void combateTerminado(EstadoCombate ganador) {
    }
}