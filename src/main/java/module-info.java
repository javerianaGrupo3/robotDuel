module com.javeriana.proyecto.robotduel {
        requires javafx.controls;
        requires javafx.fxml;

        opens com.javeriana.proyecto.robotduel to javafx.fxml;
        exports com.javeriana.proyecto.robotduel;

        opens com.javeriana.proyecto.robotduel.modelo;
        opens com.javeriana.proyecto.robotduel.logica;
}