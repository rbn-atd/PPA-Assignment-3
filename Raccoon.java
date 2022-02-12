import java.util.List;
import java.util.Random;
import java.util.Iterator;

/**
 * A simple model of a raccoon;
 * Raccoons age, move, eat radish and die.
 *
 * @author Reuben Atendido
 * @version (a version number or a date)
 */
public class Raccoon extends Animal
{
    // Characteristics shared by all raccoon (class variables).

    // The age at which a raccoon can start to breed.
    private static final int BREEDING_AGE = 10;
    // The age to which a raccoon can live.
    private static final int MAX_AGE = 70;
    // The likelihood of a raccoon breeding.
    private static final double BREEDING_PROBABILITY = 0.08;
    //The likelihood of a birth being female;
    private static final double FEMALE_PROBABILITY = 0.5;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 6;
    
    private static final int RADISH_FOOD_VALUE = 20;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    
    private static final boolean isNocturnal = true;
    
    // Individual characteristics (instance fields).
    
    // The raccoon's age.
    private int age;
    //The raccoon's gender
    private boolean isFemale;
    
    private int foodLevel;
    
    /**
     * Create a new raccoon. A raccoon may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the raccoon will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Raccoon(boolean randomAge, Field field, Location location, boolean isFemale)
    {
        super(field, location, isNocturnal, isFemale);
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
    public void act(List<Animal> newRaccoons)
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
    private void giveBirth(List<Animal> newRaccoons)
    {
        // New rabbits are born into adjacent locations.
        // Get a list of adjacent free locations.
        Random rand = Randomizer.getRandom();
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            if(rand.nextDouble() <= FEMALE_PROBABILITY){
                     Raccoon young = new Raccoon(true, field, loc, true);
                     newRaccoons.add(young);
            }
            else{
                     Raccoon young = new Raccoon(true, field, loc, false);
                     newRaccoons.add(young);
            }
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
            Object animal = field.getObjectAt(where);
            if(animal instanceof Radish ) {
                Radish radish = (Radish) animal;
                if(radish.isAlive()) { 
                    radish.setDead();
                    foodLevel = RADISH_FOOD_VALUE;
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
            Field field = getField();
            List<Location> adjacent = field.adjacentLocations(getLocation());
            Iterator<Location> it = adjacent.iterator();
            while(it.hasNext()) {
                Location where = it.next();
                Object animal = field.getObjectAt(where);
                if (animal instanceof Raccoon){
                    Raccoon raccoon = (Raccoon) animal;
                    if(raccoon.getIsFemale() != getIsFemale()){
                        births = rand.nextInt(MAX_LITTER_SIZE) + 1;
                    }
                }
            }
        }
        return births;
    }

    /**
     * A raccoon can breed if it has reached the breeding age.
     * @return true if the rabbit can breed, false otherwise.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE;
    }
    
    /**
     * Getter function to return if the instance of an animal
     * is female or not.
     */
    private boolean isFemale()
    {
        return isFemale;
    }
}
