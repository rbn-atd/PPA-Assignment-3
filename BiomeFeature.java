import java.util.ArrayList;

/**
 * Write a description of class BiomeFeatures here.
 *
 * @author (your name)
 * @version (a version number or a date)
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
