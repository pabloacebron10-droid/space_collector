# 🚀 Space Collector - Arcade 2D

Proyecto académico de videojuego Arcade 2D desarrollado en **Java Swing** para la asignatura de **Desarrollo de Interfaces**. El proyecto implementa una arquitectura desacoplada y orientada a objetos donde las entidades del juego son modelos POJO puros renderizados en un `JPanel` mediante `Graphics2D`.

---

## 📋 Cumplimiento de la Rúbrica y Arquitectura

| Requisito Técnico | Implementación en el Proyecto |
| :--- | :--- |
| **Separación Modelo / Vista (Sin JLabel)** | `Jugador`, `Obstaculo`, `Coleccionable` y `Estrella` son clases POJO con coordenadas, velocidad y métodos de dibujo vectorial, sin heredar de componentes Swing. |
| **Ventana (`VentanaJuego`)** | Hereda de `JFrame`, dimensiones fijas (800x600 px), no redimensionable, centrada en pantalla (`setLocationRelativeTo(null)`) y cierre con `EXIT_ON_CLOSE`. |
| **Lienzo y Bucle (`PanelJuego`)** | Hereda de `JPanel`, contenedor del bucle principal mediante `javax.swing.Timer` a ~60 FPS (16 ms). Sobrescribe `paintComponent(Graphics g)` llamando a `super.paintComponent(g)`. |
| **Máquina de Estados** | Enum `EstadoJuego`: `INICIO`, `JUGANDO`, `GAMEOVER`, `VICTORIA`. |
| **Controles (Key Bindings)** | `InputMap` y `ActionMap` vinculados a `WHEN_IN_FOCUSED_WINDOW`. Soporta flechas del cursor y WASD. Maneja eventos Pressed/Released con banderas booleanas para movimiento continuo fluido sin repetición del SO. |
| **Física y Colisiones** | Movimiento limitado por los bordes de la pantalla. Detección de impacto mediante `Rectangle.intersects()`. Periodo de invulnerabilidad temporal al recibir daño con parpadeo visual. |
| **HUD y Calidad Visual** | Dibujo de vidas (corazones vectoriales), puntuación, cronómetro de tiempo transcurrido en el HUD. Renderizado con `Graphics2D` y antialiasing (`RenderingHints`). |
| **Reinicio limpio** | Método `reiniciarPartida()` que restablece vidas, puntuación, tiempo y entidades sin destruir ni reinstanciar el `Timer`. |

---

## 🎮 Controles y Mecánica de Juego

```text
       [ W ] o [ ↑ ]       -> Moverse hacia arriba
 [ A ] o [ ← ]   [ D ] o [ → ] -> Moverse a izquierda / derecha
       [ S ] o [ ↓ ]       -> Moverse hacia abajo

 [ ESPACIO ] -> Comenzar la partida desde el menú de inicio
 [ R ]       -> Reiniciar la partida (en pantallas de Game Over o Victoria)
```

1. **Objetivo:** Recoger los cristales de energía que flotan en el espacio exterior (+10 puntos cada uno).
2. **Victoria:** Alcanzar **100 puntos** recolectando 10 cristales.
3. **Peligro:** Esquivar los asteroides y meteoritos en trayectoria descendente.
4. **Vidas:** Inicias con **3 vidas**. Cada impacto resta una vida y activa 1 segundo de invulnerabilidad (la nave parpadea). Al llegar a 0 vidas, la nave queda destruida (**Game Over**).

---

## 🏛️ Estructura de Clases

```text
src/main/java/clases/
├── Main.java             # Punto de entrada (SwingUtilities.invokeLater)
├── VentanaJuego.java     # JFrame contenedor (800x600 px, no redimensionable)
├── PanelJuego.java       # JPanel con el Timer, Key Bindings, HUD y máquina de estados
├── EstadoJuego.java      # Enum con los estados { INICIO, JUGANDO, GAMEOVER, VICTORIA }
├── Jugador.java          # POJO de la nave: posición, velocidad, hitbox, invulnerabilidad
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

### 3. ¿Por qué los Key Bindings superan al `KeyListener` tradicional en Swing?
* **Problema del foco (`Focus`) en `KeyListener`:** Un `KeyListener` sólo recibe eventos si el componente específico tiene el foco activo del teclado (`hasFocus()`). Si el usuario hace clic fuera o se añade otro componente al contenedor, el `KeyListener` deja de responder silenciosamente.
* **Solución con `InputMap` y `WHEN_IN_FOCUSED_WINDOW`:** Los Key Bindings permiten enlazar combinaciones de teclas con un ámbito de ventana completa, garantizando que los controles respondan siempre sin importar qué componente interno tenga el foco.
* **Mapeo desacoplado (KeyStroke -> Nombre -> Action):** Permite reutilizar acciones (por ejemplo, asignar tanto la tecla `W` como `Flecha Arriba` a la misma acción de movimiento) y separar completamente la tecla física de la lógica que ejecuta.
* **Banderas booleanas (Pressed / Released):** Al activar una bandera con `KeyStroke` *pressed* y desactivarla con `KeyStroke` *released*, se logra un movimiento continuo y suave a 60 FPS, evitando el intervalo de retardo inicial ("delay") que impone el sistema operativo por defecto cuando se mantiene pulsada una tecla en un `KeyListener`.

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

