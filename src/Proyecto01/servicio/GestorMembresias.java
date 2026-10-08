package Proyecto01.servicio;

import Proyecto01.modelo.Membresia;
import Proyecto01.modelo.Plan;
import Proyecto01.modelo.Usuario;

import java.util.List;

/**
 * Servicio de membresías. Guarda las membresías en Repositorio<Membresia>
 * y, al cobrar, deja la fecha de vencimiento como fuente del estado de pago.
 */
public class GestorMembresias implements IGestorMembresias {

    private final Repositorio<Membresia> membresias;

    /**
     * Crea el repositorio vacío de membresías.
     */
    public GestorMembresias() {
        this.membresias = new Repositorio<>();
    }

    @Override
    public Membresia asignarPlanYCobrar(Usuario usuario, Plan plan) {
        Membresia existente = buscarMembresiaPorUsuario(usuario);
        if (existente != null) {
            existente.setPlan(plan);
            existente.registrarPago();
            usuario.setPagoAlDia(existente.estaAlDia());
            return existente;
        }
        Membresia nueva = new Membresia(usuario, plan);
        usuario.setPagoAlDia(nueva.estaAlDia());
        membresias.agregar(nueva);
        return nueva;
    }

    @Override
    public void registrarPago(Usuario usuario) {
        Membresia membresia = buscarMembresiaPorUsuario(usuario);
        if (membresia == null) {
            throw new IllegalStateException("El socio no tiene una membresía asignada.");
        }
        membresia.registrarPago();
        usuario.setPagoAlDia(membresia.estaAlDia());
    }

    @Override
    public Membresia buscarMembresiaPorUsuario(Usuario usuario) {
        return membresias.buscar(m -> m.getUsuario().equals(usuario));
    }

    @Override
    public void eliminarMembresiaDeUsuario(Usuario usuario) {
        Membresia membresia = buscarMembresiaPorUsuario(usuario);
        if (membresia != null) {
            membresias.eliminar(membresia);
        }
    }

    @Override
    public List<Membresia> listarMembresias() {
        return membresias.obtenerTodos();
    }
}
