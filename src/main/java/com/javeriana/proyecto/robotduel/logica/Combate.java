package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;
import com.javeriana.proyecto.robotduel.modelo.Movimiento;
import com.javeriana.proyecto.robotduel.modelo.ResultadoAtaque;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Combate {

    private final Turnos turnos;
    private final FuenteMovimientos fuenteA;
    private final FuenteMovimientos fuenteB;
    private final ResolvedorImpacto resolvedor;

    private final List<RegistroTurno> historial = Collections.synchronizedList(new ArrayList<>());
    private final List<CombateListener> listeners = new CopyOnWriteArrayList<>();
    private int numeroTurno = 0;

    public Combate(EstadoCombate jugadorA, FuenteMovimientos fuenteA,
                   EstadoCombate jugadorB, FuenteMovimientos fuenteB,
                   ResolvedorImpacto resolvedor, Random azarTurnos) {
        if (fuenteA == null || fuenteB == null || resolvedor == null) {
            throw new IllegalArgumentException("Faltan fuentes de movimientos o resolvedor");
        }
        this.turnos = new Turnos(jugadorA, jugadorB, azarTurnos);
        this.fuenteA = fuenteA;
        this.fuenteB = fuenteB;
        this.resolvedor = resolvedor;
    }

    public void agregarListener(CombateListener listener) {
        listeners.add(listener);
    }

    // Juega hasta que un robot sea destruido
    public EstadoCombate jugar() throws InterruptedException {
        while (!terminado()) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("Combate detenido");
            }
            jugarTurno();
        }
        EstadoCombate ganador = getGanador();
        for (CombateListener listener : listeners) {
            listener.combateTerminado(ganador);
        }
        return ganador;
    }

    private void jugarTurno() throws InterruptedException {
        EstadoCombate actor = turnos.siguienteTurno();
        EstadoCombate rival = turnos.getRival(actor);

        // Efectos de estado al inicio del turno
        actor.aplicarEfectos(rival);
        if (terminado()) {
            return;
        }

        // Movimiento si no se puede ejecutar, se pierde el turnio
        Movimiento movimiento = fuenteDe(actor).siguienteMovimiento();
        boolean valido = movimiento.puedeEjecutar(actor);
        ResultadoAtaque resultado = ResultadoAtaque.NO_APLICA;

        if (valido) {
            //  Si es un ataque, el defensor decide si acierta o lo esquiva
            if (movimiento.requiereRespuesta()) {
                resultado = resolvedor.resolver(actor, rival);
            }
            //  Ejecuta
            movimiento.ejecutar(actor, rival, resultado);
        }

        //  Registrar y avisar
        numeroTurno++;
        RegistroTurno registro = new RegistroTurno(numeroTurno, actor.getRobot().getNombre(),
                movimiento.getNombre(), resultado, actor.getVida(), rival.getVida(), valido);
        historial.add(registro);
        for (CombateListener listener : listeners) {
            listener.turnoJugado(registro);
        }
    }

    private FuenteMovimientos fuenteDe(EstadoCombate actor) {
        return actor == turnos.getJugadorA() ? fuenteA : fuenteB;
    }

    public boolean terminado() {
        return turnos.getJugadorA().estaDestruido() || turnos.getJugadorB().estaDestruido();
    }

    // Devuelve null mientras la partida esta en curso
    public EstadoCombate getGanador() {
        if (turnos.getJugadorA().estaDestruido()) {
            return turnos.getJugadorB();
        }
        if (turnos.getJugadorB().estaDestruido()) {
            return turnos.getJugadorA();
        }
        return null;
    }

    public List<RegistroTurno> getHistorial() {
        synchronized (historial) {
            return List.copyOf(historial);
        }
    }

    public EstadoCombate getJugadorA() {
        return turnos.getJugadorA();
    }

    public EstadoCombate getJugadorB() {
        return turnos.getJugadorB();
    }
}