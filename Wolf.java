import javax.swing.ImageIcon;

public class Wolf extends Alive {
    private final static String     type            = "wolf";
    private final static ImageIcon  image           = new ImageIcon(PastureProperties.getInstance().getValue(type + ".image"));
    private final static int        LIVE_DELAY      = PastureProperties.getInstance().getIntValue(type + ".no_food_limit");
    private final static int        DUPLICATE_DELAY = PastureProperties.getInstance().getIntValue(type + ".duplicate_limit");
    private final static int        MOVE_DELAY      = PastureProperties.getInstance().getIntValue(type + ".move_delay");

    public Wolf(Pasture pasture) {
        super(pasture, type, LIVE_DELAY, MOVE_DELAY, DUPLICATE_DELAY);
    }

    public ImageIcon getImage() { return image; }

    protected Entity clone() {
        return new Wolf(this.pasture);
    }

    /* No one can (or want) to share space with a wolf */
    public boolean isCompatible(Entity otherEntity) {
        return false;
    }
}
