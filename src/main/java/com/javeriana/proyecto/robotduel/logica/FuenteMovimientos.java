package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.Movimiento;

public interface FuenteMovimientos {
    Movimiento siguienteMovimiento() throws InterruptedException;
}