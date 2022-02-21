
/**
 * A simple model of 5 different weather states utilising 3 boolean weather flags
 * A specific combination of flags will cause the species on the grid to completely 
 * stop acting. Determines the weather text returned to be displayed on the gui.
 *
 * @author Reuben Atendido and Oliver Macpherson
 * @version 1
 */
public class Weather
{
    //multiple flags for different weather types
    private boolean isRaining = false;
    private boolean isSnowing = false;
    private boolean isSunny = false;
    
    /**
     * Returns weather message based of combination of the weather flags
     */
    public String getWeather() {
        String weathertype = "Neutral";
        if( isRaining && !isSnowing && !isSunny ){
            return "Drab Rain";
        }
        else if ( isSnowing && !isRaining && !isSunny){
            return "Nicely Snowing";
        }
        else if ( isSnowing && isRaining){
            return "Totally Frozen";
        }
        else if ( isSunny && !isSnowing && !isRaining){
            return "Exceedingly Hot";
        }
        else{
            return "Clear Day";
        }
    }
    
    
    public void toggleRain() {
        isRaining = !isRaining;
    }
    
    public void toggleSnow() {
        isSnowing = !isSnowing;
    }
    
    public void toggleSun() {
        isSunny = !isSunny;
    }
    
    public boolean getSnow() {
        return  isSnowing;
    }
    
    public boolean getRain() {
        return isRaining;
    }
    
    public boolean getSun() {
        return isSunny;
    }
    
}
