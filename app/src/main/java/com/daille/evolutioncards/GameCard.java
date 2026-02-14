package com.daille.evolutioncards;

public class GameCard {
    public final String id;
    public final String name;
    public final String type;
    public final String description;
    public final String metadata;

    public GameCard(String id, String name, String type, String description, String metadata) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.metadata = metadata;
    }
}
