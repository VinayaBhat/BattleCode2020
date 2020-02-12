package teamplayer;

import battlecode.common.*;
import player10pdx.Robot;
import player10pdx.Util;


import java.util.ArrayList;

public class RobotPlayer {
    static RobotController rc;
    static ArrayList<MapLocation> soupLocations = new ArrayList<MapLocation>();
    static ArrayList<MapLocation>  refineryLocations = new ArrayList<MapLocation>();



    static int teamSecret=12345;


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

    static int turnCount;
    static int numMiners = 0;
    static MapLocation hqLoc;
    static int landscapers=0;

    private static boolean broadcastedCreation=false;

    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;

        turnCount = 0;
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                switch (rc.getType()) {
                    case HQ: {
                        runHQ();
                        sendHqLoc(rc.getLocation());
                        break;
                    }
                    case MINER:
                        runMiner();
                        break;
                    case DESIGN_SCHOOL:
                        runDesignSchool();
                        break;
                    case LANDSCAPER:         runLandscaper();        break;


                }
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    private static void runLandscaper() throws GameActionException {
        if(hqLoc==null){
            findHQ();
        }
        // first, save HQ by trying to remove dirt from it
        if (hqLoc != null && hqLoc.isAdjacentTo(rc.getLocation())) {
            Direction dirtohq = rc.getLocation().directionTo(hqLoc);
            if(rc.canDigDirt(dirtohq)){
                rc.digDirt(dirtohq);
                System.out.println("Dug dirt from HQ");
            }
        }
        if(rc.getDirtCarrying() == 0){
            tryDig();
        }

        MapLocation bestPlaceToBuildWall = null;
        // find best place to build
        if(hqLoc != null) {
            int lowestElevation = 9999999;
            for (Direction dir : directions) {
                boolean build=true;
                MapLocation tileToCheck = hqLoc.add(dir);
                RobotInfo[] ri=rc.senseNearbyRobots();
                for(RobotInfo r:ri){
                    if(r.type== RobotType.MINER){
                        build=false;
                    }
                }

                if(build && rc.getLocation().distanceSquaredTo(tileToCheck) < 9
                        && rc.canDepositDirt(rc.getLocation().directionTo(tileToCheck))) {
                    if (rc.senseElevation(tileToCheck) < lowestElevation) {
                        lowestElevation = rc.senseElevation(tileToCheck);
                        bestPlaceToBuildWall = tileToCheck;
                    }
                }
            }
        }

        //if (Math.random() < 0.8){
            // build the wall
            if (bestPlaceToBuildWall != null) {
                rc.depositDirt(rc.getLocation().directionTo(bestPlaceToBuildWall));
                rc.setIndicatorDot(bestPlaceToBuildWall, 0, 255, 0);
                System.out.println("building a wall");

            }
        //}

        // otherwise try to get to the hq
        if(hqLoc != null){
            goTo(hqLoc);
        } else {
            goTo(randomDirection());
        }
    }

    private static boolean tryDig() throws GameActionException {
        Direction dir;
        if(hqLoc == null){
            dir = randomDirection();
        } else {
            dir = hqLoc.directionTo(rc.getLocation());
        }
        if(rc.canDigDirt(dir)){
            rc.digDirt(dir);
            rc.setIndicatorDot(rc.getLocation().add(dir), 255, 0, 0);
            return true;
        }
        return false;
    }

      


    private static void runDesignSchool() throws GameActionException {
        if(landscapers<3){
        for(Direction dir:directions){
            if(tryBuild(RobotType.LANDSCAPER,dir)) {
                System.out.println("Built Landscaper");
                landscapers++;
            }
            }
        }}



    private static void runMiner() throws GameActionException {
        if(hqLoc==null){
            findHQ();
        }
        updateSoupLocations();
        checkIfSoupGone();

        for (Direction dir : directions)
            if (tryMine(dir)) {
                System.out.println("I mined soup! " + rc.getSoupCarrying());
                MapLocation soupLoc = rc.getLocation().add(dir);
                if (!soupLocations.contains(soupLoc)) {
                    broadcastSoupLocation(soupLoc);
                }
            }
        if(refineryLocations.size()==0){
        updaterefineryLocations();
        if(rc.getTeamSoup()>200){
            if(refineryLocations.size()<3) {
                RobotInfo[] ri = rc.senseNearbyRobots();
                boolean builrefinery = true;
                for (RobotInfo r : ri) {
                    if (r.type == RobotType.HQ || r.type == RobotType.REFINERY || rc.senseNearbySoup().length==0 ) {
                        builrefinery = false;
                    }
                }
                if(builrefinery){
                    Direction rd=randomDirection();
                    if(tryBuild(RobotType.REFINERY,rd)){
                        System.out.println("Refinery built "+rd);
                        RobotInfo[] r=rc.senseNearbyRobots();
                        for(RobotInfo rb:r){
                            if(rb.type==RobotType.REFINERY){
                                refineryLocations.add(rb.location);
                                broadcastRefineryLocation(rb.location);
                            }
                        }
                    }
                }
            }
        }}else {
            boolean detectdesignschools = designschoolsnearby();
            if (getNewDesignSchoolCount() < 3) {
                if (detectdesignschools) {
                    if (tryBuild(RobotType.DESIGN_SCHOOL, randomDirection())) {
                        System.out.println("Built design school");
                        RobotInfo[] r = rc.senseNearbyRobots();
                        for (RobotInfo rb : r) {
                            if (rb.type == RobotType.DESIGN_SCHOOL) {
                                broadcastDesignSchoolCreation(rb.location);
                            }
                        }
                    }
                }
            }
        }



        // mine first, then when full, deposit
        for (Direction dir : directions)
            if (tryRefine(dir))
                System.out.println("I refined soup! " + rc.getTeamSoup());




        if (rc.getSoupCarrying() == RobotType.MINER.soupLimit) {
            MapLocation depositloc=hqLoc;
            int mindist=rc.getLocation().distanceSquaredTo(hqLoc);
            for(MapLocation loc: refineryLocations){
                if(rc.getLocation().distanceSquaredTo(loc) < mindist){
                    mindist=rc.getLocation().distanceSquaredTo(loc);
                    depositloc=loc;
                }
            }
            if(goTo(depositloc))
                System.out.println("moved towards "+depositloc);
        } else if (soupLocations.size() > 0) {
            goTo(soupLocations.get(0));
        } else if (goTo(randomDirection())) {
            System.out.println("I moved randomly!");
        }


    }

