package Proyecto01.servicio;

import Proyecto01.modelo.Membresia;
import Proyecto01.modelo.Pago;
import Proyecto01.modelo.Plan;
import Proyecto01.modelo.Usuario;
import Proyecto01.persistencia.IMembresiaDao;
import Proyecto01.persistencia.MembresiaDaoJDBC;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de membresías. Guarda las membresías en MySQL detrás de IMembresiaDao
 * y, al cobrar, deja la fecha de vencimiento como fuente del estado de pago.
 */
public class GestorMembresias implements IGestorMembresias {

    private final IMembresiaDao dao;

    /**
     * Crea el servicio con su DAO de persistencia de membresías.
     */
    public GestorMembresias() {
        this.dao = new MembresiaDaoJDBC();
    }

    @Override
    public Membresia asignarPlanYCobrar(Usuario usuario, Plan plan) {
        try {
            Membresia existente = buscarMembresiaPorUsuario(usuario);
            if (existente != null) {
                existente.setPlan(plan);
                existente.registrarPago();
                usuario.setPagoAlDia(existente.estaAlDia());
                if (!dao.actualizar(existente)) {
                    throw new IllegalStateException("No se pudo actualizar la membresía en la base de datos.");
                }
                // Persistir el pago recién registrado en la tabla pagos
                registrarPagoEnBD(existente);
                return existente;
            }
            Membresia nueva = new Membresia(usuario, plan);
            usuario.setPagoAlDia(nueva.estaAlDia());
            dao.crear(nueva);
            // Persistir el pago inicial en la tabla pagos
            registrarPagoEnBD(nueva);
            return nueva;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo asignar el plan en la base de datos: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public void registrarPago(Usuario usuario) {
        try {
            Membresia membresia = buscarMembresiaPorUsuario(usuario);
            if (membresia == null) {
                throw new IllegalStateException("El socio no tiene una membresía asignada.");
            }
            membresia.registrarPago();
            usuario.setPagoAlDia(membresia.estaAlDia());
            if (!dao.actualizar(membresia)) {
                throw new IllegalStateException("No se pudo registrar el pago en la base de datos.");
            }
            // Persistir el pago recién registrado en la tabla pagos
            registrarPagoEnBD(membresia);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar el pago en la base de datos: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Inserta el último pago de una membresía en la tabla pagos de la base de datos.
     *
     * @param membresia membresía cuyo último pago se debe persistir
     * @throws SQLException si falla la inserción
     */
    private void registrarPagoEnBD(Membresia membresia) throws SQLException {
        List<Pago> pagos = membresia.getPagos();
        if (pagos.isEmpty()) {
            return;
        }
        Pago ultimo = pagos.get(pagos.size() - 1);
        if (!dao.insertarPago(membresia.getIdMembresia(), ultimo.getFecha(), ultimo.getMonto())) {
            throw new IllegalStateException("No se pudo insertar el pago en la tabla pagos.");
        }
    }

    @Override
    public Membresia buscarMembresiaPorUsuario(Usuario usuario) {
        try {
            return dao.buscarPorUsuario(usuario);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar la membresía del socio en la base de datos: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarMembresiaDeUsuario(Usuario usuario) {
        try {
            Membresia membresia = buscarMembresiaPorUsuario(usuario);
            if (membresia != null) {
                if (!dao.eliminar(membresia.getIdMembresia())) {
                    throw new IllegalStateException("No se pudo eliminar la membresía de la base de datos.");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar la membresía de la base de datos: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public List<Membresia> listarMembresias() {
        try {
            return dao.listarTodos();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo leer la lista de membresías de la base de datos: "
                    + e.getMessage(), e);
        }
    }

    /**
     * Lista las membresías cuya fecha de vencimiento es anterior a hoy
     * (útil para detectar socios morosos).
     *
     * @return lista de membresías vencidas
     */
    public List<Membresia> listarMembresiasVencidas() {
        try {
            return dao.listarVencidas(LocalDate.now());
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo listar las membresías vencidas: "
                    + e.getMessage(), e);
        }
    }
}
