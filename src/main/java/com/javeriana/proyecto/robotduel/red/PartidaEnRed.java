package com.javeriana.proyecto.robotduel.red;

import com.javeriana.proyecto.robotduel.logica.Combate;
import com.javeriana.proyecto.robotduel.logica.FuenteMovimientos;
import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;
import com.javeriana.proyecto.robotduel.modelo.Robot;

import java.io.Closeable;
import java.io.IOException;
import java.util.Random;

// Prepara una partida en red sobre una conexion ya establecida
// intercambia la semilla y los robots y arma el Combate igual en los dos computadores

public class PartidaEnRed implements Closeable {

    private final Conexion conexion;
    private final EstadoCombate miEstado;
    private final EstadoCombate estadoRival;
    private final Combate combate;

    private PartidaEnRed(Conexion conexion, EstadoCombate miEstado, EstadoCombate estadoRival, Combate combate) {
        this.conexion = conexion;
        this.miEstado = miEstado;
        this.estadoRival = estadoRival;
        this.combate = combate;
    }

    // Hace el intercambio inicial el juego Bloquea hasta recibir los datos del rival
    public static PartidaEnRed iniciar(Conexion conexion, Robot miRobot, FuenteMovimientos miFuente)
            throws IOException {
        if (conexion == null || miRobot == null || miFuente == null) {
            throw new IllegalArgumentException("Faltan la conexión, el robot o la fuente de movimientos");
        }

        // Semilla para los empates de turno la genera el anfitrion
        long semilla;
        if (conexion.esAnfitrion()) {
            semilla = new Random().nextLong();
            conexion.enviar(Protocolo.textoSemilla(semilla));
        } else {
            semilla = Protocolo.leerSemilla(conexion.recibir());
        }

        // Intercambio de robots
        conexion.enviar(Protocolo.textoRobot(miRobot));
        Robot robotRival = Protocolo.leerRobot(conexion.recibir());

        // Si los dos se llaman igual se renombra la copia local del rival para distinguirlos
        if (robotRival.getNombre().equalsIgnoreCase(miRobot.getNombre())) {
            robotRival = new Robot(robotRival.getNombre() + " (rival)",
                    robotRival.getAtaque(), robotRival.getDefensa(), robotRival.getVelocidad(),
                    robotRival.getEnergia(), robotRival.getEfectos());
        }

        // Estados y fuentes
        EstadoCombate miEstado = new EstadoCombate(miRobot);
        EstadoCombate estadoRival = new EstadoCombate(robotRival);
        FuenteMovimientos miFuenteEnRed = new FuenteLocalEnRed(miFuente, conexion);
        FuenteMovimientos fuenteRival = new JugadorRemoto(conexion);
        ImpactoEnRed impacto = new ImpactoEnRed(conexion, miEstado);
        Random azarTurnos = new Random(semilla);

        // El jugador A es siempre el robot del anfitrión, en los dos computadores
        Combate combate;
        if (conexion.esAnfitrion()) {
            combate = new Combate(miEstado, miFuenteEnRed, estadoRival, fuenteRival, impacto, azarTurnos);
        } else {
            combate = new Combate(estadoRival, fuenteRival, miEstado, miFuenteEnRed, impacto, azarTurnos);
        }
        return new PartidaEnRed(conexion, miEstado, estadoRival, combate);
    }

    public Combate getCombate() {
        return combate;
    }

    public EstadoCombate getMiEstado() {
        return miEstado;
    }

    public EstadoCombate getEstadoRival() {
        return estadoRival;
    }

    public Conexion getConexion() {
        return conexion;
    }

    @Override
    public void close() throws IOException {
        conexion.close();
    }
}