package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.Movimiento;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EstrategiaManual implements FuenteMovimientos {

    private final BlockingQueue<Movimiento> pendientes = new LinkedBlockingQueue<>();
    private final List<Movimiento> elegidos = new ArrayList<>();

    public void elegir(Movimiento movimiento) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento no puede ser null");
        }
        pendientes.add(movimiento);
    }

    @Override
    public Movimiento siguienteMovimiento() throws InterruptedException {
        Movimiento movimiento = pendientes.take();
        synchronized (elegidos) {
            elegidos.add(movimiento);
        }
        return movimiento;
    }

    public List<Movimiento> getMovimientosElegidos() {
        synchronized (elegidos) {
            return Collections.unmodifiableList(new ArrayList<>(elegidos));
        }
    }
}