import java.util.Random;
import java.util.List;
import java.util.Iterator;

/**
 * A simple model of a bear.
 * Bears age, move, eats rabbits , foxes, raccoons, pigs, and radishes, and die.
 * They live moderately long lives and are benefitted by
 * being able to eat the widest range of species on the grid
 *
 * @author Oliver Macpherson and Reuben Atendido
 * @version 2022/02/21 (2)
 */
public class Bear extends Species
{
    //The age at which the bear can breed
    private static final int BREEDING_AGE = 10;
    //The max age which a bear can live to
    private static final int MAX_AGE = 300;
    //The probability of a bear breeding
    private static final double BREEDING_PROBABILITY = 0.02;
    //The maximum size of a litter of bear cubs
    private static final int MAX_LITTER_SIZE = 2;
    //The food value gained by eating rabbits
    private static final int RABBIT_FOOD_VALUE = 15;
    //The food value gained by eating raccoons
    private static final int RACCOON_FOOD_VALUE = 17;
    //The food value gained by eating pigs
    private static final int PIG_FOOD_VALUE = 25;
    //The food value gained by eating foxes
    private static final int FOX_FOOD_VALUE = 20;
    //The food value gained by eating radishes
    private static final int RADISH_FOOD_VALUE = 15;
    //The max a bear can eat before being full
    private static final int MAX_HUNGER = 100;
    //The probability of a bear getting diseased
    private static final double DISEASE_PROBABILITY = 0.8;
    //flag for whether bear is nocturnal
    private static final boolean isNocturnal = false;
    //A shared random number generator to control breeding
    private static final Random rand = Randomizer.getRandom();
    //The age of the bear
    private int age;
    //The food level of the bear
    private int foodLevel;
    //how much hunger the bear loses per step
    private int hungerLoss;
    
    private Weather weather;
    /**
     * Constructor for objects of class Bear
     */
    public Bear(boolean randomAge, Field field, Location location)
    {
        super(field, location, isNocturnal);
        this.isFemale = isFemale;
        this.hungerLoss = 1;
        if(randomAge) {
            age = rand.nextInt(MAX_AGE);
            foodLevel = rand.nextInt((FOX_FOOD_VALUE+RADISH_FOOD_VALUE+PIG_FOOD_VALUE+RACCOON_FOOD_VALUE+RABBIT_FOOD_VALUE)/5);
        }
        else {
            age = 0;
            foodLevel = (FOX_FOOD_VALUE+RADISH_FOOD_VALUE+PIG_FOOD_VALUE+RACCOON_FOOD_VALUE+RABBIT_FOOD_VALUE)/5;
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
        foodLevel = foodLevel - hungerLoss;
        if(foodLevel <= 0) {
            setDead();
        }
    }
    
    /**
     * Mutator method which increases a bears hunger loss when it eats
     * an infected rabbit.
     */
    
    private void diseaseEffect()
    {
      hungerLoss = 3;  
    }
    
    /**
     * Method for the usual actions of the bear.
     * It will move around and look for food, and can possibly breed or die in the
     * process.
     * @param field The current field of the bear
     * @param newBears List to return newly born bears
     */
    public void act(List<Species> newBears)
    {   
        //hibernationCheck();
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
    
    /**
     * Method to determine whether a bear can breed (ie if they are old enough and have a 
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
     * Check whether or not this bear is to give birth at this step.
     * New births will be made into free adjacent locations.
     * @param newBears A list to return newly born bears.
     */
    private void giveBirth(List<Species> newBears)
    {
        Field field = getField();
        List<Location> free = field.getFreeAdjacentLocations(getLocation());
        int births = breed();
        for(int b = 0; b < births && free.size() > 0; b++) {
            Location loc = free.remove(0);
            Bear young = new Bear(false, field, loc);
            if(rand.nextDouble() <= DISEASE_PROBABILITY){
                young.toggleInfection();
            }
            newBears.add(young);
        }
    }
    
    /**
     * Look for rabbits and foxes adjacent to the current location.
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
            if(species instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) species;
                if(rabbit.isAlive()) { 
                    rabbit.setDead();
                    if(foodLevel+RABBIT_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += RABBIT_FOOD_VALUE;
                    }
                    else {
                        foodLevel = MAX_HUNGER;
                    }
                    
                    if(rabbit.isInfected() && rand.nextDouble() <= DISEASE_PROBABILITY){
                        toggleInfection();
                        diseaseEffect();
                        System.out.println("Bear diseased");
                    }
                    return where;
                }
            }
            else if(species instanceof Fox) {
                Fox fox = (Fox) species;
                if(fox.isAlive()) {
                    fox.setDead();
                    if(foodLevel+FOX_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += FOX_FOOD_VALUE;
                    }
                    else {
                        foodLevel = MAX_HUNGER;
                    }
                    foodLocation = where;
                }
            }    
            else if(species instanceof Pig) {
                Pig pig = (Pig) species;
                if(pig.isAlive()) {
                    pig.setDead();
                    if(foodLevel+PIG_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += PIG_FOOD_VALUE;
                    }
                    else {
                        foodLevel = MAX_HUNGER;
                    }
                    foodLocation = where;
                }
            }   
            else if (species instanceof Raccoon) {
                Raccoon raccoon = (Raccoon) species;
                if(raccoon.isAlive()) {
                    raccoon.setDead();
                    if(foodLevel+RACCOON_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += RACCOON_FOOD_VALUE;
                    }
                    else {
                        foodLevel = MAX_HUNGER;
                    }
                    foodLocation = where;
                }
            }
            else if (species instanceof Radish) {
                Radish radish = (Radish) species;
                if(radish.isAlive()) {
                    radish.setDead();
                    if(foodLevel+RADISH_FOOD_VALUE <= MAX_HUNGER) {
                        foodLevel += RADISH_FOOD_VALUE;
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
