# 🚀 Space Collector - Arcade 2D

Proyecto académico de videojuego Arcade 2D desarrollado en **Java Swing** para la asignatura de **Desarrollo de Interfaces**. El proyecto implementa una arquitectura desacoplada y orientada a objetos donde las entidades del juego son modelos POJO puros renderizados en un `JPanel` mediante `Graphics2D`, gobernados a través de un panel de mandos con **JButtons**.

---

## 📋 Cumplimiento de la Rúbrica y Arquitectura

| Requisito Técnico | Implementación en el Proyecto |
| :--- | :--- |
| **Manejo de la Nave con JButtons (Rúbrica)** | La nave se pilota **exclusivamente mediante JButtons creados** (`▲ ARRIBA`, `▼ ABAJO`, `◄ IZQUIERDA`, `DERECHA ►`). Las flechas del teclado han sido desactivadas por especificación de la rúbrica. |
| **Separación Modelo / Vista (Sin JLabel)** | `Jugador`, `Obstaculo`, `Coleccionable` y `Estrella` son clases POJO con coordenadas, velocidad y métodos de dibujo vectorial, sin heredar de componentes Swing. |
| **Ventana (`VentanaJuego`)** | Hereda de `JFrame`, dimensiones fijas con `BorderLayout` (Lienzo al Centro + Mando de JButtons al Sur), no redimensionable, centrada en pantalla (`setLocationRelativeTo(null)`) y cierre con `EXIT_ON_CLOSE`. |
| **Lienzo y Bucle (`PanelJuego`)** | Hereda de `JPanel`, contenedor del bucle principal mediante `javax.swing.Timer` a ~60 FPS (16 ms). Sobrescribe `paintComponent(Graphics g)` llamando a `super.paintComponent(g)`. |
| **Máquina de Estados** | Enum `EstadoJuego`: `INICIO`, `JUGANDO`, `GAMEOVER`, `VICTORIA`. |
| **Panel de Mando (JButtons)** | Botones estilizados con diseño arcade retro-futurista dispuestos en cruceta (D-Pad). Implementan `MouseListener` para responder con un paso instantáneo al hacer clic y con desplazamiento continuo fluido a 60 FPS al mantener pulsado. `setFocusable(false)` evita interferencias de foco. |
| **Física y Colisiones** | Movimiento restringido a los bordes de la pantalla. Detección de impacto mediante `Rectangle.intersects()`. Periodo de invulnerabilidad temporal al recibir daño con parpadeo visual. |
| **HUD y Calidad Visual** | Dibujo de vidas (corazones vectoriales), puntuación, cronómetro de tiempo transcurrido en el HUD superior. Renderizado con `Graphics2D` y antialiasing (`RenderingHints`). |
| **Reinicio limpio** | Método `reiniciarPartida()` y botón `🔄 REINICIAR` que restablece vidas, puntuación, tiempo y entidades sin destruir ni reinstanciar el `Timer`. |

---

## 🎮 Controles y Mecánica de Juego

```text
       Panel de Mando Inferior (JButtons):
                 [ ▲ ARRIBA ]
   [ ◄ IZQUIERDA ] [ ▼ ABAJO ] [ DERECHA ► ]

  [ ▶ INICIAR MISIÓN ] -> Comenzar la partida desde el menú de inicio
  [ 🔄 REINICIAR ]     -> Reiniciar la partida en cualquier momento
```

> **Nota de rúbrica:** Las flechas del teclado y WASD han sido eliminadas para asegurar que el control de la nave se realice únicamente mediante los `JButton` creados.

1. **Objetivo:** Recoger los cristales de energía que flotan en el espacio exterior (+10 puntos cada uno).
2. **Victoria:** Alcanzar **100 puntos** recolectando 10 cristales.
3. **Peligro:** Esquivar los asteroides y meteoritos en trayectoria descendente.
4. **Vidas:** Inicias con **3 vidas**. Cada impacto resta una vida y activa 1 segundo de invulnerabilidad (la nave parpadea). Al llegar a 0 vidas, la nave queda destruida (**Game Over**).

---

## 🏛️ Estructura de Clases

