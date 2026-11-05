package clases;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.Random;

/**
 * Entidad modelo (POJO) que representa un meteorito que desciende por la pantalla.
 * Puede descender de forma recta o rebotar suavemente en las paredes laterales.
 * Dispone de rotación geométrica y renderizado rocoso mediante Graphics2D.
 *
 * @author Pablo Acebrón
 */
public class Obstaculo {

    private double x;
    private double y;
    private double vx;
    private double vy;
    private final int radio;

    // Ángulo y velocidad de giro para dinamismo visual
    private double anguloRotacion;
    private final double velocidadRotacion;

    // Vértices de un polígono irregular que le da aspecto de asteroide rocoso
    private final Polygon formaRocosa;
    private final int[] crateresX;
    private final int[] crateresY;
    private final int[] crateresRadio;

    private static final Random RANDOM = new Random();

    public Obstaculo(int anchoPantalla, int altoPantalla) {
        this.radio = 18 + RANDOM.nextInt(16); // Radio entre 18 y 33 px
        this.velocidadRotacion = (RANDOM.nextDouble() - 0.5) * 0.08;
        this.anguloRotacion = RANDOM.nextDouble() * Math.PI * 2;

        // Generar vértices irregulares relativos al centro (0, 0)
        int numPuntos = 8 + RANDOM.nextInt(4);
        int[] vxPuntos = new int[numPuntos];
        int[] vyPuntos = new int[numPuntos];
        for (int i = 0; i < numPuntos; i++) {
            double angle = (2 * Math.PI / numPuntos) * i;
            double r = radio * (0.75 + RANDOM.nextDouble() * 0.45);
            vxPuntos[i] = (int) (Math.cos(angle) * r);
            vyPuntos[i] = (int) (Math.sin(angle) * r);
        }
        this.formaRocosa = new Polygon(vxPuntos, vyPuntos, numPuntos);

        // Cráteres decorativos
        int numCrateres = 2 + RANDOM.nextInt(3);
        this.crateresX = new int[numCrateres];
        this.crateresY = new int[numCrateres];
        this.crateresRadio = new int[numCrateres];
        for (int i = 0; i < numCrateres; i++) {
            crateresX[i] = (int) ((RANDOM.nextDouble() - 0.5) * (radio * 0.8));
            crateresY[i] = (int) ((RANDOM.nextDouble() - 0.5) * (radio * 0.8));
            crateresRadio[i] = 3 + RANDOM.nextInt(5);
        }

        reaparecer(anchoPantalla, altoPantalla);
        // Distribución inicial aleatoria vertical para que no aparezcan todos juntos arriba al iniciar
        this.y = -radio - RANDOM.nextInt(altoPantalla);
    }

    /**
     * Reubica el obstáculo por encima del límite superior con nueva velocidad.
     */
    public void reaparecer(int anchoPantalla, int altoPantalla) {
        this.x = RANDOM.nextInt(Math.max(1, anchoPantalla - radio * 2)) + radio;
        this.y = -radio - (RANDOM.nextInt(150) + 20);

        // Velocidad vertical (entre 2.5 y 5.5 px/frame)
        this.vy = 2.5 + (RANDOM.nextDouble() * 3.0);

        // Velocidad horizontal suave para balanceo o rebote lateral
        this.vx = (RANDOM.nextDouble() - 0.5) * 2.8;
    }

    /**
     * Actualiza la posición y la rotación. Rebota suavemente si toca un lateral.
     */
    public void actualizar(int anchoPantalla, int altoPantalla) {
        x += vx;
        y += vy;
        anguloRotacion += velocidadRotacion;

        // Rebote lateral suave en los bordes de la pantalla
        if (x - radio < 0) {
            x = radio;
            vx = Math.abs(vx);
        } else if (x + radio > anchoPantalla) {
            x = anchoPantalla - radio;
            vx = -Math.abs(vx);
        }

        // Si sale por debajo de la pantalla, reaparece en la parte superior
        if (y - radio > altoPantalla) {
            reaparecer(anchoPantalla, altoPantalla);
        }
    }

    /**
     * Retorna el rectángulo de colisión envolvente ajustado al tamaño del asteroide.
     */
    public Rectangle getHitbox() {
        int margen = (int) (radio * 0.2); // Reducción ligera para ajustar a la forma poligonal
        return new Rectangle(
                (int) (x - radio + margen),
                (int) (y - radio + margen),
                (radio - margen) * 2,
                (radio - margen) * 2
        );
    }

    /**
     * Dibuja el asteroide rotado con sombreado rocoso y cráteres mediante Graphics2D.
     */
    public void dibujar(Graphics2D g2d) {
        AffineTransform transformOriginal = g2d.getTransform();

        // Trasladar al centro del asteroide y rotar
        g2d.translate(x, y);
        g2d.rotate(anguloRotacion);

        // Gradiente rocoso con relieve de luz cenital
        GradientPaint gradRoca = new GradientPaint(
                -radio, -radio, new Color(160, 110, 85),
                radio, radio, new Color(55, 35, 30)
        );
        g2d.setPaint(gradRoca);
        g2d.fillPolygon(formaRocosa);

        // Borde exterior rocoso
        g2d.setColor(new Color(40, 25, 20));
        g2d.drawPolygon(formaRocosa);

        // Dibujo de cráteres
        for (int i = 0; i < crateresX.length; i++) {
            g2d.setColor(new Color(45, 28, 22, 190));
            g2d.fillOval(crateresX[i] - crateresRadio[i], crateresY[i] - crateresRadio[i],
                    crateresRadio[i] * 2, crateresRadio[i] * 2);

            // Borde brillante del cráter (luz de impacto)
            g2d.setColor(new Color(190, 150, 120, 120));
            g2d.drawArc(crateresX[i] - crateresRadio[i], crateresY[i] - crateresRadio[i],
                    crateresRadio[i] * 2, crateresRadio[i] * 2, 45, 180);
        }

        g2d.setTransform(transformOriginal);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getRadio() { return radio; }
}
