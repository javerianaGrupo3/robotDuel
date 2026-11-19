package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;
import com.javeriana.proyecto.robotduel.modelo.ResultadoAtaque;

import java.util.Random;

public class ImpactoAleatorio implements ResolvedorImpacto {

    private final Random random;

    public ImpactoAleatorio() {
        this(new Random());
    }

    public ImpactoAleatorio(Random random) {
        this.random = random;
    }

    @Override
    public ResultadoAtaque resolver(EstadoCombate atacante, EstadoCombate defensor) {
        return random.nextDouble() < defensor.getProbabilidadEsquive() ? ResultadoAtaque.ESQUIVE : ResultadoAtaque.HIT;
    }
}