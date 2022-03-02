import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Color;

/**
 * A simple predator-prey simulator, based on a rectangular field
 * containing fairies, grendlees, gnomes, titans, daemons, unicorns, and faegrasses.
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
    // The probability that a grendle will be created in any given grid position.
    private static final double GRENDLE_CREATION_PROBABILITY = 0.07;
    // The probability that a fairy will be created in any given grid position.
    private static final double FAIRY_CREATION_PROBABILITY = 0.18;
    // The probability that a daemon will be created in any given grid position.
    private static final double DAEMON_CREATION_PROBABILITY = 0.07;
    // The probability that a faegrass will be created in any given grid position.
    private static final double FAEGRASS_CREATION_PROBABILITY = 0.1;
    // The probability that a unicorn will be created in any given grid position.
    private static final double UNICORN_CREATION_PROBABILITY = 0.18;
    // The probability that a titan will be created in any given grid position.
    private static final double TITAN_CREATION_PROBABILITY = 0.05;
    // The probability that a gnome will be created in any given grid position.
    private static final double GNOME_CREATION_PROBABILITY = 0.1;
    //The probability a fairy is already infected at creation
    //Only fairies will have such probability, acting as a vector for the disease.
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
    //probability of animal death in adverse weather
    private static final double WEATHER_DEATH_CHANCE = 0.01;
    //probability of animal acting when its raining
    private static final double RAIN_ACT_CHANCE = 0.7;
    //probability of animal acts when its snowing
    private static final double SNOW_ACT_CHANCE = 0.4;
    //boolean for whether a river will generate in the simulation
    private boolean GENERATE_RIVER = true;
    // List of species in the field.
    private List<Species> species;
    //default delay in ms
    private int delay = 150;
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
        view.setColor(Fairy.class, Color.MAGENTA);
        view.setColor(Grendle.class, Color.ORANGE);
        view.setColor(Daemon.class, Color.RED);
        view.setColor(Faegrass.class, Color.GREEN);
        view.setColor(Unicorn.class, Color.YELLOW);
        view.setColor(Titan.class, Color.DARK_GRAY);
        view.setColor(Gnome.class, Color.BLUE);
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
     * It also calls methods to change grid color depending on time of day.
     */
    public void simulateOneStep()
    {
        step++;
        Random rand = Randomizer.getRandom();
        //String timeTag = ""
        //toggle between day and night every 2 steps
        //This means 1 full day is 4 steps
        if (step % 2 == 1) {
            view.setTransitionColor(); //odd numbered steps change the grid to gray for a more smooth looking transition to day and night.
        }
        else if(step % 2 == 0){
            time.toggleDayAndNight();
            //every 2 steps the background changes between black and white 
            //as a visual indicator of day and night.
            if(time.getIsDay()) {
                view.setDayColor();
            }
            else {
                view.setNightColor();
            }
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
            if ((weather.getWeather().equals("Clear Day")) && (canAct(species))) {
                species.act(newSpecies);
            }
            else if((weather.getWeather().equals("Exceedingly Hot")) && (canAct(species))) {
                species.act(newSpecies);
                weatherDamage(species);
            }
            else if((weather.getWeather().equals("Nicely Snowing")) && (canAct(species))) {
                if(rand.nextDouble() <= SNOW_ACT_CHANCE) {
                    species.act(newSpecies);
                }
            }
            else if((weather.getWeather().equals("Drab Rain")) && (canAct(species))) {
                if(rand.nextDouble() <= RAIN_ACT_CHANCE) {
                    species.act(newSpecies);
                }
            }
            if(!species.isAlive()) {
                it.remove();
            }
        }       
        // Add the newly born creatures to the main lists.
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
        setDelay(150);
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
                if(rand.nextDouble() <= GRENDLE_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Grendle grendle = new Grendle(true, field, location);
                    species.add(grendle);
                }
                else if(rand.nextDouble() <= FAIRY_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Fairy fairy = new Fairy(true, field, location);
                    species.add(fairy);
                }
                else if(rand.nextDouble() <= DAEMON_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Daemon daemon = new Daemon(true, field, location);
                    species.add(daemon);
                }
                else if(rand.nextDouble() <= FAEGRASS_CREATION_PROBABILITY) { 
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Faegrass faegrass = new Faegrass(true, field, location);
                    species.add(faegrass);
                }
                else if(rand.nextDouble() <= UNICORN_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Unicorn unicorn = new Unicorn(true, field, location);
                    species.add(unicorn);
                }
                else if(rand.nextDouble() <= TITAN_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Titan titan = new Titan(true, field, location);
                    species.add(titan);
                }
                else if(rand.nextDouble() <= GNOME_CREATION_PROBABILITY) {
                    if(riverCheck(row, col)) {break;}
                    Location location = new Location(row, col);
                    Gnome gnome = new Gnome(true, field, location);
                    species.add(gnome);
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
    
    /**
     * Return delay integer, if >0 then return to GUI that delay is on
     * or else it is displayed off.
     */
    private String getDelay() {
        if (delay == 0 ) {
            return "Off";
        }
        else{
            return "On";
        }
    }
    
    /**
     * Returns whether a creature can act based on the time and whether its nocturnal.
     * @param species The animal to check
     */
    private boolean canAct(Species species) {
        return (species.getIsNocturnal() && !time.getIsDay()) || (!species.getIsNocturnal() && time.getIsDay());
    }
    
    /**
     * Method for a chance to kill an animal based on constant. To be called when the whether is dangerous.
     * @param species The animal to attempt to kill
     */
    private void weatherDamage(Species species) {
        Random rand = Randomizer.getRandom();
        if(rand.nextDouble() <= WEATHER_DEATH_CHANCE) {
            species.setDead();
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

