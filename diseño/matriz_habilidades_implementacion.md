# Matriz de implementación de habilidades (A1–A126)

Formato compatible para revisión en PR (texto plano renderizable).

| ID | Nombre | Estado | Habilidad diseno | Observaciones |
| --- | --- | --- | --- | --- |
| A1 | Mandíbula Trituradora | Implementada | Obtiene más 1 de alimento cada vez que un individuo es eliminado. \| En Sabana: +1 Ataque durante forrajeo y ataques. |  |
| A2 | Colmillos Serrados | Implementada | Al atacar: ignora la armadura de la presa en ese ataque. |  |
| A3 | Pico Cortante | Implementada | En Montaña: +1 Ataque durante forrajeo y ataques. |  |
| A4 | Mandíbula Canina | Implementada | Al jugarla: roba 1 carta y luego descarta 1 carta. |  |
| A5 | Mordida Venenosa | Implementada | Si el ataque es exitoso: el objetivo queda Envenenado. |  |
| A6 | Mandíbula Proyectable | Implementada | Si ataca con éxito el defensor queda paralizado |  |
| A7 | Dientes de Aguja | Implementada | En Arrecife: +1 Ataque durante forrajeo y ataques. |  |
| A8 | Mandíbula Filtradora | Implementada | Esta especie obtiene 1 comida sin depender de la zona de forrageo en cada turno |  |
| A9 | Pico Perforador | Sin habilidad en diseño |  |  |
| A10 | Mandíbula Omnívora | Implementada | En Bosque: +1 Ataque |  |
| A11 | Molares Trituradores | Sin habilidad en diseño |  |  |
| A12 | Boca Succionadora | Implementada | Si ataca con éxito el defensor queda paralizado |  |
| A13 | Colmillos Curvos | Implementada | En Sabana: +1 Ataque |  |
| A14 | Mandíbula infecciosa | Implementada | Si el ataque es exitoso: aplica un estado aleatorio a la especie objetivo. |  |
| A15 | Mandíbula con colmillos | Implementada | al atacar obtiene +1 de ataque |  |
| A16 | Pelaje Espeso | Implementada | En Glaciares: +1 Armadura. |  |
| A17 | Pelaje Impermeable | Parcial | Al jugarla: mira la mano de un rival y elige 1 carta; ese rival la descarta. \| En Tundra: +1 Armadura. | Descarta carta rival al azar, no elegida. |
| A18 | Plumas Aislantes | Parcial | +1 de velocidad si tiene una \| En Glaciares: +1 Armadura. | Falta aplicar condicion "si tiene una" de velocidad. |
| A19 | Caparazón Óseo | Implementada | Si recibe 0 de daño tras un ataque el atacante queda aturdido |  |
| A20 | Exoesqueleto Quitinoso | Implementada | En Montaña: +1 Armadura. |  |
| A21 | Escamas Reforzadas | Implementada | En Manglar: +1 Armadura. |  |
| A22 | Piel Tóxica | Implementada | Si es dañado por un ataque el atacante queda envenenado. \| En Jungla: +1 Armadura. |  |
| A23 | Piel Gruesa | Implementada | En Sabana: +1 Armadura. |  |
| A24 | Patrón de Camuflaje | Implementada | En Jungla: +1 Armadura. |  |
| A25 | Cromatogoros | Implementada | En Desierto: +1 Armadura. |  |
| A26 | Piel Elástica | Implementada | obtiene +1 de velocidad en playa y arrecife |  |
| A27 | Espinas Dérmicas | Implementada | Si es dañado por un ataque el atacante recibe 1 de daño \| En Montaña Fría: +1 Armadura. |  |
| A28 | Escamas Plateadas | Parcial | Al jugarla: elige una especie rival; queda Confundida. | Objetivo rival se elige aleatoriamente, no por jugador. |
| A29 | Piel Rugosa | Sin habilidad en diseño |  |  |
| A30 | Cobertura Estacional | Parcial | al jugarla el usuario selecciona si quiere subir o bajar la temperatura de la especie en 1 \| En Tundra: +1 Armadura. | Cambio de temperatura +/-1 se decide aleatorio, no por jugador. |
| A31 | Patas Saltadoras | Implementada | Al jugarla: roba 1 carta y luego descarta 1 carta. |  |
| A32 | Garras Retráctiles | No implementada | Al jugarla: intercambia 2 cartas de tu mano con 2 cartas al azar de la mano de un rival. \| En Montaña: +1 Velocidad. | No hay implementación específica del intercambio de 2x2. |
| A33 | Aletas Potentes | Implementada | manglares y arrecife +1 de velocidad |  |
| A34 | Patas Excavadoras | Parcial | al jugarla el usuario selecciona si quiere subir o bajar la temperatura de la especie en 2 \| En Estepa: +1 Velocidad. | Cambio de temperatura +/-2 se decide aleatorio, no por jugador. |
| A35 | Membrana Planeadora | Implementada | En Taiga: +1 Velocidad. |  |
| A36 | Alas Batientes | No implementada | + 2 de ataque contra especies con alas. \| En Montaña: +1 Velocidad. | No hay bonificación +2 ataque contra especies con alas. |
| A37 | Patas Largas | Parcial | Al jugarla: elige una especie; aumenta su fertilidad en +1 (permanente). | Especie objetivo para fertilidad se elige aleatoria. |
| A38 | Almohadillas Adhesivas | Implementada | +2 de velocidad en jungla y montaña. \| En Montaña: +1 Velocidad. |  |
| A39 | Extremidades Prensiles | Implementada | +2 de velocidad en jungla y bosque. \| En Jungla: +1 Velocidad. |  |
| A40 | Tentáculos | Parcial | Al jugarla: cambia 1 adaptación de una especie por 1 de tu mano. | Intercambio de adaptación se resuelve de forma aleatoria. |
| A41 | Aleta Dorsal Estabilizadora | Parcial | Obtiene +1 de velocidad en manglares, playa y arrecife. -1 en otros biomas | El -1 en otros biomas depende del parser genérico, no regla explícita. |
| A42 | Patas Anchas | Implementada | +2 de velocidad en taiga, estepa, tundras, montaña fría y glaciares \| En Glaciares: +1 Velocidad. |  |
| A43 | Garras para Cavado | Implementada | Al jugarla: roba 1 carta del mazo de otro jugador |  |
| A44 | Patas Robustas | Implementada | En Sabana: +1 Velocidad. |  |
| A45 | Extremidades Segmentadas | Implementada | si es atacado por emboscada recibe un bono de +2 de velocidad |  |
| A46 | Alas Cortas | Implementada | mira la mano de otro jugador y desearle 2 cartas |  |
| A47 | Patas de Aceleración | Sin habilidad en diseño |  |  |
| A48 | Almohadilla nupcial | Implementada | En Manglar: +1 Velocidad. |  |
| A49 | Regeneración de Extremidad | Implementada | Al jugarla: cura 2 punto de salud de esta especie (si está dañada). |  |
| A50 | Patas Trepadoras | Implementada | Mientras esté en juego: hay +1 planta adicional en la Zona de Forraje al crecer plantas. |  |
| A51 | Cola Prensil | Implementada | Obtiene +2 de velocidad en jungla y bosque. |  |
| A52 | Cola Látigo | Parcial | Al jugarla: busca una carta en tu mazo y ponla en tu mano, luego naraja el mazo | Busca carta en mazo de forma aleatoria, no selección directa. |
| A53 | Cola Propulsora | Implementada | si está en un bioma de playa o manglar, obtiene más 2 de velocidad al atacar. |  |
| A54 | Cola Camuflada | Implementada | Mientras esté en juego: los ataques contra esta especie requieren emboscada para ignorar su escape. |  |
| A55 | Cola Venenosa | Implementada | Si el ataque es exitoso: el objetivo queda Envenenado. \| En Estepa: +1 Velocidad. |  |
| A56 | Cola Afilada | Sin habilidad en diseño |  |  |
| A57 | Cola Equilibradora | Sin habilidad en diseño |  |  |
| A58 | Autotomía Caudal | Implementada | si un rival te ataca a esta especie  y falla, ese rival queda Confundido. |  |
| A59 | Cola Plumosa | Implementada | En Estepa: +1 Velocidad. |  |
| A60 | Cola bufanda térmica | Parcial | Al jugarla: selcciona si quieres que tu especie tenga +1 o -1 de temperatura al tener está adaptación | Cambio de temperatura +/-1 aleatorio en vez de selección. |
| A61 | Cola Sensorial | Implementada | En Estepa: +1 Velocidad. |  |
| A62 | Cola Reserva | Implementada | En lugar de eliminar un individuo de esta especie elimina esta carta y restaura por completo la salud de la especie. |  |
| A63 | Cola Natatoria | Parcial | Al jugarla: mira la mano de un rival y elige 1 carta; ese rival la descarta. | Descarta carta rival aleatoria, no elegida. |
| A64 | Cola Cortante | Implementada | Mientras esté en juego: si un rival te ataca y falla, ese rival queda Confundido. |  |
| A65 | Cola Señuelo | Implementada | El próximo ataque por emboscada resulta en exito siempre, una vez usada está habilidad Descarta está adaptación. \| En Estepa: +1 Velocidad. |  |
| A66 | Visión Nocturna | Implementada | En Taiga: +1 Percepción. |  |
| A67 | Ojos Compuestos | Implementada | En Llanura: +1 Percepción. |  |
| A68 | Ecolocación | Implementada | En Montaña Fría: +1 Percepción. |  |
| A69 | Bigotes Sensoriales | Implementada | Al jugarla: roba 1 carta |  |
| A70 | Línea Lateral | Parcial | Al jugarla: mira la mano de un rival y elige 1 carta; ese rival la descarta. | Descarta carta rival aleatoria, no elegida. |
| A71 | Olfato Agudo | Parcial | Al jugarla: mira las 5 primeras cartas de tu mazo, selecciona 1 y ponla en tu mano \| En Taiga: +1 Percepción. | Top 5 se procesa con elección aleatoria. |
| A72 | Visión Térmica | Parcial | Al jugarla: elige una especie rival; queda Confundida. \| En Desierto: +1 Percepción. | Especie rival para confundir se elige aleatoria. |
| A73 | Audición Direccional | Parcial | Al jugarla: intercambia 1 cartas de tu mano con 1 cartas al azar de la mano de un rival. \| En Taiga: +1 Percepción. | Intercambio usa carta propia aleatoria. |
| A74 | Electrorrecepción | Sin habilidad en diseño |  |  |
| A75 | Visión Panorámica | Implementada | En Llanura: +1 Percepción. |  |
| A76 | Detección de Presión | Implementada | Mientras esté en juego: la especie puede pagar 1 comida para ignorar un estado negativo (una vez por ronda). |  |
| A77 | Ojos Independientes | Implementada | En Desierto: +1 Percepción. |  |
| A78 | Sismorrecepción | Implementada | En Montaña Fría: +1 Percepción. |  |
| A79 | Super fuerza | Implementada | En Llanura: +1 Percepción. |  |
| A80 | Visión Ultravioleta | Implementada | En Montaña Fría: +1 Percepción. |  |
| A81 | Audición Subacuática | Implementada | obtiene mas 2 de persepcion en manglar, arecife, playa y Glaciares |  |
| A82 | Sensores de Corriente | Implementada | Al jugarla: roba 1 carta y luego descarta 1 carta. |  |
| A83 | Apotematismo | Sin habilidad en diseño |  |  |
| A84 | Osteodermos dérmicos | Sin habilidad en diseño |  |  |
| A85 | Conglobación | Implementada | En Bosque: +1 Salud. |  |
| A86 | Autohémorragia refleja | Implementada | Si recibe 0 de daño tras un ataque el atacante queda aturdido |  |
| A87 | Sangre Caliente | Implementada | En Glaciares: +1 Salud. |  |
| A88 | Hibernación | Implementada | Mientras esté en juego: esta especie puede ignorar 1 consumo de alimento por ronda. \| En Tundra: +1 Salud. |  |
| A89 | Migración Estacional | Parcial | Al jugarla: mueve 2 planta desde la Zona de Forraje a tu reserva de comida. \| En Tundra: +1 Salud. | No mueve desde zona de forraje/reserva; suma comida directa. |
| A90 | Reproducción Rápida | Implementada | Al jugarla: roba 1 carta y luego descarta 1 carta. |  |
| A91 | Cuidado Parental | Implementada | En Sabana: +1 Salud. |  |
| A92 | Antenas filiformes | Implementada | En Llanura: +1 Salud. |  |
| A93 | Bioluminiscencia | Sin habilidad en diseño |  |  |
| A94 | Metamorfosis Completa | Implementada | En Bosque: +1 Salud. |  |
| A95 | Defensa Química | Implementada | Tras realizar una defensa su atacante queda confundido |  |
| A96 | Regeneración Celular | Implementada | Al jugarla: cura 1 punto de salud de esta especie (si está dañada). |  |
| A97 | Exhibición Intimidante | Parcial | Al jugarla: elige una especie rival; queda en Terror. | Especie rival en Terror se aplica aleatoriamente. |
| A98 | Simbiosis | No implementada | Selecciona una especie, cada vez que alguna gane "fichas de comida" la otra tambien | No hay vínculo de simbiosis entre especies para compartir comida. |
| A99 | Resistencia a Toxinas | Implementada | inmunidad a todos los estados |  |
| A100 | Especialización Dietaria | Implementada | Mientras esté en juego: la primera vez que esta especie sea atacada cada ronda, el atacante pierde 1 velocidad para ese ataque. |  |
| A101 | lengua extensible | Implementada | si golpea a la presa está queda paralizada |  |
| A102 | Molares de Pastoreo | Implementada | Al forrajear: si extraes al menos 1 comida, obtén +1 comida extra \| En Pradera: +1 Ataque durante forrajeo y ataques. |  |
| A103 | Mandíbula Rumiadora | Implementada | En Pradera: +1 Ataque durante forrajeo y ataques. |  |
| A104 | Incisivos de Roedor | Implementada | Al jugarla: puedes cambiar la carta de bioma activa por una al azar. |  |
| A105 | Pico Folívoro | Implementada | En Bosque: +1 Ataque durante forrajeo y ataques. |  |
| A106 | Mandíbula de Broteo | Implementada | Si no logro obtener fichas de comida en la fase de forrajeo, agrega 1 ficha de comida a esta especie |  |
| A107 | Dentición Hipodonta | Implementada | Si forrajeas en Pradera/Estepa: obtén +1 comida adicional. \| En Pradera: +1 Ataque durante forrajeo y ataques. |  |
| A108 | Mandíbula Lofodonta | Implementada | Al forrajear: puedes repartir la comida obtenida entre 2 especies aliadas herbivoras adyacentes. |  |
| A109 | Mandíbula Excavadora | Sin habilidad en diseño |  |  |
| A110 | Boca de Filtrado Vegetal | Implementada | En Playa: +1 Ataque durante forrajeo y ataques. |  |
| A111 | Dientes en Peine | Sin habilidad en diseño |  |  |
| A112 | Pico Granívoro | Implementada | Al jugarla : coloca 2 de comida sobre una de tus especies \| En Taiga: +1 Ataque durante forrajeo y ataques. |  |
| A113 | Mandíbula de Corte de Corteza | Implementada | Mientras esté en juego: si sufre un ataque fallido (el rival no logra alcanzarla), gana 1 comida (una vez por ronda). |  |
| A114 | Boca Succionadora de Néctar | Implementada | En Jungla: +1 Ataque durante forrajeo y ataques. |  |
| A115 | Mandíbula de Trituración de Conchas Vegetales | Implementada | En Playa: +1 Ataque durante forrajeo y ataques. |  |
| A116 | Dentición Selectiva | Implementada | En Pradera: +1 Ataque durante forrajeo y ataques. |  |
| A117 | Patas Dunares | Implementada | Esta especie tiene -1 de metabolismo en desierto, montaña y sabana |  |
| A118 | Pezuñas Escaladoras | Implementada | Esta especie tiene +1 de velocidad en desierto, montaña y sabana \| En Montaña Fría: +1 Velocidad. |  |
| A119 | Placas de Corteza | Sin habilidad en diseño |  |  |
| A120 | Pelaje Ártico | Implementada | En Glaciares: +1 Armadura. |  |
| A121 | Zancada de Pradera | Implementada | En Pradera: +1 Velocidad. |  |
| A122 | Aletas Litorales | Sin habilidad en diseño |  |  |
| A123 | Piel Osmorreguladora | Sin habilidad en diseño |  |  |
| A124 | Escamas Reflectantes (Desierto) | Implementada | En Desierto: +1 Armadura. |  |
| A125 | Garras de Hielo | Implementada | Esta especie tiene +1 de velocidad en Taiga, Estepa y Tundra \| En Tundra: +1 Velocidad. |  |
| A126 | Microhábitat Favorable | Implementada | Al jugarla : roba una carta si estas en llanura o estepa \| En Llanura: +1 Percepción. |  |
