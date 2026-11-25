package com.javeriana.proyecto.robotduel.vista;

import com.javeriana.proyecto.robotduel.archivos.ArchivoCPU;
import com.javeriana.proyecto.robotduel.red.Conexion;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// se ingresa la IP y el puerto de entrada del rival y se espera la conexión
class PantallaConexion {

    private final MechaDuelApp app;

    private final TextField campoMiPuerto = new TextField("5000");
    private final TextField campoIpRival = new TextField("127.0.0.1");
    private final TextField campoPuertoRival = new TextField("5001");
    private final PixelBoton botonConectar = new PixelBoton("CONECTAR", 3).principal();
    private final PixelBoton botonCancelar = new PixelBoton("CANCELAR", 3);
    private final PixelBoton botonCPU = new PixelBoton("VS CPU", 3);
    private final PixelText estado = Componentes.texto("", 2, Componentes.CLARO).conAnchoMaximo(820);

    private volatile boolean cancelado;

    PantallaConexion(MechaDuelApp app) {
        this.app = app;
    }

    Parent getVista() {
        for (TextField campo : List.of(campoMiPuerto, campoIpRival, campoPuertoRival)) {
            campo.setPrefWidth(220);
            campo.setOnAction(e -> botonConectar.disparar());
        }
        GridPane formulario = new GridPane();
        formulario.setHgap(18);
        formulario.setVgap(14);
        formulario.addRow(0, Componentes.texto("TU PUERTO DE ENTRADA", 2), campoMiPuerto);
        formulario.addRow(1, Componentes.texto("IP DEL RIVAL", 2), campoIpRival);
        formulario.addRow(2, Componentes.texto("PUERTO DEL RIVAL", 2), campoPuertoRival);

        PixelText ayuda = Componentes.texto("TUS IP: " + direccionesLocales()
                + "\nEN UN SOLO COMPUTADOR ABRE DOS VENTANAS: UNA CON 5000 -> 5001 Y OTRA CON 5001 -> 5000.",
                2, Componentes.TEXTO_SUAVE).conAnchoMaximo(560);

        VBox caja = Componentes.caja(18, formulario, ayuda);
        caja.setMaxWidth(640);

        botonConectar.setOnAction(this::conectar);
        botonCancelar.setOnAction(() -> {
            cancelado = true;
            Componentes.estado(estado, "CANCELANDO...", Componentes.CLARO);
        });
        botonCancelar.setDisable(true);
        botonCPU.setAyuda("Un jugador: la CPU usa el robot y repite los movimientos de cpu.txt (se crea si no existe)");
        botonCPU.setOnAction(this::jugarContraCPU);
        HBox botones = new HBox(16, botonConectar, botonCancelar, botonCPU);
        botones.setAlignment(Pos.CENTER);

        HBox desfile = new HBox(22);
        desfile.setAlignment(Pos.BOTTOM_CENTER);
        int i = 0;
        for (ModeloMecha modelo : ModeloMecha.values()) {
            desfile.getChildren().add(modelo.vista(96, 130, i % 2 == 0));
            i++;
        }

        VBox raiz = new VBox(22, Componentes.encabezado("PASO 1 DE 4 - CONEXION"), caja, botones, estado, desfile);
        raiz.setAlignment(Pos.CENTER);
        raiz.getStyleClass().add("pantalla");
        return raiz;
    }

    private void conectar() {
        int miPuerto;
        int puertoRival;
        try {
            miPuerto = Integer.parseInt(campoMiPuerto.getText().trim());
            puertoRival = Integer.parseInt(campoPuertoRival.getText().trim());
        } catch (NumberFormatException e) {
            Componentes.estado(estado, "LOS PUERTOS DEBEN SER NUMEROS", Componentes.ROJO_CLARO);
            return;
        }
        String ipRival = campoIpRival.getText().trim();

        cancelado = false;
        bloquear(true);
        String mensaje = "ESPERANDO AL RIVAL EN EL PUERTO " + miPuerto + " Y LLAMANDO A " + ipRival + ":" + puertoRival;
        Componentes.estado(estado, mensaje + "...", Componentes.CLARO);

        MechaDuelApp.enSegundoPlano("conexion", () -> {
            try {
                Conexion conexion = Conexion.conectar(miPuerto, ipRival, puertoRival, () -> cancelado);
                Platform.runLater(() -> app.conexionLista(conexion));
            } catch (Exception e) {
                Platform.runLater(() -> {
                    bloquear(false);
                    if (cancelado) {
                        Componentes.estado(estado, "CONEXION CANCELADA", Componentes.CLARO);
                    } else {
                        Componentes.estado(estado, "NO SE PUDO CONECTAR: " + e.getMessage(), Componentes.ROJO_CLARO);
                    }
                });
            }
        });
    }

    // Modo un jugador lee cpu.txt lo crea con un ejemplo si no existe
    private void jugarContraCPU() {
        try {
            ArchivoCPU.crearSiNoExiste(ArchivoCPU.RUTA_POR_DEFECTO);
            app.jugarContraCPU(ArchivoCPU.leer(ArchivoCPU.RUTA_POR_DEFECTO));
        } catch (IOException | IllegalArgumentException e) {
            Componentes.estado(estado, "CPU.TXT NO VALIDO: " + e.getMessage(), Componentes.ROJO_CLARO);
        }
    }

    private void bloquear(boolean conectando) {
        campoMiPuerto.setDisable(conectando);
        campoIpRival.setDisable(conectando);
        campoPuertoRival.setDisable(conectando);
        botonConectar.setDisable(conectando);
        botonCancelar.setDisable(!conectando);
        botonCPU.setDisable(conectando);
    }

    private static String direccionesLocales() {
        List<String> ips = new ArrayList<>();
        try {
            for (NetworkInterface interfaz : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!interfaz.isUp() || interfaz.isLoopback()) {
                    continue;
                }
                for (InetAddress direccion : Collections.list(interfaz.getInetAddresses())) {
                    if (direccion instanceof Inet4Address) {
                        ips.add(direccion.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            return "NO DISPONIBLES";
        }
        return ips.isEmpty() ? "NO DISPONIBLES" : String.join(", ", ips);
    }
}
