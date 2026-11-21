package com.javeriana.proyecto.robotduel.vista;

import com.javeriana.proyecto.robotduel.modelo.Movimiento;
import com.javeriana.proyecto.robotduel.modelo.Robot;
import com.javeriana.proyecto.robotduel.red.Conexion;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

// Ventana principal. Lleva el flujo del juego del enunciado:
//  Conexión ->  Ensamble ->  Estrategia ->  Combate -> Fin de la partida
public class MechaDuelApp extends Application {

    private static final double ANCHO = 1100;
    private static final double ALTO = 760;

    private Stage ventana;
    private Conexion conexion;
    private Robot miRobot;
    private Robot robotRival;
    private Thread hiloCombate;

    @Override
    public void start(Stage stage) {
        this.ventana = stage;
        stage.setTitle("Mecha Duel");
        stage.setMinWidth(1100);
        stage.setMinHeight(760);
        irAConexion();
        stage.show();
    }

    // Navegación entre pantallas

    void irAConexion() {
        detenerCombate();
        cerrarConexion();
        miRobot = null;
        robotRival = null;
        mostrar(new PantallaConexion(this).getVista());
    }

    void conexionLista(Conexion nuevaConexion) {
        this.conexion = nuevaConexion;
        mostrar(new PantallaEnsamble(this, nuevaConexion).getVista());
    }

    void robotsListos(Robot mio, Robot rival) {
        this.miRobot = mio;
        this.robotRival = rival;
        mostrar(new PantallaEstrategia(this, mio, rival).getVista());
    }

    // estrategia == null significa jugar a mano; crearArchivo: guardar los movimientos en estrategia.txt
    void iniciarCombate(List<Movimiento> estrategia, boolean crearArchivo) {
        PantallaCombate pantalla = new PantallaCombate(this, conexion, miRobot, robotRival, estrategia, crearArchivo);
        mostrar(pantalla.getVista());
        hiloCombate = pantalla.comenzar();
    }

    // La conexión se perdió o el rival envió algo inválido se avisa y se vuelve al inicio
    void errorDeRed(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.initOwner(ventana);
        alerta.getDialogPane().getStylesheets().add(MechaDuelApp.class.getResource("estilos.css").toExternalForm());
        alerta.setTitle("Mecha Duel");
        alerta.setHeaderText("Problema con la conexión");
        alerta.setContentText(mensaje == null ? "Se perdió la conexión con el rival" : mensaje);
        alerta.showAndWait();
        irAConexion();
    }

    Stage getVentana() {
        return ventana;
    }

    private void mostrar(Parent vista) {
        Scene escena = ventana.getScene();
        if (escena == null) {
            escena = new Scene(vista, ANCHO, ALTO);
            escena.getStylesheets().add(MechaDuelApp.class.getResource("estilos.css").toExternalForm());
            ventana.setScene(escena);
        } else {
            escena.setRoot(vista);
        }
    }

    // Hilos para no congelar la ventana
    static void enSegundoPlano(String nombre, Runnable tarea) {
        Thread hilo = new Thread(tarea, nombre);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void detenerCombate() {
        if (hiloCombate != null) {
            hiloCombate.interrupt();
            hiloCombate = null;
        }
    }

    private void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (IOException ignorada) {
                // ya estaba cerrada
            }
            conexion = null;
        }
    }

    @Override
    public void stop() {
        detenerCombate();
        cerrarConexion();
    }
}
