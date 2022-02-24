import java.util.List;
import java.util.Random;

/**
 * Simple model of a faegrass.
 * Faegrasses age, spread (breed) and die.
 * Feeding the vegetarian populations, with a relatively high spreading rate
 * and extremely short life span.
 *
 * @author Reuben Atendido and Oliver Macpherson
 * @version 1
 */
public class Faegrass extends Species
{
     // Characteristics shared by all faegrasses (class variables).

    // The age to which a faegrass can live.
    private static final int MAX_AGE = 2;
    // The likelihood of a faegrass spreading.
    private static final double BREEDING_PROBABILITY = 0.4;
    // The maximum number of plantings.
    private static final int MAX_LITTER_SIZE = 100;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    
    
    
    // The faegrasses's age.
    private int age;

    /**
     * Create a new faegrass. A faegrass may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the faegrass will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Faegrass(boolean randomAge, Field field, Location location)
    {
        super(field, location, false);
        age = 0;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
    }
    
    /**
     * This is what the faegrass does most of the time - it stays still 
     * Sometimes it will spread or die of old age.
     * @param newFaegrasses A list to return newly planted faegrasses.
     */
    public void act(List<Species> newFaegrasses)
    {
        incrementAge();
        if(isAlive()){
            giveBirth(newFaegrasses);
        }
    }

    /**
     * Increase the age.
     * This could result in the faegrasses death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this faegrass is to spread at this step.
     * Newly spread plants will be made into free adjacent locations.
     * @param newFaegrasses A list to return newly born faegrasses.
     */
    private void giveBirth(List<Species> newFaegrasses)
    {
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Faegrass seed = new Faegrass(false, field, loc);
            newFaegrasses.add(seed);
        }
    }
        
    /**
     * Generate a number representing the number of spread plants.
     * if it can spread.
     * @return The number of spread plants (may be zero).
     */
    private int breed()
    {
        int births = 0;
        if(rand.nextDouble() <= BREEDING_PROBABILITY) {
            births = rand.nextInt(MAX_LITTER_SIZE) + 1;
        }
        return births;
    }
}
