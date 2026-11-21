package com.javeriana.proyecto.robotduel.archivos;

import com.javeriana.proyecto.robotduel.modelo.Robot;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GestorRobots {

    public static final String NOMBRE_ARCHIVO = "robots.bin";

    private final Path archivo;

    public GestorRobots() {
        this(Path.of(NOMBRE_ARCHIVO));
    }

    public GestorRobots(Path archivo) {
        if (archivo == null) {
            throw new IllegalArgumentException("La ruta del archivo no puede ser nula");
        }
        this.archivo = archivo;
    }

    public boolean existe() {
        return Files.isRegularFile(archivo);
    }

    // Devuelve todos los robots guardados la esta lista vacía si todavía no hay archivo
    public List<Robot> cargar() throws IOException {
        if (!existe()) {
            return new ArrayList<>();
        }
        try (InputStream entrada = Files.newInputStream(archivo);
             ObjectInputStream objetos = new ObjectInputStream(entrada)) {

            Object leido = objetos.readObject();
            if (!(leido instanceof List<?> lista)) {
                throw new IOException("El archivo " + archivo.getFileName() + " no contiene una lista de robots");
            }
            List<Robot> robots = new ArrayList<>();
            for (Object elemento : lista) {
                if (!(elemento instanceof Robot robot)) {
                    throw new IOException("El archivo " + archivo.getFileName() + " contiene datos que no son robots");
                }
                robots.add(robot);
            }
            return robots;

        } catch (ClassNotFoundException e) {
            throw new IOException("El archivo " + archivo.getFileName() + " fue creado por otra versión del juego", e);
        }
    }

    // Guarda un robot si ya existe uno con el mismo nombre lo reemplaza
    public void guardar(Robot robot) throws IOException {
        if (robot == null) {
            throw new IllegalArgumentException("El robot no puede ser nulo");
        }
        List<Robot> robots = cargar();
        robots.removeIf(r -> r.getNombre().equalsIgnoreCase(robot.getNombre()));
        robots.add(robot);
        escribir(robots);
    }

    // Busca un robot por nombre no le importan mayusculas
    public Optional<Robot> buscar(String nombre) throws IOException {
        for (Robot robot : cargar()) {
            if (robot.getNombre().equalsIgnoreCase(nombre)) {
                return Optional.of(robot);
            }
        }
        return Optional.empty();
    }

    // Elimina un robot por nombre devuelve true si existe
    public boolean eliminar(String nombre) throws IOException {
        List<Robot> robots = cargar();
        boolean eliminado = robots.removeIf(r -> r.getNombre().equalsIgnoreCase(nombre));
        if (eliminado) {
            escribir(robots);
        }
        return eliminado;
    }

    private void escribir(List<Robot> robots) throws IOException {
        Path carpeta = archivo.toAbsolutePath().getParent();
        if (carpeta != null) {
            Files.createDirectories(carpeta);
        }
        try (OutputStream salida = Files.newOutputStream(archivo);
             ObjectOutputStream objetos = new ObjectOutputStream(salida)) {
            objetos.writeObject(new ArrayList<>(robots));
        }
    }

    public Path getArchivo() {
        return archivo;
    }
}