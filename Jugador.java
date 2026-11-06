package clases;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/**
 * Entidad modelo (POJO) que representa la nave del jugador.
 * Gestiona su posición, velocidad, estado de invulnerabilidad tras impactos
 * y su propio renderizado vectorial estilizado mediante Graphics2D.
 *
 * @author Pablo Acebrón
 */
public class Jugador {

    // Coordenadas cartesianas y dimensiones en pantalla
    private double x;
    private double y;
    private final int ancho;
    private final int alto;

    // Velocidad de desplazamiento en píxeles por ciclo de refresco
    private double velocidad;

    // Banderas de control de movimiento continuo (Key Bindings)
    private boolean arriba;
    private boolean abajo;
    private boolean izquierda;
    private boolean derecha;

    // Temporizador de invulnerabilidad (en fotogramas de juego)
    private int tiempoInvulnerabilidad;
    private static final int DURACION_INVULNERABILIDAD = 60; // ~1 segundo a 60 FPS

    // Contador auxiliar para animar la llama del propulsor
    private int cicloPropulsor;

    public Jugador(double inicioX, double inicioY) {
        this.ancho = 36;
        this.alto = 46;
        this.velocidad = 5.5;
        this.cicloPropulsor = 0;
        resetear(inicioX, inicioY);
    }

    /**
     * Restablece la nave a sus valores predeterminados de inicio.
     */
    public void resetear(double nuevoX, double nuevoY) {
        this.x = nuevoX;
        this.y = nuevoY;
        this.arriba = false;
        this.abajo = false;
        this.izquierda = false;
        this.derecha = false;
        this.tiempoInvulnerabilidad = 0;
    }

    /**
     * Actualiza la posición física de la nave según los controles pulsados
     * y restringe estrictamente sus coordenadas para no salir del área visible.
     *
     * @param anchoLimite Límite horizontal del panel
     * @param altoLimite  Límite vertical del panel
     */
    public void actualizar(int anchoLimite, int altoLimite) {
        // Cálculo del desplazamiento vectorial
        double dx = 0;
        double dy = 0;

        if (arriba)    dy -= velocidad;
        if (abajo)     dy += velocidad;
        if (izquierda) dx -= velocidad;
        if (derecha)   dx += velocidad;

        // Normalización si se mueve en diagonal para no correr más rápido
        if (dx != 0 && dy != 0) {
            double factor = 1.0 / Math.sqrt(2.0);
            dx *= factor;
            dy *= factor;
        }

        x += dx;
        y += dy;

        // Restricción dentro de los márgenes del panel (dejar margen superior para el HUD)
        if (x < 0) {
            x = 0;
        } else if (x > anchoLimite - ancho) {
            x = anchoLimite - ancho;
        }

        if (y < 40) { // Margen para el HUD superior
            y = 40;
        } else if (y > altoLimite - alto) {
            y = altoLimite - alto;
        }

        // Reducción del temporizador de invulnerabilidad
        if (tiempoInvulnerabilidad > 0) {
            tiempoInvulnerabilidad--;
        }

        cicloPropulsor++;
    }

    /**
     * Aplica un desplazamiento incremental directo a la posición de la nave,
     * garantizando que permanezca siempre dentro de los márgenes visibles del juego.
     * Utilizado para responder de forma inmediata a los clics sobre los JButtons.
     *
     * @param dx          Desplazamiento horizontal en píxeles
     * @param dy          Desplazamiento vertical en píxeles
     * @param anchoLimite Límite horizontal del panel
     * @param altoLimite  Límite vertical del panel
     */
    public void desplazar(double dx, double dy, int anchoLimite, int altoLimite) {
        this.x += dx;
        this.y += dy;

        if (this.x < 0) {
            this.x = 0;
        } else if (this.x > anchoLimite - ancho) {
            this.x = anchoLimite - ancho;
        }

        if (this.y < 40) {
            this.y = 40;
        } else if (this.y > altoLimite - alto) {
            this.y = altoLimite - alto;
        }
    }

    /**
     * Activa el estado de invulnerabilidad temporal al sufrir una colisión.
     */
    public void activarInvulnerabilidad() {
        this.tiempoInvulnerabilidad = DURACION_INVULNERABILIDAD;
    }

    public boolean esInvulnerable() {
        return tiempoInvulnerabilidad > 0;
    }

    /**
     * Retorna el rectángulo de colisión (Hitbox).
     * Se aplica un margen interior (padding) de 4px para que las colisiones
     * con figuras triangulares/cóncavas se sientan justas y precisas.
     */
    public Rectangle getHitbox() {
        int padding = 4;
        return new Rectangle(
                (int) x + padding,
                (int) y + padding,
                ancho - (padding * 2),
                alto - (padding * 2)
        );
    }

