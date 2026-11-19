package com.javeriana.proyecto.robotduel.red;

import com.javeriana.proyecto.robotduel.logica.FuenteMovimientos;
import com.javeriana.proyecto.robotduel.modelo.Movimiento;

import java.io.IOException;

// Fuente de movimientos del riva cada movimiento llega por la red
public class JugadorRemoto implements FuenteMovimientos {

    private final Conexion conexion;

    public JugadorRemoto(Conexion conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula");
        }
        this.conexion = conexion;
    }

    // Espera hasta que el rival envíe su movimiento
    @Override
    public Movimiento siguienteMovimiento() {
        try {
            String mensaje = conexion.recibir();
            return Protocolo.leerMovimiento(mensaje);
        } catch (IOException e) {
            throw new ConexionPerdidaException(e.getMessage(), e);
        }
    }
}