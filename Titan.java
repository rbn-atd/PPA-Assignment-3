import java.util.Random;
import java.util.List;
import java.util.Iterator;

/**
 * A simple model of a titan.
 * Titans age, move, eats fairies , grendlees, gnomes, unicorns, and faegrasses, and die.
 * They live moderately long lives and are benefitted by
 * being able to eat the widest range of species on the grid
 *
 * @author Oliver Macpherson and Reuben Atendido
 * @version 2022/02/21 (2)
 */
public class Titan extends Species
{
    //The age at which the titan can breed
    private static final int BREEDING_AGE = 10;
    //The max age which a titan can live to
    private static final int MAX_AGE = 300;
    //The probability of a titan breeding
    private static final double BREEDING_PROBABILITY = 0.02;
    //The maximum size of a litter of titan cubs
    private static final int MAX_LITTER_SIZE = 2;
    //The food value gained by eating fairies
    private static final int FAIRY_FOOD_VALUE = 15;
    //The food value gained by eating gnomes
    private static final int GNOME_FOOD_VALUE = 17;
    //The food value gained by eating unicorns
    private static final int UNICORN_FOOD_VALUE = 25;
    //The food value gained by eating grendlees
    private static final int GRENDLE_FOOD_VALUE = 20;
    //The food value gained by eating faegrasses
    private static final int FAEGRASS_FOOD_VALUE = 15;
    //The max a titan can eat before being full
    private static final int MAX_HUNGER = 100;
    //The probability of a titan getting diseased
    private static final double DISEASE_PROBABILITY = 0.8;
    //flag for whether titan is nocturnal
    private static final boolean isNocturnal = false;
    //A shared random number generator to control breeding
    private static final Random rand = Randomizer.getRandom();
    //The age of the titan
    private int age;
    //The food level of the titan
    private int foodLevel;
    //how much hunger the titan loses per step
    private int hungerLoss;
    
    private Weather weather;
    /**
     * Constructor for objects of class Titan
     */
    public Titan(boolean randomAge, Field field, Location location)
    {
        super(field, location, isNocturnal);
        this.isFemale = isFemale;
        this.hungerLoss = 1;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt((GRENDLE_FOOD_VALUE+FAEGRASS_FOOD_VALUE+UNICORN_FOOD_VALUE+GNOME_FOOD_VALUE+FAIRY_FOOD_VALUE)/5);
        }
        else {
            age = 0;
            foodLevel = (GRENDLE_FOOD_VALUE+FAEGRASS_FOOD_VALUE+UNICORN_FOOD_VALUE+GNOME_FOOD_VALUE+FAIRY_FOOD_VALUE)/5;
        }
    }

    /**
     * Increase the age. This could result in the titan's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this titan more hungry. This could result in the titan's death.
     */
    private void incrementHunger()
    {
        foodLevel = foodLevel - hungerLoss;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Mutator method which increases a titans hunger loss when it eats
     * an infected fairy.
     */
    
    private void diseaseEffect()
    {
      hungerLoss = 3;  
    }
    
    /**
     * Method for the usual actions of the titan.
     * It will move around and look for food, and can possibly breed or die in the
     * process.
     * @param field The current field of the titan
     * @param newTitans List to return newly born titans
     */
    public void act(List<Species> newTitans)
    {   
        //hibernationCheck();
        incrementAge();
        incrementHunger();
    
        if(isAlive()) {
            giveBirth(newTitans);            
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
            if(animal instanceof Titan) {
                Titan titan = (Titan) animal;
                if(titan.gender()!=this.gender()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Method to determine whether a titan can breed (ie if they are old enough and have a 
     * mate.
     */
    private boolean canBreed()
    {
        return age >= BREEDING_AGE && hasMate();
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
     * Check whether or not this titan is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newTitans A list to return newly born titans.
     */
    private void giveBirth(List<Species> newTitans)
    {
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Titan young = new Titan(false, field, loc);
            if(rand.nextDouble() <= DISEASE_PROBABILITY){
                young.toggleInfection();
            }
            newTitans.add(young);
        }
    }
    
    /**
     * Look for fairys and grendlees adjacent to the current location.
     * Modified to eat every adjacent species found
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
                    
                    if(fairy.isInfected() && rand.nextDouble() <= DISEASE_PROBABILITY){
                        toggleInfection();
                        diseaseEffect();
                    }
                    return where;
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
            else if (species instanceof Gnome) {
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
            else if (species instanceof Faegrass) {
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
        }
        return foodLocation;
    }
}