    /**
     * Dibuja la nave espacial utilizando primitivas vectoriales de Graphics2D,
     * gradientes metálicos, cabina luminosa y llama del motor propulsor animada.
     */
    public void dibujar(Graphics2D g2d) {
        // Efecto visual de parpadeo durante el periodo de invulnerabilidad
        if (esInvulnerable() && (tiempoInvulnerabilidad / 6) % 2 == 0) {
            return; // No se dibuja en este frame para producir el parpadeo
        }

        int px = (int) x;
        int py = (int) y;

        // 1. Llama del propulsor (animada con oscilación senoidal)
        int largoLlama = 10 + (int) (Math.sin(cicloPropulsor * 0.4) * 5);
        if (arriba) {
            largoLlama += 8; // Más potente al acelerar hacia adelante
        }

        Polygon llamaExterna = new Polygon(
                new int[]{ px + 12, px + ancho / 2, px + ancho - 12 },
                new int[]{ py + alto - 2, py + alto + largoLlama, py + alto - 2 },
                3
        );
        g2d.setColor(new Color(255, 120, 20, 220));
        g2d.fillPolygon(llamaExterna);

        Polygon llamaInterna = new Polygon(
                new int[]{ px + 15, px + ancho / 2, px + ancho - 15 },
                new int[]{ py + alto - 2, py + alto + (largoLlama * 2 / 3), py + alto - 2 },
                3
        );
        g2d.setColor(new Color(255, 255, 100, 240));
        g2d.fillPolygon(llamaInterna);

        // 2. Alas y alerones laterales
        Polygon alerones = new Polygon(
                new int[]{ px, px + ancho / 2, px + ancho },
                new int[]{ py + alto, py + 16, py + alto },
                3
        );
        GradientPaint gradAlas = new GradientPaint(
                px, py + 20, new Color(40, 80, 160),
                px + ancho, py + alto, new Color(20, 40, 90)
        );
        g2d.setPaint(gradAlas);
        g2d.fillPolygon(alerones);
        g2d.setColor(new Color(0, 220, 255));
        g2d.drawPolygon(alerones);

        // 3. Fuselaje central aerodinámico
        Polygon fuselaje = new Polygon(
                new int[]{ px + ancho / 2, px + 8, px + ancho / 2, px + ancho - 8 },
                new int[]{ py, py + alto - 6, py + alto - 12, py + alto - 6 },
                4
        );
        GradientPaint gradFuselaje = new GradientPaint(
                px + ancho / 2f, py, new Color(220, 235, 255),
                px + ancho / 2f, py + alto, new Color(90, 120, 170)
        );
        g2d.setPaint(gradFuselaje);
        g2d.fillPolygon(fuselaje);
        g2d.setColor(new Color(15, 30, 60));
        g2d.drawPolygon(fuselaje);

        // 4. Cabina del piloto (cristal de energía cian con brillo)
        int cabinaAncho = 10;
        int cabinaAlto = 16;
        int cabinaX = px + (ancho - cabinaAncho) / 2;
        int cabinaY = py + 12;

        GradientPaint gradCabina = new GradientPaint(
                cabinaX, cabinaY, new Color(180, 255, 255),
                cabinaX + cabinaAncho, cabinaY + cabinaAlto, new Color(0, 150, 220)
        );
        g2d.setPaint(gradCabina);
        g2d.fillOval(cabinaX, cabinaY, cabinaAncho, cabinaAlto);
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillOval(cabinaX + 2, cabinaY + 3, 3, 5); // Reflejo especular

        // 5. Cañones de plasma / luces en los extremos de las alas
        g2d.setColor(new Color(0, 255, 230));
        g2d.fillRect(px, py + alto - 8, 3, 6);
        g2d.fillRect(px + ancho - 3, py + alto - 8, 3, 6);
    }

    // ================= GETTERS Y SETTERS =================

    public double getX() { return x; }
    public double getY() { return y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    public double getVelocidad() { return velocidad; }
    public void setVelocidad(double velocidad) { this.velocidad = velocidad; }

    public void setArriba(boolean arriba) { this.arriba = arriba; }
    public void setAbajo(boolean abajo) { this.abajo = abajo; }
    public void setIzquierda(boolean izquierda) { this.izquierda = izquierda; }
    public void setDerecha(boolean derecha) { this.derecha = derecha; }
}
