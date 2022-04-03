import java.awt.Point;
import java.util.List;

import javax.swing.ImageIcon;

public class Plant extends Stationary {
    private final static String     type            = "plant";
    private final static ImageIcon  image           = new ImageIcon(PastureProperties.getInstance().getValue(type + ".image"));
    private final static int        DUPLICATE_DELAY = PastureProperties.getInstance().getIntValue(type + ".duplicate_limit");

    private int duplicateDelay;

    public Plant(Pasture pasture) {
        super(pasture, type);
        duplicateDelay = DUPLICATE_DELAY;
    }

    public ImageIcon getImage() { return image; }

    public void tick() {
        duplicateDelay--;

        if (duplicateDelay <= 0) {
            /* Always reset plant duplication time to avoid over population */
            pollinate();
            duplicateDelay = PastureProperties.getInstance().getIntValue(this.getType() + ".duplicate_limit");
        }
    }

    /* I didn't want the Plant object to be an instance
     * of alive objects since it would be bad OOP to let a plant know how to walk and eat
     * and things like that. However, a plant can in fact duplicate itself so
     * this method is a light "static" variation of entity duplication
     */
    private boolean pollinate() {       
        List<Point> freeNeighbors = pasture.getFreeNeighbours(this);

        if (freeNeighbors.size() < 1)
            return false;

        Point n = freeNeighbors.get((int)(Math.random() * freeNeighbors.size()));
        Plant child = new Plant(this.pasture);
        pasture.addEntity(child, n);
        return true;
    }


    /* A plant can share space with a sheep or a wolf */
    public boolean isCompatible(Entity otherEntity) {
        return otherEntity.getType().equals("sheep") || otherEntity.getType().equals("wolf");
    }
}
