import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;

/**
 * A simple predator-prey simulator, based on a rectangular field
 * containing rabbits and foxes.
 * 
 * @author David J. Barnes and Michael Kölling
 * @version 2016.02.29 (2)
 */
public class Simulator
{   
    // Constants representing configuration information for the simulation.
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 300;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 300;
    // The probability that a fox will be created in any given grid position.
    private static final double FOX_CREATION_PROBABILITY = 0.07;
    // The probability that a rabbit will be created in any given grid position.
    private static final double RABBIT_CREATION_PROBABILITY = 0.18;
    
    private static final double EAGLE_CREATION_PROBABILITY = 0.07;
    
    private static final double RADISH_CREATION_PROBABILITY = 0.1;
    
    private static final double PIG_CREATION_PROBABILITY = 0.087;
    
    private static final double BEAR_CREATION_PROBABILITY = 0.05;
    
    private static final double RACCOON_CREATION_PROBABILITY = 0.07;
    //The probability that a female of any animal will be created at any grid position
    private static final double FEMALE_CREATION_PROBABILITY = 0.5;
    //The probability a rabbit is already infected at creation
    //Only rabbits will have such probability, acting as a vector.
    private static final double DISEASE_CREATION_PROBABILITY = 0.5;
    
    private static final double RAIN_PROBABILITY = 0.02;
    private static final double SNOW_PROBABILITY = 0.009;
    private static final double SUN_PROBABILITY = 0.75;
    private static final double FOG_PROBABILITY = 0.01;

    // List of animals in the field.
    private List<Animal> animals;
    
    private List<Animal> infected;
    //private List<Plant> plants;
    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private SimulatorView view;
    
    private List<BiomeFeature> features;
    //Starts the simulator on day time.
    private boolean isDay = true;
    
    private boolean isRaining = false;
    private boolean isSnowing = false;
    private boolean isSunny = false;
    private boolean isFoggy = false;
    
    /**
     * Construct a simulation field with default size.
     */
    public Simulator()
    {
        this(DEFAULT_DEPTH, DEFAULT_WIDTH);
    }
    
    /**
     * Create a simulation field with the given size.
     * @param depth Depth of the field. Must be greater than zero.
     * @param width Width of the field. Must be greater than zero.
     */
    public Simulator(int depth, int width)
    {
        if(width <= 0 || depth <= 0) {
            System.out.println("The dimensions must be greater than zero.");
            System.out.println("Using default values.");
            depth = DEFAULT_DEPTH;
            width = DEFAULT_WIDTH;
        }
        
        animals = new ArrayList<>();
        infected = new ArrayList<>();
        //plants = new ArrayList<>();

        field = new Field(depth, width);
        
        features = new ArrayList<>();
        // Create a view of the state of each location in the field.
        view = new SimulatorView(depth, width);
        view.setColor(Rabbit.class, Color.ORANGE);
        view.setColor(Fox.class, Color.BLUE);
        view.setColor(Eagle.class, Color.YELLOW);
        view.setColor(Radish.class, Color.GREEN);
        view.setColor(Pig.class, Color.MAGENTA);
        view.setColor(Bear.class, Color.RED);
        view.setColor(Raccoon.class, Color.DARK_GRAY);
        view.setColor(River.class, Color.CYAN);
        
        // Setup a valid starting point.
        reset();
    }
    
    /**
     * Run the simulation from its current state for a reasonably long period,
     * (4000 steps).
     */
    public void runLongSimulation()
    {
        simulate(4000);
    }
    
