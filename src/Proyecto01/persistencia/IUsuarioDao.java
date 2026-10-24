package Proyecto01.persistencia;

import Proyecto01.modelo.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Contrato de persistencia de socios. La aplicación no sabe SQL:
 * solo conoce esta interfaz; el SQL vive en la implementación JDBC.
 */

public interface IUsuarioDao {


    Usuario crear(Usuario usuario) throws SQLException;

    List<Usuario> listarTodos() throws SQLException;

    Usuario buscarPorNumero(int numeroUsuario) throws SQLException;

    boolean actualizar(Usuario usuario) throws SQLException;

    boolean eliminar(int numeroUsuario) throws SQLException;

    boolean actualizarEstadoDePago(int numeroUsuario, boolean alDia) throws SQLException;

}
