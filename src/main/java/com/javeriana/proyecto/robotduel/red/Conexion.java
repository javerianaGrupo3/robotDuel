package com.javeriana.proyecto.robotduel.red;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Conexion implements Closeable {

    public static final int TIEMPO_MAXIMO_CONEXION_MS = 10_000;

    private final Socket socket;
    private final BufferedReader entrada;
    private final BufferedWriter salida;
    private final boolean anfitrion;

    private Conexion(Socket socket, boolean anfitrion) throws IOException {
        this.socket = socket;
        this.anfitrion = anfitrion;
        this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.salida = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    // Crear la conexiOn

    // abre el puerto y espera a que el invitado se conecte
    public static Conexion esperarInvitado(int puerto) throws IOException {
        validarPuerto(puerto);
        try (ServerSocket servidor = new ServerSocket(puerto)) {
            return esperarInvitado(servidor);
        }
    }

    // Variante que recibe el ServerSocket ya abierto
    public static Conexion esperarInvitado(ServerSocket servidor) throws IOException {
        Socket socket = servidor.accept();
        return new Conexion(socket, true);
    }

    // se conecta a la IP y puerto del anfitrión
    public static Conexion unirse(String ip, int puerto) throws IOException {
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("Falta la dirección IP del anfitrión");
        }
        validarPuerto(puerto);
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip.trim(), puerto), TIEMPO_MAXIMO_CONEXION_MS);
        } catch (IOException e) {
            socket.close();
            throw new IOException("No se pudo conectar con " + ip + ":" + puerto, e);
        }
        return new Conexion(socket, false);
    }

    private static void validarPuerto(int puerto) {
        if (puerto < 1 || puerto > 65535) {
            throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535");
        }
    }

    // Enviar y recibir

    // Envía una linea de texto al rival
    public synchronized void enviar(String mensaje) throws IOException {
        if (mensaje == null || mensaje.contains("\n")) {
            throw new IllegalArgumentException("El mensaje debe ser una sola línea");
        }
        salida.write(mensaje);
        salida.newLine();
        salida.flush();
        System.out.println("[RED] -> " + mensaje);
    }

    // Espera y devuelve la siguiente línea del rival
    public String recibir() throws IOException {
        String mensaje = entrada.readLine();
        if (mensaje == null) {
            throw new IOException("El rival se desconectó");
        }
        System.out.println("[RED] <- " + mensaje);
        return mensaje.trim();
    }

    //Estado

    public boolean esAnfitrion() {
        return anfitrion;
    }

    public boolean estaAbierta() {
        return !socket.isClosed();
    }

    public String getDireccionRival() {
        return socket.getInetAddress().getHostAddress();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}