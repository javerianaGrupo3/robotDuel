package com.javeriana.proyecto.robotduel.red;

import com.javeriana.proyecto.robotduel.logica.ResolvedorImpacto;
import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;
import com.javeriana.proyecto.robotduel.modelo.ResultadoAtaque;

import java.io.IOException;
import java.util.Random;

// Decide si un ataque acierta o es esquivado en una partida en red
// Solo el computador del rival le envia el resultado al otro

public class ImpactoEnRed implements ResolvedorImpacto {

    private final Conexion conexion;
    private final EstadoCombate propio;
    private final Random dados;

    public ImpactoEnRed(Conexion conexion, EstadoCombate propio) {
        this(conexion, propio, new Random());
    }

    public ImpactoEnRed(Conexion conexion, EstadoCombate propio, Random dados) {
        if (conexion == null || propio == null || dados == null) {
            throw new IllegalArgumentException("Faltan la conexión, el robot propio o el generador");
        }
        this.conexion = conexion;
        this.propio = propio;
        this.dados = dados;
    }

    @Override
    public ResultadoAtaque resolver(EstadoCombate atacante, EstadoCombate defensor) {
        try {
            //si me atacan
            if (defensor == propio) {
                ResultadoAtaque resultado = dados.nextDouble() < defensor.getProbabilidadEsquive() ? ResultadoAtaque.DODGE : ResultadoAtaque.HIT;
                conexion.enviar(Protocolo.textoResultado(resultado));
                return resultado;
            }
            //si ataco yo
            return Protocolo.leerResultado(conexion.recibir());
        } catch (IOException e) {
            throw new ConexionPerdidaException(e.getMessage(), e);
        }
    }
}