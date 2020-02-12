package team10pdx;
import battlecode.common.*;

public class Refinery extends Building {
    public Refinery(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        // will only actually happen if we haven't already broadcasted the creation
        RobotType refinery = rc.getType();
        comms.broadcastCreation(rc.getLocation(), 3);
    }
}
