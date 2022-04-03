import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.*;

abstract class Alive implements Entity {
    protected final Pasture pasture;
    private Point lastPosition;
    private int liveDelay, moveDelay, duplicateDelay;
    private String type;

    public abstract ImageIcon getImage();
    public abstract boolean isCompatible(Entity otherEntity);
    protected abstract Entity clone();

    public Alive(Pasture pasture, String type, int ld, int md, int dd) {
        this.pasture = pasture;
        this.type = type;
        lastPosition = null; /* No last position when an entity is born */
        liveDelay = ld;
        moveDelay = md;
        duplicateDelay = dd;
    }

    public String getType() { return type; }

    public void tick() {
        /* Reduce all defaults each tick so we know when to make an action */
        liveDelay--;
        moveDelay--;
        duplicateDelay--;

        /* An entity will die if liveDelay reaches 0 */
        if (liveDelay == 0) {
            pasture.removeEntity(this);
            return;
        }

        /* If an entity is old enough to duplicate, find free neighbors and make a baby!
         * If there are no free slots, try next tick
         */
        if (duplicateDelay <= 0) {
            Entity clone = (Entity) this.clone();

            if (duplicateEntity(clone)) {
                duplicateDelay = PastureProperties.getInstance().getIntValue(getType() + ".duplicate_limit");
            }
        }

        /* If the moveDelay (some kind of reverse speed (lower = faster) reaches zero
         * calculate what the next move will be and go there!
         * 
         * If landed on a spot where other entities are found, try to eat them!
         */
        if (moveDelay == 0) {
            Point newPosition = getNextMove();

            if (newPosition != null) {
                if (tryToEat(newPosition)) {
                    liveDelay = PastureProperties.getInstance().getIntValue(getType() + ".no_food_limit");
                }

                lastPosition = pasture.getPosition(this);
                pasture.moveEntity(this, newPosition);
            }

            moveDelay = PastureProperties.getInstance().getIntValue(getType() + ".move_delay");
        }
    }

    private static <X> X getRandomMember(List<X> c) {
        if (c.size() == 0)
            return null;

        return c.get((int)(Math.random() * c.size()));
    }

    /* If it's time to make a baby, check free neighbors and duplicate
     * if free ones are found
     */
    private boolean duplicateEntity(Entity child) {
        List<Point> freeNeighbors = pasture.getFreeNeighbours(this);

        if (freeNeighbors.size() > 0) {
            pasture.addEntity(child, getRandomMember(freeNeighbors));
            return true;
        }

        return false;
    }

    /* Each time a move is made, scan the terrain and try to eat what's found */
    private boolean tryToEat(Point eatAt) {
        Collection<Entity> whosAt = pasture.getEntitiesAt(eatAt);
        String isEating = PastureProperties.getInstance().getValue(getType() + ".is_eating");

        if (whosAt == null)
            return false;

        for (Entity e : whosAt) {
            if (e.getType().equals(isEating)) {
                pasture.removeEntity(e);
                return true;
            }
        }
        return false;
    }

    /* Compare two points (a and b) and test which one is closer to point toCheck
     * This is used when the terrain is scanned by vision length.
     * Since the list returned will stretch from far most top left to down right
     * of the vision, we need compare everything to see what's closest
     */
    private Point getClosestPoint(Point toCheck, Point a, Point b) {
        int axDiff = toCheck.x > a.x ? toCheck.x - a.x : a.x - toCheck.x;
        int ayDiff = toCheck.y > a.y ? toCheck.y - a.y : a.y - toCheck.y;
        int bxDiff = toCheck.x > b.x ? toCheck.x - b.x : b.x - toCheck.x;
        int byDiff = toCheck.y > b.y ? toCheck.x - b.y : b.y - toCheck.y;

        if (axDiff <= bxDiff && ayDiff <= byDiff) {
            return a;
        } else {
            return b;
        }
    }

