package com.javeriana.proyecto.robotduel.red;

import com.javeriana.proyecto.robotduel.logica.FuenteMovimientos;
import com.javeriana.proyecto.robotduel.modelo.Movimiento;

import java.io.IOException;

// obtiene su movimiento se lo envía al rival y lo devuelve al combate.
public class FuenteLocalEnRed implements FuenteMovimientos {

    private final FuenteMovimientos fuenteLocal;
    private final Conexion conexion;

    public FuenteLocalEnRed(FuenteMovimientos fuenteLocal, Conexion conexion) {
        if (fuenteLocal == null || conexion == null) {
            throw new IllegalArgumentException("Faltan la fuente local o la conexión");
        }
        this.fuenteLocal = fuenteLocal;
        this.conexion = conexion;
    }

    @Override
    public Movimiento siguienteMovimiento() throws InterruptedException {
        Movimiento movimiento = fuenteLocal.siguienteMovimiento();
        try {
            conexion.enviar(Protocolo.textoMovimiento(movimiento));
        } catch (IOException e) {
            throw new ConexionPerdidaException("No se pudo enviar el movimiento: " + e.getMessage(), e);
        }
        return movimiento;
    }

    public FuenteMovimientos getFuenteLocal() {
        return fuenteLocal;
    }
}