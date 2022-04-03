import java.util.*;
import javax.swing.Timer;
import java.awt.event.*;

/**
 * The simulation is run by an internal timer that sends out a 'tick'
 * with a given interval. One tick from the timer means that each
 * entity in the pasture should obtain a tick. When an entity obtains
 * a tick, this entity is allowed to carry out their tasks according
 * to what kind they are. This could mean moving the entity, making
 * the entity starve from hunger, or producing a new offspring.
 */

public class Engine implements ActionListener {

    private final int   SPEED_REFERENCE = 1000; /* 1000 */
    private final int   speed           = 10;
    private final Timer timer           = new Timer(SPEED_REFERENCE/speed,this);
    private int         time            = 0;

    private Pasture pasture;


    public Engine (Pasture pasture) {
        this.pasture = pasture;
    }

    public void actionPerformed(ActionEvent event) {

        /* Since every tick can remove an object from the pasture, this loop
         * must make sure that things that can be removed never gets a call to tick()
         * when it's cone.
         * I.e. a wolf can eat a sheep so we need to tick() all sheeps before
         * wolfs and thus making sure an eaten sheep won't get a tick().
         * 
         * NOTE: If a sheep and wolf has the same speed, the sheep will run away until
         * it get's traped because it can move to the next square before the wolf
         * eats it. Hanv't really seen any problem with this though (not even with the same speed)
         */

        List<Entity> queue = pasture.getEntities();
        for (String type : new String[]{"plant", "sheep", "wolf", "fence"}) {
            for (Entity e : queue) {
                if (!e.getType().equals(type))
                    continue;

                e.tick();
            }
        }
        pasture.refresh();
        time++;
    }

    public void setSpeed(int speed) {
        timer.setDelay(SPEED_REFERENCE/speed);
    }

    public void start() {
        setSpeed(speed);
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public int getTime () {
        return time;
    }

}
