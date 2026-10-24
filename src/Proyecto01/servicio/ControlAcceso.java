package Proyecto01.servicio;

import Proyecto01.modelo.Membresia;
import Proyecto01.modelo.Usuario;

/**
 * Control de acceso del gimnasio. Une cobro, vencimiento y entrada:
 * solo permite pasar si la membresía existe y su fecha no está vencida.
 */
public class ControlAcceso implements IControlAcceso {

    private final IGestorUsuarios gestorUsuarios;
    private final IGestorMembresias gestorMembresias;

    /**
     * @param gestorUsuarios servicio donde se buscan los socios
     * @param gestorMembresias servicio donde se busca la membresía y su vencimiento
     */
    public ControlAcceso(IGestorUsuarios gestorUsuarios, IGestorMembresias gestorMembresias) {
        this.gestorUsuarios = gestorUsuarios;
        this.gestorMembresias = gestorMembresias;
    }

    @Override
    public String verificarAcceso(int numeroUsuario) {
        Usuario usuario = gestorUsuarios.buscarUsuarioPorNumero(numeroUsuario);
        if (usuario == null) {
            return "Acceso Denegado: socio no registrado";
        }
        Membresia membresia = gestorMembresias.buscarMembresiaPorUsuario(usuario);
        if (membresia == null) {
            // No membresía: el socio no está al día. Persistir el estado en BD.
            gestorUsuarios.sincronizarEstadoDePago(usuario);
            return "Acceso Denegado: el socio no tiene una membresía activa";
        }
        boolean alDia = membresia.estaAlDia();
        // Sincronizar el estado de pago en la base de datos para que
        // pago_al_dia quede acorde con la fecha de vencimiento real.
        gestorUsuarios.sincronizarEstadoDePago(usuario);
        return alDia ? "Acceso Permitido" : "Acceso Denegado por Morosidad";
    }
}
