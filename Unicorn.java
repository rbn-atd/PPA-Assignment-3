import java.util.Random;
import java.util.List;
import java.util.Iterator;

/**
 * A simple model of a unicorn
 * Unicorns age, move, eat faegrasses and die.
 * They gain a larger amount of food value via faegrasses but also
 * lose 2 times as much hunger as other species.
 *
 * @author Reuben Atendido and Oliver Macpherson
 * @version 1
 */
public class Unicorn extends Species
{
    // Characteristics shared by all unicorns (class variables).
    
    // The age at which a unicorn can start to breed.
    private static final int BREEDING_AGE = 10;
    // The age to which a unicorn can live.
    private static final int MAX_AGE = 500;
    // The likelihood of a unicorn breeding.
    private static final double BREEDING_PROBABILITY = 0.8;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 3;
    // The food value of a single faegrass. In effect, this is the
    // number of steps a faegrass can go before it has to eat again.
    private static final int FAEGRASS_FOOD_VALUE = 30;
    //The maximum a pif can eat before becoming full
    private static final int MAX_HUNGER = 150;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //A flag which determines the time of day this species moves.
    private static final boolean isNocturnal = false;
    // Individual characteristics (instance fields).
    // The unicorn's age.
    private int age;
    // The unicorn's food level, which is increased by eating.
    private int foodLevel;

    /**
     * Create a unicorn. A unicorn can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the unicorn will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param isNocturnal The flag indicating what time of day this species moves.
     * @param isFemale The flag indicating if the instance of a unicorn is female.
     */
    public Unicorn(boolean randomAge, Field field, Location location)
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
     * This is what the unicorn does most of the time: it searches for
     * faegrasses. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newGrendlees A list to return newly born unicorns.
     */
    public void act(List<Species> newUnicorn)
    {
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newUnicorn);            
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
     * Increase the age. This could result in the grendle's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this unicorn more hungry. This could result in the unicorn's death.
     * Unicorn's lose 2 food level vs the usual 1
     */
    private void incrementHunger()
    {
        foodLevel=foodLevel-2;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for faegrasses adjacent to the current location.
     * All adjacent faegrasses detected are eaten.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        Location foodLocation = null;
        while(it.hasNext()) {
            Location where = it.next();
            Object species = field.getObjectAt(where);
            if(species instanceof Faegrass) {
                Faegrass faegrass = (Faegrass) species;
                if(faegrass.isAlive()) { 
                    faegrass.setDead();
                    if(foodLevel+FAEGRASS_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += FAEGRASS_FOOD_VALUE;
                    }
                    else {
                        foodLevel = MAX_HUNGER;
                    }
                    foodLocation = where;
                }
            }
        }
        return foodLocation;
    }
    
    /**
     * Check whether or not this unicorn is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newUnicorns A list to return newly born unicorns.
     */
    private void giveBirth(List<Species> newUnicorns)
    {
        // New grendlees are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Unicorn young = new Unicorn(false, field, loc);
            newUnicorns.add(young);
        }
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
     * A unicorn can breed if it has reached the breeding age and has a mate.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE && hasMate();
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
            if(animal instanceof Unicorn) {
                Unicorn unicorn = (Unicorn) animal;
                if(unicorn.gender()!=this.gender()) {
                    return true;
                }
            }
        }
        return false;
    }
}
