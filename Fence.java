import javax.swing.ImageIcon;

public class Fence extends Stationary {
    private final static String     type    = "fence";
    private final static ImageIcon  image   = new ImageIcon(PastureProperties.getInstance().getValue(type + ".image"));

    public Fence(Pasture pasture) {
        super(pasture, type);
    }

    public ImageIcon getImage() { return image; }

    // Nothing ever happens with a fence
    public void tick() {}

    // A fence cannot share it's square
    public boolean isCompatible(Entity otherEntity) {
        return false;
    }
}
