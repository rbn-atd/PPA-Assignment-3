import java.util.List;
import java.util.Random;
import java.util.Iterator;

/**
 * A simple model of a rabbit.
 * Rabbits age, move, breed, and die.
 * Designed as purely a prey species they survive by having
 * an exceptionally high breeding rate as well as birthing a larger litter.
 * 
 * @author David J. Barnes and Michael Kölling
 *          with Reuben Atendido and Oliver Macpherson
 * @version 2022.02.17 (2)
 */
public class Rabbit extends Species
{
    // Characteristics shared by all rabbits (class variables).

    // The age at which a rabbit can start to breed.
    private static final int BREEDING_AGE = 1;
    // The age to which a rabbit can live.
    private static final int MAX_AGE = 40;
    // The likelihood of a rabbit breeding.
    private static final double BREEDING_PROBABILITY = 0.9;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 4;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //The probability a rabbit becomes diseased during its existence.
    //The program attempts to infect it every step.
    private static final double DISEASE_PROBABILITY = 0.1;
    //This animal is not nocturnal (acts only during day)
    private static final boolean isNocturnal = false;
    
    // Individual characteristics (instance fields).
    
    // The rabbit's age.
    private int age;
    //The rabbit's gender
    private boolean isFemale;
    
    /**
     * Create a new rabbit. A rabbit may be created with age
     * zero (a new born) or with a random age.
     * 
     * @param randomAge If true, the rabbit will have a random age.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Rabbit(boolean randomAge, Field field, Location location)//, boolean isDiseased)
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
     * This is what the rabbit does most of the time - it runs 
     * around. Sometimes it will breed or die of old age.
     * This method has been modified to possibly disease a rabbit every step.
     * @param newRabbits A list to return newly born rabbits.
     */
    public void act(List<Species> newRabbits)
    {
        incrementAge();
        if(isAlive()) {
            giveBirth(newRabbits);
            if (rand.nextDouble() <= DISEASE_PROBABILITY ){
                toggleIsDiseased();
                System.out.println("Rabbit diseased");
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
     * This could result in the rabbit's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Check whether or not this rabbit is to give birth at this step.
     * New births will be made into free adjacent locations.
     * Method has been modified to randomly toggle rabbits to be female.
     * @param newRabbits A list to return newly born rabbits.
     */
    private void giveBirth(List<Species> newRabbits)
    {
        // New rabbits are born into adjacent locations.
        // Get a list of adjacent free locations.
        Random rand = Randomizer.getRandom();
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            //Rabbit young = new Rabbit(false, field, loc, isFemale);
            Rabbit young = new Rabbit(true, field, loc);
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
     * A rabbit can breed if it has reached the breeding age and has a mate.
     * @return true if the rabbit can breed, false otherwise.
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
            if(animal instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) animal;
                if(rabbit.gender()!=this.gender()) {
                    return true;
                }
            }
        }
        return false;
    }
}
