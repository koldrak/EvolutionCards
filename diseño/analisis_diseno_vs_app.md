# Análisis: diseño vs estado actual de la app

## Fuentes revisadas
- `diseño/Reglamento_Juego_Adaptacion_Animal_V5_1.docx`
- `diseño/Cartas de Adaptacion 2.0.xlsx`
- `diseño/Cartas De Bioma .xlsx`
- `diseño/Ejemplo UI principal de juego.docx`
- Código actual en `app/src/main/java/com/daille/evolutioncards/`

## Resumen rápido
La app ya implementa el loop principal de partida (creación de especies, forrajeo, depredación, resolución, reproducción y reposición), y además una UI de tablero cercana al mockup.

Sigue faltando una parte importante de reglas finas y, sobre todo, cobertura completa de habilidades de cartas.

## Qué sí está alineado con el diseño
- 2–3 jugadores.
- Máximo 3 especies por jugador.
- Crear especie con 2 cartas y al menos 1 mandíbula.
- Forrajeo por orden de velocidad y desempate por suma de atributos.
- Depredación con lógica de percepción + resultado (emboscada/huida con ventaja/huida).
- Fases de resolución, reproducción y reposición.
- Final de partida por 150 puntos o agotamiento de mazo.
- Biomas revelados y avance de bioma activo por umbrales de score (50 / 100).
- Regla de estados con límite de 2 y máximo 1 estado nuevo aplicado por ronda por especie.
- Limpieza de estados cuando una especie pierde individuo.
- Tablero con mazos laterales, zona de forrajeo, log y mano del jugador (similar al ejemplo UI).

## Brechas funcionales principales (pendientes)

### 1) Habilidades especiales de cartas: cobertura todavía parcial
Aunque existe integración de metadatos de diseño para cartas 1–126 y se aplican algunos efectos puntuales (forrajeo, ataques, on-play, temperatura seleccionable, algunos estados), la cobertura no es completa para todas las habilidades descritas en diseño.

Falta implementar una porción relevante de habilidades condicionales por bioma, disparadores de ataque/defensa específicos y efectos complejos “al jugar”.

### 2) Puntuación y timing de puntaje todavía simplificados frente al reglamento
Actualmente se suman puntos en varios momentos (por ejemplo ataque exitoso, reproducción y reposición), lo cual puede diferir del flujo textual del reglamento si este espera un corte más estricto por cierre de turno con desglose fijo.

### 3) UX de juego físico no replicada al 100%
- El azar (dado/moneda) se resuelve por RNG interno en vez de interacción manual.
- No hay manipulación visual tipo stack físico de cartas en especie.
- No hay drag & drop ni gestión visual de pozo/descarte como componente táctil explícito.

### 4) Módulos del menú principal aún en placeholder
Tienda, configuración y ranking siguen como navegación a pantalla placeholder, sin funcionalidad real.

## Conclusión
Sí: todavía hay contenido/instrucciones de diseño que no están implementados en el código actual.

El gap principal no está en el esqueleto de turnos (que ya existe), sino en:
1) completar el motor de habilidades de cartas,
2) cerrar la especificación exacta de scoring,
3) elevar UX para parecerse más al flujo físico,
4) implementar módulos de menú fuera del modo partida.
