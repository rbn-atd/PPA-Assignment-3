import java.util.List;
import java.util.Random;
import java.util.Iterator;

/**
 * A simple model of a fairy.
 * Rabbits age, move, breed, and die.
 * Designed as purely a prey species they survive by having
 * an exceptionally high breeding rate as well as birthing a larger litter.
 * 
 * @author David J. Barnes and Michael Kölling
 *          with Reuben Atendido and Oliver Macpherson
 * @version 2022.02.17 (2)
 */
public class Fairy extends Species
{
    // Characteristics shared by all fairys (class variables).

    // The age at which a fairy can start to breed.
    private static final int BREEDING_AGE = 1;
    // The age to which a fairy can live.
    private static final int MAX_AGE = 40;
    // The likelihood of a fairy breeding.
    private static final double BREEDING_PROBABILITY = 0.85;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 4;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //The probability a fairy becomes diseased during its existence.
    //The program attempts to infect it every step.
    private static final double DISEASE_PROBABILITY = 0.1;
    //This animal is not nocturnal (acts only during day)
    private static final boolean isNocturnal = false;
    
    // Individual characteristics (instance fields).
    
    // The fairy's age.
    private int age;
    //The fairy's gender
    private boolean isFemale;
    
    /**
     * Create a new fairy. A fairy may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the fairy will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Fairy(boolean randomAge, Field field, Location location)//, boolean isDiseased)
    {
        super(field, location, isNocturnal);//, isDiseased);
        age = 0;
        this.isFemale = isFemale;
        //this.isDiseased = isDiseased;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
        }
    }
    
    /**
     * This is what the fairy does most of the time - it runs 
     * around. Sometimes it will breed or die of old age.
     * This method has been modified to possibly disease a fairy every step.
     * @param newRabbits A list to return newly born fairys.
     */
    public void act(List<Species> newRabbits)
    {
        incrementAge();
        if(isAlive()) {
            giveBirth(newRabbits);
            if (rand.nextDouble() <= DISEASE_PROBABILITY ){
                toggleInfection();
            }
            // Try to move into a free location.
            Location newLocation = getField().freeAdjacentLocation(getLocation());
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
     * This could result in the fairy's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this fairy is to give birth at this step.
     * New births will be made into free adjacent locations.
     * Method has been modified to randomly toggle fairys to be female.
     * @param newRabbits A list to return newly born fairys.
     */
    private void giveBirth(List<Species> newRabbits)
    {
        // New fairys are born into adjacent locations.
        // Get a list of adjacent free locations.
        Random rand = Randomizer.getRandom();
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            //Rabbit young = new Rabbit(false, field, loc, isFemale);
            Fairy young = new Fairy(true, field, loc);
            if(rand.nextDouble() <= DISEASE_PROBABILITY) {
                young.toggleInfection();
            }
            newRabbits.add(young);
            //newRabbits.add(young);
        }
    }
        
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * Method has been modified to now make the species check adjacent fields
     * for a species of the same subtype as well as opposite isFemale flag i.e can only breed if opposite sexes.
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
     * A fairy can breed if it has reached the breeding age and has a mate.
     * @return true if the fairy can breed, false otherwise.
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
            if(animal instanceof Fairy) {
                Fairy fairy = (Fairy) animal;
                if(fairy.gender()!=this.gender()) {
                    return true;
                }
            }
        }
        return false;
    }
}
