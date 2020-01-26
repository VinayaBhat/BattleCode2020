package team10player;

import battlecode.common.*;
import com.sun.org.apache.regexp.internal.recompile;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
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
    static int refineries = 0;
    static Communications comms = new Communications();
    static ArrayList<MapLocation> souplocation = new ArrayList<>();
    static ArrayList<MapLocation> refinerylocation = new ArrayList<>();




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
        if (miners < 10) {
            for (Direction dir : directions) {  //Building miners in all directions around HQ
                if (tryBuild(RobotType.MINER, dir)) {
                    System.out.println("Miner" + miners + " created at " + rc.getLocation());
                    miners = miners + 1;
                }
            }
        }
    }

    static void runMiner() throws GameActionException {
        int[][] messages = findInBlockChain(1);
        int attemptstomine=0;

        if (HQLoc == null) {
            RobotInfo[] hq = rc.senseNearbyRobots();
            for (RobotInfo ishqpresent : hq) {
                if (ishqpresent.type == RobotType.HQ && rc.getTeam() == ishqpresent.team) {
                    HQLoc = ishqpresent.location;
                    System.out.println("HQ found at " + HQLoc);
                }
            }
        }
        checkIfSoupGone();
        MapLocation[] souploc = rc.senseNearbySoup();
        for (MapLocation sd : souploc) {
            boolean repeated = false;
            for (int[] m : messages) {
                if (m[1] == 0) {
                    if (!souplocation.contains(new MapLocation(m[2], m[3]))) {
                        System.out.println("Adding message soup location to souplocaiton");
                        souplocation.add(new MapLocation(m[2], m[3]));
                    }
                }else if(m[1]==1){
                    if(!refinerylocation.contains(new MapLocation(m[2],m[3]))) {
                        refinerylocation.add(new MapLocation(m[2], m[3]));
                        System.out.println("Refinery built "+m[2]+" "+m[3]);
                        refineries++;
                    }
                }
            }
            if (!souplocation.contains(sd)) {
                souplocation.add(sd);
                System.out.println("Soup location added");
                int[] refineryLocationTransaction = {comms.teamId, 0, sd.x, sd.y, 0, 0, 0};
                rc.submitTransaction(refineryLocationTransaction, 1);
            }
        }
        if(rc.getTeamSoup()>=200) {
            if (refineries < 4) {
                Direction rd = randomDirection();
                if (tryBuild(RobotType.REFINERY, rd)) {
                    refineries++;
                    int x=rd.dx;
                    int y=rd.dy;
                    refinerylocation.add(new MapLocation(x,y));
                    int[] refineryLocationTransaction = {comms.teamId, 1, x,y, 0, 0, 0};
                    rc.submitTransaction(refineryLocationTransaction, 1);

                }
            }
        }


        checkIfSoupGone();
        if (rc.getSoupCarrying() == RobotType.MINER.soupLimit) {
            Direction minerdir;
            int mindist=rc.getLocation().distanceSquaredTo(HQLoc);
            minerdir = rc.getLocation().directionTo(HQLoc);
            for(MapLocation ml:refinerylocation){
                if(rc.getLocation().distanceSquaredTo(ml)<mindist){
                    minerdir=rc.getLocation().directionTo(ml);
                    mindist=rc.getLocation().distanceSquaredTo(ml);
                }
            }
            if (tryMove(minerdir)) {
                System.out.println("trying to move towards HQ to deposit soup");
            }
            if (tryRefine(minerdir)) {
                System.out.println("Pollution before refining " + rc.sensePollution(HQLoc));
                System.out.println("Miner deposited soup at HQ and refined");
                System.out.println("Pollution after refining " + rc.sensePollution(HQLoc));
            }
        } else if (souplocation.size() > 0) {
                if (goTo(rc.getLocation().directionTo(souplocation.get(0)))) {
                    System.out.println("Ming towards soup 0");
                }
                if (tryMine(rc.getLocation().directionTo(souplocation.get(0)))) {
                    System.out.println("ROBOT MINED SOUP");
                }
        } else {
            Direction rd = randomDirection();
            if (tryMove(rd)) {
                System.out.println("ROBOT moved randomly");
            }

            }



        //tryBlockchain();


    }


    private static int[][] findInBlockChain(int i) throws GameActionException {
        Transaction[] transactions = rc.getBlock(rc.getRoundNum() - 1);
        int[][] allMessages = new int[7][7];
        int count = 0;
        for (Transaction transaction : transactions) {
            int[] message = transaction.getMessage();
            if (message[0] == comms.teamId) {
                allMessages[count] = message;
                count++;
            }
        }
        return allMessages;
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
        for (Direction d : toTry) {
            if (tryMove(d))
                return true;
        }
        return false;
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
     * @param dir  The intended direction of movement
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


}

class Communications {
    public Communications() {
    }

    public int teamId = 5682394;
    public String[] messageType = {"HQ Location", "Refinery Location", "Soup Location"};
}