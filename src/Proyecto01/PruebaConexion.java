package Proyecto01;

import Proyecto01.modelo.Usuario;
import Proyecto01.persistencia.IUsuarioDao;
import Proyecto01.persistencia.UsuarioDaoJDBC;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class PruebaConexion {

    public static void main(String[] args) throws Exception {
        String url = "jdbc:mysql://localhost:3306/gimnasio_db"
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

        IUsuarioDao dao = new UsuarioDaoJDBC();
        System.out.println(dao.listarTodos());
        Usuario nuevo = dao.crear(new Usuario("Prueba", 20, "p@correo.com", 88880000, 105, 0, false, "", ""));
        System.out.println("Creado con id=" + nuevo.getIdUsuario());
        System.out.println("Eliminado: " + dao.eliminar(105));





        try (Connection conn = DriverManager.getConnection(url, "root", "rootroot");
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT numero_socio, nombre_completo FROM usuarios")) {

            while (rs.next()) {
                System.out.println(rs.getInt("numero_socio") + " - " + rs.getString("nombre_completo"));
            }
        }
    }
}
