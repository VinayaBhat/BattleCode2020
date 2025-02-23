package team10pdx;
import battlecode.common.*;

public class Unit extends Robot {

    Navigation nav;
    MapLocation hqLoc;
    int mapheight;
    int mapwidth;



    public Unit(RobotController r) {
        super(r);
        nav = new Navigation(rc);
        mapheight=rc.getMapHeight();
        mapwidth=rc.getMapWidth();


    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        findHQ();
    }

    public MapLocation findHQ() throws GameActionException {
        if (hqLoc == null) {
            // search surroundings for HQ
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    hqLoc = robot.location;
                    return hqLoc;
                }
            }
            if(hqLoc == null) {
                // if still null, search the blockchain
                hqLoc = comms.getHqLocFromBlockchain();
            }
        }
        return hqLoc;
    }
}
