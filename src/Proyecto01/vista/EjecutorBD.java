package Proyecto01.vista;

import javax.swing.*;
import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Utilidad para ejecutar operaciones de base de datos fuera del hilo de la interfaz.
 * El trabajo corre en un hilo de fondo (SwingWorker) y el resultado se entrega de
 * vuelta en el hilo de Swing, de modo que la ventana no se congela y las tablas
 * se actualizan de forma segura.
 */
public final class EjecutorBD {

    private EjecutorBD() {
    }

    /**
     * Ejecuta una operación de base de datos en un hilo de fondo.
     * Si lanza una excepción de negocio (IllegalArgumentException / IllegalStateException)
     * se muestra su mensaje en un diálogo; cualquier otro error inesperado también
     * se informa, para que la aplicación nunca se cierre sola.
     *
     * @param padre componente sobre el que se muestran los diálogos de error
     * @param tituloError título del diálogo de error
     * @param trabajo operación a ejecutar en segundo plano
     * @param alTerminar acción a ejecutar en el hilo de Swing con el resultado (puede ser null)
     * @param <T> tipo del resultado de la operación
     */
    public static <T> void ejecutar(Component padre, String tituloError,
                                    Callable<T> trabajo, Consumer<T> alTerminar) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return trabajo.call();
            }

            @Override
            protected void done() {
                try {
                    T resultado = get();
                    if (alTerminar != null) {
                        alTerminar.accept(resultado);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable causa = e.getCause() != null ? e.getCause() : e;
                    String mensaje = causa.getMessage() != null ? causa.getMessage() : causa.toString();
                    int tipo = (causa instanceof IllegalArgumentException)
                            ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE;
                    JOptionPane.showMessageDialog(padre, mensaje, tituloError, tipo);
                }
            }
        }.execute();
    }

    /**
     * Variante sin resultado: ejecuta una acción en segundo plano y luego otra en el hilo de Swing.
     *
     * @param padre componente sobre el que se muestran los diálogos de error
     * @param tituloError título del diálogo de error
     * @param trabajo operación a ejecutar en segundo plano
     * @param alTerminar acción a ejecutar en el hilo de Swing al terminar bien (puede ser null)
     */
    public static void ejecutarSinResultado(Component padre, String tituloError,
                                            Runnable trabajo, Runnable alTerminar) {
        ejecutar(padre, tituloError, () -> {
            trabajo.run();
            return null;
        }, (Void ignorado) -> {
            if (alTerminar != null) {
                alTerminar.run();
            }
        });
    }
}
