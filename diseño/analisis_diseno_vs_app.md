# Análisis: diseño vs estado actual de la app

## Fuentes revisadas
- `diseño/Reglamento_Juego_Adaptacion_Animal_V5_1.docx`
- `diseño/Cartas de Adaptacion 2.0.xlsx`
- `diseño/Cartas De Bioma .xlsx`
- `diseño/Ejemplo UI principal de juego.docx`

## Resumen rápido
La app ya implementa el esqueleto principal de partida (creación de especies, forrajeo, depredación, resolución, reproducción y reposición) y una UI de tablero bastante cercana al mockup. Sin embargo, todavía faltan varias reglas finas del reglamento y gran parte de las habilidades especiales de cartas.

## Qué sí está alineado con el diseño
- 2–3 jugadores.
- Máximo 3 especies por jugador.
- Crear especie con 2 cartas y al menos 1 mandíbula.
- Forrajeo por orden de velocidad y desempate por suma de atributos.
- Depredación con lógica de percepción + resultado (emboscada/huida con ventaja/huida).
- Fases de resolución, reproducción y reposición.
- Final de partida por 150 puntos o agotamiento de mazo.
- Tablero con mazos laterales, zona de forrajeo, log y mano del jugador (similar al ejemplo UI).

## Brechas funcionales principales

### 1) Habilidades especiales de cartas: cobertura parcial
En diseño hay muchas cartas con texto de habilidad (activas por bioma, al jugar, por ataque exitoso/fallido, etc.), pero en app solo está implementado un subconjunto pequeño:
- Estados por ataque para A5, A6/A12 y efecto aleatorio de A14.
- Reglas de selección de objetivo para un subconjunto de mandíbulas.
- Un caso parcial de temperatura seleccionable para A30/A34/A60.

Falta implementar la mayor parte de habilidades de las 126 cartas de adaptación (incluyendo bonus situacionales por bioma y efectos al jugar carta).

### 2) Estados: faltan restricciones del reglamento
El reglamento indica “un estado aplicado por turno” y que los estados “se eliminan cuando muere un individuo”.
Actualmente:
- Sí hay máximo 2 estados.
- No se controla explícitamente “máximo 1 estado nuevo por turno”.
- No se limpian estados al perder individuo.

### 3) Flujo de biomas simplificado
El diseño de reglamento habla de revelar 3 biomas al inicio y cambiar el activo a 50 y 100 puntos.
La app usa los 3 primeros del deck de biomas barajado para cambios por score, pero no muestra una mecánica de “revelado” ni visualización explícita de biomas futuros.

### 4) Puntuación y timing de puntaje distinto al texto de reglamento
Se otorgan puntos en varios momentos (por ejemplo, al atacar exitosamente se suman puntos inmediatos según ataque), además de la reposición.
Esto puede diferir del esquema textual de “sumar puntos del turno” con desglose final por comida/cartas/individuos/especies.

### 5) Diferencias de UX respecto al diseño de juego físico
- No hay interacción manual para lanzar dado o moneda (se resuelve por RNG interno).
- No hay manipulación visual tipo “stack al 50%” de cartas en especie (se representa en texto y paneles).
- No hay arrastrar/soltar ni gestión visual de descarte/pozo.

### 6) Módulos del menú principal aún en placeholder
Tienda, configuración y ranking existen como navegación, pero no tienen funcionalidad real.

## Conclusión
La app está en un buen MVP jugable del loop principal, pero para quedar “alineada al diseño” faltan sobre todo:
1) motor completo de habilidades de cartas,
2) cerrar reglas finas de estados y puntaje,
3) enriquecer UX de biomas/azar/stack de cartas,
4) construir los módulos no jugables del menú.
