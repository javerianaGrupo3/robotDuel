package com.javeriana.proyecto.robotduel.red;

import com.javeriana.proyecto.robotduel.modelo.Efecto;
import com.javeriana.proyecto.robotduel.modelo.Movimiento;
import com.javeriana.proyecto.robotduel.modelo.ResultadoAtaque;
import com.javeriana.proyecto.robotduel.modelo.Robot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Convierte los datos del juego en texto y los envia a red
// Cualquier mensaje mal formado del rival se reporta como IOException.
public final class Protocolo {

    private static final String PREFIJO_SEMILLA = "SEED ";

    private Protocolo() {
    }

    // Semilla para los empates de turno

    public static String textoSemilla(long semilla) {
        return PREFIJO_SEMILLA + semilla;
    }

    public static long leerSemilla(String mensaje) throws IOException {
        if (mensaje == null || !mensaje.startsWith(PREFIJO_SEMILLA)) {
            throw error("se esperaba la semilla", mensaje);
        }
        try {
            return Long.parseLong(mensaje.substring(PREFIJO_SEMILLA.length()).trim());
        } catch (NumberFormatException e) {
            throw error("semilla no válida", mensaje);
        }
    }

    // Robot

    public static String textoRobot(Robot robot) {
        StringBuilder efectos = new StringBuilder();
        for (Efecto efecto : robot.getEfectos()) {
            if (!efectos.isEmpty()) {
                efectos.append(", ");
            }
            efectos.append(efecto.getLetra());
        }
        return "{HP:" + Robot.VIDA_INICIAL + ", ATK:" + robot.getAtaque() + ", DEF:" + robot.getDefensa() + ", SPD:" + robot.getVelocidad() + ", NRG:" + robot.getEnergia() + ", EFF:[" + efectos + "]" + ", NAME:" + robot.getNombre() + "}";
    }

    public static Robot leerRobot(String mensaje) throws IOException {
        if (mensaje == null || !mensaje.startsWith("{") || !mensaje.endsWith("}")) {
            throw error("se esperaban las estadísticas del robot", mensaje);
        }
        int hp = leerNumero(mensaje, "HP");
        if (hp != Robot.VIDA_INICIAL) {
            throw error("el robot debe empezar con " + Robot.VIDA_INICIAL + " de vida", mensaje);
        }
        int ataque = leerNumero(mensaje, "ATK");
        int defensa = leerNumero(mensaje, "DEF");
        int velocidad = leerNumero(mensaje, "SPD");
        int energia = leerNumero(mensaje, "NRG");
        List<Efecto> efectos = leerEfectos(mensaje);
        String nombre = leerNombre(mensaje);

        try {
            // El constructor valida todas las reglas
            return new Robot(nombre, ataque, defensa, velocidad, energia, efectos);
        } catch (IllegalArgumentException e) {
            throw error("robot no válido (" + e.getMessage() + ")", mensaje);
        }
    }

    private static int leerNumero(String mensaje, String clave) throws IOException {
        Matcher m = Pattern.compile("\\b" + clave + "\\s*:\\s*(-?\\d+)").matcher(mensaje);
        if (!m.find()) {
            throw error("falta el valor " + clave, mensaje);
        }
        return Integer.parseInt(m.group(1));
    }

    private static List<Efecto> leerEfectos(String mensaje) throws IOException {
        Matcher m = Pattern.compile("\\bEFF\\s*:\\s*\\[([^\\]]*)\\]").matcher(mensaje);
        if (!m.find()) {
            throw error("faltan los efectos (EFF)", mensaje);
        }
        List<Efecto> efectos = new ArrayList<>();
        for (String parte : m.group(1).split(",")) {
            String letra = parte.trim();
            if (letra.isEmpty()) {
                continue;
            }
            if (letra.length() != 1) {
                throw error("efecto no válido \"" + letra + "\"", mensaje);
            }
            try {
                efectos.add(Efecto.desdeLetra(letra.charAt(0)));
            } catch (IllegalArgumentException e) {
                throw error("efecto desconocido \"" + letra + "\"", mensaje);
            }
        }
        return efectos;
    }

    // El nombre es opcional va al final y puede contener espacios
    private static String leerNombre(String mensaje) {
        Matcher m = Pattern.compile("\\bNAME\\s*:\\s*(.*)}$").matcher(mensaje);
        if (m.find() && !m.group(1).isBlank()) {
            return m.group(1).trim();
        }
        return "Rival";
    }

    // Movimientos

    public static String textoMovimiento(Movimiento movimiento) {
        return movimiento.getNombre();
    }

    public static Movimiento leerMovimiento(String mensaje) throws IOException {
        try {
            return Movimiento.desdeTexto(mensaje);
        } catch (IllegalArgumentException e) {
            throw error("movimiento no válido", mensaje);
        }
    }

    // Resultado de un ataque
    public static String textoResultado(ResultadoAtaque resultado) {
        if (resultado != ResultadoAtaque.HIT && resultado != ResultadoAtaque.DODGE) {
            throw new IllegalArgumentException("Solo se envían HIT o DODGE");
        }
        return resultado.name();
    }

    public static ResultadoAtaque leerResultado(String mensaje) throws IOException {
        if ("HIT".equalsIgnoreCase(mensaje)) {
            return ResultadoAtaque.HIT;
        }
        if ("DODGE".equalsIgnoreCase(mensaje)) {
            return ResultadoAtaque.DODGE;
        }
        throw error("se esperaba HIT o DODGE", mensaje);
    }

    private static IOException error(String motivo, String mensaje) {
        return new IOException("Mensaje del rival no válido: " + motivo + " -> \"" + mensaje + "\"");
    }
}