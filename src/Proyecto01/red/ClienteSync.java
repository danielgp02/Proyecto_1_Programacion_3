package Proyecto01.red;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * Cliente de sincronización: mantiene una conexión TCP con el {@link ServidorSync},
 * envía avisos de cambio y entrega los avisos que llegan de otras instancias.
 */
public class ClienteSync {

    private static final int TIMEOUT_CONEXION_MS = 1500;

    private final Socket socket;
    private final PrintWriter salida;
    private final BufferedReader entrada;

    /**
     * Abre la conexión con el servidor.
     *
     * @param host dirección del servidor (por ejemplo "localhost")
     * @param puerto puerto TCP del servidor
     * @throws IOException si no se puede conectar
     */
    public ClienteSync(String host, int puerto) throws IOException {
        this.socket = new Socket();
        this.socket.connect(new InetSocketAddress(host, puerto), TIMEOUT_CONEXION_MS);
        this.salida = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
        this.entrada = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    }

    /**
     * Envía un mensaje al servidor, que lo retransmite a las demás instancias.
     *
     * @param mensaje texto de una sola línea
     */
    public synchronized void enviar(String mensaje) {
        salida.println(mensaje);
    }

    /**
     * Lee mensajes hasta que se cierre la conexión. Bloquea el hilo que lo llama,
     * por eso debe ejecutarse en un hilo de fondo.
     *
     * @param alRecibir acción a ejecutar con cada mensaje recibido
     */
    public void escuchar(Consumer<String> alRecibir) {
        try {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                alRecibir.accept(linea);
            }
        } catch (IOException e) {
            // Conexión perdida: el que llamó decide si reconectar
        } finally {
            cerrar();
        }
    }

    /**
     * Cierra la conexión con el servidor.
     */
    public void cerrar() {
        try {
            socket.close();
        } catch (IOException ignorada) {
            // Ya estaba cerrado
        }
    }
}
