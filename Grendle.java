import java.util.List;
import java.util.Iterator;
import java.util.Random;

/**
 * A simple model of a grendle.
 * Grendlees age, move, eat fairies, unicorns and daemons, and die.
 * Grendlees are the most average of species, not having any outstanding
 * traits. They thrive off the abundance of fairies, but are also probable to becoming
 * infected if eating a diseased fairies.
 *  
 * @author David J. Barnes and Michael Kölling + Reuben Atendido and Oliver Macpherson
 * @version 2022.02.21 (2)
 */
public class Grendle extends Species
{
    // Characteristics shared by all grendlees (class variables).
    
    // The age at which a grendle can start to breed.
    private static final int BREEDING_AGE = 14;
    // The age to which a grendle can live.
    private static final int MAX_AGE = 275;
    // The likelihood of a grendle breeding.
    private static final double BREEDING_PROBABILITY = 0.15;
    // The maximum number of births.
    private static final int MAX_LITTER_SIZE = 4;
    // The food values of a single fairy. In effect, this is the
    // number of steps a grendle can go before it has to eat again.
    private static final int FAIRY_FOOD_VALUE = 25;
    //The food value of a single unicorn.
    private static final int UNICORN_FOOD_VALUE = 25;
    //The food value of a single daemon
    private static final int DAEMON_FOOD_VALUE = 20;
    //The max a grendle can eat before it is full
    private static final int MAX_HUNGER = 70;
    //The probability a grendle will become infected when eating an infected fairy.
    private static final double DISEASE_PROBABILITY = 0.9;
    // A shared random number generator to control breeding.
    private static final Random rand = Randomizer.getRandom();
    //This species only acts at night.
    private static final boolean isNocturnal = true;
    // Individual characteristics (instance fields).
    // The grendle's age.
    private int age;
    // The grendle's food level, which is increased by eating.
    private int foodLevel;
    //How much hunger is lost per step
    private int hungerLoss;
    
    /**
     * Create a grendle. A grendle can be created as a new born (age zero
     * and not hungry) or with a random age and food level.
     * 
     * @param randomAge If true, the grendle will have random age and hunger level.
     * @param field The field currently occupied.
     * @param location The location within the field.
     */
    public Grendle(boolean randomAge, Field field, Location location)
    {
        super(field, location, isNocturnal);
        this.isFemale = isFemale;
        this.hungerLoss = 1;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt(FAIRY_FOOD_VALUE+UNICORN_FOOD_VALUE+DAEMON_FOOD_VALUE/3);
        }
        else {
            age = 0;
            foodLevel = FAIRY_FOOD_VALUE+UNICORN_FOOD_VALUE+DAEMON_FOOD_VALUE/3;
        }
    }
    
    /**
     * This is what the grendle does most of the time: it hunts for
     * fairies. In the process, it might breed, die of hunger,
     * or die of old age.
     * @param field The field currently occupied.
     * @param newGrendlees A list to return newly born grendlees.
     */
    public void act(List<Species> newGrendlees)
    {   
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newGrendlees);            
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
     * Make this grendle more hungry. This could result in the grendle's death.
     */
    private void incrementHunger()
    {
        foodLevel = foodLevel - hungerLoss;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Mutator method which increases a grendlees hunger loss when it eats
     * an infected fairy.
     */
    
    private void diseaseEffect()
    {
      hungerLoss = 7;  
    }
    
    /**
     * Look for food adjacent to the current location.
     * Modified so that the grendle eats every adjacent species.
     * @return Where food was found
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
            if(species instanceof Fairy ) {
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
                    if(fairy.isInfected() && rand.nextDouble() <= DISEASE_PROBABILITY){
                        toggleInfection();
                        diseaseEffect();
                    }
                }
            }
            else if(species instanceof Unicorn) {
                Unicorn unicorn = (Unicorn) species;
                if(unicorn.isAlive()) { 
                    unicorn.setDead();
                    if(foodLevel+UNICORN_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += UNICORN_FOOD_VALUE;
                    }
                    else {
                        foodLevel = MAX_HUNGER;
                    }
                    foodLocation = where;
                }
            }
            else if(species instanceof Daemon) {
                Daemon daemon = (Daemon) species;
                if(daemon.isAlive()) { 
                    daemon.setDead();
                    if(foodLevel+DAEMON_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += DAEMON_FOOD_VALUE;
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
     * Check whether or not this grendle is to give birth at this step.
     * New births will be made into free adjacent locations.
     * Modified to randomly generate female grendlees
     * @param newGrendlees A list to return newly born grendlees.
     */
    private void giveBirth(List<Species> newGrendlees)
    {
        // New grendlees are born into adjacent locations.
        // Get a list of adjacent free locations.
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Grendle young = new Grendle(true, field, loc);
            if(rand.nextDouble() <= DISEASE_PROBABILITY){
                young.toggleInfection();
            }
            newGrendlees.add(young);
        }
    }
        
    /**
     * Generate a number representing the number of births,
     * if it can breed.
     * Checks for if the adjacent animal is a grendle and is opposite sex.
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
     * A grendle can breed if it has reached the breeding age and has a mate.
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
            if(animal instanceof Grendle) {
                Grendle grendle = (Grendle) animal;
                if(grendle.gender()!=this.gender()) {
                    return true;
                }
            }
        }
        return false;
    }
}
