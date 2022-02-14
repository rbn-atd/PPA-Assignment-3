
import java.util.Random;
import java.util.List;
import java.util.Iterator;

/**
 * A simple model of a bear.
 * Bears age, move, eats rabbits and foxes, and die.
 *
 * @author Oliver Macpherson
 * @version 1.0
 */
public class Bear extends Animal
{
    // instance variables - replace the example below with your own
    private static final int BREEDING_AGE = 17;
    private static final int MAX_AGE = 400;
    private static final double BREEDING_PROBABILITY = 0.05;
    private static final int MAX_LITTER_SIZE = 1;
    private static final int RABBIT_FOOD_VALUE = 12;
    private static final int FOX_FOOD_VALUE = 13;
    private static final int PIG_FOOD_VALUE = 15;

    private static final boolean isNocturnal = true;
    private static final Random rand = Randomizer.getRandom();
    
    private int age;
    private int foodLevel;
    

    /**
     * Constructor for objects of class Bear
     */
    public Bear(boolean randomAge, Field field, Location location)
    {
        super(field, location, isNocturnal);
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt((RABBIT_FOOD_VALUE+FOX_FOOD_VALUE+PIG_FOOD_VALUE)/3);
        }
        else {
            age = 0;
            foodLevel = (RABBIT_FOOD_VALUE+FOX_FOOD_VALUE+PIG_FOOD_VALUE)/3;
        }
    }

    /**
     * Increase the age. This could result in the bear's death.
     */
    private void incrementAge()
    {
        age++;
        if(age > MAX_AGE) {
            setDead();
        }
    }
    
    /**
     * Make this bear more hungry. This could result in the bear's death.
     */
    private void incrementHunger()
    {
        foodLevel--;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Method for the usual actions of the bear.
     * It will move around and look for food, and can possibly breed or die in the
     * process.
     * @param field The current field of the bear
     * @param newBears List to return newly born bears
     */
    public void act(List<Animal> newBears)
    {
        incrementAge();
        incrementHunger();
        if(isAlive()) {
            giveBirth(newBears);   
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
     * Method to determine whether a bear can breed
     */
    private boolean canBreed()
    {
        //return age >= BREEDING_AGE;
        return (age >= BREEDING_AGE && hasMate()/*&&!hasBredThisStep()*/);
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
     * Check whether or not this bear is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newBears A list to return newly born bears.
     */
    private void giveBirth(List<Animal> newBears)
    {
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Bear young = new Bear(false, field, loc);
            newBears.add(young);
        }
    }
    
    /**
     * Look for rabbits and foxes adjacent to the current location.
     * Only the first live rabbit or fox is eaten.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood()
    {
        Field field = getField();
        List<Location> adjacent = field.adjacentLocations(getLocation());
        Iterator<Location> it = adjacent.iterator();
        while(it.hasNext()) {
            Location where = it.next();
            Object animal = field.getObjectAt(where);
            if(animal instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) animal;
                if(rabbit.isAlive()) { 
                    rabbit.setDead();
                    foodLevel = RABBIT_FOOD_VALUE;
                    return where;
                }
            }
            if(animal instanceof Fox) {
                Fox fox = (Fox) animal;
                if(fox.isAlive()) {
                    fox.setDead();
                    foodLevel = FOX_FOOD_VALUE;
                    return where;
                }
            }
            if(animal instanceof Pig) {
                Pig pig = (Pig) animal;
                if(pig.isAlive()) {
                    pig.setDead();
                    foodLevel = PIG_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
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
            if(animal instanceof Bear) {
                Bear bear = (Bear) animal;
                if(bear.gender()!=this.gender()) {
                    return true;
                }
            }
        }
        return false;
    }
}
