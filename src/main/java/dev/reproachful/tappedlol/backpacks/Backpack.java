package dev.reproachful.tappedlol.backpacks;

public class Backpack {

    private final String identifier;
    private final String name;
    private final int size;
    private final String texture;

    public Backpack(String identifier, String name, int size, String texture) {
        this.identifier = identifier;
        this.name = name;
        this.size = size;
        this.texture = texture;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public String getTexture() {
        return texture;
    }
}
