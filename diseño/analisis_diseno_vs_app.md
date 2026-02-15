# Análisis diseño vs implementación actual

## Alcance revisado
- `diseño/Reglamento_Juego_Adaptacion_Animal_V5_1.docx`
- `diseño/Ejemplo UI principal de juego.docx`
- `diseño/Cartas de Adaptacion 2.0.xlsx`
- `diseño/Cartas De Bioma .xlsx`

## Requisitos de diseño detectados
1. **Partida de 2 a 3 jugadores**.
2. **Creación de especie**: 2 cartas (al menos 1 mandíbula), máximo 3 especies por jugador.
3. **Capacidad de cartas por especie**: una especie puede tener tantas cartas no mandíbula como fichas de individuo.
4. **Fase de forrajeo**: comen herbívoros/omnívoros por velocidad, desempate por suma de atributos.
5. **Fase de depredación**: atacan carnívoros/omnívoros; objetivo según habilidades de adaptación.
6. **Resolución**: consumo de metabolismo, pérdida de individuos por hambre/daño, extinción.
7. **Reproducción**: con comida >= fertilidad, +1 individuo y se agrega **slot para carta extra**.
8. **Estados**: envenenado, paralizado, confundido, enfermedad, terror (máximo 2 activos por especie).
9. **Fin de partida**: cuando se acaba el mazo de un jugador o alguien llega a 150 puntos.
10. **Puntaje adicional**: 10 pts por especie no extinta y 4 pts por especie extinguida por depredación.
11. **Cartas de adaptación**: incluyen metadatos de diseño como tipo de alimentación, objetivo de ataque, stats y habilidad.
12. **Cartas de bioma**: modificadores de atributos + temperatura.

## Implementado en la app (sí está)
- Flujo base por fases: jugador, forrajeo, depredación, resolución, reproducción y reposición.
- Límite de 3 especies por jugador y creación con 2 cartas incluyendo mandíbula.
- Forrajeo por velocidad y desempate por atributos.
- Selección de objetivo de ataque por reglas de mandíbula (parte del diseño).
- Consumo de metabolismo en resolución y pérdida de individuos/extinción.
- Reposición hasta 5 cartas.
- Cambio de bioma activo en 0/50/100 puntos.
- UI principal de juego con zonas de mazos, especies, forrajeo, log y mano.

## Faltantes o parciales respecto al diseño
1. **Cantidad de jugadores fija en 3**: no hay modo de partida de 2 jugadores.
2. **Slots dinámicos de adaptación por especie**:
   - El diseño pide crecer slots no mandíbula con individuos/reproducción.
   - En la app solo se crea especie con 2 cartas y se puede reemplazar una no mandíbula, pero no se agrega slot por reproducción.
3. **Estados de especie no implementados**:
   - No hay motor para envenenado/paralizado/confundido/enfermedad/terror.
   - Tampoco límite de 2 estados ni limpieza al morir individuo.
4. **Habilidades de cartas sin ejecución real**:
   - Existe catálogo de habilidades de diseño, pero solo se muestran en detalle.
   - No hay efectos activos (robar/descartar, ignorar armadura, aplicar estados, bonificaciones contextuales, etc.).
5. **Atributos simplificados**:
   - Ataque/velocidad/armadura/percepción se calculan por conteo de tipo de carta, no por stats de cada carta de diseño.
   - Fertilidad se fuerza a `individuos`, sin considerar bonificadores de cartas/bioma.
6. **Fin de partida no implementado**:
   - No se corta automáticamente en 150 puntos ni cuando un mazo llega a 0.
7. **Puntaje incompleto vs reglamento**:
   - No se suman 10 puntos por especie no extinta.
   - No se suman 4 puntos por especie extinguida por depredación.
8. **Reglas de depredación parcialmente simplificadas**:
   - No se ve control de varias restricciones/efectos de habilidad más allá del modo de ataque y objetivo por mandíbula.
9. **UI de interacción para jugadas avanzadas**:
   - Falta una interfaz para gestionar estados, resolver habilidades activas y seleccionar objetivos complejos según texto de carta.
10. **Metadatos de diseño no visibles en colección**:
   - En colección se muestra id/nombre/tipo/rareza/descripcion, pero no alimentación, objetivo de ataque, stats de diseño ni habilidad completa por carta.

## Conclusión rápida
La app ya cubre bien el **MVP del flujo principal** y la estructura visual base del tablero, pero todavía falta implementar el bloque de reglas avanzadas del diseño: **estados**, **habilidades activas de cartas**, **slots de adaptación por reproducción**, **fin de partida formal**, y **puntuación completa del reglamento**.
