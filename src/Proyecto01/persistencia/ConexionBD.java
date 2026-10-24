package Proyecto01.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ConexionBD {


    private static final String URL =
            "jdbc:mysql://localhost:3306/gimnasio_db"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "rootroot";


    /**
     * Abre una conexión nueva al servidor.
     * Quien llama es responsable de cerrarla (try-with-resources).
     *
     * @return conexión abierta a gimnasio_db
     * @throws SQLException si el servidor rechaza la conexión
     */
    public Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}
