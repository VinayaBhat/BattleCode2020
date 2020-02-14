package team10pdx;
import battlecode.common.*;

public class Vaporator extends Building {
    public Vaporator(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        // will only actually happen if we haven't already broadcasted the creation
        RobotType Vaparator = rc.getType();
        //comms.broadcastCreation(rc.getLocation(), 4);
    }
}
