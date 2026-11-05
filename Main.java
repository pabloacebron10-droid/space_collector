package clases;

import javax.swing.SwingUtilities;

/**
 * Punto de entrada del programa.
 * Asegura la instanciación de la interfaz gráfica en el hilo de despacho
 * de eventos de Swing (Event Dispatch Thread - EDT) mediante SwingUtilities.invokeLater.
 *
 * @author Pablo Acebrón
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaJuego ventana = new VentanaJuego();
            ventana.setVisible(true);
        });
    }
}
