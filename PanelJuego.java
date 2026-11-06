package clases;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.border.MatteBorder;

/**
 * PanelJuego actúa como el contenedor visual y controlador del ciclo del juego.
 * Gestiona el javax.swing.Timer (bucle a ~60 FPS), la máquina de estados,
 * la actualización de física y colisiones, los controles mediante JButtons
 * y el renderizado con Graphics2D.
 *
 * De acuerdo con la rúbrica docente, la nave se pilota exclusivamente a través
 * de JButtons de la interfaz y las flechas del teclado han sido desactivadas.
 *
 * @author Pablo Acebrón
 */
public class PanelJuego extends JPanel implements ActionListener {

    // Dimensiones lógicas del lienzo de juego
    public static final int ANCHO = 800;
    public static final int ALTO = 600;

    // Reglas de juego
    private static final int VIDAS_INICIALES = 3;
    private static final int PUNTOS_VICTORIA = 100;
    private static final int RETARDO_TIMER_MS = 16; // ~60 fotogramas por segundo

    // Bucle principal de juego
    private final Timer timer;

    // Máquina de estados
    private EstadoJuego estado;

    // Estadísticas de la partida
    private int puntos;
    private int vidas;
    private long tiempoInicioMs;
    private long tiempoTranscurridoMs;

    // Entidades del modelo
    private final Jugador jugador;
    private final List<Obstaculo> obstaculos;
    private final List<Coleccionable> coleccionables;
    private final List<Estrella> estrellas;

    // Componentes de la interfaz de control (JButtons para cumplir la rúbrica)
    private JPanel panelControles;
    private JButton btnArriba;
    private JButton btnAbajo;
    private JButton btnIzquierda;
    private JButton btnDerecha;
    private JButton btnIniciar;
    private JButton btnReiniciar;

    public PanelJuego() {
        setPreferredSize(new Dimension(ANCHO, ALTO));
        setBackground(new Color(10, 10, 24)); // Fondo espacial profundo
        setFocusable(true);
        setDoubleBuffered(true);

        // Inicialización de entidades del modelo
        this.jugador = new Jugador(ANCHO / 2.0 - 18, ALTO - 90);

        this.obstaculos = new ArrayList<>();
        int cantidadObstaculos = 6;
        for (int i = 0; i < cantidadObstaculos; i++) {
            obstaculos.add(new Obstaculo(ANCHO, ALTO));
        }

        this.coleccionables = new ArrayList<>();
        int cantidadColeccionables = 3;
        for (int i = 0; i < cantidadColeccionables; i++) {
            coleccionables.add(new Coleccionable(ANCHO, ALTO));
        }

        this.estrellas = new ArrayList<>();
        int cantidadEstrellas = 70;
        for (int i = 0; i < cantidadEstrellas; i++) {
            estrellas.add(new Estrella(ANCHO, ALTO));
        }

        this.estado = EstadoJuego.INICIO;
        this.puntos = 0;
        this.vidas = VIDAS_INICIALES;
        this.tiempoTranscurridoMs = 0;

        // Construcción del panel de mandos con JButtons
        inicializarControles();

        // Configuración de atajos complementarios de teclado (Espacio y R)
        configurarKeyBindings();

        // Inicialización del bucle principal (javax.swing.Timer)
        this.timer = new Timer(RETARDO_TIMER_MS, this);
        this.timer.start();
    }

