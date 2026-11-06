package com.javeriana.proyecto.robotduel.modelo;

public abstract class MovimientoOfensivo extends Movimiento {

    protected MovimientoOfensivo(String nombre) {
        super(nombre);
    }

    @Override
    public boolean requiereRespuesta() {
        return true;
    }

    @Override
    public final void ejecutar(EstadoCombate actor, EstadoCombate rival, ResultadoAtaque resultado) {
        if (resultado == ResultadoAtaque.HIT) {
            rival.recibirDmg(calcularDmg(actor, rival));
        }
        rival.consumirEsquives();
        despuesDeAtacar(actor);
    }

    protected abstract double calcularDmg(EstadoCombate actor, EstadoCombate rival);

    protected void despuesDeAtacar(EstadoCombate actor) {
    }

    protected static double formulaDmg(double potencia, int defensa) {
        double divisor = potencia + defensa;
        if (divisor <= 0) {
            return 0;
        }
        return (potencia * potencia) / divisor;
    }
}