    /**
     * Run the simulation from its current state for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps)
    {
        for(int step = 1; step <= numSteps && view.isViable(field); step++) {
            simulateOneStep();
            //delay(750);   // uncomment this to run more slowly
        }
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each species.
     * This method has been modified now to toggle between day and night if
     * the step count is divisible by 2.
     */
    public void simulateOneStep()
    {
        step++;
        Random rand = Randomizer.getRandom();
        //String timeTag = ""
        if(step % 2 == 0){
            toggleDayAndNight();
        }
        
        if(rand.nextDouble() <= RAIN_PROBABILITY) {
            toggleRain();
        }
        else if (rand.nextDouble() <= SNOW_PROBABILITY) {
            toggleSnow();
        }
        else if (rand.nextDouble() <= SUN_PROBABILITY) {
            toggleSun();
        }
        
        // Provide space for newborn animals.
        List<Animal> newAnimals = new ArrayList<>();

        // Let all rabbits act.
        for(Iterator<Animal> it = animals.iterator(); it.hasNext(); ) {
            Animal animal = it.next();
            if ( (isSnowing == false || isRaining == false) && ( (animal.getIsNocturnal() && !getIsDay()) || (!animal.getIsNocturnal() && getIsDay())  )) {
                animal.act(newAnimals);
            }
            if(! animal.isAlive()) {
                it.remove();
            }
        }
               
        // Add the newly born foxes and rabbits to the main lists.
        animals.addAll(newAnimals);
        view.showStatus(step, getTimeOfDay(), getWeather(), field);
    }
        
    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        animals.clear();
        populate();
        generateRiver(0.5, 0.6);
        // Show the starting state in the view.
        view.showStatus(step, getTimeOfDay(), getWeather(), field);
    }
    
    /**
     * Randomly populate the field with foxes and rabbits.
     * This method has been modified to now consider a female probability
     * Now animals are created with a flag indicating if it is female
     */
    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                if(rand.nextDouble() <= FOX_CREATION_PROBABILITY) {
                   Location location = new Location(row, col);
                   if(rand.nextDouble() <= FEMALE_CREATION_PROBABILITY){
                        Fox fox = new Fox(true, field, location, true);
                        animals.add(fox);
                    }
                    else{
                        Fox fox = new Fox(true, field, location, false);
                        animals.add(fox);
                    }
                }
                else if(rand.nextDouble() <= RABBIT_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    if(rand.nextDouble() <= FEMALE_CREATION_PROBABILITY){
                        Rabbit rabbit = new Rabbit(true, field, location, true);
                        animals.add(rabbit);
                    }
                    else{
                        Rabbit rabbit = new Rabbit(true, field, location, false);
                        animals.add(rabbit);
                    }
                }
                else if(rand.nextDouble() <= EAGLE_CREATION_PROBABILITY) { 
                    Location location = new Location(row, col);
                    if(rand.nextDouble() <= FEMALE_CREATION_PROBABILITY){
                        Eagle eagle = new Eagle(true, field, location, true);
                        animals.add(eagle);
                    }
                    else{
                        Eagle eagle = new Eagle(true, field, location, false);
                        animals.add(eagle);
                    }
                }
                else if(rand.nextDouble() <= RADISH_CREATION_PROBABILITY) { 
                    Location location = new Location(row, col);
                    Radish radish = new Radish(true, field, location);
                    animals.add(radish);
                }
                else if(rand.nextDouble() <= PIG_CREATION_PROBABILITY) {
                   Location location = new Location(row, col);
                   if(rand.nextDouble() <= FEMALE_CREATION_PROBABILITY){
                       Pig pig = new Pig(true, field, location, true);
                       animals.add(pig);
                    }
                   else{
                        Pig pig = new Pig(true, field, location, false);
                        animals.add(pig);
                    }
                }
                else if(rand.nextDouble() <= BEAR_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    if(rand.nextDouble() <= FEMALE_CREATION_PROBABILITY){
                        Bear bear = new Bear(true, field, location, true);
                        animals.add(bear);
                    }
                    else{
                        Bear bear = new Bear(true, field, location, false);
                        animals.add(bear);
                    }
                }
                else if(rand.nextDouble() <= RACCOON_CREATION_PROBABILITY) {
                    Location location = new Location(row, col);
                    if(rand.nextDouble() <= FEMALE_CREATION_PROBABILITY){
                        Raccoon raccoon = new Raccoon(true, field, location, true);
                        animals.add(raccoon);
                    }
                    else{
                        Raccoon raccoon = new Raccoon(true, field, location, false);
                        animals.add(raccoon);
                    }
                }
                // else leave the location empty.
            }
        }
    }
    
    /**
     * Pause for a given time.
     * @param millisec  The time to pause for, in milliseconds
     */
    private void delay(int millisec)
    {
        try {
            Thread.sleep(millisec);
        }
        catch (InterruptedException ie) {
            // wake up
        }
    }
    
    /**
     * Toggles isDay between true and false
     * acts as an indicator for when it is day
     * allowing a day night cycle to exist
     */
    private void toggleDayAndNight() {
        isDay = !isDay;
    }
    
    private void toggleRain() {
        isRaining = !isRaining;
    }
    
    private void toggleSnow() {
        isSnowing = !isSnowing;
    }
    
    private void toggleSun() {
        isSunny = !isSunny;
    }
    
    /**
     * returns a String to be passed into the paramaters of
     * the showStatus function in SimulatorView.
     * Appears on the GUI to indicate the time of day to the user
     */
    private String getTimeOfDay() {
        if(!isDay){
            return "Night";
        }
        else{
            return "Day";
        }
    }
    
    private String getWeather() {
        if( isRaining && !isSnowing && !isSunny ){
            return "Raining";
        }
        else if ( isSnowing && !isRaining && !isSunny ){
            return "Snowing";
        }
        else if ( isSnowing && isRaining ){
            return "Cold Storm";
        }
        else if ( isSunny && !isSnowing && ! isRaining){
            return "Hot";
        }
        else{
            return "Neutral";
        }
    }
    
    /**
     * returns the current state of the isDay flag.
     */
    private boolean getIsDay()
    {
        return isDay;
    }
    
    /**
     * Create a river
     * @param bottomStartFraction the fraction along the bottom where the river meets the edge
     * @param topStartFraction the fraction along the top where the river meets the edge
     */
    private void generateRiver(double bottomStartFraction, double topStartFraction) {
        int tempWidth = field.getWidth();
        int tempDepth = field.getDepth();
        int riverWidth = (tempWidth/10)*3;
        Location start = new Location(tempDepth-1, (int) bottomStartFraction*tempWidth);
        River riverStart = new River(field, start);
        features.add(riverStart);
        Location end = new Location(0, (int) topStartFraction*tempWidth);
        River riverEnd = new River(field, end);
        features.add(riverStart);
        int gradient = (tempDepth/(int) (((int) topStartFraction*tempWidth)-(bottomStartFraction*tempWidth)));
        for(int i = 1; i < tempDepth-1; i++) {
            for(int j = 1; i < tempDepth-1; i++) {
                Location location = new Location(i, j);
                River river = new River(field, location);
                features.add(river);
            }
        }
    }
}
