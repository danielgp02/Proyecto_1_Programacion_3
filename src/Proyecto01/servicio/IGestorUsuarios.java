package Proyecto01.servicio;

import Proyecto01.modelo.Usuario;

import java.util.List;

/**
 * Contrato del servicio de socios. El almacenamiento no vive en el modelo:
 * lo implementa GestorUsuarios con el repositorio genérico.
 */
public interface IGestorUsuarios {

    /**
     * Registra un socio nuevo. El pago al día queda en falso hasta que se cobre una membresía.
     *
     * @param nombreCompleto nombre del socio
     * @param edad edad del socio
     * @param correoElectronico correo del socio
     * @param telefono teléfono del socio
     * @param numeroUsuario número de socio elegido en el formulario
     * @param idUsuario identificador interno
     * @param contactoEmergencia contacto de emergencia
     * @param condicionesMedicas condiciones médicas
     * @return el socio creado
     */
    Usuario registrarUsuario(String nombreCompleto, int edad, String correoElectronico, int telefono,
                             int numeroUsuario, int idUsuario,
                             String contactoEmergencia, String condicionesMedicas);

    /**
     * Actualiza al socio existente sin reemplazar el objeto, para que la membresía
     * siga apuntando a la misma instancia.
     *
     * @param numeroOriginal número de socio antes de la edición
     * @param nombreCompleto nombre actualizado
     * @param edad edad actualizada
     * @param correoElectronico correo actualizado
     * @param telefono teléfono actualizado
     * @param numeroUsuario número de socio actualizado
     * @param idUsuario identificador interno actualizado
     * @param contactoEmergencia contacto de emergencia actualizado
     * @param condicionesMedicas condiciones médicas actualizadas
     */
    void actualizarUsuario(int numeroOriginal, String nombreCompleto, int edad, String correoElectronico,
                           int telefono, int numeroUsuario, int idUsuario,
                           String contactoEmergencia, String condicionesMedicas);

    /**
     * Elimina al socio y la membresía asociada, si existe.
     *
     * @param usuario socio a eliminar
     */
    void eliminarUsuario(Usuario usuario);

    /**
     * Lista los socios registrados.
     *
     * @return copia de la lista de socios
     */
    List<Usuario> listarUsuarios();

    /**
     * Busca un socio por su número.
     *
     * @param numeroUsuario número de socio
     * @return el socio, o null si no existe
     */
    Usuario buscarUsuarioPorNumero(int numeroUsuario);

    /**
     * Alinea la casilla de pago al día con la fecha de vencimiento de la membresía.
     *
     * @param usuario socio cuyo estado se debe sincronizar
     */
    void sincronizarEstadoDePago(Usuario usuario);

    /**
     * Registra un aviso para refrescar las pantallas cuando cambian los socios.
     *
     * @param listener acción a ejecutar tras un cambio
     */
    void agregarListener(Runnable listener);
}