```text
src/main/java/clases/
├── Main.java             # Punto de entrada (SwingUtilities.invokeLater)
├── VentanaJuego.java     # JFrame contenedor (BorderLayout: Juego + Panel de JButtons)
├── PanelJuego.java       # JPanel con el Timer, HUD, máquina de estados y creación de los JButtons
├── EstadoJuego.java      # Enum con los estados { INICIO, JUGANDO, GAMEOVER, VICTORIA }
├── Jugador.java          # POJO de la nave: posición, velocidad, hitbox, invulnerabilidad y paso incremental
├── Obstaculo.java        # POJO de meteoritos: física descendente, rotación, rebote lateral
├── Coleccionable.java    # POJO de cristales: flotación senoidal, halo de energía y puntuación
└── Estrella.java         # Entidad visual: partículas de fondo con efecto de paralaje multicapa
```

---

## 💡 Fundamentos Teóricos de Desarrollo de Interfaces

### 1. ¿Por qué el Jugador no debe heredar de `JPanel` ni usar `JLabel`?
En las introducciones básicas a Swing es habitual ver ejemplos donde cada personaje es un `JLabel` con un icono, moviéndose mediante `setLocation(x, y)`. Aunque es intuitivo para principiantes, es un **grave antipatrón en desarrollo de interfaces**:
* **Sobrecarga de recursos (Overhead):** Un `JPanel` o `JLabel` es un componente de interfaz pesada (`JComponent`) que gestiona jerarquías de foco, bordes, layouts, accesibilidad, listeners de ratón y búferes de pintado propios. Para un juego con decenas de entidades móviles (disparos, enemigos, partículas), el consumo de memoria y CPU se dispara.
* **Separación de responsabilidades:** La nave es un objeto del **Modelo** (datos cartesianos, velocidad, colisión). No debe mezclarse con la **Vista** de la arquitectura de la ventana.
* **Flexibilidad gráfica:** Un POJO renderizado directamente en `Graphics2D` permite rotaciones precisas, gradientes, escalado, máscaras alfa y efectos de partículas que son imposibles o extremadamente lentos si se usan componentes Swing.

### 2. ¿Por qué el dibujo va en `paintComponent` y la lógica de puntuación NO debe ejecutarse allí?
* El método `paintComponent(Graphics g)` es invocado de forma **asíncrona y no determinista** por el sistema de ventanas de Java (el hilo EDT). Puede ejecutarse cuando la ventana se desenfoca, se minimiza, se superpone otra ventana o el sistema operativo fuerza un redibujado.
* Si incrementáramos la puntuación o restáramos vidas dentro de `paintComponent`, la lógica del juego dependería de cuántas veces el sistema operativo decide redibujar el panel (por ejemplo, al mover el ratón encima o mover la ventana), rompiendo por completo la consistencia de la partida.
* La lógica debe residir de manera pura en el método de actualización (`actualizarLogica()`) sincronizado por el reloj a intervalos fijos (`javax.swing.Timer`), y `paintComponent` debe limitarse estrictamente a **leer el estado actual y dibujarlo**.

### 3. ¿Cómo implementar el control mediante `JButton` para movimiento fluido en Swing?
* **Gestión de eventos con `MouseListener`:** A diferencia de `ActionListener` (que sólo se dispara al liberar el botón tras hacer clic), `MouseListener` permite capturar los eventos `mousePressed`, `mouseReleased` y `mouseExited`.
* **Paso discreto vs. Movimiento continuo:** Al pulsar (`mousePressed`), se aplica un desplazamiento instantáneo a la nave para responder con inmediatez al clic, y a la vez se activa la bandera booleana de dirección. Mientras el botón permanezca pulsado, el bucle del `Timer` a 60 FPS desplaza la nave de forma fluida. Al soltar (`mouseReleased`) o sacar el cursor del botón (`mouseExited`), la nave se detiene de forma instantánea.
* **Desacoplamiento de foco (`setFocusable(false)`):** Los `JButton` por defecto capturan el foco del teclado al ser pulsados. Estableciendo `setFocusable(false)` se evita que el foco quede retenido en los botones y se elimina el borde punteado de foco de Swing, logrando una estética limpia y profesional.

---

## 🚀 Cómo Ejecutar

Desde la raíz del proyecto (`space_collector`):

```bash
mvn compile
mvn exec:java -Dexec.mainClass="clases.Main"
```
O directamente ejecutando la clase `Main.java` desde tu IDE favorito (VS Code, IntelliJ IDEA, NetBeans, Eclipse).

---

## 👨‍💻 Autor

**Pablo Acebrón**

Proyecto académico de **Java Swing** para la asignatura de **Desarrollo de Interfaces**.
