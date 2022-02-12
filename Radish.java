import java.util.List;
import java.util.Random;

/**
 * Simple model of a radish.
 * Radishes age, spread (breed) and die.
 *
 * @author Reuben Atendido
 * @version (a version number or a date)
 */
public class Radish extends Animal
{
     // Characteristics shared by all radishes (class variables).

    // The age to which a radish can live.
    private static final int MAX_AGE = 6;
    // The likelihood of a radish spreading.
    private static final double BREEDING_PROBABILITY = 0.075;
    // The maximum number of plantings.
    private static final int MAX_LITTER_SIZE = 6;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //Plants do not move around so it being nocturnal is irrelevant
    //isNocturnal is always set to false.
    private static final boolean isNocturnal = false;
    
    // Individual characteristics (instance fields).
    
    // The radishes's age.
    private int age;
    //As a plant its assumed gender is irrelevant for spreading
    //Gender is set to always not female.
    private static final boolean isFemale = false;

    /**
     * Create a new radish. A rabbit may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the rabbit will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Radish(boolean randomAge, Field field, Location location, boolean isFemale)
    {
        super(field, location, isNocturnal, isFemale);
        age = 0;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
    }
    
    /**
     * This is what the radish does most of the time - it stays still 
     * Sometimes it will spread or die of old age.
     * @param newRadishes A list to return newly planted radishes.
     */
    public void act(List<Animal> newRadishes)
    {
        incrementAge();
        if(isAlive()){
            giveBirth(newRadishes);
        }
    }

    /**
     * Increase the age.
     * This could result in the radishes death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this radish is to spread at this step.
     * Newly spread plants will be made into free adjacent locations.
     * @param newRaadishes A list to return newly born radishes.
     */
    private void giveBirth(List<Animal> newRadishes)
    {
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Radish seed = new Radish(false, field, loc, false);
            newRadishes.add(seed);
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
