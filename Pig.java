import java.util.Random;
import java.util.List;
import java.util.Iterator;

/**
 * A simple model of a pig
 * Pigs age, move, eat radishes and die.
 * Pigs are designed to live moderately long
 * and gain a larger amount of food value but also
 * lose 2 times as much hunger as other animals.
 *
 * @author Reuben Atendido
 * @version (a version number or a date)
 */
public class Pig extends Animal
{
    // Characteristics shared by all pigs (class variables).
    
    // The age at which a pig can start to breed.
    private static final int BREEDING_AGE = 10;
    // The age to which a pig can live.
    private static final int MAX_AGE = 500;
    // The likelihood of a pig breeding.
    private static final double BREEDING_PROBABILITY = 0.4;
    
    private static final double FEMALE_PROBABILITY = 0.5;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 4;
    // The food value of a single radish. In effect, this is the
    // number of steps a radish can go before it has to eat again.
    private static final int PLANT_FOOD_VALUE = 30;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //A flag which determines the time of day this animal moves.
    private static final boolean isNocturnal = false;
    // Individual characteristics (instance fields).
    // The pig's age.
    private int age;
    // The pig's food level, which is increased by eating rabbits.
    private int foodLevel;
    //The flag indicating the gender of a specific instance of a pig.
    private boolean isFemale;

    /**
     * Create a pig. A pig can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the pig will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     * @param isNocturnal The flag indicating what time of day this animal moves.
     * @param isFemale The flag indicating if the instance of a pig is female.
     */
    public Pig(boolean randomAge, Field field, Location location, boolean isFemale)
    {
        super(field, location, isNocturnal, isFemale);
        this.isFemale = isFemale;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(PLANT_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = PLANT_FOOD_VALUE;
        }
    }
    
    /**
     * This is what the pig does most of the time: it searches for
     * radishes. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newFoxes A list to return newly born pigs.
     */
    public void act(List<Animal> newPig)
    {
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newPig);            
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
     * Increase the age. This could result in the fox's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this pig more hungry. This could result in the pig's death.
     * Pig's are designed to lose their hunger much faster than other animals.
     */
    private void incrementHunger()
    {
        foodLevel=foodLevel-2;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Look for radishes adjacent to the current location.
     * Only the first radishe detected is eaten.
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
            Object animal = field.getObjectAt(where);
            if(animal instanceof Radish) {
                Radish radish = (Radish) animal;
                if(radish.isAlive()) { 
                    radish.setDead();
                    foodLevel = PLANT_FOOD_VALUE;
                    foodLocation = where;
                }
            }
        }
        return foodLocation;
    }
    
    /**
     * Check whether or not this pig is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newPigs A list to return newly born pigs.
     */
    private void giveBirth(List<Animal> newPigs)
    {
        // New foxes are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Pig young = new Pig(false, field, loc, isFemale);
            newPigs.add(young);
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
            Field field = getField();
            List<Location> adjacent = field.adjacentLocations(getLocation());
            Iterator<Location> it = adjacent.iterator();
            while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if (animal instanceof Pig){
                    Pig pig = (Pig) animal;
                    if(pig.getIsFemale() != getIsFemale()){
                        births = rand.nextInt(MAX_LITTER_SIZE) + 1;
                    }
                }
            }
        }
        return births;
    }

    /**
     * A pig can breed if it has reached the breeding age.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
}
