package clases;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 * PanelJuego actúa como el contenedor visual y controlador del ciclo del juego.
 * Gestiona el javax.swing.Timer (bucle a ~60 FPS), la máquina de estados,
 * la actualización de física y colisiones, los Key Bindings y el renderizado con Graphics2D.
 *
 * @author Pablo Acebrón
 */
public class PanelJuego extends JPanel implements ActionListener {

    // Dimensiones lógicas del panel de juego
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

        // Configuración de los controles desacoplados del foco
        configurarKeyBindings();

        // Inicialización del bucle principal (javax.swing.Timer)
        this.timer = new Timer(RETARDO_TIMER_MS, this);
        this.timer.start();
    }

    /**
     * Configuración del sistema de entrada mediante InputMap y ActionMap.
     * Se enlaza a WHEN_IN_FOCUSED_WINDOW para garantizar que los eventos de teclado
     * se capturen de forma global en la ventana, resolviendo los problemas clásicos de foco.
     * Además, se utilizan eventos Pressed y Released para banderas booleanas, evitando
     * el molesto retraso por repetición de tecla del sistema operativo (OS key repeat delay).
     */
    private void configurarKeyBindings() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        // Mapeo de teclas de movimiento (Flechas y WASD) - PRESIONAR
        mapearAccion(inputMap, actionMap, "arr_press", KeyEvent.VK_UP, false, new MoverAction("arriba", true));
        mapearAccion(inputMap, actionMap, "w_press",   KeyEvent.VK_W,  false, new MoverAction("arriba", true));

        mapearAccion(inputMap, actionMap, "abj_press", KeyEvent.VK_DOWN, false, new MoverAction("abajo", true));
        mapearAccion(inputMap, actionMap, "s_press",   KeyEvent.VK_S,    false, new MoverAction("abajo", true));

        mapearAccion(inputMap, actionMap, "izq_press", KeyEvent.VK_LEFT, false, new MoverAction("izquierda", true));
        mapearAccion(inputMap, actionMap, "a_press",   KeyEvent.VK_A,    false, new MoverAction("izquierda", true));

        mapearAccion(inputMap, actionMap, "der_press", KeyEvent.VK_RIGHT, false, new MoverAction("derecha", true));
        mapearAccion(inputMap, actionMap, "d_press",   KeyEvent.VK_D,     false, new MoverAction("derecha", true));

        // Mapeo de teclas de movimiento - SOLTAR (Released)
        mapearAccion(inputMap, actionMap, "arr_rel", KeyEvent.VK_UP, true, new MoverAction("arriba", false));
        mapearAccion(inputMap, actionMap, "w_rel",   KeyEvent.VK_W,  true, new MoverAction("arriba", false));

        mapearAccion(inputMap, actionMap, "abj_rel", KeyEvent.VK_DOWN, true, new MoverAction("abajo", false));
        mapearAccion(inputMap, actionMap, "s_rel",   KeyEvent.VK_S,    true, new MoverAction("abajo", false));

        mapearAccion(inputMap, actionMap, "izq_rel", KeyEvent.VK_LEFT, true, new MoverAction("izquierda", false));
        mapearAccion(inputMap, actionMap, "a_rel",   KeyEvent.VK_A,    true, new MoverAction("izquierda", false));

        mapearAccion(inputMap, actionMap, "der_rel", KeyEvent.VK_RIGHT, true, new MoverAction("derecha", false));
        mapearAccion(inputMap, actionMap, "d_rel",   KeyEvent.VK_D,     true, new MoverAction("derecha", false));

        // Tecla ESPACIO: Iniciar partida desde la pantalla de inicio
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

        // Tecla R: Reiniciar partida tras GameOver o Victoria
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
     * Utilidad para registrar una asociación entre KeyStroke y Action en InputMap/ActionMap.
     */
    private void mapearAccion(InputMap im, ActionMap am, String nombre, int keyCode, boolean alSoltar, AbstractAction accion) {
        im.put(KeyStroke.getKeyStroke(keyCode, 0, alSoltar), nombre);
        am.put(nombre, accion);
    }

    /**
     * Acción interna para modificar el estado de las banderas booleanas de dirección del jugador.
     */
    private class MoverAction extends AbstractAction {
        private final String direccion;
        private final boolean activado;

        public MoverAction(String direccion, boolean activado) {
            this.direccion = direccion;
            this.activado = activado;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (estado != EstadoJuego.JUGANDO) {
                return;
            }
            switch (direccion) {
                case "arriba"    -> jugador.setArriba(activado);
                case "abajo"     -> jugador.setAbajo(activado);
                case "izquierda" -> jugador.setIzquierda(activado);
                case "derecha"   -> jugador.setDerecha(activado);
            }
        }
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
        int ty = 200;

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
        int cardW = 460;
        int cardH = 150;
        int cardX = (ANCHO - cardW) / 2;
        int cardY = 270;
        g2d.setColor(new Color(20, 30, 60, 190));
        g2d.fillRoundRect(cardX, cardY, cardW, cardH, 20, 20);
        g2d.setColor(new Color(0, 200, 255, 100));
        g2d.drawRoundRect(cardX, cardY, cardW, cardH, 20, 20);

        g2d.setFont(new Font("Segoe UI", Font.BOLD, 15));
        g2d.setColor(new Color(255, 230, 100));
        g2d.drawString("MISIÓN Y CONTROLES:", cardX + 30, cardY + 34);

        g2d.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2d.setColor(Color.WHITE);
        g2d.drawString("• Pilota con las FLECHAS o con las teclas W, A, S, D.", cardX + 30, cardY + 64);
        g2d.drawString("• Recoge cristales de energía (+10 pts) hasta llegar a 100.", cardX + 30, cardY + 92);
        g2d.drawString("• Esquiva los meteoritos; ¡solo dispones de 3 vidas!", cardX + 30, cardY + 120);

        // Llamada a la acción pulsante
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 20));
        fm = g2d.getFontMetrics();
        String cta = "Presiona [ ESPACIO ] para despegar";
        g2d.setColor(new Color(0, 255, 200));
        g2d.drawString(cta, (ANCHO - fm.stringWidth(cta)) / 2, 475);
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
        String reintentar = "Presiona [ R ] para reparar y reiniciar";
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
        String reintentar = "Presiona [ R ] para jugar otra partida";
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

    // ================= GETTERS PARA PRUEBAS =================
    public EstadoJuego getEstado() { return estado; }
    public int getPuntos() { return puntos; }
    public int getVidas() { return vidas; }
    public Jugador getJugador() { return jugador; }
    public Timer getTimer() { return timer; }
}
