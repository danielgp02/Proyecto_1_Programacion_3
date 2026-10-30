package Proyecto01.red;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * Punto de entrada de la comunicación por sockets. Funciona en un hilo de fondo:
 * intenta conectarse al servidor; si no existe y el host es esta misma máquina,
 * la primera instancia en abrirse levanta el servidor y las demás se conectan a él.
 * Si la conexión se pierde, reintenta (y otra instancia puede asumir el rol de servidor).
 * <p>
 * Para usar dos computadoras distintas, ejecutar la segunda con
 * {@code -Dsync.host=IP_DE_LA_PRIMERA}.
 */
public class SincronizadorRed {

    /** Mensaje que se envía cuando cambian los datos en la base de datos. */
    public static final String MENSAJE_CAMBIO = "CAMBIO_DATOS";

    private static final int PUERTO_DEFECTO = 5050;
    private static final long ESPERA_REINTENTO_MS = 2000;

    private final String host;
    private final int puerto;
    private final Runnable alCambioRemoto;
    private final Consumer<String> alCambiarEstado;

    private volatile ClienteSync cliente;
    private volatile ServidorSync servidorPropio;
    private volatile boolean activo = true;

    /**
     * Crea el sincronizador (aún no se conecta; llamar a {@link #iniciar()}).
     *
     * @param alCambioRemoto acción a ejecutar cuando otra instancia avisa de un cambio
     *                       (se invoca desde un hilo de red, no del hilo de la interfaz)
     * @param alCambiarEstado recibe un texto describiendo el estado de la conexión
     */
    public SincronizadorRed(Runnable alCambioRemoto, Consumer<String> alCambiarEstado) {
        this.host = System.getProperty("sync.host", "localhost");
        this.puerto = Integer.getInteger("sync.puerto", PUERTO_DEFECTO);
        this.alCambioRemoto = alCambioRemoto;
        this.alCambiarEstado = alCambiarEstado;
    }

    /**
     * Arranca el hilo de fondo que mantiene la conexión.
     */
    public void iniciar() {
        Thread hilo = new Thread(this::ciclo, "sync-cliente");
        hilo.setDaemon(true);
        hilo.start();
    }

    /**
     * Avisa a las demás instancias que los datos cambiaron. Si no hay conexión
     * no hace nada: los datos ya están guardados en MySQL.
     */
    public void notificarCambio() {
        ClienteSync actual = cliente;
        if (actual != null) {
            actual.enviar(MENSAJE_CAMBIO);
        }
    }

    /**
     * Detiene la sincronización y libera conexiones y el servidor propio.
     */
    public void detener() {
        activo = false;
        ClienteSync actual = cliente;
        if (actual != null) {
            actual.cerrar();
        }
        ServidorSync propio = servidorPropio;
        if (propio != null) {
            propio.detener();
        }
    }

    /** Bucle de conexión: conectar, escuchar, y reintentar si se cae. */
    private void ciclo() {
        while (activo) {
            try {
                ClienteSync nuevo = new ClienteSync(host, puerto);
                cliente = nuevo;
                alCambiarEstado.accept(servidorPropio != null
                        ? "sincronización: servidor (puerto " + puerto + ")"
                        : "sincronización: conectado a " + host + ":" + puerto);
                nuevo.escuchar(mensaje -> {
                    if (MENSAJE_CAMBIO.equals(mensaje)) {
                        alCambioRemoto.run();
                    }
                });
                cliente = null;
                alCambiarEstado.accept("sincronización: desconectado, reintentando...");
            } catch (IOException e) {
                cliente = null;
                if (esEstaMaquina() && servidorPropio == null && intentarLevantarServidor()) {
                    continue; // se vuelve a intentar conectar, ahora contra el servidor propio
                }
                alCambiarEstado.accept("sincronización: sin conexión");
            }
            dormir();
        }
    }

    /**
     * Intenta abrir el servidor en esta instancia.
     *
     * @return true si el puerto se abrió; false si otra instancia ya lo tomó
     */
    private boolean intentarLevantarServidor() {
        try {
            ServidorSync nuevo = new ServidorSync(puerto);
            nuevo.iniciar();
            servidorPropio = nuevo;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean esEstaMaquina() {
        return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
    }

    private void dormir() {
        try {
            Thread.sleep(ESPERA_REINTENTO_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            activo = false;
        }
    }
}
