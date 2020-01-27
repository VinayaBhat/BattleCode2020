package player10pdx;
import battlecode.common.*;
import java.util.ArrayList;

public class Miner extends Unit {

    int numDesignSchools = 0;
    int numRefineries = 0;
    int current_total = 0;
    int round_check = 1;
    ArrayList<MapLocation> soupLocations = new ArrayList<MapLocation>();
    public Miner(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        MapLocation garbage = new MapLocation(rc.getRoundNum(),rc.getRoundNum());
        numRefineries += comms.getnewBuildingCount( 3);
        numDesignSchools += comms.getnewBuildingCount(1);
        comms.updateSoupLocations(soupLocations);
        checkIfSoupGone();

        for (Direction dir : Util.directions) {
            if (tryMine(dir)) {
                System.out.println("I mined soup! " + rc.getSoupCarrying());
                MapLocation soupLoc = rc.getLocation().add(dir);
                if (!soupLocations.contains(soupLoc)) {
                    comms.broadcastSoupLocation(soupLoc);
                }
            }
        }
        // mine first, then when full, deposit
        for (Direction dir : Util.directions)
            if (tryRefine(dir))
               System.out.println("I refined soup! " + rc.getTeamSoup());

        if (numRefineries == 0 || ((0 < numRefineries && numRefineries < 3)  && numDesignSchools == 2)){
            if(rc.canBuildRobot(RobotType.REFINERY, Util.randomDirection())){
                rc.buildRobot(RobotType.REFINERY, Util.randomDirection());
                numRefineries += 1;
                System.out.println("created a refinery. it is number " + numDesignSchools);
            }


        }

        if ((numRefineries > 0 && numRefineries < 2) && numDesignSchools < 3){
            if(tryBuild(RobotType.DESIGN_SCHOOL, Util.randomDirection()))
               System.out.println("created a design school. it is number " + numDesignSchools);
        }

        if (rc.getSoupCarrying() == RobotType.MINER.soupLimit) {
            // time to go back to the HQ
            if(nav.goTo(hqLoc))
                System.out.println("moved towards HQ");
        } else if (soupLocations.size() > 0) {
            nav.goTo(soupLocations.get(0));
            rc.setIndicatorLine(rc.getLocation(), soupLocations.get(0), 255, 255, 0);
        } else if (nav.goTo(Util.randomDirection())) {
            // otherwise, move randomly as usual
           System.out.println("I moved randomly!");
        }
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }

    void checkIfSoupGone() throws GameActionException {
        if (soupLocations.size() > 0) {
            MapLocation targetSoupLoc = soupLocations.get(0);
            if (rc.canSenseLocation(targetSoupLoc)
                    && rc.senseSoup(targetSoupLoc) == 0) {
                soupLocations.remove(0);
            }
        }
    }
}
