package team10pdx;
import battlecode.common.*;

public class Vaporator extends Building {
    public Vaporator(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        // will only actually happen if we haven't already broadcasted the creation
        RobotType Vaporator = rc.getType();
        MapLocation vaporatorlocation;
        vaporatorlocation = rc.getLocation();
        System.out.println("the vaporator is located at " + vaporatorlocation);
        comms.broadcastCreation(vaporatorlocation, 12);
    }
}
