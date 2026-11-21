package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.ResultadoAtaque;

public record RegistroTurno(
        int numero,
        String robot,
        String movimiento,
        ResultadoAtaque resultado,
        int vidaActor,
        int vidaRival) {

    public String descripcion() {
        String texto = "Turno " + numero + ": " + robot + " usó " + movimiento;
        if (resultado != ResultadoAtaque.NO_APLICA) {
            texto += " (" + resultado + ")";
        }
        return texto + " | vida propia " + vidaActor + " | vida rival " + vidaRival;
    }
}
