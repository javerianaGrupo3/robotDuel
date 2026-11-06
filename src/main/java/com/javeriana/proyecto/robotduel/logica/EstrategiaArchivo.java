package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.Movimiento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EstrategiaArchivo implements FuenteMovimientos {

    private final List<Movimiento> movimientos;
    private int indice;

    public EstrategiaArchivo(List<Movimiento> movimientos) {
        if (movimientos == null || movimientos.isEmpty()) {
            throw new IllegalArgumentException("La estrategia debe tener al menos un movimiento");
        }
        this.movimientos = new ArrayList<>(movimientos);
        this.indice = 0;
    }

    @Override
    public Movimiento siguienteMovimiento() {
        Movimiento movimiento = movimientos.get(indice);
        indice = (indice + 1) % movimientos.size(); // al llegar al final, vuelve al inicio
        return movimiento;
    }

    public List<Movimiento> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }
}