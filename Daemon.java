import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of an daemon.
 * daemons age, move, eat fairys, faegrasses, gnomes, and grendlees, and die.
 * Basically flying grendlees, they compete with grendlees to eat fairys and others
 * whilst also eating faegrasses to make up for a lower breeding rate.
 * 
 * @author Reuben Atendido and Oliver Macpherson
 * @version 2022/02/21 (1)
 */
public class Daemon extends Species
{
    // Characteristics shared by all daemons (class variables).
    
    // The age at which a daemon can start to breed.
    private static final int BREEDING_AGE = 14;
    // The age to which a daemon can live.
    private static final int MAX_AGE = 275;
    // The likelihood of a daemon breeding.
    private static final double BREEDING_PROBABILITY = 0.03;
    
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 4 ;
    // The food value of a single fairy. In effect, this is the
    // number of steps an daemon can go before it has to eat again.
    private static final int FAIRY_FOOD_VALUE = 20;
    //The food value of a single faegrass.
    private static final int FAEGRASS_FOOD_VALUE = 12;
    //The food value of a single gnome
    private static final int GNOME_FOOD_VALUE = 20;
    //The food value of a single grendle.
    private static final int GRENDLE_FOOD_VALUE = 20;
    //The maximum food daemon can consume before being "full"
    private static final int MAX_HUNGER = 50;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //boolean flag to determine if this species is nocturnal or not
    //This species is not nocturnal.
    private static final boolean isNocturnal = false;
    // Individual characteristics (instance fields).
    // The daemons's age.
    private int age;
    // The daemons's food level, which is increased by eating.
    private int foodLevel;
    // The daemon's sex, male or female, which is randomly assigned at birth.
    private boolean isFemale;

    /**
     * Create an daemon. An daemon can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the daemon will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param isNocturnal The flag indicating what time of day this species moves.
     * @param isFemale The flag indicating if the instance of an daemon is female.
     */
    public Daemon(boolean randomAge, Field field, Location location)
    {
        super(field, location, isNocturnal);
        this.isFemale = isFemale;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(FAIRY_FOOD_VALUE+FAEGRASS_FOOD_VALUE+GNOME_FOOD_VALUE+GRENDLE_FOOD_VALUE/4);
        }
        else {
            age = 0;
            foodLevel = (FAIRY_FOOD_VALUE+FAEGRASS_FOOD_VALUE+GNOME_FOOD_VALUE+GRENDLE_FOOD_VALUE/4);
        }
    }
    
    /**
     * This is what the daemon does most of the time: it hunts for
     * fairys. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newDaemons A list to return newly born daemons.
     */
    public void act(List<Species> newDaemons)
    {
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newDaemons);            
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
     * Increase the age. This could result in the daemon's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this daemon more hungry. This could result in the daemon's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for fairys adjacent to the current location.
     * The method has been modified to allow daemons to eat
     * every adjacent fairy or faegrass found.
     * @return The last location where a fairy was found.
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
            if(species instanceof Fairy) {
                Fairy fairy = (Fairy) species;
                if(fairy.isAlive()) { 
                    fairy.setDead();
                    if(foodLevel+FAIRY_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += FAIRY_FOOD_VALUE;
                    }
                    else {
                        foodLevel = MAX_HUNGER;
                    }
                    foodLocation = where;
                }
            }
            else if(species instanceof Faegrass) {
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
            else if(species instanceof Gnome) {
                Gnome gnome = (Gnome) species;
                if(gnome.isAlive()) { 
                    gnome.setDead();
                    if(foodLevel+GNOME_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += GNOME_FOOD_VALUE;
                    }
                    else {
                        foodLevel = MAX_HUNGER;
                    }
                    foodLocation = where;
                }
            }
            else if(species instanceof Grendle) {
                Grendle grendle = (Grendle) species;
                if(grendle.isAlive()) { 
                    grendle.setDead();
                    if(foodLevel+GRENDLE_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += GRENDLE_FOOD_VALUE;
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
     * Check whether or not this daemon is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newDaemons A list to return newly born daemons.
     */
    private void giveBirth(List<Species> newDaemons)
    {
        // New daemons are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Daemon young = new Daemon(false, field, loc);
            newDaemons.add(young);
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
     * An daemon can breed if it has reached the breeding age and has a mate.
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
            if(animal instanceof Daemon) {
                Daemon daemon = (Daemon) animal;
                if(daemon.gender()!=this.gender()) {
                    return true;
                }
            }
        }
        return false;
    }
}