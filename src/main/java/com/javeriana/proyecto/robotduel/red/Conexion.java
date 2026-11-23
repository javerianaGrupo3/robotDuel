package com.javeriana.proyecto.robotduel.red;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.function.BooleanSupplier;

// Conexión entre los dos jugadores
//  - socket de entrada: abrimos NUESTRO puerto y el rival se conecta a el y por ahi recibimos
//  - socket de salida: nos conectamos a la IP y puerto de entrada del rival por ahi enviamos
// Los dos jugadores hacen exactamente lo mismo, no hay anfitrión ni invitado.
public class Conexion implements Closeable {

    public static final int INTERVALO_REINTENTO_MS = 500;

    private final Socket entrada;
    private final Socket salida;
    private final int miPuerto;
    private final int puertoRival;
    private final BufferedReader lector;
    private final BufferedWriter escritor;

    private Conexion(Socket entrada, Socket salida, int miPuerto, int puertoRival) throws IOException {
        this.entrada = entrada;
        this.salida = salida;
        this.miPuerto = miPuerto;
        this.puertoRival = puertoRival;
        this.lector = new BufferedReader(new InputStreamReader(entrada.getInputStream(), StandardCharsets.UTF_8));
        this.escritor = new BufferedWriter(new OutputStreamWriter(salida.getOutputStream(), StandardCharsets.UTF_8));
    }

    // Abre nuestro puerto y se conecta al del rival. Reintenta hasta que el rival esté listo
    // o hasta que cancelado devuelva true.
    public static Conexion conectar(int miPuerto, String ipRival, int puertoRival, BooleanSupplier cancelado)
            throws IOException {
        validarPuerto(miPuerto);
        validarPuerto(puertoRival);
        if (ipRival == null || ipRival.isBlank()) {
            throw new IllegalArgumentException("Falta la dirección IP del rival");
        }
        InetAddress direccionRival = InetAddress.getByName(ipRival.trim());
        if (miPuerto == puertoRival && esDireccionPropia(direccionRival)) {
            throw new IllegalArgumentException(
                    "Esa es tu propia dirección: en el mismo computador usa dos puertos distintos");
        }

        Socket entrada = null;
        Socket salida = null;
        try (ServerSocket servidor = new ServerSocket()) {
            servidor.setReuseAddress(true);
            servidor.bind(new InetSocketAddress(miPuerto));
            servidor.setSoTimeout(INTERVALO_REINTENTO_MS);

            while (entrada == null || salida == null) {
                if (cancelado.getAsBoolean() || Thread.currentThread().isInterrupted()) {
                    throw new InterruptedIOException("Conexión cancelada");
                }
                if (salida == null) {
                    salida = intentarConectar(direccionRival, puertoRival);
                }
                if (entrada == null) {
                    try {
                        entrada = servidor.accept();
                    } catch (SocketTimeoutException e) {
                        // el rival todavía no se conecta, se vuelve a intentar
                    }
                } else if (salida == null) {
                    dormir();
                }
            }
            return new Conexion(entrada, salida, miPuerto, puertoRival);
        } catch (IOException | RuntimeException e) {
            cerrarSilencioso(entrada);
            cerrarSilencioso(salida);
            throw e;
        }
    }

    private static Socket intentarConectar(InetAddress direccion, int puerto) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(direccion, puerto), INTERVALO_REINTENTO_MS);
            return socket;
        } catch (IOException e) {
            cerrarSilencioso(socket);
            return null;
        }
    }

    private static void dormir() throws InterruptedIOException {
        try {
            Thread.sleep(INTERVALO_REINTENTO_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Conexión cancelada");
        }
    }

    private static boolean esDireccionPropia(InetAddress direccion) {
        if (direccion.isLoopbackAddress() || direccion.isAnyLocalAddress()) {
            return true;
        }
        try {
            return NetworkInterface.getByInetAddress(direccion) != null;
        } catch (IOException e) {
            return false;
        }
    }

    private static void validarPuerto(int puerto) {
        if (puerto < 1 || puerto > 65535) {
            throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535");
        }
    }

    private static void cerrarSilencioso(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignorada) {
            }
        }
    }

    // Enviar y recibir

    // Envía una línea de texto al rival por el socket de salida
    public synchronized void enviar(String mensaje) throws IOException {
        if (mensaje == null || mensaje.contains("\n")) {
            throw new IllegalArgumentException("El mensaje debe ser una sola línea");
        }
        escritor.write(mensaje);
        escritor.newLine();
        escritor.flush();
        System.out.println("[RED] -> " + mensaje);
    }

    // Espera el siguiente mensaje del rival por el socket de entrada.
    // Si empieza con "{" y no se cierra en la misma línea robot en varias líneas, lee hasta la "}".
    public String recibir() throws IOException {
        String mensaje = leerLinea().trim();
        if (mensaje.startsWith("{")) {
            StringBuilder bloque = new StringBuilder(mensaje);
            while (bloque.indexOf("}") < 0) {
                bloque.append(' ').append(leerLinea().trim());
            }
            mensaje = bloque.toString();
        }
        System.out.println("[RED] <- " + mensaje);
        return mensaje;
    }

    private String leerLinea() throws IOException {
        String linea;
        do {
            linea = lector.readLine();
            if (linea == null) {
                throw new IOException("El rival se desconectó");
            }
        } while (linea.isBlank());
        return linea;
    }

    // Desempate de turnos

    // Cuando los dos robots llegan a 100 con el mismo acumulado y la misma velocidad,
    // juega primero el jugador con la dirección "IP:puerto" menor. Los dos computadores
    // ven las mismas dos direcciones, así que siempre llegan a la misma respuesta.
    public boolean ganoLosEmpates() {
        String mia = entrada.getLocalAddress().getHostAddress() + ":" + miPuerto;
        String delRival = salida.getInetAddress().getHostAddress() + ":" + puertoRival;
        return mia.compareTo(delRival) < 0;
    }

    // Estado

    public boolean estaAbierta() {
        return !entrada.isClosed() && !salida.isClosed();
    }

    public String getDireccionRival() {
        return salida.getInetAddress().getHostAddress() + ":" + puertoRival;
    }

    @Override
    public void close() throws IOException {
        cerrarSilencioso(entrada);
        cerrarSilencioso(salida);
    }
}
