package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.Movimiento;

public class RivalCPU implements FuenteMovimientos {

    private final FuenteMovimientos movimientos;
    private final long pausaMs;

    public RivalCPU(FuenteMovimientos movimientos, long pausaMs) {
        if (movimientos == null) {
            throw new IllegalArgumentException("La CPU necesita una fuente de movimientos");
        }
        this.movimientos = movimientos;
        this.pausaMs = Math.max(0, pausaMs);
    }

    @Override
    public Movimiento siguienteMovimiento() throws InterruptedException {
        if (pausaMs > 0) {
            Thread.sleep(pausaMs);
        }
        return movimientos.siguienteMovimiento();
    }
}
