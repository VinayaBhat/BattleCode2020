package colin;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class DeliveryDrone extends Unit {
    public DeliveryDrone(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        //first run the main code
        if(nav.tryMove(nav.randomDirection())){
            System.out.println("Moving the delivery drone in random direction");
        }



    }
}
