import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of an eagle.
 * eagles age, move, eat rabbits, and die.
 * Basically flying foxes, they compete with foxes to eat rabbits
 * whilst also eating radishes to make up for a lower breeding rate.
 * 
 * @author Reuben Atendido and Oliver Macpherson
 * @version 1
 */
public class Eagle extends Species
{
    // Characteristics shared by all eagles (class variables).
    
    // The age at which a eagle can start to breed.
    private static final int BREEDING_AGE = 14;
    // The age to which a eagle can live.
    private static final int MAX_AGE = 275;
    // The likelihood of a eagle breeding.
    private static final double BREEDING_PROBABILITY = 0.09;
    
    private static final double FEMALE_PROBABILITY = 0.5;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 5 ;
    // The food value of a single rabbit or radish. In effect, this is the
    // number of steps an eagle can go before it has to eat again.
    private static final int RABBIT_FOOD_VALUE = 20;
    
    private static final int RADISH_FOOD_VALUE = 12;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //boolean flag to determine if this species is nocturnal or not
    //This species is not nocturnal.
    private static final boolean isNocturnal = false;
    // Individual characteristics (instance fields).
    // The eagles's age.
    private int age;
    // The eagles's food level, which is increased by eating rabbits.
    private int foodLevel;
    // The eagle's sex, male or female, which is randomly assigned at birth.
    private boolean isFemale;

    /**
     * Create an eagle. An eagle can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the eagle will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param isNocturnal The flag indicating what time of day this species moves.
     * @param isFemale The flag indicating if the instance of an eagle is female.
     */
    public Eagle(boolean randomAge, Field field, Location location)
    {
        super(field, location, isNocturnal);
        this.isFemale = isFemale;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(RABBIT_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = RABBIT_FOOD_VALUE;
        }
    }
    
    /**
     * This is what the eagle does most of the time: it hunts for
     * rabbits. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newEagles A list to return newly born eagles.
     */
    public void act(List<Species> newEagles)
    {
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newEagles);            
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
     * Increase the age. This could result in the eagle's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this eagle more hungry. This could result in the eagle's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for rabbits adjacent to the current location.
     * The method has been modified to allow eagles to eat
     * every adjacent rabbit or radish found.
     * @return The last location where a rabbit was found.
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
            if(species instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) species;
                if(rabbit.isAlive()) { 
                    rabbit.setDead();
                    foodLevel = RABBIT_FOOD_VALUE;
                    foodLocation = where;
                }
            }
            else if(species instanceof Radish) {
                Radish radish = (Radish) species;
                if(radish.isAlive()) { 
                    radish.setDead();
                    foodLevel = RADISH_FOOD_VALUE;
                    foodLocation = where;
                }
            }
        }
        return foodLocation;
    }
    
    /**
     * Check whether or not this eagle is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newEagles A list to return newly born eagles.
     */
    private void giveBirth(List<Species> newEagles)
    {
        // New eagles are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Eagle young = new Eagle(false, field, loc);
            newEagles.add(young);
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
     * An eagle can breed if it has reached the breeding age.
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
            if(animal instanceof Eagle) {
                Eagle eagle = (Eagle) animal;
                if(eagle.gender()!=this.gender()) {
                    return true;
                }
            }
        }
        return false;
    }
}