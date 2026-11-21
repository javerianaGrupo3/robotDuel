package com.javeriana.proyecto.robotduel.red;

import com.javeriana.proyecto.robotduel.logica.Combate;
import com.javeriana.proyecto.robotduel.logica.FuenteMovimientos;
import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;
import com.javeriana.proyecto.robotduel.modelo.Robot;

import java.io.Closeable;
import java.io.IOException;

// Partida en red sobre una conexión ya establecida.
// Los dos computadores simulan el mismo combate: cada uno conoce las estadísticas de ambos robots,
// envía sus propios movimientos y recibe los del rival.
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

    // Fase de ensamble: envía mis estadísticas y espera las del rival
    public static Robot intercambiarRobots(Conexion conexion, Robot miRobot) throws IOException {
        if (conexion == null || miRobot == null) {
            throw new IllegalArgumentException("Faltan la conexión o el robot");
        }
        conexion.enviar(Protocolo.textoRobot(miRobot));
        String nombreRival = miRobot.getNombre().equalsIgnoreCase(Protocolo.NOMBRE_RIVAL)
                ? Protocolo.NOMBRE_RIVAL + " (oponente)"
                : Protocolo.NOMBRE_RIVAL;
        return Protocolo.leerRobot(conexion.recibir(), nombreRival);
    }

    // Arma el combate. A es siempre mi robot; el desempate se acuerda con la dirección de cada uno.
    public static PartidaEnRed crear(Conexion conexion, Robot miRobot, Robot robotRival, FuenteMovimientos miFuente) {
        if (conexion == null || miRobot == null || robotRival == null || miFuente == null) {
            throw new IllegalArgumentException("Faltan la conexión, los robots o la fuente de movimientos");
        }
        EstadoCombate miEstado = new EstadoCombate(miRobot);
        EstadoCombate estadoRival = new EstadoCombate(robotRival);
        FuenteMovimientos miFuenteEnRed = new FuenteLocalEnRed(miFuente, conexion);
        FuenteMovimientos fuenteRival = new JugadorRemoto(conexion);
        ImpactoEnRed impacto = new ImpactoEnRed(conexion, miEstado);

        Combate combate = new Combate(miEstado, miFuenteEnRed, estadoRival, fuenteRival, impacto,
                conexion.ganoLosEmpates());
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
