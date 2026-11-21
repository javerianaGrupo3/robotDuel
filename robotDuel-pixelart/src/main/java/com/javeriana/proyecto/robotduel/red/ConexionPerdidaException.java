package com.javeriana.proyecto.robotduel.red;

// Error de red durante el combate el rival se desconectO o envió algo incorrecto
// Es "unchecked" para poder lanzarla desde las interfaces
// FuenteMovimientos y ResolvedorImpacto sin cambiarlas
public class ConexionPerdidaException extends RuntimeException {

    public ConexionPerdidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}