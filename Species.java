import java.util.List;
import java.util.Random;

/**
 * A class representing shared characteristics of species.
 * 
 * @author David J. Barnes and Michael Kölling
 *          with Reuben Atendido and Oliver Macpherson    
 * @version 2022.02.17 (3)
 */
public abstract class Species
{
    // Whether the species is alive or not.
    private boolean alive;
    // The species's field.
    private Field field;
    // The species's position in the field.
    private Location location;
    //Flag for if the specie is active at day or night.
    private boolean isNocturnal;
    //Flag for if the instance of a specie is female or not.
    protected boolean isFemale;
    //Flag for if the instance of a specie is diseased or not.
    protected boolean isInfected;
    /**
     * Create a new species at location in field.
     * Randomly determines whether an instance is male of female.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Species(Field field, Location location, boolean isNocturnal)//, boolean isDiseased)
    {
        alive = true;
        this.field = field;
        this.isNocturnal = isNocturnal;
        Random rand = Randomizer.getRandom();
        if(rand.nextInt(2)==0) {
            isFemale=true;
        }
        else {
            isFemale=false;
        }
        this.isInfected = false;
        setLocation(location);
    }
    
    /**
     * Make this species act - that is: make it do
     * whatever it wants/needs to do.
     * @param newSpecies A list to receive newly born/planted species.
     */
    abstract public void act(List<Species> newSpecies);

    /**
     * Check whether the species is alive or not.
     * @return true if the species is still alive.
     */
    protected boolean isAlive()
    {
        return alive;
    }

    /**
     * Indicate that the species is no longer alive.
     * It is removed from the field.
     */
    protected void setDead()
    {
        alive = false;
        if(location != null) {
            field.clear(location);
            location = null;
            field = null;
        }
    }

    /**
     * Return the species's location.
     * @return The species's location.
     */
    protected Location getLocation()
    {
        return location;
    }
    
    /**
     * Place the species at the new location in the given field.
     * @param newLocation The species's new location.
     */
    protected void setLocation(Location newLocation)
    {
        if(location != null) {
            field.clear(location);
        }
        location = newLocation;
        field.place(this, newLocation);
    }
    
    /**
     * Return the species's field.
     * @return The species's field.
     */
    protected Field getField()
    {
        return field;
    }
    /**
     * Return the specie's nocturnal flag
     * @return if the species is nocturnal true or false
     */
    protected boolean getIsNocturnal()
    {
        return isNocturnal;
    }
    /**
     * return the specie's female truth value
     * given this is false then the instance is male.
     */
    protected boolean gender()
    {
        return isFemale;
    }
    /**
     * return if the species is diseased or not
     */
    protected boolean isInfected(){
        return isInfected;
    }
    /**
     * toggles if the instance of a species is
     * positive with the disease.
     * @toggle true to false and vice versa
     */
    protected void toggleInfection()
    {
        isInfected = !isInfected;
    }
    
}
