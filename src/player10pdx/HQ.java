package player10pdx;
import battlecode.common.*;

public class HQ extends Shooter {
    static int numMiners = 0;

    public HQ(RobotController r) throws GameActionException {
        super(r);

        comms.sendHqLoc(rc.getLocation());
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        if (rc.getRoundNum() % 8 == 0) {
            if (numMiners < 10) {
                for (Direction dir : Util.directions)
                    if (rc.canBuildRobot(RobotType.MINER, dir)) {
                        rc.buildRobot(RobotType.MINER, dir);
                        numMiners++;
                    }
            }
        }
    }
}