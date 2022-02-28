
/**
 * Model of a river, taking up grid space and only allowing certain animals to pass through it.
 * only certain animals may spawn aswell.
 *
 * @author Reuben Atendido and Oliver Macpherson
 * @version 1
 */
public class River extends BiomeFeature
{
    // instance variables - replace the example below with your own

    /**
     * Constructor for objects of class River
     */
    public River(Field field, Location location)
    {
        // initialise instance variables
        super(field, location);
        setLocation(location);
    }
    
      /**
     * Allows certain species to pass through grid space occupied by River.
     */
    private void initialiseAnimalMovement()
    {
        canMoveThrough.add(Daemon.class);
        canMoveThrough.add(Titan.class);
    }
    
    /**
     * Place river object onto a new location on the grid.
     */
    protected void setLocation(Location newLocation)
    {
        if(location != null) {
            field.clear(location);
        }
        location = newLocation;
        field.place(this, newLocation);
    }
}
