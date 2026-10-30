package Proyecto01.red;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servidor de sincronización. Acepta conexiones de las instancias de la aplicación
 * y retransmite cada mensaje que recibe a todas las demás instancias conectadas.
 * <p>
 * No guarda datos: la fuente de verdad sigue siendo MySQL. Solo avisa
 * "algo cambió, vuelva a leer" para que las pantallas se actualicen entre sí.
 */
public class ServidorSync {

    private final ServerSocket servidor;
    private final List<Conexion> clientes = new CopyOnWriteArrayList<>();
    private volatile boolean activo = true;

    /**
     * Abre el puerto y deja listo el servidor (aún no acepta clientes).
     *
     * @param puerto puerto TCP donde se escuchará
     * @throws IOException si el puerto ya está en uso o no se puede abrir
     */
    public ServidorSync(int puerto) throws IOException {
        this.servidor = new ServerSocket(puerto);
    }

    /**
     * Inicia en un hilo de fondo el ciclo que acepta clientes nuevos.
     * Cada cliente aceptado recibe a su vez un hilo propio que lee sus mensajes.
     */
    public void iniciar() {
        Thread aceptador = new Thread(() -> {
            while (activo) {
                try {
                    Socket socket = servidor.accept();
                    Conexion conexion = new Conexion(socket);
                    clientes.add(conexion);
                    Thread lector = new Thread(conexion, "sync-servidor-cliente");
                    lector.setDaemon(true);
                    lector.start();
                } catch (IOException e) {
                    if (activo) {
                        System.err.println("[Sync] Error aceptando cliente: " + e.getMessage());
                    }
                }
            }
        }, "sync-servidor-aceptador");
        aceptador.setDaemon(true);
        aceptador.start();
    }

    /**
     * Cierra el servidor y todas las conexiones de clientes.
     */
    public void detener() {
        activo = false;
        try {
            servidor.close();
        } catch (IOException ignorada) {
            // Se está cerrando: no hay nada más que hacer
        }
        for (Conexion c : clientes) {
            c.cerrar();
        }
        clientes.clear();
    }

    /**
     * Envía una línea a todos los clientes excepto al que la originó.
     *
     * @param mensaje texto a retransmitir
     * @param origen conexión que envió el mensaje (no recibe su propio eco)
     */
    private void retransmitir(String mensaje, Conexion origen) {
        for (Conexion c : clientes) {
            if (c != origen) {
                c.enviar(mensaje);
            }
        }
    }

    /** Conexión con un cliente: lee sus mensajes y puede escribirle. */
    private class Conexion implements Runnable {
        private final Socket socket;
        private final PrintWriter salida;

        Conexion(Socket socket) throws IOException {
            this.socket = socket;
            this.salida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        }

        @Override
        public void run() {
            try (BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = entrada.readLine()) != null) {
                    retransmitir(linea, this);
                }
            } catch (IOException e) {
                // El cliente se desconectó: se limpia abajo
            } finally {
                clientes.remove(this);
                cerrar();
            }
        }

        synchronized void enviar(String mensaje) {
            salida.println(mensaje);
        }

        void cerrar() {
            try {
                socket.close();
            } catch (IOException ignorada) {
                // Ya estaba cerrado
            }
        }
    }
}
