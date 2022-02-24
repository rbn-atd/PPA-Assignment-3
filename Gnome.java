import java.util.List;
import java.util.Random;
import java.util.Iterator;

/**
 * A simple model of a gnome;
 * Gnomes age, move, eat faegrass and die.
 * Essentially vegetarian grendlees, only eating faegrasses.
 * They live shorter but yield a larger litter size.
 *
 * @author Reuben Atendido and Oliver Macpherson
 * @version 1
 */
public class Gnome extends Species
{
    // Characteristics shared by all gnome (class variables).

    // The age at which a gnome can start to breed.
    private static final int BREEDING_AGE = 10;
    // The age to which a gnome can live.
    private static final int MAX_AGE = 200;
    // The likelihood of a gnome breeding.
    private static final double BREEDING_PROBABILITY = 0.25;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 6;
    //The food value gained by eating faegrasses
    private static final int FAEGRASS_FOOD_VALUE = 20;
    //The max a gnome can eat before it is full
    private static final int MAX_HUNGER = 80;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //Flag for whether the animal is nocturnal
    private static final boolean isNocturnal = true;
    
    // Individual characteristics (instance fields).
    
    // The gnome's age.
    private int age;
    //How many steps the gnome can move before dying of hunger. Increased by eating
    private int foodLevel;
    
    /**
     * Create a new gnome. A gnome may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the gnome will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Gnome(boolean randomAge, Field field, Location location)
    {
        super(field, location, isNocturnal);
        this.isFemale = isFemale;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(FAEGRASS_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = FAEGRASS_FOOD_VALUE;
        }
    }
    
    /**
     * This is what the gnome does most of the time - it runs 
     * around. Sometimes it will breed or die of old age.
     * @param newGnomes A list to return newly born gnome.
     */
    public void act(List<Species> newGnomes)
    {
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newGnomes);            
            // Move towards a source of food if found.
            Location newLocation = findFood();
            if(newLocation == null) { 
                // No food found - try to move to a free location.
                newLocation = getField().freeAdjacentLocation(getLocation());
            }
            // See if it was possible to move.
            if(newLocation != null) {
                setLocation(newLocation);
            }
            else {
                // Overcrowding.
                setDead();
            }
        }
    }

    /**
     * Increase the age.
     * This could result in the gnome's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this grendle more hungry. This could result in the gnome's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this gnome is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newGnomes A list to return newly born gnome.
     */
    private void giveBirth(List<Species> newGnomes)
    {
        // New gnomes are born into adjacent locations.
        // Get a list of adjacent free locations.
        Random rand = Randomizer.getRandom();
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            
            Gnome young = new Gnome(true, field, loc);
            newGnomes.add(young);
        }
    }
        
    /**
     * Look for faegrasses adjacent to the current location.
     * The method has been modified such that the gnome
     * eats every adjacent faegrass.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        Location faegrassLocation = null;
        while(it.hasNext()) {
            Location where = it.next();
            Object species = field.getObjectAt(where);
            if(species instanceof Faegrass ) {
                Faegrass faegrass = (Faegrass) species;
                if(faegrass.isAlive()) { 
                    faegrass.setDead();
                    if(foodLevel+FAEGRASS_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += FAEGRASS_FOOD_VALUE;
                    }
                    else {
                        foodLevel = MAX_HUNGER;
                    }
                    faegrassLocation = where;
                }
            }
        }
        return faegrassLocation;
    }
    
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * @return The number of births (may be zero).
     */
    private int breed()
    {
        int births = 0;
        if(canBreed() && rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }

    /**
     * A gnome can breed if it has reached the breeding age and has a mate.
     * @return true if the gnome can breed, false otherwise.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE &&hasMate();
    }
    
    /**
     * Checks whether animal has a mate available
     */
    private boolean hasMate()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Gnome) {
                Gnome gnome = (Gnome) animal;
                if(gnome.gender()!=this.gender()) {
                    return true;
                }
            }
        }
        return false;
    }
}
