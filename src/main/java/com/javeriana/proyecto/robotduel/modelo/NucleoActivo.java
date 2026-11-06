package com.javeriana.proyecto.robotduel.modelo;

public class NucleoActivo extends Efecto{
    public static final int Energia = 2;

    public NucleoActivo(){
        super('N', "Nucle Activo" );
    }

    @Override
    public void aplicar (EstadoCombate propio, EstadoCombate enemigo){
        propio.cargarEnergia(Energia);
    }
}