    private static boolean designschoolsnearby() {
        RobotInfo[] ri=rc.senseNearbyRobots();
        for(RobotInfo ds:ri){
            if(ds.type==RobotType.DESIGN_SCHOOL || ds.type==RobotType.HQ){
                return false;
            }
        }
        return true;
    }

    private static void broadcastRefineryLocation(MapLocation loc) throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = 3;
        message[2] = loc.x; // x coord of HQ
        message[3] = loc.y; // y coord of HQ
        if (rc.canSubmitTransaction(message, 1)) {
            rc.submitTransaction(message, 1);
            System.out.println("new refinery!" + loc);
        }
    }


    private static void runHQ() throws GameActionException {

        for (Direction dir : directions)
            if(numMiners < 10) {
                if(tryBuild(RobotType.MINER, dir)){
                    numMiners++;
                }
            }

    }



    public static void sendHqLoc(MapLocation loc) throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = -1;
        message[2] = loc.x; // x coord of HQ
        message[3] = loc.y; // y coord of HQ
        if (rc.canSubmitTransaction(message, 4))
            rc.submitTransaction(message, 4);
    }

    public static MapLocation getHqLocFromBlockchain() throws GameActionException {
        for (int i = 1; i < rc.getRoundNum(); i++){
            for(Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if(mess[0] == teamSecret && mess[1] == -1){
                    System.out.println("found the HQ!");
                    return new MapLocation(mess[2], mess[3]);
                }
            }
        }
        return null;
    }

    public static void broadcastDesignSchoolCreation(MapLocation loc) throws GameActionException {
        if(broadcastedCreation) return; // don't re-broadcast

        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = 1;
        message[2] = loc.x; // x coord of HQ
        message[3] = loc.y; // y coord of HQ
        if (rc.canSubmitTransaction(message, 4)) {
            rc.submitTransaction(message, 4);
            broadcastedCreation = true;
        }
    }

    // check the latest block for unit creation messages
    public static int getNewDesignSchoolCount() throws GameActionException {
        int count = 0;
        for(Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
            int[] mess = tx.getMessage();
            if(mess[0] == teamSecret && mess[1] == 1){
                System.out.println("heard about a cool new school");
                count += 1;
            }
        }
        return count;
    }

    public static void broadcastSoupLocation(MapLocation loc) throws GameActionException {
        int[] message = new int[7];
        message[0] = teamSecret;
        message[1] = 2;
        message[2] = loc.x; // x coord of HQ
        message[3] = loc.y; // y coord of HQ
        if (rc.canSubmitTransaction(message, 3)) {
            rc.submitTransaction(message, 3);
            System.out.println("new soup!" + loc);
        }
    }

    public static void updateSoupLocations() throws GameActionException {
        if(rc.getRoundNum()!=0) {
            for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == 2) {
                    if (!soupLocations.contains(new MapLocation(mess[2], mess[3]))) {
                        System.out.println("heard about a tasty new soup location");
                        soupLocations.add(new MapLocation(mess[2], mess[3]));
                    }
                }
            }
        }
    }

    public static void updaterefineryLocations() throws GameActionException {
        if(rc.getRoundNum()!=0) {
            for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamSecret && mess[1] == 3) {
                    if (!refineryLocations.contains(new MapLocation(mess[2], mess[3]))) {
                        System.out.println("heard about a refinery");
                        refineryLocations.add(new MapLocation(mess[2], mess[3]));
                    }
                }
            }
        }
    }

    public static void findHQ() throws GameActionException {
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
                hqLoc = getHqLocFromBlockchain();
            }
        }
    }
    static boolean tryMove(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
            rc.move(dir);
            return true;
        } else return false;
    }

    // tries to move in the general direction of dir
    static boolean goTo(Direction dir) throws GameActionException {
        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
        for (Direction d : toTry){
            if(tryMove(d))
                return true;
        }
        return false;
    }

    // navigate towards a particular location
    static boolean goTo(MapLocation destination) throws GameActionException {
        return goTo(rc.getLocation().directionTo(destination));
    }

    static boolean tryMine(Direction dir) throws GameActionException {
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
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }

    static void checkIfSoupGone() throws GameActionException {
        if (soupLocations.size() > 0) {
            MapLocation targetSoupLoc = soupLocations.get(0);
            if (rc.canSenseLocation(targetSoupLoc)
                    && rc.senseSoup(targetSoupLoc) == 0) {
                soupLocations.remove(0);
            }
        }
    }
    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        }
        return false;
    }

    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

}