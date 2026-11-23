package com.javeriana.proyecto.robotduel.archivos;

import com.javeriana.proyecto.robotduel.logica.CombateListener;
import com.javeriana.proyecto.robotduel.logica.RegistroTurno;
import com.javeriana.proyecto.robotduel.modelo.EstadoCombate;
import com.javeriana.proyecto.robotduel.modelo.Movimiento;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RegistroPartida implements CombateListener {

    private final String robotPropio;
    private final List<Movimiento> movimientosPropios = new ArrayList<>();
    private final List<String> historial = new ArrayList<>();
    private String resultadoFinal;

    // robotPropio nombre del robot de este jugador
    public RegistroPartida(String robotPropio) {
        if (robotPropio == null || robotPropio.isBlank()) {
            throw new IllegalArgumentException("Falta el nombre del robot propio");
        }
        this.robotPropio = robotPropio.trim();
    }

    // Se llama desde el hilo del combate en cada turno
    @Override
    public synchronized void turnoJugado(RegistroTurno registro) {
        historial.add(registro.descripcion());
        if (registro.robot().equalsIgnoreCase(robotPropio)) {
            movimientosPropios.add(Movimiento.desdeTexto(registro.movimiento()));
        }
    }

    @Override
    public synchronized void combateTerminado(EstadoCombate ganador) {
        resultadoFinal = "Ganador: " + ganador.getRobot().getNombre();
        historial.add(resultadoFinal);
    }

    // Guarda los movimientos propios en formato de estrategia
    public synchronized void guardarComoEstrategia(GestorEstrategia gestor) throws IOException {
        gestor.guardar(new ArrayList<>(movimientosPropios));
    }

    // Guarda el historial completo de la partida
    public synchronized void guardarHistorial(Path archivo) throws IOException {
        Path carpeta = archivo.toAbsolutePath().getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
        Files.write(archivo, historial, StandardCharsets.UTF_8);
    }

    public synchronized List<Movimiento> getMovimientosPropios() {
        return List.copyOf(movimientosPropios);
    }

    public synchronized List<String> getHistorial() {
        return List.copyOf(historial);
    }

    public synchronized boolean terminado() {
        return resultadoFinal != null;
    }
}