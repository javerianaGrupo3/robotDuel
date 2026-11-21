package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.archivos.GestorEstrategia;
import com.javeriana.proyecto.robotduel.modelo.Movimiento;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Modo sin archivo de estrategia: el jugador elige cada movimiento en la interfaz.
// Si se le da un GestorEstrategia, el archivo se va creando durante el combate con cada movimiento elegido.
public class EstrategiaManual implements FuenteMovimientos {

    private final BlockingQueue<Movimiento> pendientes = new LinkedBlockingQueue<>();
    private final List<Movimiento> elegidos = new ArrayList<>();
    private final GestorEstrategia archivo;

    public EstrategiaManual() {
        this(null);
    }

    public EstrategiaManual(GestorEstrategia archivo) {
        this.archivo = archivo;
    }

    public void elegir(Movimiento movimiento) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento no puede ser null");
        }
        pendientes.add(movimiento);
    }

    @Override
    public Movimiento siguienteMovimiento() throws InterruptedException {
        Movimiento movimiento = pendientes.take();
        boolean primero;
        synchronized (elegidos) {
            primero = elegidos.isEmpty();
            elegidos.add(movimiento);
        }
        if (archivo != null) {
            try {
                // El primer movimiento crea (o reemplaza) el archivo; los demás se agregan al final
                if (primero) {
                    archivo.guardar(List.of(movimiento));
                } else {
                    archivo.agregar(movimiento);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("No se pudo escribir " + archivo.getArchivo(), e);
            }
        }
        return movimiento;
    }

    public List<Movimiento> getMovimientosElegidos() {
        synchronized (elegidos) {
            return Collections.unmodifiableList(new ArrayList<>(elegidos));
        }
    }
}
