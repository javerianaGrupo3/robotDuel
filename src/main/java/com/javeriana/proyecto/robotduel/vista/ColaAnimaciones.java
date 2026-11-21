package com.javeriana.proyecto.robotduel.vista;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;


final class ColaAnimaciones {

    private final Deque<Consumer<Runnable>> pasos = new ArrayDeque<>();
    private boolean ocupada;

    void agregar(Consumer<Runnable> paso) {
        pasos.add(paso);
        if (!ocupada) {
            siguiente();
        }
    }

    void pausa(double milis) {
        agregar(listo -> {
            PauseTransition p = new PauseTransition(Duration.millis(milis));
            p.setOnFinished(e -> listo.run());
            p.play();
        });
    }

    private void siguiente() {
        Consumer<Runnable> paso = pasos.poll();
        if (paso == null) {
            ocupada = false;
            return;
        }
        ocupada = true;
        boolean[] llamado = {false};
        paso.accept(() -> {
            if (!llamado[0]) {
                llamado[0] = true;
                Platform.runLater(this::siguiente);
            }
        });
    }

    void vaciar() {
        pasos.clear();
    }
}
