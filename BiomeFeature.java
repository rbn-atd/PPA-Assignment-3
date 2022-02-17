import java.util.ArrayList;

/**
 * Simple model of broad biome features for the grid e.g.
 * rivers 
 *
 * @author Oliver Macpherson
 * @version 1
 */
public class BiomeFeature
{
    // instance variables - replace the example below with your own
    protected ArrayList<Class> canMoveThrough;
    protected Field field;
    protected Location location;

    /**
     * Constructor for objects of class BiomeFeatures
     */
    public BiomeFeature(Field field, Location location)
    {
        // initialise instance variables
        canMoveThrough = new ArrayList<>();
        this.field = field;
        this.location = location;
    }
}
