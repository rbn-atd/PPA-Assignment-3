import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;

/**
 * A simple predator-prey simulator, based on a rectangular field
 * containing rabbits, foxes, raccoons, bears, eagles, pigs, and radishes.
 * Certain animals occaisionally get diseased.
 * 
 * @author David J. Barnes and Michael Kölling
 *          with Reuben Atendido and Oliver Macpherson
 * @version 2022.02.21 (4)
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
    // The probability that a eagle will be created in any given grid position.
    private static final double EAGLE_CREATION_PROBABILITY = 0.07;
    // The probability that a radish will be created in any given grid position.
    private static final double RADISH_CREATION_PROBABILITY = 0.1;
    // The probability that a pig will be created in any given grid position.
    private static final double PIG_CREATION_PROBABILITY = 0.1;
    // The probability that a bear will be created in any given grid position.
    private static final double BEAR_CREATION_PROBABILITY = 0.05;
    // The probability that a raccoon will be created in any given grid position.
    private static final double RACCOON_CREATION_PROBABILITY = 0.1;
    //The probability a rabbit is already infected at creation
    //Only rabbits will have such probability, acting as a vector for the disease.
    private static final double DISEASE_CREATION_PROBABILITY = 0.7;
    //probability the weather will be set to rain
    private static final double RAIN_PROBABILITY = 0.02;
    //probability the weather will be set to snow
    private static final double SNOW_PROBABILITY = 0.01;
    //probabilty the weather will be set to sunny
    private static final double SUN_PROBABILITY = 0.75;
    //the fraction along the bottom of the field where the river will start from
    private static final double DEFAULT_RIVER_START = 0.4;
    //the fraction along the top of the field where the river will end
    private static final double DEFAULT_RIVER_END = 0.6;
    //boolean for whether a river will generate in the simulation
    private boolean GENERATE_RIVER = true;
    // List of species in the field.
    private List<Species> species;
    
    private int delay = 0;
    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private SimulatorView view;
    //Create an instance of weather
    private Weather weather;
    //Create an instance of time
    private Time time;
    //List of all biome features placed on the grid
    private List<BiomeFeature> features;
    
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
        
        species= new ArrayList<>();
    
        field = new Field(depth, width);
        
        weather = new Weather();
        
        time = new Time();
        
        features = new ArrayList<>();
        // Create a view of the state of each location in the field.
        view = new SimulatorView(depth, width);
        view.setColor(Rabbit.class, Color.ORANGE);
        view.setColor(Fox.class, Color.BLUE);
        view.setColor(Eagle.class, Color.MAGENTA);
        view.setColor(Radish.class, Color.GREEN);
        view.setColor(Pig.class, Color.PINK);
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
            delay(delay);   // uncomment this to run more slowly
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
        //toggle between day and night every 2 steps
        //This means 1 full day is 4 steps
        if(step % 2 == 0){
            time.toggleDayAndNight();
        }
        //toggle weather depending on their probabilities of occurring
        if(rand.nextDouble() <= RAIN_PROBABILITY) {
            weather.toggleRain();
        }
        else if (rand.nextDouble() <= SNOW_PROBABILITY) {
            weather.toggleSnow();
        }
        else if (rand.nextDouble() <= SUN_PROBABILITY) {
            weather.toggleSun();
        }
        
        // Provide space for newborn species.
        List<Species> newSpecies = new ArrayList<>();
        // Let all species act.
        for(Iterator<Species> it = species.iterator(); it.hasNext(); ) {
            Species species = it.next();
            //species do not act if it is both snowing and raining, and only act in their respective times of day
            if ((!weather.getSnow() || !weather.getRain()) && ( (species.getIsNocturnal() && !time.getIsDay()) || (!species.getIsNocturnal() && time.getIsDay())  )) {
                species.act(newSpecies);
            }
            if(!species.isAlive()) {
                it.remove();
            }
        }       
        // Add the newly born foxes and rabbits to the main lists.
        species.addAll(newSpecies);
        
        view.showStatus(step, time.getTimeOfDay(), weather.getWeather(), field, getRiverString(), getDelay());
        if(GENERATE_RIVER) {
            if(step<200) {
                generateRiver(DEFAULT_RIVER_START, DEFAULT_RIVER_END);
            }
        }
    }
            
    /**
     * Reset the simulation to a starting position.
     */
    public void reset()
    {
        step = 0;
        setDelay(0);
        species.clear();
        populate();
        if(GENERATE_RIVER) {
            generateRiver(DEFAULT_RIVER_START, DEFAULT_RIVER_END);
        }
        
        // Show the starting state in the view.
        view.showStatus(step, time.getTimeOfDay(), weather.getWeather(), field, getRiverString(), getDelay());
    }
    
    /**
     * Randomly populate the field with many different species.
     * Now species are created with a flag indicating if it is female
     */
    private void populate()
    {
        Random rand = Randomizer.getRandom();
        field.clear();
        for(int row = 0; row < field.getDepth(); row++) {
            for(int col = 0; col < field.getWidth(); col++) {
                if(rand.nextDouble() <= FOX_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Fox fox = new Fox(true, field, location);
                    species.add(fox);
                }
                else if(rand.nextDouble() <= RABBIT_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Rabbit rabbit = new Rabbit(true, field, location);
                    species.add(rabbit);
                }
                else if(rand.nextDouble() <= EAGLE_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Eagle eagle = new Eagle(true, field, location);
                    species.add(eagle);
                }
                else if(rand.nextDouble() <= RADISH_CREATION_PROBABILITY) { 
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Radish radish = new Radish(true, field, location);
                    species.add(radish);
                }
                else if(rand.nextDouble() <= PIG_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Pig pig = new Pig(true, field, location);
                    species.add(pig);
                }
                else if(rand.nextDouble() <= BEAR_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Bear bear = new Bear(true, field, location);
                    species.add(bear);
                }
                else if(rand.nextDouble() <= RACCOON_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Raccoon raccoon = new Raccoon(true, field, location);
                    species.add(raccoon);
                }
                // else leave the location empty.
            }
        }
    }
    
    /**
     * Checks if a river is occupying a given location
     */
    private boolean riverCheck(int row, int column)
    {
        if(field.getObjectAt(row, column) instanceof River) {
            return true;
        }
        return false;
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
    
    public void setDelay(int newDelay) {
        delay = newDelay;
    }
    
    /**
     * toggles the GENERATE_RIVER boolean flag between
     * true and false (on and off).
     * The grid is immediately reset for the river to be generated.
     */
    public void toggleRiver() {
        GENERATE_RIVER = !GENERATE_RIVER;
        reset();
    }
    
    /**
     * Returns a string passed into the showStatus method in SimulatorView
     * which is then showed on the GUI showing whether a river is on or off;
     */
    private String getRiverString() {
        if (GENERATE_RIVER == true){
            return "On";
        }
        else {
            return "Off";
        }
    }
    
    private String getDelay() {
        if (delay == 0 ) {
            return "Off";
        }
        else{
            return "On";
        }
    }
    
    /**
     * Create a river
     * Currently contains a variety of bugs
     * @param bottomStartFraction the fraction along the bottom where the river meets the edge
     * @param topStartFraction the fraction along the top where the river meets the edge
     */
    private void generateRiver(double bottomStartFraction, double topStartFraction) {
        int tempWidth = field.getWidth();
        int tempDepth = field.getDepth();
        int riverWidth = (int) (tempWidth/25);
        Location start = new Location(tempDepth-1, (int) (bottomStartFraction*tempWidth));
        River riverStart = new River(field, start);
        features.add(riverStart);
        Location end = new Location(0, (int) (topStartFraction*tempWidth));
        River riverEnd = new River(field, end);
        features.add(riverStart);
        double gradient;
        if(topStartFraction!=bottomStartFraction) {
            gradient = (tempDepth/ (( ((topStartFraction*tempWidth)-(bottomStartFraction*tempWidth)))));
        }
        else {
            for(int i = 0; i < tempDepth; i++) {
                for(int j = 0; j <= riverWidth; j++) {
                    for(int k = 0; k <= riverWidth; k++) {
                        Location location = new Location(i, ((int) (topStartFraction*tempWidth))+j);
                        River river = new River(field, location);
                        features.add(river);
                    }
                }
            }
            return;
        }
        
        for(int i = 0; i < tempDepth; i++) {
            for(int j = start.getCol(); j <(end.getCol()); j++) {
                int x = j-start.getCol();                    
                if(i == gradient*x) {
                    for(int k = 0; k <= riverWidth; k++) {
                        if(j+k < tempWidth) {
                            int counter = 0;
                            while(counter<5) {
                                Location location = new Location(i+counter, j+k);
                                River river = new River(field, location);
                                features.add(river);
                                counter++;
                            }
                        }
                    }
                }
            }
        }
            
    }
}

