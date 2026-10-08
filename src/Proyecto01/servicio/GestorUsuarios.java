package Proyecto01.servicio;

import Proyecto01.modelo.Membresia;
import Proyecto01.modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de socios. El almacenamiento vive aquí, reutilizando Repositorio<Usuario>,
 * y no en un singleton del modelo.
 */
public class GestorUsuarios implements IGestorUsuarios {

    private final IGestorMembresias gestorMembresias;
    private final Repositorio<Usuario> usuarios;
    private final List<Runnable> listeners;

    /**
     * Crea el gestor y carga socios de prueba para la simulación de acceso.
     *
     * @param gestorMembresias servicio de membresías, usado al eliminar y al sincronizar el pago
     */
    public GestorUsuarios(IGestorMembresias gestorMembresias) {
        this.gestorMembresias = gestorMembresias;
        this.usuarios = new Repositorio<>();
        this.listeners = new ArrayList<>();
        precargarSociosDePrueba();
    }

    @Override
    public Usuario registrarUsuario(String nombreCompleto, int edad, String correoElectronico, int telefono,
                                    int numeroUsuario, int idUsuario,
                                    String contactoEmergencia, String condicionesMedicas) {
        if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del socio es obligatorio.");
        }
        if (buscarUsuarioPorNumero(numeroUsuario) != null) {
            throw new IllegalArgumentException("Ya existe un socio registrado con el número " + numeroUsuario + ".");
        }
        // Sin membresía cobrada el socio no puede estar al día.
        Usuario usuario = new Usuario(nombreCompleto, edad, correoElectronico, telefono,
                numeroUsuario, idUsuario, false, contactoEmergencia, condicionesMedicas);
        usuarios.agregar(usuario);
        notificarCambios();
        return usuario;
    }

    @Override
    public void actualizarUsuario(int numeroOriginal, String nombreCompleto, int edad, String correoElectronico,
                                  int telefono, int numeroUsuario, int idUsuario,
                                  String contactoEmergencia, String condicionesMedicas) {
        Usuario existente = buscarUsuarioPorNumero(numeroOriginal);
        if (existente == null) {
            throw new IllegalArgumentException("No existe el socio número " + numeroOriginal + ".");
        }
        if (numeroUsuario != numeroOriginal && buscarUsuarioPorNumero(numeroUsuario) != null) {
            throw new IllegalArgumentException("Ya existe un socio registrado con el número " + numeroUsuario + ".");
        }
        // Se muta la misma instancia: la membresía conserva la referencia al socio.
        existente.setNombreCompleto(nombreCompleto);
        existente.setEdad(edad);
        existente.setCorreoElectronico(correoElectronico);
        existente.setTelefono(telefono);
        existente.setNumeroSocio(numeroUsuario);
        existente.setIdUsuario(idUsuario);
        existente.setContactoEmergencia(contactoEmergencia);
        existente.setCondicionesMedicas(condicionesMedicas);
        sincronizarEstadoDePago(existente);
    }

    @Override
    public void eliminarUsuario(Usuario usuario) {
        gestorMembresias.eliminarMembresiaDeUsuario(usuario);
        usuarios.eliminar(usuario);
        notificarCambios();
    }

    @Override
    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios.obtenerTodos());
    }

    @Override
    public Usuario buscarUsuarioPorNumero(int numeroUsuario) {
        return usuarios.buscar(u -> u.getNumeroUsuario() == numeroUsuario);
    }

    @Override
    public void sincronizarEstadoDePago(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        Membresia membresia = gestorMembresias.buscarMembresiaPorUsuario(usuario);
        usuario.setPagoAlDia(membresia != null && membresia.estaAlDia());
        notificarCambios();
    }

    @Override
    public void agregarListener(Runnable listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private void notificarCambios() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    /**
     * Carga socios de ejemplo. El estado de pago real lo define la membresía
     * que se les asigna al arrancar la ventana.
     */
    private void precargarSociosDePrueba() {
        usuarios.agregar(new Usuario("Maria Gonzalez", 32, "maria.gonzalez@correo.com", 55510101, 101, 1, false,
                "Juan Gonzalez / 88880001", "Ninguna"));
        usuarios.agregar(new Usuario("Carlos Perez", 45, "carlos.perez@correo.com", 55520202, 102, 2, false,
                "Ana Perez / 88880002", "Asma leve"));
        usuarios.agregar(new Usuario("Lucia Fernandez", 28, "lucia.fernandez@correo.com", 55530303, 103, 3, false,
                "Pedro Fernandez / 88880003", "Ninguna"));
        usuarios.agregar(new Usuario("Jorge Ramirez", 51, "jorge.ramirez@correo.com", 55540404, 104, 4, false,
                "Rosa Ramirez / 88880004", "Hipertensión"));
    }
}
