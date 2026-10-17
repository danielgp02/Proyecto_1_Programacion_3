package Proyecto01;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PruebaConexion {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/gimnasio_db"
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        try (Connection conn = DriverManager.getConnection(url, "root", "root");
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT numero_socio, nombre_completo FROM usuarios")) {

            while (rs.next()) {
                System.out.println(rs.getInt("numero_socio") + " - " + rs.getString("nombre_completo"));
            }
        }
    }
}
