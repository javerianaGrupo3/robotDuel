module com.javeriana.proyecto.robotduel {
        requires javafx.controls;

        exports com.javeriana.proyecto.robotduel;
        exports com.javeriana.proyecto.robotduel.vista;

        opens com.javeriana.proyecto.robotduel.modelo;
        opens com.javeriana.proyecto.robotduel.logica;
        opens com.javeriana.proyecto.robotduel.archivos;
        opens com.javeriana.proyecto.robotduel.red;
}
