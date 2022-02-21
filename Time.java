
/**
 * A simple model of a day night cycle, which controls which times animals can act.
 * 
 *
 * @author Reuben Atendido and Oliver Macpherson
 * @version 1
 */
public class Time
{
    //boolean for whether is is daytime
    private boolean isDay = true;
    
    /**
     * Toggles between day and night
     */
    public void toggleDayAndNight() {
        isDay = !isDay;
    }
    
    /**
     * returns the current state of the isDay flag.
     */
    public boolean getIsDay()
    {
        return isDay;
    }
    
    /**
     * returns a String to be passed into the paramaters of
     * the showStatus function in SimulatorView.
     * Appears on the GUI to indicate the time of day to the user
     */
    public String getTimeOfDay() {
        if(!isDay){
            return "Night";
        }
        else{
            return "Day";
        }
    }
}
