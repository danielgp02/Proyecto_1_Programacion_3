package Proyecto01.servicio;

/**
 * Contrato de la simulación de acceso. La decisión usa la fecha de vencimiento
 * de la membresía, no la casilla manual del formulario de socios.
 */
public interface IControlAcceso {

    /**
     * Verifica si el socio puede entrar según su membresía y su vencimiento.
     *
     * @param numeroSocio número de socio digitado en la pantalla de acceso
     * @return "Acceso Permitido", "Acceso Denegado por Morosidad" u otro motivo de rechazo
     */
    String verificarAcceso(int numeroSocio);
}