    /* getNextMove is based on the vision length of the entity. If an entity can
     * see something that will eat it, it's going to run away from it.
     * If no scary creatures is around but food is found, move towards the food.
     * If none of the above is found, just continue the same way as before or a random
     * direction if trapped
     * 
     * If more than one is found (of any kind), act on the closest one
     */
    private Point getNextMove() {   
        int vision = PastureProperties.getInstance().getIntValue(this.getType() + ".vision_length");

        Collection<Entity> seen = pasture.getEntitiesByVision(this, vision);
        Point seenWolf = null;
        Point seenPlant = null;
        Point seenSheep = null;
        Point thisPoint = pasture.getEntityPosition(this);

        /* So we loop through the WHOLE list first. This is because it's more important to
         * run away from a wolf than towards a plant. Each time anything at all is found,
         * compare it with other entities of the same type to see which one is closer.
         * When the loop is done, this should represent the closest of each type (if any)
         */
        for (Entity someone : seen) {
            Point someonePosition = pasture.getEntityPosition(someone);

            if (someone.getType().equals("wolf")) {
                if (seenWolf != null) {
                    seenWolf = getClosestPoint(thisPoint, seenWolf, someonePosition);
                } else {
                    seenWolf = someonePosition;
                }
            } else if (someone.getType().equals("sheep")) {
                if (seenSheep != null) {
                    seenSheep = getClosestPoint(thisPoint, seenSheep, someonePosition);
                } else {
                    seenSheep = someonePosition;
                }
            } else if (someone.getType().equals("plant")) {
                if (seenPlant != null) {
                    seenPlant = getClosestPoint(thisPoint, seenPlant, someonePosition);
                } else {
                    seenPlant = someonePosition;
                }
            }
        }

        /* Based on what the entity is trying to avoid or trying to eat we need to
         * prioritize the next move.
         */
        String avoid = PastureProperties.getInstance().getValue(this.getType() + ".is_eaten_by");
        String lookFor = PastureProperties.getInstance().getValue(this.getType() + ".is_eating");

        if (avoid.equals("wolf") && seenWolf != null) {
            return moveAwayFrom(seenWolf);
        } else if (lookFor.equals("sheep") && seenSheep != null) {
            return moveTowards(seenSheep);
        } else if (lookFor.equals("plant") && seenPlant != null) {
            return moveTowards(seenPlant);
        } else {
            return continueDirection();
        }
    }

    /* Below are the methods moveAwayFrom, moveTowards, continueDirection and randomDirection
     * moveAwayFrom and moveTowards need a point as input and will get a list of valid moves
     * based on that point
     * 
     * continueDirection will use lastPosition to calculate how to move and randomDirection
     * will chose a random free neighbor.
     */
    private Point moveAwayFrom(Point p) {
        ArrayList<Point> awayFrom = getDirections(p, true); 
        for (Point point : awayFrom) {
            if (pasture.freeSpace(point, this)) {
                return point;
            }
        }

        return continueDirection();
    }

    private Point moveTowards(Point p) {
        ArrayList<Point> towards = getDirections(p, false); 
        for (Point point : towards) {
            if (pasture.freeSpace(point, this)) {
                return point;
            }
        }

        return continueDirection();
    }

    private Point continueDirection() {
        Point lastPos = lastPosition;

        if (lastPos == null)
            return randomDirection();

        ArrayList<Point> continuePos = getDirections(lastPos, true);
        for (Point point : continuePos) {
            if (pasture.freeSpace(point, this)) {
                return point;
            }
        }

        return randomDirection();
    }

    private Point randomDirection() {   
        List<Point> freeNeighbors = pasture.getFreeNeighbours(this);
        if (freeNeighbors.size() > 0) {
            return getRandomMember(freeNeighbors);
        }

        return null;
    }

