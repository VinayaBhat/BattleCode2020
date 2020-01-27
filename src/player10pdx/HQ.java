package player10pdx;
import battlecode.common.*;

import java.util.ArrayList;

public class HQ extends Shooter {
    static int numMiners = 0;

    public HQ(RobotController r) throws GameActionException {
        super(r);

        comms.sendHqLoc(rc.getLocation());
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        int minercount = 0;
        ArrayList<Direction> gooddirections = new ArrayList<Direction>();
        for (Direction dir : Util.directions)
            if (rc.canBuildRobot(RobotType.MINER, dir)) {
                minercount += 1;
                gooddirections.add(dir);
            }
        if (rc.getRoundNum() != 1) {
            // numMiners = comms.getnewBuildingCount(RobotType.MINER, 4);}
            System.out.println("num miners = " + numMiners);
            if (numMiners < 30 && minercount > 0) {
                for (Direction dir : gooddirections) {
                    rc.buildRobot(RobotType.MINER, dir);
                    numMiners += 1;
                }
                gooddirections = null;
            }
        }
    }
}
