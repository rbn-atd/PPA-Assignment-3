import java.util.List;
import java.util.Random;
import java.util.Iterator;

/**
 * A simple model of a raccoon;
 * Raccoons age, move, eat radish and die.
 * Essentially vegetarian foxes, only eating radishes.
 * They live shorter but yield a larger litter size.
 *
 * @author Reuben Atendido and Oliver Macpherson
 * @version 1
 */
public class Raccoon extends Species
{
    // Characteristics shared by all raccoon (class variables).

    // The age at which a raccoon can start to breed.
    private static final int BREEDING_AGE = 10;
    // The age to which a raccoon can live.
    private static final int MAX_AGE = 200;
    // The likelihood of a raccoon breeding.
    private static final double BREEDING_PROBABILITY = 0.18;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 6;
    //The food value gained by eating radishes
    private static final int RADISH_FOOD_VALUE = 20;
    //The max a raccoon can eat before it is full
    private static final int MAX_HUNGER = 80;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //Flag for whether the animal is nocturnal
    private static final boolean isNocturnal = true;
    
    // Individual characteristics (instance fields).
    
    // The raccoon's age.
    private int age;
    //How many steps the raccoon can move before dying of hunger. Increased by eating
    private int foodLevel;
    
    /**
     * Create a new raccoon. A raccoon may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the raccoon will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Raccoon(boolean randomAge, Field field, Location location)
    {
        super(field, location, isNocturnal);
        this.isFemale = isFemale;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(RADISH_FOOD_VALUE);
        }
        else {
            age = 0;
            foodLevel = RADISH_FOOD_VALUE;
        }
    }
    
    /**
     * This is what the raccoon does most of the time - it runs 
     * around. Sometimes it will breed or die of old age.
     * @param newRaccoons A list to return newly born raccoon.
     */
    public void act(List<Species> newRaccoons)
    {
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newRaccoons);            
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
     * This could result in the raccoon's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this fox more hungry. This could result in the raccoon's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this raccoon is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newRaccoons A list to return newly born raccoon.
     */
    private void giveBirth(List<Species> newRaccoons)
    {
        // New rabbits are born into adjacent locations.
        // Get a list of adjacent free locations.
        Random rand = Randomizer.getRandom();
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            
            Raccoon young = new Raccoon(true, field, loc);
            newRaccoons.add(young);
        }
    }
        
    /**
     * Look for radishes adjacent to the current location.
     * The method has been modified such that the raccoon
     * eats every adjacent radish.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        Location radishLocation = null;
        while(it.hasNext()) {
            Location where = it.next();
            Object species = field.getObjectAt(where);
            if(species instanceof Radish ) {
                Radish radish = (Radish) species;
                if(radish.isAlive()) { 
                    radish.setDead();
                    if(foodLevel+RADISH_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += RADISH_FOOD_VALUE;
                    }
                    else {
                        foodLevel = MAX_HUNGER;
                    }
                    radishLocation = where;
                }
            }
        }
        return radishLocation;
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
     * A raccoon can breed if it has reached the breeding age and has a mate.
     * @return true if the raccoon can breed, false otherwise.
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
            if(animal instanceof Raccoon) {
                Raccoon raccoon = (Raccoon) animal;
                if(raccoon.gender()!=this.gender()) {
                    return true;
                }
            }
        }
        return false;
    }
}
