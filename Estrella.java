package clases;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Entidad de apoyo visual que representa una estrella del fondo espacial.
 * Proporciona un efecto de paralaje multicapa y titileo luminoso.
 */
public class Estrella {

    private double x;
    private double y;
    private final double velocidad;
    private final int tamano;
    private final Color color;
    private double faseTitileo;

    private static final Random RANDOM = new Random();

    public Estrella(int anchoPantalla, int altoPantalla) {
        this.x = RANDOM.nextInt(anchoPantalla);
        this.y = RANDOM.nextInt(altoPantalla);
        this.velocidad = 0.5 + RANDOM.nextDouble() * 2.2; // Diferentes velocidades para efecto paralaje
        this.tamano = (velocidad > 1.8) ? 3 : (velocidad > 1.0 ? 2 : 1);
        this.faseTitileo = RANDOM.nextDouble() * Math.PI * 2;

        // Tonos cósmicos: blanco puro, azul pálido, amarillo o cian suave
        int tipo = RANDOM.nextInt(4);
        switch (tipo) {
            case 0 -> this.color = new Color(255, 255, 255);
            case 1 -> this.color = new Color(180, 220, 255);
            case 2 -> this.color = new Color(255, 250, 190);
            default -> this.color = new Color(170, 255, 240);
        }
    }

    public void actualizar(int anchoPantalla, int altoPantalla) {
        y += velocidad;
        faseTitileo += 0.05;
        if (y > altoPantalla) {
            y = 0;
            x = RANDOM.nextInt(anchoPantalla);
        }
    }

    public void dibujar(Graphics2D g2d) {
        int alfa = 160 + (int) (Math.sin(faseTitileo) * 70);
        if (alfa < 70) alfa = 70;
        if (alfa > 255) alfa = 255;

        g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alfa));
        g2d.fillOval((int) x, (int) y, tamano, tamano);
    }
}
