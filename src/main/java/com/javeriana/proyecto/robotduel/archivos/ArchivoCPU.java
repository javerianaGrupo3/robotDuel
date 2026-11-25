package com.javeriana.proyecto.robotduel.archivos;

import com.javeriana.proyecto.robotduel.modelo.Movimiento;
import com.javeriana.proyecto.robotduel.modelo.Robot;
import com.javeriana.proyecto.robotduel.red.Protocolo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ArchivoCPU {

    public static final Path RUTA_POR_DEFECTO = Path.of("cpu.txt");


    public record ConfiguracionCPU(Robot robot, List<Movimiento> movimientos) {
    }

    private static final String CONTENIDO_POR_DEFECTO = """
            # Archivo de la CPU para Mecha Duel.
            # 1) El robot, con el mismo formato que se envía al rival (ATK+DEF+SPD+NRG = 200).
            # 2) Los movimientos, uno por línea: se ejecutan en ciclo hasta que termine el combate.
            {
              HP: 100,
              ATK: 75,
              DEF: 50,
              SPD: 45,
              NRG: 30,
              EFF: [ Q ]
            }
            ATTACK
            CHARGE
            ATTACK
            SPECIAL
            DODGE
            HEAL
            """;

    private ArchivoCPU() {
    }

    public static ConfiguracionCPU leer(Path ruta) throws IOException {
        String texto = Files.readString(ruta, StandardCharsets.UTF_8).replace(String.valueOf((char) 0xFEFF), "");
        int inicio = texto.indexOf('{');
        int fin = texto.indexOf('}', inicio + 1);
        if (inicio < 0 || fin < 0) {
            throw new IllegalArgumentException(ruta + ": falta el robot entre { } con HP, ATK, DEF, SPD, NRG y EFF");
        }
        Robot robot;
        try {
            robot = Protocolo.leerRobot(texto.substring(inicio, fin + 1), "CPU");
        } catch (IOException e) {
            throw new IllegalArgumentException(ruta + ": robot no válido. " + e.getMessage());
        }
        String resto = texto.substring(0, inicio) + "\n" + texto.substring(fin + 1);
        List<Movimiento> movimientos = GestorEstrategia.interpretar(resto);
        if (movimientos.isEmpty()) {
            throw new IllegalArgumentException(ruta + ": la CPU necesita al menos un movimiento");
        }
        return new ConfiguracionCPU(robot, movimientos);
    }

    public static void crearSiNoExiste(Path ruta) throws IOException {
        if (!Files.exists(ruta)) {
            Files.writeString(ruta, CONTENIDO_POR_DEFECTO, StandardCharsets.UTF_8);
        }
    }
}
