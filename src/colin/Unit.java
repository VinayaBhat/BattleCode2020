package colin;
import battlecode.common.*;
import colin.Navigation;
import colin.Robot;

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

    public void findHQ() throws GameActionException {
        if (hqLoc == null) {
            // search surroundings for HQ
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    hqLoc = robot.location;
                }
            }
            if(hqLoc == null) {
                // if still null, search the blockchain
                hqLoc = comms.getHqLocFromBlockchain();
            }
        }
    }
}