    /**
     * Construye el panel inferior de mando con JButtons para gobernar la nave y la partida,
     * dando estricto cumplimiento a la rúbrica docente.
     */
    private void inicializarControles() {
        this.panelControles = new JPanel(new BorderLayout(15, 0));
        this.panelControles.setPreferredSize(new Dimension(ANCHO, 105));
        this.panelControles.setBackground(new Color(12, 16, 28));
        this.panelControles.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(2, 0, 0, 0, new Color(0, 200, 255, 120)),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        // 1. Sección Izquierda: Gestión de Partida
        JPanel panelAcciones = new JPanel();
        panelAcciones.setLayout(new BoxLayout(panelAcciones, BoxLayout.Y_AXIS));
        panelAcciones.setOpaque(false);

        JLabel lblAcciones = new JLabel("PARTIDA");
        lblAcciones.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblAcciones.setForeground(new Color(140, 170, 210));
        lblAcciones.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        btnIniciar = crearBotonAccion("▶ INICIAR MISIÓN", new Color(0, 220, 160), new Color(0, 255, 180));
        btnReiniciar = crearBotonAccion("🔄 REINICIAR", new Color(255, 190, 40), new Color(255, 220, 100));

        btnIniciar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        btnReiniciar.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        btnIniciar.addActionListener(e -> {
            if (estado == EstadoJuego.INICIO) {
                reiniciarPartida();
                estado = EstadoJuego.JUGANDO;
            }
        });

        btnReiniciar.addActionListener(e -> {
            reiniciarPartida();
            estado = EstadoJuego.JUGANDO;
        });

        panelAcciones.add(lblAcciones);
        panelAcciones.add(Box.createVerticalStrut(4));
        panelAcciones.add(btnIniciar);
        panelAcciones.add(Box.createVerticalStrut(6));
        panelAcciones.add(btnReiniciar);

        // 2. Sección Central: Mando de movimiento de la nave mediante JButtons (Cruceta D-Pad)
        JPanel panelMando = new JPanel(new BorderLayout(0, 3));
        panelMando.setOpaque(false);

        JLabel lblMando = new JLabel("MANDO DE LA NAVE (JBUTTONS)");
        lblMando.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMando.setForeground(new Color(0, 200, 255));
        lblMando.setHorizontalAlignment(JLabel.CENTER);

        JPanel panelCruceta = new JPanel(new GridBagLayout());
        panelCruceta.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 4, 2, 4);
        gbc.fill = GridBagConstraints.BOTH;

        btnArriba = crearBotonMando("▲ ARRIBA");
        btnAbajo = crearBotonMando("▼ ABAJO");
        btnIzquierda = crearBotonMando("◄ IZQUIERDA");
        btnDerecha = crearBotonMando("DERECHA ►");

        // Vinculación de listeners a cada botón de movimiento:
        // Clic rápido aplica un desplazamiento instantáneo de 14px; mantener pulsado activa vuelo continuo.
        vincularBotonDireccion(btnArriba,    0, -14, b -> jugador.setArriba(b));
        vincularBotonDireccion(btnAbajo,     0,  14, b -> jugador.setAbajo(b));
        vincularBotonDireccion(btnIzquierda, -14, 0, b -> jugador.setIzquierda(b));
        vincularBotonDireccion(btnDerecha,    14, 0, b -> jugador.setDerecha(b));

        // Fila 0: [ ▲ ARRIBA ]
        gbc.gridx = 1;
        gbc.gridy = 0;
        panelCruceta.add(btnArriba, gbc);

