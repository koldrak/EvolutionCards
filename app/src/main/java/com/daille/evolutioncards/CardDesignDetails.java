package com.daille.evolutioncards;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class CardDesignDetails {
    private static final Map<String, DesignCardInfo> BY_ID;
    private static final Map<String, DesignCardInfo> BY_NAME;

    static {
        Map<String, DesignCardInfo> byId = new HashMap<>();
        Map<String, DesignCardInfo> byName = new HashMap<>();
        add(byId, byName, new DesignCardInfo("1","Mandíbula Trituradora","Carnivoro","La especie con menor velocidad","1","","","","","","","","Obtiene más 1 de alimento cada vez que un individuo es eliminado. | En Sabana: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("2","Colmillos Serrados","Carnivoro","La especie con mayor armadura","1","","","","","","","","Al atacar: ignora la armadura de la presa en ese ataque."));
        add(byId, byName, new DesignCardInfo("3","Pico Cortante","Carnivoro","La especie con menor velocidad","1","","","1","","","","","En Montaña: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("4","Mandíbula Canina","Carnivoro","La especie con menor velocidad","1","-1","","","1","","","","Al jugarla: roba 1 carta y luego descarta 1 carta."));
        add(byId, byName, new DesignCardInfo("5","Mordida Venenosa","Carnivoro","La especie con menor persepcion","1","","-1","","1","","","","Si el ataque es exitoso: el objetivo queda Envenenado."));
        add(byId, byName, new DesignCardInfo("6","Mandíbula Proyectable","Carnivoro","La especie con menor Percepción","1","","","1","","","","","Si ataca con éxito el defensor queda paralizado"));
        add(byId, byName, new DesignCardInfo("7","Dientes de Aguja","Carnivoro","La especie con menor Percepción","1","","","","1","","","","En Arrecife: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("8","Mandíbula Filtradora","Carnivoro","La especie con menor velocidad","","","","","1","","","","Esta especie obtiene 1 comida sin depender de la zona de forrageo en cada turno"));
        add(byId, byName, new DesignCardInfo("9","Pico Perforador","Carnivoro","La especie con menor Percepción","","","","","2","","","",""));
        add(byId, byName, new DesignCardInfo("10","Mandíbula Omnívora","Carnivoro","la especie con menor velocidad","1","","","","","","","","En Bosque: +1 Ataque"));
        add(byId, byName, new DesignCardInfo("11","Molares Trituradores","Hervivoro","----","1","","","","","","","",""));
        add(byId, byName, new DesignCardInfo("12","Boca Succionadora","Carnivoro","La especie con menor Percepción","","","","","2","","","","Si ataca con éxito el defensor queda paralizado"));
        add(byId, byName, new DesignCardInfo("13","Colmillos Curvos","Carnivoro","La especie con menor persepcion","2","","","-1","","","","","En Sabana: +1 Ataque"));
        add(byId, byName, new DesignCardInfo("14","Mandíbula infecciosa","Carnivoro","La especie con menor defensa","2","","","","","","","","Si el ataque es exitoso: aplica un estado aleatorio a la especie objetivo."));
        add(byId, byName, new DesignCardInfo("15","Mandíbula con colmillos","Omnívoro","La especie con menor defensa","1","2","","-1","-1","","","","al atacar obtiene +1 de ataque"));
        add(byId, byName, new DesignCardInfo("16","Pelaje Espeso","","","","1","1","-1","","","","1","En Glaciares: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("17","Pelaje Impermeable","","","","1","1","1","","","","1","Al jugarla: mira la mano de un rival y elige 1 carta; ese rival la descarta. | En Tundra: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("18","Plumas Aislantes","","","","","","-1","","","","1","+1 de velocidad si tiene una | En Glaciares: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("19","Caparazón Óseo","","","","4","","-2","","","","1","Si recibe 0 de daño tras un ataque el atacante queda aturdido"));
        add(byId, byName, new DesignCardInfo("20","Exoesqueleto Quitinoso","","","","2","","1","","","","-2","En Montaña: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("21","Escamas Reforzadas","","","","1","1","","","","","-1","En Manglar: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("22","Piel Tóxica","","","","","1","","1","","","-1","Si es dañado por un ataque el atacante queda envenenado. | En Jungla: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("23","Piel Gruesa","","","","3","3","-4","1","","","","En Sabana: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("24","Patrón de Camuflaje","","","","","","","2","","","","En Jungla: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("25","Cromatogoros","","","","-1","","","3","","","","En Desierto: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("26","Piel Elástica","","","","1","1","1","","","","-1","obtiene +1 de velocidad en playa y arrecife"));
        add(byId, byName, new DesignCardInfo("27","Espinas Dérmicas","","","","1","","-1","","","","","Si es dañado por un ataque el atacante recibe 1 de daño | En Montaña Fría: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("28","Escamas Plateadas","","","","1","","1","-2","","","-1","Al jugarla: elige una especie rival; queda Confundida."));
        add(byId, byName, new DesignCardInfo("29","Piel Rugosa","","","","","2","","","","","1",""));
        add(byId, byName, new DesignCardInfo("30","Cobertura Estacional","","","","1","1","","","","","","al jugarla el usuario selecciona si quiere subir o bajar la temperatura de la especie en 1 | En Tundra: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("31","Patas Saltadoras","","","","","","2","","","","","Al jugarla: roba 1 carta y luego descarta 1 carta."));
        add(byId, byName, new DesignCardInfo("32","Garras Retráctiles","","","1","","","","","","","","Al jugarla: intercambia 2 cartas de tu mano con 2 cartas al azar de la mano de un rival. | En Montaña: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("33","Aletas Potentes","","","","","","1","","","","","manglares y arrecife +1 de velocidad"));
        add(byId, byName, new DesignCardInfo("34","Patas Excavadoras","","","","1","","","1","","","","al jugarla el usuario selecciona si quiere subir o bajar la temperatura de la especie en 2 | En Estepa: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("35","Membrana Planeadora","","","-1","","-1","2","2","","","","En Taiga: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("36","Alas Batientes","","","-1","-2","1","4","","","","","+ 2 de ataque contra especies con alas. | En Montaña: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("37","Patas Largas","","","1","-1","","1","","","","","Al jugarla: elige una especie; aumenta su fertilidad en +1 (permanente)."));
        add(byId, byName, new DesignCardInfo("38","Almohadillas Adhesivas","","","","","","","1","","","","+2 de velocidad en jungla y montaña. | En Montaña: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("39","Extremidades Prensiles","","","-1","","","1","1","","","","+2 de velocidad en jungla y bosque. | En Jungla: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("40","Tentáculos","","","","","1","-1","1","","","","Al jugarla: cambia 1 adaptación de una especie por 1 de tu mano."));
        add(byId, byName, new DesignCardInfo("41","Aleta Dorsal Estabilizadora","","","","","","2","1","","","","Obtiene +1 de velocidad en manglares, playa y arrecife. -1 en otros biomas"));
        add(byId, byName, new DesignCardInfo("42","Patas Anchas","","","","","","1","","","","1","+2 de velocidad en taiga, estepa, tundras, montaña fría y glaciares | En Glaciares: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("43","Garras para Cavado","","","1","","","","","","","","Al jugarla: roba 1 carta del mazo de otro jugador"));
        add(byId, byName, new DesignCardInfo("44","Patas Robustas","","","","1","1","","","","","","En Sabana: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("45","Extremidades Segmentadas","","","","-1","1","2","","","","","si es atacado por emboscada recibe un bono de +2 de velocidad"));
        add(byId, byName, new DesignCardInfo("46","Alas Cortas","","","","-2","","2","1","","","","mira la mano de otro jugador y desearle 2 cartas"));
        add(byId, byName, new DesignCardInfo("47","Patas de Aceleración","","","","","1","4","","","","",""));
        add(byId, byName, new DesignCardInfo("48","Almohadilla nupcial","","","","","","-1","1","-1","","","En Manglar: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("49","Regeneración de Extremidad","","","","","1","-1","","","","","Al jugarla: cura 2 punto de salud de esta especie (si está dañada)."));
        add(byId, byName, new DesignCardInfo("50","Patas Trepadoras","","","","","","2","1","","","","Mientras esté en juego: hay +1 planta adicional en la Zona de Forraje al crecer plantas."));
        add(byId, byName, new DesignCardInfo("51","Cola Prensil","","","","","","","","","","","Obtiene +2 de velocidad en jungla y bosque."));
        add(byId, byName, new DesignCardInfo("52","Cola Látigo","","","1","","","","","","","","Al jugarla: busca una carta en tu mazo y ponla en tu mano, luego naraja el mazo"));
        add(byId, byName, new DesignCardInfo("53","Cola Propulsora","","","","","","2","","","","","si está en un bioma de playa o manglar, obtiene más 2 de velocidad al atacar."));
        add(byId, byName, new DesignCardInfo("54","Cola Camuflada","","","-1","","1","","2","","","","Mientras esté en juego: los ataques contra esta especie requieren emboscada para ignorar su escape."));
        add(byId, byName, new DesignCardInfo("55","Cola Venenosa","","","2","","","","-1","","","","Si el ataque es exitoso: el objetivo queda Envenenado. | En Estepa: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("56","Cola Afilada","","","1","1","","","","","","",""));
        add(byId, byName, new DesignCardInfo("57","Cola Equilibradora","","","","-1","","2","1","","","",""));
        add(byId, byName, new DesignCardInfo("58","Autotomía Caudal","","","","","-1","2","1","","","","si un rival te ataca a esta especie  y falla, ese rival queda Confundido."));
        add(byId, byName, new DesignCardInfo("59","Cola Plumosa","","","","","","","","1","","","En Estepa: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("60","Cola bufanda térmica","","","","","","","1","","","","Al jugarla: selcciona si quieres que tu especie tenga +1 o -1 de temperatura al tener está adaptación"));
        add(byId, byName, new DesignCardInfo("61","Cola Sensorial","","","","","","","2","","","","En Estepa: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("62","Cola Reserva","","","-1","","2","","","","","","En lugar de eliminar un individuo de esta especie elimina esta carta y restaura por completo la salud de la especie."));
        add(byId, byName, new DesignCardInfo("63","Cola Natatoria","","","","","","2","","","","","Al jugarla: mira la mano de un rival y elige 1 carta; ese rival la descarta."));
        add(byId, byName, new DesignCardInfo("64","Cola Cortante","","","","","","1","1","","","","Mientras esté en juego: si un rival te ataca y falla, ese rival queda Confundido."));
        add(byId, byName, new DesignCardInfo("65","Cola Señuelo","","","","","1","","","","","","El próximo ataque por emboscada resulta en exito siempre, una vez usada está habilidad Descarta está adaptación. | En Estepa: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("66","Visión Nocturna","","","","","","","2","","","","En Taiga: +1 Percepción."));
        add(byId, byName, new DesignCardInfo("67","Ojos Compuestos","","","","-1","","","2","","","","En Llanura: +1 Percepción."));
        add(byId, byName, new DesignCardInfo("68","Ecolocación","","","-1","-1","-1","","3","","","","En Montaña Fría: +1 Percepción."));
        add(byId, byName, new DesignCardInfo("69","Bigotes Sensoriales","","","-1","","","","2","","","","Al jugarla: roba 1 carta"));
        add(byId, byName, new DesignCardInfo("70","Línea Lateral","","","","-1","","","","","","","Al jugarla: mira la mano de un rival y elige 1 carta; ese rival la descarta."));
        add(byId, byName, new DesignCardInfo("71","Olfato Agudo","","","-1","","","","","","","","Al jugarla: mira las 5 primeras cartas de tu mazo, selecciona 1 y ponla en tu mano | En Taiga: +1 Percepción."));
        add(byId, byName, new DesignCardInfo("72","Visión Térmica","","","","","","-1","2","","","","Al jugarla: elige una especie rival; queda Confundida. | En Desierto: +1 Percepción."));
        add(byId, byName, new DesignCardInfo("73","Audición Direccional","","","","-1","","-1","","","","","Al jugarla: intercambia 1 cartas de tu mano con 1 cartas al azar de la mano de un rival. | En Taiga: +1 Percepción."));
        add(byId, byName, new DesignCardInfo("74","Electrorrecepción","","","","-1","1","-2","3","","","",""));
        add(byId, byName, new DesignCardInfo("75","Visión Panorámica","","","-1","","","","2","","","","En Llanura: +1 Percepción."));
        add(byId, byName, new DesignCardInfo("76","Detección de Presión","","","","-1","-1","","2","","","","Mientras esté en juego: la especie puede pagar 1 comida para ignorar un estado negativo (una vez por ronda)."));
        add(byId, byName, new DesignCardInfo("77","Ojos Independientes","","","","","1","-3","2","-1","","","En Desierto: +1 Percepción."));
        add(byId, byName, new DesignCardInfo("78","Sismorrecepción","","","","","-1","","2","","","","En Montaña Fría: +1 Percepción."));
        add(byId, byName, new DesignCardInfo("79","Super fuerza","","","2","1","","-1","","1","","","En Llanura: +1 Percepción."));
        add(byId, byName, new DesignCardInfo("80","Visión Ultravioleta","","","-1","","","","1","","","1","En Montaña Fría: +1 Percepción."));
        add(byId, byName, new DesignCardInfo("81","Audición Subacuática","","","","","","1","","","","","obtiene mas 2 de persepcion en manglar, arecife, playa y Glaciares"));
        add(byId, byName, new DesignCardInfo("82","Sensores de Corriente","","","","","","","2","","","","Al jugarla: roba 1 carta y luego descarta 1 carta."));
        add(byId, byName, new DesignCardInfo("83","Apotematismo","","","","2","1","","-3","","","",""));
        add(byId, byName, new DesignCardInfo("84","Osteodermos dérmicos","","","","2","1","","-1","","","",""));
        add(byId, byName, new DesignCardInfo("85","Conglobación","","","","2","1","-1","-2","","","","En Bosque: +1 Salud."));
        add(byId, byName, new DesignCardInfo("86","Autohémorragia refleja","","","1","1","","-1","","","","","Si recibe 0 de daño tras un ataque el atacante queda aturdido"));
        add(byId, byName, new DesignCardInfo("87","Sangre Caliente","","","","","1","","","","","1","En Glaciares: +1 Salud."));
        add(byId, byName, new DesignCardInfo("88","Hibernación","","","","","4","-3","-3","","","1","Mientras esté en juego: esta especie puede ignorar 1 consumo de alimento por ronda. | En Tundra: +1 Salud."));
        add(byId, byName, new DesignCardInfo("89","Migración Estacional","","","","","1","","","-1","","1","Al jugarla: mueve 2 planta desde la Zona de Forraje a tu reserva de comida. | En Tundra: +1 Salud."));
        add(byId, byName, new DesignCardInfo("90","Reproducción Rápida","","","","","","1","","-1","1","","Al jugarla: roba 1 carta y luego descarta 1 carta."));
        add(byId, byName, new DesignCardInfo("91","Cuidado Parental","","","-1","1","","","-1","-1","","","En Sabana: +1 Salud."));
        add(byId, byName, new DesignCardInfo("92","Antenas filiformes","","","","","","2","-1","","","","En Llanura: +1 Salud."));
        add(byId, byName, new DesignCardInfo("93","Bioluminiscencia","","","","","","","-1","-1","","",""));
        add(byId, byName, new DesignCardInfo("94","Metamorfosis Completa","","","","","-1","3","-2","","","","En Bosque: +1 Salud."));
        add(byId, byName, new DesignCardInfo("95","Defensa Química","","","","1","1","","","","","","Tras realizar una defensa su atacante queda confundido"));
        add(byId, byName, new DesignCardInfo("96","Regeneración Celular","","","","","2","","","","","","Al jugarla: cura 1 punto de salud de esta especie (si está dañada)."));
        add(byId, byName, new DesignCardInfo("97","Exhibición Intimidante","","","","1","","","","","","","Al jugarla: elige una especie rival; queda en Terror."));
        add(byId, byName, new DesignCardInfo("98","Simbiosis","","","","","","","-2","1","","","Selecciona una especie, cada vez que alguna gane \"fichas de comida\" la otra tambien"));
        add(byId, byName, new DesignCardInfo("99","Resistencia a Toxinas","","","","","2","","","","","","inmunidad a todos los estados"));
        add(byId, byName, new DesignCardInfo("100","Especialización Dietaria","","","","1","-1","","","","","","Mientras esté en juego: la primera vez que esta especie sea atacada cada ronda, el atacante pierde 1 velocidad para ese ataque."));
        add(byId, byName, new DesignCardInfo("101","lengua extensible","","","","","","-3","4","","","","si golpea a la presa está queda paralizada"));
        add(byId, byName, new DesignCardInfo("102","Molares de Pastoreo","Herbívoro","","1","","","","","-1","","","Al forrajear: si extraes al menos 1 comida, obtén +1 comida extra | En Pradera: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("103","Mandíbula Rumiadora","Herbívoro","","1","","","","","-1","","","En Pradera: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("104","Incisivos de Roedor","Herbívoro","","1","","","","","-1","","","Al jugarla: puedes cambiar la carta de bioma activa por una al azar."));
        add(byId, byName, new DesignCardInfo("105","Pico Folívoro","Herbívoro","","1","","","","","1","","","En Bosque: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("106","Mandíbula de Broteo","Herbívoro","","1","","1","","","","","","Si no logro obtener fichas de comida en la fase de forrajeo, agrega 1 ficha de comida a esta especie"));
        add(byId, byName, new DesignCardInfo("107","Dentición Hipodonta","Herbívoro","","1","","","","","-1","","","Si forrajeas en Pradera/Estepa: obtén +1 comida adicional. | En Pradera: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("108","Mandíbula Lofodonta","Herbívoro","","2","","","","","","","","Al forrajear: puedes repartir la comida obtenida entre 2 especies aliadas herbivoras adyacentes."));
        add(byId, byName, new DesignCardInfo("109","Mandíbula Excavadora","Omnívoro","","1","","1","-1","-1","","","",""));
        add(byId, byName, new DesignCardInfo("110","Boca de Filtrado Vegetal","Herbívoro","","1","","","","","","","","En Playa: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("111","Dientes en Peine","Herbívoro","","1","","","","","","","",""));
        add(byId, byName, new DesignCardInfo("112","Pico Granívoro","Herbívoro","","1","","-1","","","","","","Al jugarla : coloca 2 de comida sobre una de tus especies | En Taiga: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("113","Mandíbula de Corte de Corteza","Herbívoro","","1","","","","","","-1","","Mientras esté en juego: si sufre un ataque fallido (el rival no logra alcanzarla), gana 1 comida (una vez por ronda)."));
        add(byId, byName, new DesignCardInfo("114","Boca Succionadora de Néctar","Herbívoro","","2","","","","","1","","","En Jungla: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("115","Mandíbula de Trituración de Conchas Vegetales","Herbívoro","","1","1","","","","","","","En Playa: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("116","Dentición Selectiva","Herbívoro","","2","","","","","","","","En Pradera: +1 Ataque durante forrajeo y ataques."));
        add(byId, byName, new DesignCardInfo("117","Patas Dunares","","","-1","","","3","-1","","","-1","Esta especie tiene -1 de metabolismo en desierto, montaña y sabana"));
        add(byId, byName, new DesignCardInfo("118","Pezuñas Escaladoras","","","","","","3","-1","","","-1","Esta especie tiene +1 de velocidad en desierto, montaña y sabana | En Montaña Fría: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("119","Placas de Corteza","","","","4","","","-2","","","-1",""));
        add(byId, byName, new DesignCardInfo("120","Pelaje Ártico","","","","2","","","","","","2","En Glaciares: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("121","Zancada de Pradera","","","","","","3","-2","","","1","En Pradera: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("122","Aletas Litorales","","","","","","3","-2","","","-1",""));
        add(byId, byName, new DesignCardInfo("123","Piel Osmorreguladora","","","","1","","","","","","-1",""));
        add(byId, byName, new DesignCardInfo("124","Escamas Reflectantes (Desierto)","","","","2","","","","","","-1","En Desierto: +1 Armadura."));
        add(byId, byName, new DesignCardInfo("125","Garras de Hielo","","","","","","3","-2","","","-1","Esta especie tiene +1 de velocidad en Taiga, Estepa y Tundra | En Tundra: +1 Velocidad."));
        add(byId, byName, new DesignCardInfo("126","Microhábitat Favorable","","","","2","","","-1","","","","Al jugarla : roba una carta si estas en llanura o estepa | En Llanura: +1 Percepción."));
        BY_ID = Collections.unmodifiableMap(byId);
        BY_NAME = Collections.unmodifiableMap(byName);
    }

    private CardDesignDetails() {}

    public static DesignCardInfo findByGameCard(GameCard card) {
        if (card == null) {
            return null;
        }
        String numericId = extractNumericId(card.id);
        if (numericId != null) {
            DesignCardInfo byId = BY_ID.get(numericId);
            if (byId != null) {
                return byId;
            }
        }
        return BY_NAME.get(normalize(card.name));
    }

    private static void add(Map<String, DesignCardInfo> byId, Map<String, DesignCardInfo> byName, DesignCardInfo info) {
        byId.put(info.id, info);
        byName.put(normalize(info.name), info);
    }

    private static String extractNumericId(String cardId) {
        if (cardId == null || cardId.length() < 2) {
            return null;
        }
        String candidate = cardId.substring(1);
        for (int i = 0; i < candidate.length(); i++) {
            if (!Character.isDigit(candidate.charAt(i))) {
                return null;
            }
        }
        return candidate;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static final class DesignCardInfo {
        public final String id; public final String name; public final String feedingType; public final String attackTarget;
        public final String attack; public final String armor; public final String health; public final String speed;
        public final String perception; public final String fertility; public final String metabolism; public final String temperature;
        public final String ability;

        private DesignCardInfo(String id, String name, String feedingType, String attackTarget, String attack,
                String armor, String health, String speed, String perception, String fertility,
                String metabolism, String temperature, String ability) {
            this.id = id; this.name = name; this.feedingType = feedingType; this.attackTarget = attackTarget;
            this.attack = attack; this.armor = armor; this.health = health; this.speed = speed;
            this.perception = perception; this.fertility = fertility; this.metabolism = metabolism;
            this.temperature = temperature; this.ability = ability;
        }
    }
}