package com.javeriana.proyecto.robotduel.modelo;

public class EstadoCombate {

    public static final int MAX_TURNOS = 100;
    public static final int MAX_ESQUIVES = 2;

    private final Robot robot;
    private double vida;
    private int energia;
    private int acumuladorTurno;
    private int esquivesActivos;

    public EstadoCombate(Robot robot) {
        if (robot == null) {
            throw new IllegalArgumentException("El robot no puede ser nulo");
        }
        this.robot = robot;
        this.vida = Robot.VIDA_INICIAL;
        this.energia = robot.getEnergia();
        this.acumuladorTurno = 0;
        this.esquivesActivos = 0;
    }

    // Vida

    public void recibirDmg(double dmg) {
        if (dmg < 0) {
            return;
        }
        vida = Math.max(0, vida - dmg);
    }

    public void curar(double cantidad) {
        if (cantidad < 0) {
            return;
        }
        vida = Math.min(Robot.VIDA_INICIAL, vida + cantidad);
    }

    public boolean estaDestruido() {
        return vida <= 0;
    }

    // Energía

    public void cargarEnergia(int cantidad) {
        if (cantidad > 0) {
            energia += cantidad;
        }
    }

    public void vaciarEnergia() {
        energia = 0;
    }

    //Turnos

    public void avanzarAcumulador() {
        acumuladorTurno += robot.getVelocidad();
    }

    public boolean puedeActuar() {
        return acumuladorTurno >= MAX_TURNOS;
    }

    public void consumirTurno() {
        acumuladorTurno -= MAX_TURNOS;
    }

    //Esquivar
    public void agregarEsquive() {
        if (esquivesActivos < MAX_ESQUIVES) {
            esquivesActivos++;
        }
    }

    public double getProbabilidadEsquive() {
        return switch (esquivesActivos) {
            case 0 -> 0.0;
            case 1 -> 0.50;
            default -> 0.75;
        };
    }

    public void consumirEsquives() {
        esquivesActivos = 0;
    }

    //Efectos
    public void aplicarEfectos(EstadoCombate enemigo) {
        for (Efecto efecto : robot.getEfectos()) {
            efecto.aplicar(this, enemigo);
        }
    }

    //Getters

    public Robot getRobot() {
        return robot;
    }

    public double getVida() {
        return vida;
    }

    public int getEnergia() {
        return energia;
    }

    public int getAcumuladorTurno() {
        return acumuladorTurno;
    }

    public int getEsquivesActivos() {
        return esquivesActivos;
    }

    @Override
    public String toString() {
        return robot.getNombre() + " | Vida " + String.format("%.1f", vida)
                + " | Energía " + energia + " | Esquives " + esquivesActivos;
    }
}