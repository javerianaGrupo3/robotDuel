package com.javeriana.proyecto.robotduel.logica;

import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;
import com.javeriana.proyecto.robotduel.modelo.ResultadoAtaque;

public interface ResolvedorImpacto {

    ResultadoAtaque resolver(EstadoCombate atacante, EstadoCombate defensor) throws InterruptedException;
}