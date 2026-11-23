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

// Convierte los datos del juego en el texto del enunciado y viceversa:
//   robot {HP:100, ATK:60, DEF:50, SPD:50, NRG:40, EFF:[Q], MECHA:KOI}
//   MECHA es un dato extra opcional la apariencia elegida Si no llega se ignora
//   movimiento ATTACK | SPECIAL | DODGE | HEAL | CHARGE
//   respuesta  HIT | DODGE
// Al leer se acepta el robot en una o varias líneas, con o sin comillas y en cualquier orden
// Cualquier mensaje mal formado del rival se reporta como IOException.
public final class Protocolo {

    public static final String NOMBRE_RIVAL = "Rival";

    private Protocolo() {
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
        return "{HP:" + Robot.VIDA_INICIAL + ", ATK:" + robot.getAtaque() + ", DEF:" + robot.getDefensa()
                + ", SPD:" + robot.getVelocidad() + ", NRG:" + robot.getEnergia() + ", EFF:[" + efectos + "]"
                + (robot.getModelo() == null ? "" : ", MECHA:" + robot.getModelo()) + "}";
    }

    // El protocolo no incluye nombre el robot del rival se llama "Rival"
    public static Robot leerRobot(String mensaje) throws IOException {
        return leerRobot(mensaje, NOMBRE_RIVAL);
    }

    public static Robot leerRobot(String mensaje, String nombre) throws IOException {
        if (mensaje == null || !mensaje.startsWith("{") || !mensaje.endsWith("}")) {
            throw error("se esperaban las estadísticas del robot", mensaje);
        }
        Integer hp = buscarNumero(mensaje, "HP");
        if (hp != null && hp != Robot.VIDA_INICIAL) {
            throw error("el robot debe empezar con " + Robot.VIDA_INICIAL + " de vida", mensaje);
        }
        int ataque = leerNumero(mensaje, "ATK");
        int defensa = leerNumero(mensaje, "DEF");
        int velocidad = leerNumero(mensaje, "SPD");
        int energia = leerNumero(mensaje, "NRG");
        List<Efecto> efectos = leerEfectos(mensaje);

        try {
            // El constructor valida todas las reglas (suma 200, etc.)
            return new Robot(nombre, ataque, defensa, velocidad, energia, efectos, leerModelo(mensaje));
        } catch (IllegalArgumentException e) {
            throw error("robot no válido para la competencia (" + e.getMessage() + ")", mensaje);
        }
    }

    private static String leerModelo(String mensaje) {
        Matcher m = Pattern.compile("\\bMECHA\"?\\s*:\\s*\"?([A-Za-z_]+)", Pattern.CASE_INSENSITIVE).matcher(mensaje);
        return m.find() ? m.group(1) : null;
    }

    private static Integer buscarNumero(String mensaje, String clave) {
        Matcher m = Pattern.compile("\\b" + clave + "\"?\\s*:\\s*\"?(-?\\d+)", Pattern.CASE_INSENSITIVE)
                .matcher(mensaje);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }

    private static int leerNumero(String mensaje, String clave) throws IOException {
        Integer valor = buscarNumero(mensaje, clave);
        if (valor == null) {
            throw error("falta el valor " + clave, mensaje);
        }
        return valor;
    }

    private static List<Efecto> leerEfectos(String mensaje) throws IOException {
        Matcher m = Pattern.compile("\\bEFF\"?\\s*:\\s*\\[([^\\]]*)\\]", Pattern.CASE_INSENSITIVE).matcher(mensaje);
        if (!m.find()) {
            throw error("faltan los efectos (EFF)", mensaje);
        }
        List<Efecto> efectos = new ArrayList<>();
        for (String parte : m.group(1).split(",")) {
            String letra = parte.replace("\"", "").replace("'", "").trim();
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
