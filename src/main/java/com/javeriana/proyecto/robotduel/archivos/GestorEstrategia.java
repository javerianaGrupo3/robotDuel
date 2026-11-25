package com.javeriana.proyecto.robotduel.archivos;

import com.javeriana.proyecto.robotduel.modelo.Movimiento;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class GestorEstrategia {

    public static final String NOMBRE_ARCHIVO = "estrategia.txt";

    private final Path archivo;

    public GestorEstrategia() {
        this(Path.of(NOMBRE_ARCHIVO));
    }

    public GestorEstrategia(Path archivo) {
        if (archivo == null) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser nula");
        }
        this.archivo = archivo;
    }

    public boolean existe() {
        return Files.isRegularFile(archivo);
    }

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

    public static List<Movimiento> interpretar(String texto) {
        List<Movimiento> movimientos = new ArrayList<>();
        String[] lineas = texto.split("\\R");
        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i].trim();
            if (linea.isEmpty() || linea.startsWith("#")) {
                continue;
            }
            try {
                movimientos.add(Movimiento.desdeTexto(linea));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Movimiento no válido \"" + linea + "\"", e);
            }
        }
        return movimientos;
    }

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

    public void agregar(Movimiento movimiento) throws IOException {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento no puede ser nulo");
        }
        Path carpeta = archivo.toAbsolutePath().getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
        Files.write(archivo, List.of(movimiento.getNombre()), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public Path getArchivo() {
        return archivo;
    }
}