    /* The method to calculate what points actually represet towards or away from a given point */
    private ArrayList<Point> getDirections(Point p2, boolean away) {
        ArrayList<Point> towards = new ArrayList<Point>();
        ArrayList<Point> awayFrom = new ArrayList<Point>();

        Point p1 = pasture.getEntityPosition(this);

        /* This is some test calculation with Math library since I'm not really satisfied with the original
         * implementation
         * 

        double angle = Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
        if (angle < 0.0)
            angle += 360;

        [Compare the double value of angle here]

         * I didn't really seem to reduce my code with this calculation but the angle will tell direction
         * 0.0 = right
         * 90.0 = down
         * 180.0 = left
         * 270.0 = up
         * 
         * However, if the angle is i.e. 65.25 degrees, I still need to add three options to move towards it
         * and three options to move away from it. Since there are four intervals with degrees (0-90, 90-180, 180-270, 270-359)
         * This would be 4 * (3 away + 3 towards) = 24 array adds + some extra if x or y is the same.
         */ 

        /* This is my original way. A lot of manual work but it's working as intended.
         * The setup is a grid calculating where p2 is in relation to p1
         * I.e if p1.x is > p2.x, p1 is more to the right and entity should go to x = p1.x + 1 to get closer
         */

        if (p1.x > p2.x) {
            if (p1.y > p2.y) {
                awayFrom.add(new Point(p1.x + 1, p1.y + 1));
                awayFrom.add(new Point(p1.x + 1, p1.y));
                awayFrom.add(new Point(p1.x, p1.y + 1));

                towards.add(new Point(p1.x - 1, p1.y - 1));
                towards.add(new Point(p1.x - 1, p1.y));
                towards.add(new Point(p1.x, p1.y - 1));
            } else if (p1.y < p2.y) {
                awayFrom.add(new Point(p1.x + 1, p1.y - 1));
                awayFrom.add(new Point(p1.x + 1, p1.y));
                awayFrom.add(new Point(p1.x, p1.y - 1));

                towards.add(new Point(p1.x - 1, p1.y + 1));
                towards.add(new Point(p1.x - 1, p1.y));
                towards.add(new Point(p1.x, p1.y + 1));
            } else {
                awayFrom.add(new Point(p1.x + 1, p1.y));
                awayFrom.add(new Point(p1.x + 1, p1.y + 1));
                awayFrom.add(new Point(p1.x + 1, p1.y - 1));
                awayFrom.add(new Point(p1.x, p1.y + 1));
                awayFrom.add(new Point(p1.x, p1.y - 1));

                towards.add(new Point(p1.x - 1, p1.y));
                towards.add(new Point(p1.x - 1, p1.y + 1));
                towards.add(new Point(p1.x - 1, p1.y - 1));
                towards.add(new Point(p1.x, p1.y + 1));
                towards.add(new Point(p1.x, p1.y - 1));
            }
        } else if (p1.x < p2.x) {
            if (p1.y < p2.y) {
                awayFrom.add(new Point(p1.x - 1, p1.y - 1));
                awayFrom.add(new Point(p1.x - 1, p1.y));
                awayFrom.add(new Point(p1.x, p1.y - 1));

                towards.add(new Point(p1.x + 1, p1.y + 1));
                towards.add(new Point(p1.x + 1, p1.y));
                towards.add(new Point(p1.x, p1.y + 1));
            } else if (p1.y > p2.y) {
                awayFrom.add(new Point(p1.x - 1, p1.y + 1));
                awayFrom.add(new Point(p1.x - 1, p1.y));
                awayFrom.add(new Point(p1.x, p1.y + 1));

                towards.add(new Point(p1.x + 1, p1.y - 1));
                towards.add(new Point(p1.x + 1, p1.y));
                towards.add(new Point(p1.x, p1.y - 1));
            } else {
                awayFrom.add(new Point(p1.x - 1, p1.y));
                awayFrom.add(new Point(p1.x - 1, p1.y + 1));
                awayFrom.add(new Point(p1.x - 1, p1.y - 1));
                awayFrom.add(new Point(p1.x, p1.y + 1));
                awayFrom.add(new Point(p1.x, p1.y - 1));

                towards.add(new Point(p1.x + 1, p1.y));
                towards.add(new Point(p1.x + 1, p1.y + 1));
                towards.add(new Point(p1.x + 1, p1.y - 1));
                towards.add(new Point(p1.x, p1.y + 1));
                towards.add(new Point(p1.x, p1.y - 1));
            }
        } else {
            if (p1.y > p2.y) {
                awayFrom.add(new Point(p1.x, p1.y + 1));
                awayFrom.add(new Point(p1.x - 1, p1.y + 1));
                awayFrom.add(new Point(p1.x + 1, p1.y + 1));
                awayFrom.add(new Point(p1.x - 1, p1.y));
                awayFrom.add(new Point(p1.x + 1, p1.y));

                towards.add(new Point(p1.x, p1.y - 1));
                towards.add(new Point(p1.x - 1, p1.y - 1));
                towards.add(new Point(p1.x + 1, p1.y - 1));
                towards.add(new Point(p1.x - 1, p1.y));
                towards.add(new Point(p1.x + 1, p1.y));
            } else if (p1.y < p2.y) {
                awayFrom.add(new Point(p1.x, p1.y - 1));
                awayFrom.add(new Point(p1.x - 1, p1.y - 1));
                awayFrom.add(new Point(p1.x + 1, p1.y - 1));
                awayFrom.add(new Point(p1.x - 1, p1.y));
                awayFrom.add(new Point(p1.x + 1, p1.y));

                towards.add(new Point(p1.x, p1.y + 1));
                towards.add(new Point(p1.x - 1, p1.y + 1));
                towards.add(new Point(p1.x + 1, p1.y + 1));
                towards.add(new Point(p1.x - 1, p1.y));
                towards.add(new Point(p1.x + 1, p1.y));
            } else {
                /* This should happen the FIRST time an object is born and want to move
                 * Or this could happen if an object has been trapped and can now move again
                 * If there are no previous direction, this turn should be random and the next
                 * move will be the same direction */ 
            }
        }

        /* Return appropriate list based on the boolean user input 'away' */
        return away ? awayFrom : towards;
    }
}
