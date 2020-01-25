package team10player;
import battlecode.common.*;
import com.sun.org.apache.regexp.internal.recompile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public strictfp class RobotPlayer {
    static RobotController rc;

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
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    static int turnCount;
    static int miners = 0;
    static MapLocation HQLoc;
    static int refineries=0;
    static ArrayList<MapLocation> souplocation=new ArrayList<>();
    static Map<MapLocation,Boolean> map=new HashMap<MapLocation, Boolean>();




    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;


        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                // System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:
                        runHQ();
                        break;
                    case MINER:
                        runMiner();
                        break;
//                    case REFINERY:           runRefinery();          break;
//                    case VAPORATOR:          runVaporator();         break;
//                    case DESIGN_SCHOOL:      runDesignSchool();      break;
//                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
//                    case LANDSCAPER:         runLandscaper();        break;
//                    case DELIVERY_DRONE:     runDeliveryDrone();     break;
//                    case NET_GUN:            runNetGun();            break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runHQ() throws GameActionException {
        if (miners < 10){
        for (Direction dir : directions) {  //Building miners in all directions around HQ
            if (tryBuild(RobotType.MINER, dir)) {
                System.out.println("Miner" + miners + " created at " + rc.getLocation());
                miners = miners + 1;
            }
        }
    }
    }

    static void runMiner() throws GameActionException {
        if(HQLoc==null){
            RobotInfo[] hq=rc.senseNearbyRobots();
            for(RobotInfo ishqpresent:hq){
                if(ishqpresent.type==RobotType.HQ && rc.getTeam()==ishqpresent.team){
                    HQLoc=ishqpresent.location;
                    System.out.println("HQ found at "+HQLoc);
                }
            }
        }
        checkIfSoupGone();



        if(rc.getSoupCarrying()==RobotType.MINER.soupLimit){
            Direction minerdir=rc.getLocation().directionTo(HQLoc);
            if(tryMove(minerdir)){
                System.out.println("trying to move towards HQ to deposit soup");
            }
            if(tryRefine(minerdir)){
                System.out.println("Pollution before refining "+rc.sensePollution(HQLoc));
                System.out.println("Miner deposited soup at HQ and refined");
                System.out.println("Pollution after refining "+rc.sensePollution(HQLoc));
            }
        }else if(souplocation.size()>0){
            goTo(rc.getLocation().directionTo(souplocation.get(0)));
            if(tryMine(rc.getLocation().directionTo(souplocation.get(0)))) {
                System.out.println("ROBOT MINED SOUP");
            }

        }else{
            Direction rd=randomDirection();
            if(tryMove(rd)){
                System.out.println("ROBOT moved randomly");
            }
        }

        //tryBlockchain();
        MapLocation[] souploc=rc.senseNearbySoup();
        for(MapLocation sd:souploc){
            if(!souplocation.contains(sd)){
                souplocation.add(sd);
            }
        }
        if(refineries<2){
            for(int i=0;i<2;i++){
                tryMove(randomDirection());
            }
            if(tryBuild(RobotType.REFINERY,randomDirection())){
                System.out.println("Robot built at refinery");
                refineries++;
            }
        }


            }

    private static void checkIfSoupGone() throws GameActionException {
        if (souplocation.size() > 0) {
            MapLocation targetSoupLoc = souplocation.get(0);
            if (rc.canSenseLocation(targetSoupLoc)
                    && rc.senseSoup(targetSoupLoc) == 0) {
                souplocation.remove(0);
            }
        }
    }

    private static boolean goTo(Direction dir) throws GameActionException {
        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
        for (Direction d : toTry){
            if(tryMove(d))
                return true;
        }
        return false;
    }

    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));

    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {

    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    static void runLandscaper() throws GameActionException {

    }

    static void runDeliveryDrone() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within capturing range
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
                System.out.println("I picked up " + robots[0].getID() + "!");
            }
        } else {
            // No close robots, so search for robots within sight radius
            tryMove(randomDirection());
        }
    }

    static void runNetGun() throws GameActionException {

    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }

    static boolean tryMove() throws GameActionException {
        for (Direction dir : directions)
            if (tryMove(dir))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
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


    static void tryBlockchain() throws GameActionException {
        if (turnCount < 3) {
            int[] message = new int[7];
            for (int i = 0; i < 7; i++) {
                message[i] = 123;
            }
            if (rc.canSubmitTransaction(message, 10))
                rc.submitTransaction(message, 10);

        }
        // System.out.println(rc.getRoundMessages(turnCount-1));

    }
}
