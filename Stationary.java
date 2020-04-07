import javax.swing.*;

abstract class Stationary implements Entity {
    protected final Pasture pasture;
    private String type;

    public Stationary(Pasture pasture, String t) {
        this.pasture = pasture;
        type = t;
    }

    abstract public void tick();

    public abstract ImageIcon getImage();
    public abstract boolean isCompatible(Entity otherEntity);

    public String getType() { return type; }
}
