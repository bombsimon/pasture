import java.util.*;
import java.awt.Point;

/**
 * A pasture contains sheep, wolves, fences, plants, and possibly
 * other entities. These entities move around in the pasture and try
 * to find food, other entities of the same kind and run away from
 * possible enimies. 
 */
public class Pasture {
    private final int width  = PastureProperties.getInstance().getIntValue("pasture.width");
    private final int height = PastureProperties.getInstance().getIntValue("pasture.height");

    private final int fences = PastureProperties.getInstance().getIntValue("fence.count_at_start");
    private final int plants = PastureProperties.getInstance().getIntValue("plant.count_at_start");
    private final int sheeps = PastureProperties.getInstance().getIntValue("sheep.count_at_start");
    private final int wolfs  = PastureProperties.getInstance().getIntValue("wolf.count_at_start");

    private final Set<Entity> world = new HashSet<Entity>();
    private final Map<Point, List<Entity>> grid = new HashMap<Point, List<Entity>>();
    private final Map<Entity, Point> point = new HashMap<Entity, Point>();

    private final PastureGUI gui;

    /** 
     * Creates a new instance of this class and places the entities in
     * it on random positions.
     */
    public Pasture() {

        Engine engine = new Engine(this);
        gui = new PastureGUI(width, height, engine);

        /* The pasture is surrounded by a fence. Replace Dummy for
         * Fence when you have created that class */
        for (int i = 0; i < width; i++) {
            addEntity(new Fence(this), new Point(i,0));
            addEntity(new Fence(this), new Point(i, height - 1));
        }
        for (int i = 1; i < height-1; i++) {
            addEntity(new Fence(this), new Point(0,i));
            addEntity(new Fence(this), new Point(width - 1,i));
        }

        /* 
         * Now insert the right number of different entities in the
         * pasture.
         */
        for (int i = 0; i < plants; i++) {
            Entity e = new Plant(this);
            addEntity(e, getFreePosition(e));
        }

        for (int i = 0; i < fences; i++) {
            Entity e = new Fence(this);
            addEntity(e, getFreePosition(e));
        }

        for (int i = 0; i < sheeps; i++) {
            Entity e = new Sheep(this);

            addEntity(e, getFreePosition(e));
        }

        for (int i = 0; i < wolfs; i++) {
            Entity e = new Wolf(this);
            addEntity(e, getFreePosition(e));
        }

        gui.update();
    }

    public void refresh() {
        gui.update();
    }

    /**
     * Returns a random free position in the pasture if there exists
     * one.
     * 
     * If the first random position turns out to be occupied, the rest
     * of the board is searched to find a free position. 
     */
    private Point getFreePosition(Entity toPlace) 
            throws MissingResourceException {
        Point position = new Point((int)(Math.random() * width),
                (int)(Math.random() * height)); 

        int p = position.x + position.y * width;
        int m = height * width;
        int q = 97; //any large prime will do

            for (int i = 0; i<m; i++) {
                int j = (p+i*q) % m;
                int x = j % width;
                int y = j / width;

                position = new Point(x,y);
                boolean free = true;

                Collection <Entity> c = getEntitiesAt(position);
                if (c != null) {
                    for (Entity thisThing : c) {
                        if(!toPlace.isCompatible(thisThing)) { 
                            free = false; break; 
                        }
                    }
                }
                if (free) return position;
            }
            throw new MissingResourceException(
                    "There is no free space"+" left in the pasture",
                    "Pasture", "");
    }


    public Point getPosition (Entity e) {
        return point.get(e);
    }

    /**
     * Add a new entity to the pasture.
     */
    public void addEntity(Entity entity, Point pos) {

        world.add(entity);

        List<Entity> l = grid.get(pos);
        if (l == null) {
            l = new  ArrayList<Entity>();
            grid.put(pos, l);
        }
        l.add(entity);

        point.put(entity,pos);

        gui.addEntity(entity, pos);
    }

    public void moveEntity(Entity e, Point newPos) {

        Point oldPos = point.get(e);
        List<Entity> l = grid.get(oldPos);
        if (!l.remove(e)) 
            throw new IllegalStateException("Inconsistent stat in Pasture");
        /* We expect the entity to be at its old position, before we
           move, right? */

        l = grid.get(newPos);
        if (l == null) {
            l = new ArrayList<Entity>();
            grid.put(newPos, l);
        }
        l.add(e);

        point.put(e, newPos);

        gui.moveEntity(e, oldPos, newPos);
    }

    /**
     * Remove the specified entity from this pasture.
     */
    public void removeEntity(Entity entity) { 

        Point p = point.get(entity);
        world.remove(entity); 
        grid.get(p).remove(entity);
        point.remove(entity);
        gui.removeEntity(entity, p);

    }

    /**
     * Various methods for getting information about the pasture
     */

    public List<Entity> getEntities() {
        return new ArrayList<Entity>(world);
    }

    public Collection<Entity> getEntitiesAt(Point lookAt) {

        Collection<Entity> l = grid.get(lookAt);

        if (l==null) {
            return null;
        }
        else {
            return new ArrayList<Entity>(l);
        }
    }

    /* This method will scan the area around an entitiy based on a vision value
     * A list with all entities will be returned.
     */
    public Collection<Entity> getEntitiesByVision(Entity e, int vision) {
        ArrayList<Entity> entities = new ArrayList<Entity>();

        Point position = getEntityPosition(e);

        for (int scanY = position.y - vision; scanY <= position.y + vision; scanY++) {
            for (int scanX = position.x - vision; scanX <= position.x + vision; scanX++) {
                Collection<Entity> found = getEntitiesAt(new Point(scanX, scanY));
                if (found == null)
                    continue;

                for (Entity someone : found) {
                    entities.add(someone);
                }
            }
        }

        return entities;
    }

    public List<Point> getFreeNeighbours(Entity entity) {
        List<Point> free = new ArrayList<Point>();


        Point entityPoint = getEntityPosition(entity);

        int entityX = entityPoint.x;
        int entityY = entityPoint.y;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                Point p = new Point(entityX + x, entityY + y);
                if (freeSpace(p, entity))
                    free.add(p);
            }
        }        
        return free;
    }

    public boolean freeSpace(Point p, Entity e) {                   
        List <Entity> l = grid.get(p);
        if ( l == null  ) return true;
        for (Entity old : l ) 
            if (! old.isCompatible(e)) return false;
        return true;
    }

    public Point getEntityPosition(Entity entity) {
        return point.get(entity);
    }

    /** The method for the JVM to run. */
    public static void main(String[] args) {
        new Pasture();
    }
}


