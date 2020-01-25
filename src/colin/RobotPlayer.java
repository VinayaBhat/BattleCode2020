package colin;
import battlecode.common.*;

import static java.lang.StrictMath.abs;

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
    static MapLocation HQLocation;
    static int numMiners;
    static final int maxMiners = 8;
    static Communications comms = new Communications();
    static int numRefineries;

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

        // System.out.println("I'm a " + rc.getType() + " and I just got created!");

        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                // System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:                 runHQ();                break;
                    case MINER:              runMiner();             break;
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
        System.out.println("Soup: "+rc.getTeamSoup());
        MapLocation[] soupLocations = rc.senseNearbySoup();

        int[][] messages = findInBlockChain(0);
        System.out.println("Messages in block: "+messages.length);
        if(rc.getTeamSoup()>25 && numMiners<maxMiners){
            boolean minerPlaced = false;
            Direction d = null;
            if(soupLocations.length!=0){
                d = rc.getLocation().directionTo(soupLocations[0]);
            }
            if(d!=null) {
                if (tryBuild(RobotType.MINER, d)) {
                    System.out.println("created miner towards soup");
                    minerPlaced = true;
                    numMiners++;
                }
            }
            if(!minerPlaced) {
                //no miner placed in preferred direction
                //place randomly
                for (int i = 0; i < 7; i++) {
                    if (tryBuild(RobotType.MINER, directions[i])) {
                        System.out.println("created miner randomly");
                        numMiners++;
                        break;
                    }
                }
            }
        }
    }

    static void runMiner() throws GameActionException {

        System.out.println("soup carrying: "+rc.getSoupCarrying());
        if (HQLocation == null) {
            RobotInfo[] searchRobot = rc.senseNearbyRobots();
            for (RobotInfo robot : searchRobot) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    HQLocation = robot.location;
                    System.out.println(" HQ location " + HQLocation);
                }
            }
        }

        MapLocation[] souplocation = rc.senseNearbySoup();

        //if no nearby soup then move randomly
        if(rc.getSoupCarrying()==RobotType.MINER.soupLimit && rc.getTeamSoup()>210){
            //needs to find a refinery

            boolean refineryPlaced = false;
            if (inRadius(HQLocation, rc.getLocation(), 3)){
                Direction dir = randomDirection();
                if(tryBuild(RobotType.REFINERY, dir)){
                    System.out.println("built");
                    RobotInfo[] robots = rc.senseNearbyRobots();
                    System.out.println("length: "+robots.length);
                    for(RobotInfo robot : robots){
                        System.out.println("Robot id: " + robot.getID() + "type: "+ robot.type);
                        if(robot.type == RobotType.REFINERY){
                            MapLocation location = robot.getLocation();
                            int[] refineryLocationTransaction = {comms.teamId, 0, location.x, location.y, 0, 0, 0};
                            rc.submitTransaction(refineryLocationTransaction, 10);
                        }
                    }
                }
                else {
                    System.out.println("can't build "+dir);
                }
            }else{
                Direction d = rc.getLocation().directionTo(HQLocation);
                if(tryMove(d))
                    System.out.println("Moving to HQ");
            }
            tryBuild(RobotType.REFINERY, randomDirection());
        }
        else if (souplocation.length == 0) {
            //this should check the block for soup locations
            Direction rd = randomDirection();
            if (tryMove(rd)) {
                System.out.println("Robot moved in random direction " + rd);
            } else {
                System.out.println("Robot could not move");
            }
        }
        //there is soup nearby
        else {
            boolean bySoup=false;
            int step=0;
            //check if by soup
            System.out.println("num soups "+souplocation.length);
            for(MapLocation loc: souplocation) {
                if(step>7) break;
                System.out.println("Soup location: "+loc);
                System.out.println("X dif: "+ abs(rc.getLocation().x - loc.x));
                System.out.println("Y dif: "+ abs(rc.getLocation().y - loc.y));
                if (inRadius(rc.getLocation(), loc, 1)) {
                    bySoup = true;
                    Direction d = rc.getLocation().directionTo(loc);
                    if(tryMine(d)){
                        System.out.println("mined "+d);
                    }else{
                        System.out.println("could not mine??");
                    }
                }
                step+=1;
            }
            if(!bySoup){
                //not by soup so move
                //System.out.println("i should move");
                for(MapLocation loc: souplocation){
                    Direction d = rc.getLocation().directionTo(loc);
                    if(tryMove(d)){
                        System.out.println("moved "+d);
                        break;
                    }
                }
            }
        }

        //try and build refinery
        //for(Direction dir:directions){
        //    boolean refinerybuilt=tryBuild(RobotType.REFINERY,dir);
        //    System.out.println("Refinery Built " +refinerybuilt);
        //}
    }

    static void minerMove(Direction d){

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

    static int[][] findInBlockChain(int messageType) throws GameActionException {
        Transaction[] transactions = rc.getBlock(rc.getRoundNum()-1);
        int[][] allMessages = new int[7][7];
        int count = 0;
        for(Transaction transaction : transactions){
            int[] message = transaction.getMessage();
            if(message[0]==comms.teamId){
                System.out.println("found one");
                allMessages[count] = message;
                count++;
            }
        }
        return allMessages;
    }

    static boolean inRadius(MapLocation loc1, MapLocation loc2, int radius){
        return (abs(loc1.x-loc2.x) < radius+1 && abs(loc1.y-loc2.y) < radius+1);
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

class Communications {
    public Communications(){}
    public int teamId = 5682394;
    public String[] messageType = {"HQ Location", "Refinery Location", "Soup Location"};
}

