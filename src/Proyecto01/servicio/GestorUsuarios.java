package Proyecto01.servicio;

import Proyecto01.modelo.Membresia;
import Proyecto01.modelo.Usuario;
import Proyecto01.persistencia.IUsuarioDao;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servicio de socios. La persistencia vive en MySQL detrás de IUsuarioDao:
 * este gestor aplica reglas de negocio y traduce los errores de base de datos
 * a mensajes comprensibles para la interfaz.
 */
public class GestorUsuarios implements IGestorUsuarios {

    private final IGestorMembresias gestorMembresias;
    private final IUsuarioDao dao;
    private final List<Runnable> listeners;
    // Avisos que solo se disparan por cambios hechos en esta instancia (ej. sincronización por red)
    private final List<Runnable> listenersLocales;
    // Socios ya cargados: una sola instancia por socio, la misma que ve la membresía
    private final Repositorio<Usuario> cache;

    /**
     * Crea el gestor con su DAO de persistencia.
     *
     * @param gestorMembresias servicio de membresías, usado al eliminar y al sincronizar el pago
     * @param dao acceso a datos de los socios (JDBC contra MySQL)
     */
    public GestorUsuarios(IGestorMembresias gestorMembresias, IUsuarioDao dao) {
        this.gestorMembresias = gestorMembresias;
        this.dao = dao;
        this.listeners = new CopyOnWriteArrayList<>();
        this.listenersLocales = new CopyOnWriteArrayList<>();
        this.cache = new Repositorio<>();
    }

