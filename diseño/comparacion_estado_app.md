# Comparación: estado actual de la app vs funcionamiento esperado (carpeta `diseño`)

## Alcance de la revisión
Se comparó el MVP Android actual con:
- `Reglamento_Juego_Adaptacion_Animal_V5_1.docx` (reglas v5.3).
- `Cartas de Adaptacion 2.0.xlsx`.
- `Cartas De Bioma .xlsx`.

## Funcionamiento esperado (según diseño)

### 1) Flujo principal de partida
El reglamento define una partida completa de 2 a 3 jugadores con:
1. Creación/edición de especies.
2. Fase de forrajeo (1D10 de comida).
3. Fase de depredación (incluye percepción, moneda y resolución de ataque/huida).
4. Fase de resolución (metabolismo, pérdida de individuos, extinción).
5. Fase de reproducción.
6. Fase de reposición y puntuación acumulada.

También define:
- Máximo 3 especies por jugador.
- Condición de término por 150 puntos o agotamiento de mazo.
- Sistema de estados (envenenado, paralizado, confundido, enfermedad, terror).
- Sistema de temperatura que modifica metabolismo en umbrales ±3 y ±5.

### 2) Datos de cartas esperados
- Adaptaciones: hoja con IDs 1..126 (con algunas filas repetidas de prueba/resumen en el archivo).
- Biomas: biomas principales 1..15 (hay una fila extra con valores `0` en la planilla).

## Estado actual de la app

### 1) Navegación/UI implementada
- Menú principal con botones: Jugar, Tienda, Configuración, Colección, Clasificación.
- Solo `Jugar` y `Colección` navegan a pantallas reales.
- Tienda/Configuración/Clasificación abren pantalla placeholder de "sección vacía por ahora (MVP)".

### 2) Juego (Play)
- `PlayActivity` muestra una pantalla estática con texto introductorio y resumen de reglas.
- No hay lógica de turnos, dados, moneda, combate, estados, ni puntuación en ejecución.

### 3) Colección
- La colección carga cartas desde `CardRepository` en un `RecyclerView`.
- Se incluyen cartas A1..A126 (adaptación) y B1..B15 (biomas) para visualización.
- No se usan en motor de juego (solo listado).

## Brechas detectadas (gap análisis)

### Brecha crítica: motor de juego ausente
Esperado: flujo completo por fases + reglas de combate/supervivencia/reproducción.
Actual: pantalla informativa sin mecánicas ejecutables.

### Brecha crítica: multijugador por turnos y estado de partida
Esperado: 2-3 jugadores, especies por jugador, puntajes y condición de victoria.
Actual: no existe modelo de partida/jugadores/turno/puntaje persistente.

### Brecha media: reglas de bioma y temperatura
Esperado: bioma activo y cambios de bioma por hitos (50/100 puntos), temperatura impactando metabolismo.
Actual: los biomas solo están como texto en cartas de colección.

### Brecha media: estados y efectos especiales
Esperado: aplicación/control de estados con límites y limpieza por muerte de individuos.
Actual: no hay sistema de estados, ni resolución de efectos especiales de cartas.

### Brecha baja: cobertura de datos
- Adaptaciones y biomas principales sí están cargados en la app para consulta.
- Falta validación automática de consistencia contra las planillas fuente.

## Recomendación de roadmap (orden sugerido)
1. **Modelo de dominio**: `GameState`, `Player`, `Species`, `TurnPhase`, `StatusEffect`, `Biome`.
2. **Motor por fases**: implementar forrajeo/depredación/resolución/reproducción/reposición.
3. **Reglas de combate**: percepción + moneda + velocidad + armadura/daño.
4. **Puntuación y fin de partida**: hitos 50/100/150 y agotamiento de mazo.
5. **UI de partida**: tablero de especies, zona de forrajeo, log de acciones.
6. **Validación de datos**: test que compare repositorio local vs archivos de diseño exportados.

## Conclusión
La app actual está correctamente posicionada como **MVP de navegación + visualización de cartas**, pero todavía **no implementa el funcionamiento jugable esperado por el reglamento v5.3**. El mayor valor inmediato está en construir el motor de turno y su modelo de estado antes de extender UI/UX.
