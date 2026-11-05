package clases;

/**
 * Representa los diferentes estados de la máquina de estados del juego.
 * Permite controlar el flujo de la partida y qué pantalla debe renderizarse.
 *
 * @author Pablo Acebrón
 */
public enum EstadoJuego {
    /** Pantalla inicial de bienvenida antes de comenzar a jugar */
    INICIO,
    
    /** Partida en curso: lógica de movimiento, colisiones y tiempo activa */
    JUGANDO,
    
    /** Fin de la partida por pérdida de todas las vidas */
    GAMEOVER,
    
    /** Victoria alcanzada al conseguir la puntuación objetivo */
    VICTORIA
}
