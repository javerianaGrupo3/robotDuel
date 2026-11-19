package com.javeriana.proyecto.robotduel.archivos;

import com.javeriana.proyecto.robotduel.modelo.Movimiento;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GestorEstrategia {

    public static final String NOMBRE_ARCHIVO = "estrategia.txt";

    private final Path archivo;

    // Usa estrategia.txt en la carpeta donde se ejecuta el juego
    public GestorEstrategia() {
        this(Path.of(NOMBRE_ARCHIVO));
    }

    public GestorEstrategia(Path archivo) {
        if (archivo == null) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser nula");
        }
        this.archivo = archivo;
    }

    // Decide el juego con archivo o sin archivo
    public boolean existe() {
        return Files.isRegularFile(archivo);
    }

    // Lee el archivo y devuelve los movimientos en orden
    public List<Movimiento> leer() throws IOException {
        List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        List<Movimiento> movimientos = new ArrayList<>();

        for (int i = 0; i < lineas.size(); i++) {
            String linea = lineas.get(i).trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            try {
                movimientos.add(Movimiento.desdeTexto(linea));
            } catch (IllegalArgumentException e) {
                throw new IOException("Línea " + (i + 1) + " de " + archivo.getFileName()
                        + ": movimiento no válido \"" + linea + "\"", e);
            }
        }

        if (movimientos.isEmpty()) {
            throw new IOException("El archivo " + archivo.getFileName() + " no tiene movimientos");
        }
        return movimientos;
    }

    // Escribe los movimientos uno por linea
    public void guardar(List<Movimiento> movimientos) throws IOException {
        if (movimientos == null || movimientos.isEmpty()) {
            throw new IllegalArgumentException("No hay movimientos para guardar");
        }
        List<String> lineas = new ArrayList<>();
        for (Movimiento movimiento : movimientos) {
            lineas.add(movimiento.getNombre());
        }
        Path carpeta = archivo.toAbsolutePath().getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
        Files.write(archivo, lineas, StandardCharsets.UTF_8);
    }

    public Path getArchivo() {
        return archivo;
    }
}