    @Override
    public synchronized Usuario registrarUsuario(String nombreCompleto, int edad, String correoElectronico, int telefono,
                                    int numeroUsuario, int idUsuario, String contactoEmergencia,
                                    String condicionesMedicas) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del socio es obligatorio.");
        }
        if (buscarUsuarioPorNumero(numeroUsuario) != null) {
            throw new IllegalArgumentException("Ya existe un socio registrado con el número " + numeroUsuario + ".");
        }
        // El idUsuario lo genera la base de datos (AUTO_INCREMENT), no el formulario.
        Usuario usuario = new Usuario(nombreCompleto, edad, correoElectronico, telefono,
                numeroUsuario, 0, false, contactoEmergencia, condicionesMedicas);

        try {
            usuario = dao.crear(usuario);
        } catch (SQLIntegrityConstraintViolationException e) {
            // La BD también protege el UNIQUE de numero_socio
            throw new IllegalArgumentException("Ya existe un socio registrado con el número " + numeroUsuario + ".");
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar el socio en la base de datos: "
                    + e.getMessage(), e);
        }
        cache.agregar(usuario);
        notificarCambios();
        return usuario;
    }

    @Override
    public synchronized void actualizarUsuario(int numeroOriginal, String nombreCompleto, int edad, String correoElectronico,
                                  int telefono, int numeroUsuario, int idUsuario, String contactoEmergencia,
                                  String condicionesMedicas) {
        Usuario existente = buscarUsuarioPorNumero(numeroOriginal);
        if (existente == null) {
            throw new IllegalArgumentException("No existe el socio número " + numeroOriginal + ".");
        }
        if (numeroUsuario != numeroOriginal && buscarUsuarioPorNumero(numeroUsuario) != null) {
            throw new IllegalArgumentException("Ya existe un socio registrado con el número " + numeroUsuario + ".");
        }
        // Se muta la misma instancia (la membresía conserva la referencia al socio).
        // OJO: idUsuario NO se toca: es la llave primaria de la tabla usuarios.
        existente.setNombreCompleto(nombreCompleto);
        existente.setEdad(edad);
        existente.setCorreoElectronico(correoElectronico);
        existente.setTelefono(telefono);
        existente.setNumeroSocio(numeroUsuario);
        existente.setContactoEmergencia(contactoEmergencia);
        existente.setCondicionesMedicas(condicionesMedicas);

        // Sincronizar primero el estado de pago según la membresía, para que
        // dao.actualizar guarde el valor correcto de pago_al_dia en la BD.
        sincronizarEstadoDePago(existente);

        try {
            if (!dao.actualizar(existente)) {
                throw new IllegalStateException("El socio número " + numeroUsuario
                        + " ya no existe en la base de datos.");
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new IllegalArgumentException("Ya existe otro socio con el número " + numeroUsuario + ".");
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el socio: " + e.getMessage(), e);
        }
        notificarCambios();
    }

    @Override
    public synchronized void eliminarUsuario(Usuario usuario) {
        gestorMembresias.eliminarMembresiaDeUsuario(usuario);
        try {
            if (!dao.eliminar(usuario.getNumeroUsuario())) {
                throw new IllegalStateException("El socio número " + usuario.getNumeroUsuario()
                        + " ya no existe en la base de datos.");
            }
            cache.eliminar(usuario);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar al socio: " + e.getMessage(), e);
        }
        notificarCambios();
    }

    @Override
    public synchronized List<Usuario> listarUsuarios() {
        try {
            List<Usuario> resultado = new ArrayList<>();
            List<Usuario> desdeBase = dao.listarTodos();
            for (Usuario desdeBD : desdeBase) {
                resultado.add(enCache(desdeBD));
            }
            // Quita del caché los socios que ya no existen (p. ej. eliminados desde otra instancia)
            cache.obtenerTodos().removeIf(c -> desdeBase.stream().noneMatch(b -> b.getIdUsuario() == c.getIdUsuario()));
            return resultado;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo leer la lista de socios de la base de datos: "
                    + e.getMessage(), e);
        }
    }

    @Override
    public synchronized Usuario buscarUsuarioPorNumero(int numeroUsuario) {
        try {
            Usuario cacheado = cache.buscar(u -> u.getNumeroUsuario() == numeroUsuario);
            if (cacheado != null) {
                return cacheado;
            }
            Usuario desdeBD = dao.buscarPorNumero(numeroUsuario);
            return desdeBD == null ? null : enCache(desdeBD);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el socio número " + numeroUsuario
                    + " en la base de datos: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void sincronizarEstadoDePago(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        Membresia membresia = gestorMembresias.buscarMembresiaPorUsuario(usuario);
        boolean alDia = membresia != null && membresia.estaAlDia();
        usuario.setPagoAlDia(alDia);
        try {
            dao.actualizarEstadoDePago(usuario.getNumeroUsuario(), alDia);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar el estado de pago del socio: "
                    + e.getMessage(), e);
        }
        notificarCambios();
    }

    /**
     * Devuelve la instancia guardada del socio o, si es la primera vez que se
     * carga, la guarda en el repositorio genérico.
     *
     * @param desdeBD socio recién leído de la base de datos
     * @return la instancia única del socio
     */
    private Usuario enCache(Usuario desdeBD) {
        Usuario existente = cache.buscar(u -> u.getIdUsuario() == desdeBD.getIdUsuario());
        if (existente != null) {
            // Se copian todos los campos por si otra instancia modificó al socio
            existente.setNombreCompleto(desdeBD.getNombreCompleto());
            existente.setEdad(desdeBD.getEdad());
            existente.setCorreoElectronico(desdeBD.getCorreoElectronico());
            existente.setTelefono(desdeBD.getTelefono());
            existente.setNumeroSocio(desdeBD.getNumeroUsuario());
            existente.setContactoEmergencia(desdeBD.getContactoEmergencia());
            existente.setCondicionesMedicas(desdeBD.getCondicionesMedicas());
            existente.setPagoAlDia(desdeBD.isPagoAlDia());
            return existente;
        }
        cache.agregar(desdeBD);
        return desdeBD;
    }

    @Override
    public void agregarListener(Runnable listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    @Override
    public void agregarListenerCambioLocal(Runnable listener) {
        if (listener != null) {
            listenersLocales.add(listener);
        }
    }

    @Override
    public void notificarCambioExterno() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    /**
     * Avisa a las pantallas y a los oyentes de cambios locales que los socios cambiaron.
     */
    private void notificarCambios() {
        notificarCambioExterno();
        for (Runnable listener : listenersLocales) {
            listener.run();
        }
    }
}
