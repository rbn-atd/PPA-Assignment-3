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
    private static final int DEFAULT_WIDTH = 250;
    // The default depth of the grid.
    private static final int DEFAULT_DEPTH = 250;
    // The probability that a fox will be created in any given grid position.
    private static final double FOX_CREATION_PROBABILITY = 0.03;
    // The probability that a rabbit will be created in any given grid position.
    private static final double RABBIT_CREATION_PROBABILITY = 0.05;
    
    private static final double EAGLE_CREATION_PROBABILITY = 0.01;
    
    
    
    private static final double PIG_CREATION_PROBABILITY = 0.04;
    private static final double BEAR_CREATION_PROBABILITY = 0.005;
    
    
    private static final double DEFAULT_RIVER_START = 0.5;
    private static final double DEFAULT_RIVER_END = 0.6;
    // List of animals in the field.
    private List<Animal> animals;
    private List<BiomeFeature> features;
    // The current state of the field.
    private Field field;
    // The current step of the simulation.
    private int step;
    // A graphical view of the simulation.
    private SimulatorView view;
    
    private boolean isDay;
    
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
        features = new ArrayList<>();
        field = new Field(depth, width);

        // Create a view of the state of each location in the field.
        view = new SimulatorView(depth, width);
        
        view.setColor(Rabbit.class, Color.ORANGE);
        view.setColor(Fox.class, Color.BLUE);
        view.setColor(Eagle.class, Color.MAGENTA);
        
        view.setColor(Pig.class, Color.PINK);
        view.setColor(Bear.class, Color.RED);
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
            //delay(60);   // uncomment this to run more slowly
        }
    }
    
    /**
     * Run the simulation from its current state for a single step.
     * Iterate over the whole field updating the state of each
     * fox and rabbit.
     */
    public void simulateOneStep()
    {
        step++;
        //String timeTag = ""
        if(step % 2 == 0){
            setDay();
        }
        
        // Provide space for newborn animals.
        List<Animal> newAnimals = new ArrayList<>();

        // Let all rabbits act.
        for(Iterator<Animal> it = animals.iterator(); it.hasNext(); ) {
            Animal animal = it.next();
            if ((animal.getIsNocturnal() == true && getIsDay()==false) || (animal.getIsNocturnal() == false && getIsDay()==true)) {
                animal.act(newAnimals);
            }
            if(! animal.isAlive()) {
                it.remove();
            }
        }
        
               
        // Add the newly born foxes and rabbits to the main lists.
        animals.addAll(newAnimals);
        view.showStatus(step, isDay, field);
        if(step<13) {
            generateRiver(0.5, 0.6);
        }
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
        view.showStatus(step, isDay, field);
    }
    
    /**
     * Randomly populate the field with foxes and rabbits.
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
                    animals.add(fox);
                }
                else if(rand.nextDouble() <= RABBIT_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Rabbit rabbit = new Rabbit(true, field, location);
                    animals.add(rabbit);
                }
                else if(rand.nextDouble() <= EAGLE_CREATION_PROBABILITY) { 
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Eagle eagle = new Eagle(true, field, location);
                    animals.add(eagle);
                }
                else if(rand.nextDouble() <= PIG_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Pig pig = new Pig(true, field, location);
                    animals.add(pig);
                }
                else if(rand.nextDouble() <= BEAR_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Bear bear = new Bear(true, field, location);
                    animals.add(bear);
                }
                // else leave the location empty.
            }
        }
    }
    
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
    
    private void setDay() {
        isDay = !isDay;
    }
    
    private boolean getIsDay() {
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
        int riverWidth = (tempWidth/10);
        Location start = new Location(tempDepth-1, (int) (bottomStartFraction*tempWidth));
        River riverStart = new River(field, start);
        features.add(riverStart);
        Location end = new Location(0, (int) (topStartFraction*tempWidth));
        River riverEnd = new River(field, end);
        features.add(riverStart);
        int gradient = (tempDepth/(int) (((int) ((topStartFraction*tempWidth)-(bottomStartFraction*tempWidth)))));
        for(int i = 0 ; i < tempDepth; i++) {
            if((i % gradient)==0) { i++; }
            for(int j = start.getCol(); j <= start.getCol()+riverWidth; j++) {
                if(j>=(riverWidth-4+start.getCol())) { break; }
                Location location = new Location(i, j);
                River river = new River(field, location);
                features.add(river);
            }
        }
        
    }
}