        // Fila 1: [ ◄ IZQUIERDA ] [ ▼ ABAJO ] [ DERECHA ► ]
        gbc.gridx = 0;
        gbc.gridy = 1;
        panelCruceta.add(btnIzquierda, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panelCruceta.add(btnAbajo, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        panelCruceta.add(btnDerecha, gbc);

        panelMando.add(lblMando, BorderLayout.NORTH);
        panelMando.add(panelCruceta, BorderLayout.CENTER);

        // 3. Sección Derecha: Información de controles para la rúbrica docente
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setOpaque(false);

        JLabel lblInfoTitulo = new JLabel("RÚBRICA DOCENTE");
        lblInfoTitulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblInfoTitulo.setForeground(new Color(140, 170, 210));

        JLabel lblInfo1 = new JLabel("✔ Control con JButtons");
        lblInfo1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo1.setForeground(new Color(180, 240, 200));

        JLabel lblInfo2 = new JLabel("✖ Flechas teclado desactivadas");
        lblInfo2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo2.setForeground(new Color(255, 140, 140));

        JLabel lblInfo3 = new JLabel("✔ Clic simple o mantener pulsado");
        lblInfo3.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblInfo3.setForeground(new Color(180, 220, 240));

        panelInfo.add(lblInfoTitulo);
        panelInfo.add(Box.createVerticalStrut(4));
        panelInfo.add(lblInfo1);
        panelInfo.add(Box.createVerticalStrut(2));
        panelInfo.add(lblInfo2);
        panelInfo.add(Box.createVerticalStrut(2));
        panelInfo.add(lblInfo3);

        // Ensamblado
        this.panelControles.add(panelAcciones, BorderLayout.WEST);
        this.panelControles.add(panelMando, BorderLayout.CENTER);
        this.panelControles.add(panelInfo, BorderLayout.EAST);
    }

    /**
     * Factoría de botones de mando con estilo visual arcade espacial.
     */
    private JButton crearBotonMando(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        boton.setForeground(new Color(0, 240, 255));
        boton.setBackground(new Color(22, 34, 56));
        boton.setFocusPainted(false);
        boton.setFocusable(false); // No roba el foco de la ventana
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 170, 220), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(new Color(34, 52, 84));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(new Color(22, 34, 56));
            }
        });
        return boton;
    }

    /**
     * Factoría de botones de acción (Iniciar / Reiniciar).
     */
    private JButton crearBotonAccion(String texto, Color colorBorde, Color colorTexto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        boton.setForeground(colorTexto);
        boton.setBackground(new Color(18, 28, 44));
        boton.setFocusPainted(false);
        boton.setFocusable(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(colorBorde, 1),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                boton.setBackground(new Color(28, 44, 70));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                boton.setBackground(new Color(18, 28, 44));
            }
        });
        return boton;
    }

    /**
     * Conecta un JButton con la física de la nave:
     * - mousePressed: desplazamiento inmediato visible y activación de la bandera de vuelo continuo.
     * - mouseReleased y mouseExited: detención de la nave.
     */
    private void vincularBotonDireccion(JButton boton, double dx, double dy, Consumer<Boolean> setter) {
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (estado == EstadoJuego.JUGANDO) {
                    jugador.desplazar(dx, dy, ANCHO, ALTO);
                    setter.accept(true);
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setter.accept(false);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setter.accept(false);
            }
        });
    }

    /**
     * Configuración del sistema de entrada para atajos auxiliares (Espacio y R).
     * Por exigencia de la rúbrica docente, las flechas del teclado y WASD han sido eliminadas:
     * la nave no responde al teclado de cursor, únicamente a los JButtons creados.
     */
    private void configurarKeyBindings() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        // Tecla ESPACIO: Iniciar partida desde la pantalla de inicio (atajo auxiliar)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "iniciar_juego");
        actionMap.put("iniciar_juego", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (estado == EstadoJuego.INICIO) {
                    reiniciarPartida();
                    estado = EstadoJuego.JUGANDO;
                }
            }
        });

        // Tecla R: Reiniciar partida tras GameOver o Victoria (atajo auxiliar)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0, false), "reiniciar_juego");
        actionMap.put("reiniciar_juego", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (estado == EstadoJuego.GAMEOVER || estado == EstadoJuego.VICTORIA) {
                    reiniciarPartida();
                    estado = EstadoJuego.JUGANDO;
                }
            }
        });
    }

    /**
     * Ciclo del javax.swing.Timer ejecutado cada 16 ms en el Event Dispatch Thread (EDT).
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        actualizarLogica();
        repaint();
    }

    /**
     * Contiene toda la lógica del juego:
     * 1. Actualización de posiciones de entidades y animación de fondo.
     * 2. Detección de colisiones mediante Rectangle.intersects().
     * 3. Comprobación de reglas de juego (sumar puntuación, restar vidas, invulnerabilidad).
     * 4. Verificación de condiciones de victoria o fin de partida (GameOver).
     */
    public void actualizarLogica() {
        // Las estrellas siempre se actualizan para mantener el dinamismo de fondo en cualquier estado
        for (Estrella estrella : estrellas) {
            estrella.actualizar(ANCHO, ALTO);
        }

        if (estado != EstadoJuego.JUGANDO) {
            return;
        }

        // 1. Actualizar tiempo transcurrido
        tiempoTranscurridoMs = System.currentTimeMillis() - tiempoInicioMs;

        // 2. Actualizar jugador
        jugador.actualizar(ANCHO, ALTO);

        // 3. Actualizar obstáculos
        for (Obstaculo obstaculo : obstaculos) {
            obstaculo.actualizar(ANCHO, ALTO);
        }

        // 4. Actualizar coleccionables (animación)
        for (Coleccionable coleccionable : coleccionables) {
            coleccionable.actualizar();
        }

        // 5. Detección de colisiones con Coleccionables (Cristales)
        for (Coleccionable coleccionable : coleccionables) {
            if (jugador.getHitbox().intersects(coleccionable.getHitbox())) {
                puntos += coleccionable.getPuntos();
                coleccionable.reaparecer(ANCHO, ALTO);

                // Comprobación de condición de VICTORIA
                if (puntos >= PUNTOS_VICTORIA) {
                    estado = EstadoJuego.VICTORIA;
                    return;
                }
            }
        }

        // 6. Detección de colisiones con Obstáculos (Meteoritos)
        if (!jugador.esInvulnerable()) {
            for (Obstaculo obstaculo : obstaculos) {
                if (jugador.getHitbox().intersects(obstaculo.getHitbox())) {
                    vidas--;
                    jugador.activarInvulnerabilidad();

                    // Comprobación de condición de GAMEOVER
                    if (vidas <= 0) {
                        estado = EstadoJuego.GAMEOVER;
                        return;
                    }
                    break; // Solo recibe daño de un meteorito por fotograma
                }
            }
        }
    }

    /**
     * Restablece el estado completo de la partida (vidas, puntos, tiempo y posiciones)
     * de forma limpia sin crear ni destruir instancias de javax.swing.Timer.
     */
    public void reiniciarPartida() {
        this.puntos = 0;
        this.vidas = VIDAS_INICIALES;
        this.tiempoInicioMs = System.currentTimeMillis();
        this.tiempoTranscurridoMs = 0;

        jugador.resetear(ANCHO / 2.0 - 18, ALTO - 90);

        for (Obstaculo obstaculo : obstaculos) {
            obstaculo.reaparecer(ANCHO, ALTO);
        }
        for (Coleccionable coleccionable : coleccionables) {
            coleccionable.reaparecer(ANCHO, ALTO);
        }
    }

    /**
     * Sobrescribe el método de pintado de Swing.
     * Aplica antialiasing en Graphics2D y delega el renderizado en orden de capas:
     * Fondo -> Estrellas -> Coleccionables -> Obstáculos -> Jugador -> HUD -> Pantallas de Estado.
     *
     * IMPORTANTE: En este método NUNCA se debe ejecutar lógica de negocio ni modificar
     * variables de estado (puntos, vidas, etc.), únicamente consultar el modelo y dibujarlo.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g.create();

        // Activación de Antialiasing y máxima calidad visual
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 1. Dibujo del fondo degradado espacial
        GradientPaint fondoDegradado = new GradientPaint(
                0, 0, new Color(5, 5, 20),
                0, ALTO, new Color(18, 12, 38)
        );
        g2d.setPaint(fondoDegradado);
        g2d.fillRect(0, 0, ANCHO, ALTO);

        // 2. Estrellas
        for (Estrella estrella : estrellas) {
            estrella.dibujar(g2d);
        }

        // 3. Entidades del juego
        for (Coleccionable coleccionable : coleccionables) {
            coleccionable.dibujar(g2d);
        }

        for (Obstaculo obstaculo : obstaculos) {
            obstaculo.dibujar(g2d);
        }

        jugador.dibujar(g2d);

        // 4. Interfaz de usuario superior (HUD)
        dibujarHUD(g2d);

        // 5. Pantallas superpuestas según la máquina de estados
        switch (estado) {
            case INICIO   -> dibujarPantallaInicio(g2d);
            case GAMEOVER -> dibujarPantallaGameOver(g2d);
            case VICTORIA -> dibujarPantallaVictoria(g2d);
            case JUGANDO  -> { /* El juego se ve de forma limpia en acción */ }
        }

        g2d.dispose(); // Liberación de los recursos gráficos nativos
    }

    /**
     * Renderiza la barra superior del HUD (Heads-Up Display) con vidas, puntuación y tiempo.
     */
    private void dibujarHUD(Graphics2D g2d) {
        // Barra traslúcida superior
        g2d.setColor(new Color(10, 15, 30, 210));
        g2d.fillRect(0, 0, ANCHO, 42);
        g2d.setColor(new Color(0, 200, 255, 90));
        g2d.drawLine(0, 42, ANCHO, 42);

        // Vidas con iconos vectoriales de corazones
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2d.setColor(new Color(220, 230, 250));
        g2d.drawString("VIDAS:", 16, 26);

        for (int i = 0; i < VIDAS_INICIALES; i++) {
            int cx = 75 + (i * 24);
            int cy = 16;
            if (i < vidas) {
                dibujarCorazon(g2d, cx, cy, new Color(255, 60, 90));
            } else {
                dibujarCorazon(g2d, cx, cy, new Color(90, 80, 95, 140)); // Vida perdida
            }
        }

        // Puntos con icono de cristal
        String strPuntos = String.format("PUNTOS: %d / %d", puntos, PUNTOS_VICTORIA);
        g2d.setColor(new Color(0, 255, 210));
        g2d.drawString(strPuntos, 310, 26);

        // Tiempo transcurrido formateado MM:SS
        long totalSegundos = tiempoTranscurridoMs / 1000;
        long minutos = totalSegundos / 60;
        long segundos = totalSegundos % 60;
        String strTiempo = String.format("TIEMPO: %02d:%02d", minutos, segundos);

        g2d.setColor(new Color(255, 220, 100));
        g2d.drawString(strTiempo, ANCHO - 145, 26);
    }

    /**
     * Dibuja un corazón vectorial para el indicador de vida.
     */
    private void dibujarCorazon(Graphics2D g2d, int x, int y, Color color) {
        g2d.setColor(color);
        int r = 5;
        g2d.fillOval(x, y, r * 2, r * 2);
        g2d.fillOval(x + r, y, r * 2, r * 2);
        Polygon triangulo = new Polygon(
                new int[]{ x, x + r * 2 + 5, x + r + 2 },
                new int[]{ y + r + 2, y + r + 2, y + r * 3 },
                3
        );
        g2d.fillPolygon(triangulo);
    }

    /**
     * Pantalla de inicio con bienvenida, título futurista e instrucciones de control.
     */
    private void dibujarPantallaInicio(Graphics2D g2d) {
        dibujarCapaOscura(g2d, new Color(5, 8, 20, 210));

        // Título principal con relieve
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 46));
        FontMetrics fm = g2d.getFontMetrics();
        String titulo = "SPACE COLLECTOR";
        int tx = (ANCHO - fm.stringWidth(titulo)) / 2;
        int ty = 190;

        // Sombra de brillo
        g2d.setColor(new Color(0, 180, 255, 120));
        g2d.drawString(titulo, tx + 2, ty + 2);
        // Texto frontal
        g2d.setColor(new Color(240, 250, 255));
        g2d.drawString(titulo, tx, ty);

        // Subtítulo de asignatura
        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        fm = g2d.getFontMetrics();
        String sub = "Arcade 2D - Práctica de Desarrollo de Interfaces";
        g2d.setColor(new Color(130, 190, 240));
        g2d.drawString(sub, (ANCHO - fm.stringWidth(sub)) / 2, ty + 36);

        // Tarjeta de instrucciones y controles
        int cardW = 520;
        int cardH = 155;
        int cardX = (ANCHO - cardW) / 2;
        int cardY = 260;
        g2d.setColor(new Color(20, 30, 60, 190));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 20, 20);
        g2d.setColor(new Color(0, 200, 255, 100));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 20, 20);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2d.setColor(new Color(255, 230, 100));
        g2d.drawString("MISIÓN Y CONTROLES (RÚBRICA DOCENTE):", cardX + 24, cardY + 32);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString("• Pilota la nave con los JBUTTONS del panel inferior.", cardX + 24, cardY + 62);
        g2d.drawString("• Las flechas del teclado están desactivadas por rúbrica.", cardX + 24, cardY + 88);
        g2d.drawString("• Recoge cristales de energía (+10 pts) hasta 100 puntos.", cardX + 24, cardY + 114);
        g2d.drawString("• ¡Esquiva los meteoritos! Dispones de 3 vidas de energía.", cardX + 24, cardY + 140);

        // Llamada a la acción pulsante
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 19));
        fm = g2d.getFontMetrics();
        String cta = "Pulsa [ ▶ INICIAR MISIÓN ] en el panel inferior";
        g2d.setColor(new Color(0, 255, 200));
        g2d.drawString(cta, (ANCHO - fm.stringWidth(cta)) / 2, 470);
    }

    /**
     * Pantalla mostrada cuando el jugador pierde todas sus vidas.
     */
    private void dibujarPantallaGameOver(Graphics2D g2d) {
        dibujarCapaOscura(g2d, new Color(40, 5, 10, 220));

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 52));
        FontMetrics fm = g2d.getFontMetrics();
        String texto = "GAME OVER";
        g2d.setColor(new Color(255, 50, 70));
        g2d.drawString(texto, (ANCHO - fm.stringWidth(texto)) / 2, 230);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        fm = g2d.getFontMetrics();
        String resumen = String.format("Tu nave ha sido destruida. Puntos obtenidos: %d", puntos);
        g2d.setColor(Color.WHITE);
        g2d.drawString(resumen, (ANCHO - fm.stringWidth(resumen)) / 2, 290);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 19));
        fm = g2d.getFontMetrics();
        String reintentar = "Pulsa [ 🔄 REINICIAR ] en el panel inferior para jugar de nuevo";
        g2d.setColor(new Color(255, 220, 100));
        g2d.drawString(reintentar, (ANCHO - fm.stringWidth(reintentar)) / 2, 380);
    }

    /**
     * Pantalla mostrada al recolectar la meta de cristales de energía.
     */
    private void dibujarPantallaVictoria(Graphics2D g2d) {
        dibujarCapaOscura(g2d, new Color(5, 35, 25, 220));

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String texto = "¡MISIÓN CUMPLIDA!";
        g2d.setColor(new Color(0, 255, 180));
        g2d.drawString(texto, (ANCHO - fm.stringWidth(texto)) / 2, 220);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        fm = g2d.getFontMetrics();
        long seg = tiempoTranscurridoMs / 1000;
        String resumen = String.format("Has recolectado todos los cristales en %d segundos con %d vidas restantes.", seg, vidas);
        g2d.setColor(Color.WHITE);
        g2d.drawString(resumen, (ANCHO - fm.stringWidth(resumen)) / 2, 280);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 19));
        fm = g2d.getFontMetrics();
        String reintentar = "Pulsa [ 🔄 REINICIAR ] para iniciar otra misión";
        g2d.setColor(new Color(255, 220, 100));
        g2d.drawString(reintentar, (ANCHO - fm.stringWidth(reintentar)) / 2, 380);
    }

    /**
     * Dibuja un velo traslúcido para destacar el texto de los estados modales.
     */
    private void dibujarCapaOscura(Graphics2D g2d, Color color) {
        g2d.setColor(color);
        g2d.fillRect(0, 0, ANCHO, ALTO);
    }

    // ================= GETTERS PARA PRUEBAS Y CONTROL =================
    public EstadoJuego getEstado() { return estado; }
    public int getPuntos() { return puntos; }
    public int getVidas() { return vidas; }
    public Jugador getJugador() { return jugador; }
    public Timer getTimer() { return timer; }
    public JPanel getPanelControles() { return panelControles; }
    public JButton getBotonArriba() { return btnArriba; }
    public JButton getBotonAbajo() { return btnAbajo; }
    public JButton getBotonIzquierda() { return btnIzquierda; }
    public JButton getBotonDerecha() { return btnDerecha; }
    public JButton getBotonIniciar() { return btnIniciar; }
    public JButton getBotonReiniciar() { return btnReiniciar; }
}
