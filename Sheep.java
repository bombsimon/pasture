import javax.swing.ImageIcon;

public class Sheep extends Alive {
    private final static String     type            = "sheep";
    private final static ImageIcon  image           = new ImageIcon(PastureProperties.getInstance().getValue(type + ".image"));
    private final static int        LIVE_DELAY      = PastureProperties.getInstance().getIntValue(type + ".no_food_limit");
    private final static int        DUPLICATE_DELAY = PastureProperties.getInstance().getIntValue(type + ".duplicate_limit");
    private final static int        MOVE_DELAY      = PastureProperties.getInstance().getIntValue(type + ".move_delay");

    public Sheep(Pasture pasture) {
        super(pasture, type, LIVE_DELAY, MOVE_DELAY, DUPLICATE_DELAY);
    }

    public ImageIcon getImage() { return image; }

    protected Entity clone() {
        return new Sheep(this.pasture);
    }

    /* A wolf can "share" space with a sheep (and eat it) */
    public boolean isCompatible(Entity otherEntity) {
        return otherEntity.getType().equals("wolf");
    }
}
