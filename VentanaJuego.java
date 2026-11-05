package clases;

import javax.swing.JFrame;

/**
 * Ventana principal contenedora del videojuego Space Collector.
 * Hereda de JFrame y establece la configuración de la ventana según
 * las directrices de diseño de interfaces: tamaño fijo, centrado,
 * título descriptivo y gestión de cierre.
 *
 * @author Pablo Acebrón
 */
public class VentanaJuego extends JFrame {

    public VentanaJuego() {
        super("Space Collector - Arcade 2D (Desarrollo de Interfaces)");
        inicializarVentana();
    }

    private void inicializarVentana() {
        // Creación del panel principal del juego
        PanelJuego panelJuego = new PanelJuego();
        setContentPane(panelJuego);

        // Operación de cierre al pulsar la 'X'
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Bloqueo de redimensionamiento para preservar la relación de aspecto del juego arcade
        setResizable(false);

        // pack() ajusta el tamaño del JFrame al tamaño preferido del PanelJuego (800x600),
        // asegurando que los bordes y la barra de título del SO no resten espacio al lienzo de dibujo.
        pack();

        // Centrar la ventana en la pantalla
        setLocationRelativeTo(null);
    }
}
