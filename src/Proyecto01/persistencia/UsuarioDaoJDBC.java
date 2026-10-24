package Proyecto01.persistencia;

import Proyecto01.modelo.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC de IUsuarioDao. Toda la SQL del sistema de socios vive aquí.
 */
public class UsuarioDaoJDBC implements IUsuarioDao {

    private final ConexionBD conexion = new ConexionBD();

    @Override
    public Usuario crear(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (numero_socio, nombre_completo, edad, "
                + "correo_electronico, telefono, contacto_emergencia, condiciones_medicas, pago_al_dia) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, usuario.getNumeroUsuario());
            ps.setString(2, usuario.getNombreCompleto());
            ps.setInt(3, usuario.getEdad());
            ps.setString(4, usuario.getCorreoElectronico());
            ps.setInt(5, usuario.getTelefono());
            ps.setString(6, usuario.getContactoEmergencia());
            ps.setString(7, usuario.getCondicionesMedicas());
            ps.setBoolean(8, usuario.isPagoAlDia());
            ps.executeUpdate();

            // La BD genera el id_usuario: lo recuperamos y se lo devolvemos al objeto
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setIdUsuario(generatedKeys.getInt(1));
                }
            }
            return usuario;
        }
    }

    @Override
    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT id_usuario, numero_socio, nombre_completo, edad, correo_electronico, "
                + "telefono, contacto_emergencia, condiciones_medicas, pago_al_dia "
                + "FROM usuarios ORDER BY nombre_completo";

        List<Usuario> resultado = new ArrayList<>();
        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                resultado.add(mapear(rs));
            }
        }
        return resultado;
    }

    @Override
    public Usuario buscarPorNumero(int numeroUsuario) throws SQLException {
        String sql = "SELECT id_usuario, numero_socio, nombre_completo, edad, correo_electronico, "
                + "telefono, contacto_emergencia, condiciones_medicas, pago_al_dia "
                + "FROM usuarios WHERE numero_socio = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, numeroUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    @Override
    public boolean actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET numero_socio = ?, nombre_completo = ?, edad = ?, "
                + "correo_electronico = ?, telefono = ?, contacto_emergencia = ?, "
                + "condiciones_medicas = ?, pago_al_dia = ? WHERE id_usuario = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuario.getNumeroUsuario());
            ps.setString(2, usuario.getNombreCompleto());
            ps.setInt(3, usuario.getEdad());
            ps.setString(4, usuario.getCorreoElectronico());
            ps.setInt(5, usuario.getTelefono());
            ps.setString(6, usuario.getContactoEmergencia());
            ps.setString(7, usuario.getCondicionesMedicas());
            ps.setBoolean(8, usuario.isPagoAlDia());
            ps.setInt(9, usuario.getIdUsuario());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean eliminar(int numeroUsuario) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE numero_socio = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, numeroUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean actualizarEstadoDePago(int numeroUsuario, boolean alDia) throws SQLException {
        String sql = "UPDATE usuarios SET pago_al_dia = ? WHERE numero_socio = ?";

        try (Connection conn = conexion.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, alDia);
            ps.setInt(2, numeroUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Convierte una fila del ResultSet en un objeto Usuario.
     *
     * @param rs resultado apuntando a la fila actual
     * @return usuario mapeado
     * @throws SQLException si falta o es de tipo incorrecto algún campo
     */
    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
                rs.getString("nombre_completo"),
                rs.getInt("edad"),
                rs.getString("correo_electronico"),
                rs.getInt("telefono"),
                rs.getInt("numero_socio"),
                rs.getInt("id_usuario"),
                rs.getBoolean("pago_al_dia"),
                rs.getString("contacto_emergencia"),
                rs.getString("condiciones_medicas")
        );
    }
}
