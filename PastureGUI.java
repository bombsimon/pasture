import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class PastureGUI extends JFrame implements ActionListener {

    private final ImageIcon II_EMPTY      = new ImageIcon("empty.gif");
    private final int       SCALE         = 30;
    private final Engine          engine;

    private final JLabel[][]      grid;
    private final Map<Point, java.util.List<ImageIcon>> icons = 
            new HashMap<Point, java.util.List<ImageIcon>>();

    private final JLabel          clockLabel    = new JLabel("Time: 0");
    private final JLabel          entitiesLabel = new JLabel("Entities: 0");
    private final JLabel          sheepLabel = new JLabel("Sheeps: 0");
    private final JLabel          wolfLabel = new JLabel("Wolfs: 0");
    private final JLabel          plantLabel = new JLabel("Plants: 0");
    private final JButton         startButton   = new JButton("Start");
    private final JButton         stopButton    = new JButton("Stop");
    private final JButton         exitButton    = new JButton("Exit");

    /* Settings */
    /* Lists */
    private final String[]  visions = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" };
    private final String[]  speeds = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" };
    
    /* Sheep */
    private final JLabel    sheepV = new JLabel("Sheep vision", JLabel.RIGHT);
    private final JComboBox sheepVision = new JComboBox(visions);
    private final JLabel    sheepS = new JLabel("Sheep move delay", JLabel.RIGHT);
    private final JComboBox sheepSpeed = new JComboBox(speeds);
    
    /* Wolf */
    private final JLabel    wolfV = new JLabel("Wolf vision", JLabel.RIGHT);
    private final JComboBox wolfVision = new JComboBox(visions);
    private final JLabel    wolfS = new JLabel("Wolf move delay", JLabel.RIGHT);
    private final JComboBox wolfSpeed = new JComboBox(speeds);
    /* End settings */

    private final int height;
    private final int width;
    private int size = 0;
    private int sheep = 0;
    private int wolf = 0;
    private int plant = 0;

    /**
     * Creates a new instance of this class with the specified
     * settings for the pasture to display. */

    public PastureGUI(int width, int height, Engine engine) {       
        this.height = height;
        this.width = width;

        this.engine = engine;

        /* Display information on how to change settings */
        JOptionPane.showMessageDialog(
                this,
                "<html>" +
                        "<div style='width: 300px; margin-bottom: 10px;'>" +
                        "You can set the speed and vision for animals at the bottom in this program." +
                        "</div>" +
                        "<div style='width: 300px; margin-bottom: 10px;'>" +
                        "If you want to change even more details, all values are stored in 'pasture.proerties'!" +
                        "</div>",
                        "Welcome!",
                        JOptionPane.PLAIN_MESSAGE
                );

        setSize(width * SCALE, height * SCALE);

        startButton.addActionListener(this);
        stopButton.addActionListener(this);
        exitButton.addActionListener(this);

        sheepVision.addActionListener(this);
        sheepSpeed.addActionListener(this);
        wolfVision.addActionListener(this);
        wolfSpeed.addActionListener(this);

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(2,8));
        buttons.add(clockLabel);
        buttons.add(entitiesLabel);
        buttons.add(sheepLabel);
        buttons.add(wolfLabel);
        buttons.add(plantLabel);
        buttons.add(startButton);
        buttons.add(stopButton);
        buttons.add(exitButton);

        /* Add settings with some defaults */
        sheepVision.setSelectedIndex(4);
        buttons.add(sheepV);
        buttons.add(sheepVision);

        sheepSpeed.setSelectedIndex(9);
        buttons.add(sheepS);
        buttons.add(sheepSpeed);

        wolfVision.setSelectedIndex(2);
        buttons.add(wolfV);
        buttons.add(wolfVision);

        wolfSpeed.setSelectedIndex(7);
        buttons.add(wolfS);
        buttons.add(wolfSpeed);

        JPanel field = new JPanel();
        field.setBackground(new Color(27,204,89));
        field.setLayout(new GridLayout(height, width));
        grid = new JLabel[width][height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[x][y] = new JLabel(II_EMPTY);
                grid[x][y].setVisible(true);
                field.add(grid[x][y]);

            }
        }

        Container display = getContentPane();
        display.setBackground(new Color(27,204,89));
        display.setLayout(new BorderLayout());
        display.add(field,BorderLayout.CENTER);
        display.add(buttons,BorderLayout.SOUTH);

        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        exitButton.setEnabled(true);

        update();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }


    /**
     * This method is called when an action event has occured and
     * carries out the correct actions depending on the event. In this
     * class, this means that someone has pressed any of the buttons
     * start, stop, or exit.
     */
    public void actionPerformed(ActionEvent e) {
        for (JComboBox box : new JComboBox[]{sheepSpeed, sheepVision, wolfSpeed, wolfVision}) {
            if (e.getSource() == box) {
                String animal = (box == sheepSpeed || box == sheepVision) ? "sheep" : "wolf";
                String attr   = (box == sheepSpeed || box == wolfSpeed ) ? "move_delay" : "vision_length";
                String key    = String.format("%s.%s", animal, attr);
                String value  = box.getSelectedItem().toString();

                PastureProperties.getInstance().setValue(key, value);
            }
        }

        if (e.getSource() == startButton) {
            sheepVision.setEnabled(false);
            sheepSpeed.setEnabled(false);
            wolfVision.setEnabled(false);
            wolfSpeed.setEnabled(false);

            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            exitButton.setEnabled(true);
            engine.start();
        }
        else if (e.getSource() == stopButton) {
            sheepVision.setEnabled(true);
            sheepSpeed.setEnabled(true);
            wolfVision.setEnabled(true);
            wolfSpeed.setEnabled(true);

            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            exitButton.setEnabled(true);
            engine.stop();
        }
        else if (e.getSource() == exitButton) {
            System.exit(0);
        }
    }

    /**
     * The method addEntity is called to notify the GUI that an entity
     * has been added to a position. The icon of the added entity is
     * displayed at the position.
     */

    public void addEntity(Entity e, Point p) {
        ImageIcon icon = e.getImage();

        java.util.List<ImageIcon> l = icons.get(p);
        if (l==null) {
            l = new ArrayList<ImageIcon>();
            icons.put(p, l);
        }
        l.add(icon);

        grid[p.x][p.y].setIcon(icon);

        size++;

        if (e.getType().equals("sheep"))
            sheep++;

        if (e.getType().equals("wolf"))
            wolf++;

        if (e.getType().equals("plant"))
            plant++;
    }

    public void moveEntity(Entity e, Point old, Point ny) {
        removeEntity(e, old);
        addEntity(e, ny);
    }

    /**
     * The method removeEntity is called to notify the GUI that an
     * entity has been removed from a position. One icon among the
     * icons of the remaining entities is displayed at the position.
     */


    public void removeEntity(Entity e, Point p) {

        ImageIcon icon0 = e.getImage();

        java.util.List<ImageIcon> l = icons.get(p);
        l.remove(icon0);

        ImageIcon icon;
        if (l.isEmpty()) icon = II_EMPTY ;
        else icon = l.get(0);

        grid[p.x][p.y].setIcon(icon);

        size--;

        if (e.getType().equals("sheep"))
            sheep--;

        if (e.getType().equals("wolf"))
            wolf--;

        if (e.getType().equals("plant"))
            plant--;
    }

    public void update() {

        clockLabel.setText("Time: " + engine.getTime());
        entitiesLabel.setText("Entities: " + size);
        sheepLabel.setText("Sheeps: " + sheep);
        wolfLabel.setText("Wolfs: " + wolf);
        plantLabel.setText("Plants: " + plant);
    }
}

