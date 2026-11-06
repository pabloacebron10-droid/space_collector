package clases;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Ventana principal contenedora del videojuego Space Collector.
 * Hereda de JFrame y establece la configuración de la ventana según
 * las directrices de diseño de interfaces: tamaño fijo, centrado,
 * título descriptivo, gestión de cierre y mando de control con JButtons.
 *
 * @author Pablo Acebrón
 */
public class VentanaJuego extends JFrame {

    private final PanelJuego panelJuego;

    public VentanaJuego() {
        super("Space Collector - Arcade 2D (Desarrollo de Interfaces)");
        this.panelJuego = new PanelJuego();
        inicializarVentana();
    }

    private void inicializarVentana() {
        // Disposición con BorderLayout: Lienzo de juego en el centro y panel de mando JButton al sur
        JPanel contenedorPrincipal = new JPanel(new BorderLayout());
        contenedorPrincipal.add(panelJuego, BorderLayout.CENTER);
        contenedorPrincipal.add(panelJuego.getPanelControles(), BorderLayout.SOUTH);

        setContentPane(contenedorPrincipal);

        // Operación de cierre al pulsar la 'X'
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Bloqueo de redimensionamiento para preservar la relación de aspecto del juego arcade
        setResizable(false);

        // pack() ajusta el tamaño del JFrame al tamaño de los paneles
        pack();

        // Centrar la ventana en la pantalla
        setLocationRelativeTo(null);
    }

    public PanelJuego getPanelJuego() {
        return panelJuego;
    }
}
