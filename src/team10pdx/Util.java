package team10pdx;
import battlecode.common.*;

// This is a file to accumulate all the random helper functions
// which don't interact with the game, but are common enough to be used in multiple places.
// For example, lots of logic involving MapLocations and Directions is common and ubiquitous.
public class Util {
    static Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST
    };

    static Direction[] diagonals = {
            Direction.SOUTHWEST,
            Direction.SOUTHEAST,
            Direction.NORTHWEST,
            Direction.NORTHEAST
    };

    static int[] movement = {
          -20,20,15,-15,25,-25,30,-30
    };

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    static int randomNumber(){return movement[(int) (Math.random() * movement.length)];}
}
