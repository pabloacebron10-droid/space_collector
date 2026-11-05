package clases;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Entidad modelo (POJO) que representa un cristal de energía coleccionable.
 * Otorga puntos al jugador al hacer contacto. Posee una animación de flotación
 * y pulsación de brillo estelar renderizada con Graphics2D.
 *
 * @author Pablo Acebrón
 */
public class Coleccionable {

    private double x;
    private double y;
    private final int ancho;
    private final int alto;
    private final int puntos;

    // Control de pulsación luminosa y flotación
    private double faseAnimacion;
    private static final Random RANDOM = new Random();

    public Coleccionable(int anchoPantalla, int altoPantalla) {
        this.ancho = 26;
        this.alto = 32;
        this.puntos = 10;
        this.faseAnimacion = RANDOM.nextDouble() * Math.PI * 2;
        reaparecer(anchoPantalla, altoPantalla);
    }

    /**
     * Reubica el cristal de forma aleatoria dentro de los límites jugables de la pantalla.
     */
    public void reaparecer(int anchoPantalla, int altoPantalla) {
        int margenX = 50;
        int margenYTop = 70;  // Espacio para no chocar con el HUD superior
        int margenYBottom = 60;

        this.x = margenX + RANDOM.nextInt(Math.max(1, anchoPantalla - margenX * 2 - ancho));
        this.y = margenYTop + RANDOM.nextInt(Math.max(1, altoPantalla - margenYTop - margenYBottom - alto));
    }

    /**
     * Avanza el ciclo de animación sinusoidal de flotación y aura.
     */
    public void actualizar() {
        faseAnimacion += 0.08;
    }

    /**
     * Retorna el rectángulo de colisión del cristal.
     */
    public Rectangle getHitbox() {
        return new Rectangle((int) x, (int) y, ancho, alto);
    }

    /**
     * Renderiza el cristal de energía con destellos, aura radial y facetas luminosas.
     */
    public void dibujar(Graphics2D g2d) {
        // Cálculo de oscilación vertical suave (flotación)
        int offsetY = (int) (Math.sin(faseAnimacion) * 4);
        int px = (int) x;
        int py = (int) y + offsetY;

        // 1. Resplandor / Halo de energía exterior (pulsación)
        float factorBrillo = 0.6f + (float) (Math.sin(faseAnimacion * 1.5) * 0.35);
        int haloRadio = ancho + 8;
        Point2D center = new Point2D.Float(px + ancho / 2f, py + alto / 2f);
        float[] dist = { 0.0f, 0.6f, 1.0f };
        Color[] colors = {
                new Color(0, 255, 200, (int) (140 * factorBrillo)),
                new Color(0, 200, 255, (int) (60 * factorBrillo)),
                new Color(0, 100, 255, 0)
        };
        RadialGradientPaint halo = new RadialGradientPaint(center, haloRadio, dist, colors);
        g2d.setPaint(halo);
        g2d.fillOval((int) center.getX() - haloRadio, (int) center.getY() - haloRadio, haloRadio * 2, haloRadio * 2);

        // 2. Cristal central en forma de diamante / gema hexagonal facetada
        Polygon facetaIzquierda = new Polygon(
                new int[]{ px + ancho / 2, px, px + ancho / 2 },
                new int[]{ py, py + alto / 2, py + alto },
                3
        );
        GradientPaint gradIzq = new GradientPaint(
                px, py, new Color(0, 255, 220),
                px + ancho / 2f, py + alto, new Color(0, 140, 210)
        );
        g2d.setPaint(gradIzq);
        g2d.fillPolygon(facetaIzquierda);

        Polygon facetaDerecha = new Polygon(
                new int[]{ px + ancho / 2, px + ancho, px + ancho / 2 },
                new int[]{ py, py + alto / 2, py + alto },
                3
        );
        GradientPaint gradDer = new GradientPaint(
                px + ancho, py, new Color(180, 255, 255),
                px + ancho / 2f, py + alto, new Color(0, 180, 255)
        );
        g2d.setPaint(gradDer);
        g2d.fillPolygon(facetaDerecha);

        // 3. Faceta central brillante y reflejos de luz
        Polygon nucleoLuz = new Polygon(
                new int[]{ px + ancho / 2, px + ancho / 3, px + ancho / 2, px + (ancho * 2) / 3 },
                new int[]{ py + 4, py + alto / 2, py + alto - 6, py + alto / 2 },
                4
        );
        g2d.setColor(new Color(240, 255, 255, 210));
        g2d.fillPolygon(nucleoLuz);

        // 4. Borde perimetral nítido
        g2d.setColor(new Color(255, 255, 255, 230));
        Polygon silueta = new Polygon(
                new int[]{ px + ancho / 2, px, px + ancho / 2, px + ancho },
                new int[]{ py, py + alto / 2, py + alto, py + alto / 2 },
                4
        );
        g2d.drawPolygon(silueta);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    public int getPuntos() { return puntos; }
}
