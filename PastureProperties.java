/*
 * PastureProperties is a singleton class which holds only one instance of itself
 * This class is used to get properties wide over the program.
 * 
 * The user can ask for the key-value by either getValue (String) or getIntValue (Integer)
 */
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PastureProperties {
    private static PastureProperties globalProperties;
    private Properties prop = new Properties();        

    private PastureProperties() {
        InputStream inStream = getClass().getClassLoader().getResourceAsStream("pasture.properties");

        try {
            prop.load(inStream);
        } catch (IOException e) {
            System.out.println("Could not read the properties");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static PastureProperties getInstance() {
        if (globalProperties == null) {
            globalProperties = new PastureProperties();
        }

        return globalProperties;
    }

    public String getValue(String key) {
        return prop.getProperty(key, null);
    }

    public int getIntValue(String key) {
        return Integer.parseInt(prop.getProperty(key, null));
    }

    public void setValue(String key, String value) {
        prop.setProperty(key, value);
    }
}
