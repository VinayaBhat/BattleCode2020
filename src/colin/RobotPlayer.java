package colin;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

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
    static final int maxMiners = 3;
    static Communications comms = new Communications();
    static int[][] refineries = {
            {-1,-1},
            {-1,-1},
            {-1,-1},
            {-1,-1},
            {-1,-1}
    };
    static int numRefineries = 0;
    static int stepsAwayFromHQ=0;
    static ArrayList<MapLocation> soupLocations = new ArrayList<>();

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

        int[][] messages = findTeamMessagesInBlockChain();
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

        //print refinery locations
        for(int[] location : refineries){
            if(location[0]==-1){
                break;
            }
            else{
                System.out.println("Refinery at x:"+location[0]+" y:"+location[1]);
            }

        }
        if(rc.getRoundNum()>0){
            int[][] messages = findTeamMessagesInBlockChain();

            //loop through messages from our team
            for(int[] mes : messages){
                //System.out.printf("Mes Type: "+mes[1]+" x: "+mes[2]+" y:"+mes[3]);
                if(mes[0]==-1){
                    System.out.println("end of messages");
                    break;
                }else{
                    switch(mes[1]){
                        case 0:
                            //message about the HQ location
                            System.out.println("HQ Location: x:"+ mes[2]+" y:"+mes[3]);
                            break;
                        case 1:
                            //message about the refinery location
                            refineries[numRefineries][0] = mes[2];
                            refineries[numRefineries][1] = mes[3];
                            numRefineries++;
                            System.out.println("Refinery Location: x:"+ mes[2]+" y:"+mes[3]);
                            break;
                        case 2:
                            //message about soup location
                            MapLocation loc = new MapLocation(mes[2], mes[3]);
                            soupLocations.add(loc);
                            System.out.println("Soup Location: x:"+ mes[2]+" y:"+mes[3]);
                    }
                }
            }
        }

        MapLocation[] souplocation = rc.senseNearbySoup();
        System.out.println("soups near me: "+souplocation.length);

        //if the miner has reached its souplimit
        //and if the teamsoup is enough to build a refinery
        //then build one
        if(rc.getSoupCarrying()==RobotType.MINER.soupLimit) {
            //deposit to HQ
            Direction d = rc.getLocation().directionTo(HQLocation);
            if (tryMove(d)) {
                System.out.println("moved to HQ");
            } else if(tryAltMoves(d)){

            }
            else{
                if (tryRefine(d)) {
                    System.out.println("refined");
                }
            }
        }//this next if never gets called right now.
        else if(rc.getSoupCarrying()==RobotType.MINER.soupLimit && rc.getTeamSoup()>210){
            System.out.println("steps away"+stepsAwayFromHQ);
            //needs to find a refinery or HQ
            boolean refineryPlaced = false;
            //we are close to the HQ.
            //need to change this if
            RobotInfo[] robots = rc.senseNearbyRobots();
            boolean nearHQ = false;
            for(RobotInfo robot: robots){
                System.out.println("robot type: "+robot.getType());
                if(robot.getType()==RobotType.HQ){
                    nearHQ = true;
                }
            }
            if(!nearHQ){
                stepsAwayFromHQ++;
            }
            if(nearHQ || stepsAwayFromHQ<4){
                //move away from HQ
                System.out.println("moving away from HQ");
                Direction d = rc.getLocation().directionTo(HQLocation);
                switch(d){
                    case NORTH:
                        if(tryMove(Direction.SOUTH)){
                            System.out.println("moved south");
                        }
                        break;
                    case SOUTH:
                        if(tryMove(Direction.NORTH)){
                            System.out.println("moved north");
                        }
                        break;
                    case EAST:
                        if(tryMove(Direction.WEST)){
                            System.out.println("moved west");
                        }
                        break;
                    case WEST:
                        if(tryMove(Direction.EAST)){
                            System.out.println("moved east");
                        }
                        break;
                    case NORTHEAST:
                        if(tryMove(Direction.SOUTHWEST)){
                            System.out.println("moved southwest");
                        }
                        break;
                    case NORTHWEST:
                        if(tryMove(Direction.SOUTHEAST)){
                            System.out.println("moved southeast");
                        }
                        break;
                    case SOUTHEAST:
                        if(tryMove(Direction.NORTHWEST)){
                            System.out.println("moved northwest");
                        }
                        break;
                    case SOUTHWEST:
                        if(tryMove(Direction.NORTHEAST)){
                            System.out.println("moved northeast");
                        }
                        break;
                }
            }
            else {
                Direction dir = randomDirection();
                if(tryBuild(RobotType.REFINERY, dir)){
                    System.out.println("built");
                    RobotInfo[] r = rc.senseNearbyRobots();
                    System.out.println("length: "+r.length);
                    for(RobotInfo robot : r){
                        System.out.println("Robot id: " + robot.getID() + "type: "+ robot.type);
                        if(robot.type == RobotType.REFINERY){
                            MapLocation location = robot.getLocation();
                            int[] refineryLocationTransaction = {comms.teamId, 1, location.x, location.y, 0, 0, 0};
                            rc.submitTransaction(refineryLocationTransaction, 10);
                            rc.submitTransaction(refineryLocationTransaction, 10);
                        }
                    }
                }
                else {
                    System.out.println("can't build "+dir);
                }
            }
        }
        else if (souplocation.length == 0) {
            //this should check the block for soup locations
            System.out.println("inside no near soup size:"+soupLocations.size());
            if(soupLocations.size()>0){
                //go to known soupLocations
                MapLocation soupLoc = soupLocations.get(0);
                Direction d = rc.getLocation().directionTo(soupLoc);
                boolean aroundMe = inRadius(rc.getLocation(), soupLoc, 1);
                if(aroundMe){
                    if(tryMine(d)){
                        System.out.println("mined "+d);
                    }
                    else{
                        System.out.println("could not mine "+d);
                    }
                }else{
                    if(tryMove(d)){
                        System.out.println("moved "+d);
                    }else if (tryAltMoves(d)){

                    }else {
                        System.out.println("could not move "+d);
                    }
                }
            }
            else{
                Direction rd = randomDirection();
                if (tryMove(rd)) {
                    System.out.println("Robot moved in random direction " + rd);
                } else if (tryAltMoves(rd)) {

                }
                else {
                    System.out.println("Robot could not move");
                }
            }
        }
        //there is soup nearby
        else {
            boolean bySoup=false;
            int step=0;
            //first check if this soup is found already
            //or near other soup we know about
            boolean soupAlreadyKnown = false;

            for(MapLocation seeSoupLoc : souplocation){
                for(MapLocation knownSoupLoc : soupLocations){
                    if(seeSoupLoc.x == knownSoupLoc.x && seeSoupLoc.y == knownSoupLoc.y){
                        //we already have this soup
                        soupAlreadyKnown = true;
                    }
                }
                if(!soupAlreadyKnown){
                    //broadcast the soup to everyone
                    int[] message = {comms.teamId, 2, seeSoupLoc.x, seeSoupLoc.y, 0,0,0};
                    if(rc.getSoupCarrying()>2){
                        rc.submitTransaction(message, 2);
                        System.out.println("Submitted transaction for soup");
                    }
                }
            }
            System.out.println("Known soup locations: ");
            for(MapLocation location : soupLocations){
                System.out.println("Location: x:"+location.x+" y:"+location.y);
            }

            //check if by soup
            //System.out.println("num soups "+souplocation.length);
            for(MapLocation loc: souplocation) {
                if(step>7) break;
                if (inRadius(rc.getLocation(), loc, 1)) {
                    bySoup = true;
                    Direction d = rc.getLocation().directionTo(loc);
                    if(tryMine(d)){
                        System.out.println("mined "+d);
                    }else{
                        //System.out.println("could not mine??");
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
                        System.out.println("moved "+loc.x+" "+loc.y);
                        break;
                    }
                }
            }
        }
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
            Direction d = randomDirection();
            if(tryMove(d)){

            }
        }
    }

    static void runNetGun() throws GameActionException {

    }

    static boolean tryAltMoves(Direction d) throws GameActionException {
        Random random = new Random();
        int choice = random.nextInt(2);
        boolean moved = false;
        switch(d) {
            case EAST:
                if (choice == 1) {
                    if (tryMove(Direction.NORTHEAST)) {
                        System.out.println("moved northeast");
                        moved = true;
                    }

                } else {
                    if (tryMove(Direction.SOUTHEAST)) {
                        System.out.println("moved southeast");
                        moved = true;
                    }
                }
                break;
            case WEST:
                if (choice == 1) {
                    if (tryMove(Direction.NORTHWEST)) {
                        System.out.println("moved northwest");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.SOUTHWEST)) {
                        System.out.println("moved southwest");
                        moved = true;
                    }
                }
                break;
            case NORTH:
                if (choice == 1) {
                    if (tryMove(Direction.NORTHEAST)) {
                        System.out.println("moved northeast");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.NORTHWEST)) {
                        System.out.println("moved northwest");
                        moved = true;
                    }
                }
                break;
            case SOUTH:
                if (choice == 1) {
                    if (tryMove(Direction.SOUTHEAST)) {
                        System.out.println("moved southeast");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.SOUTHWEST)) {
                        System.out.println("moved southwest");
                        moved = true;
                    }
                }
                break;
            case NORTHEAST:
                if (choice == 1) {
                    if (tryMove(Direction.NORTH)) {
                        System.out.println("moved north");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.EAST)) {
                        System.out.println("moved east");
                        moved = true;
                    }
                }
                break;
            case NORTHWEST:
                if (choice == 1) {
                    if (tryMove(Direction.NORTH)) {
                        System.out.println("moved north");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.WEST)) {
                        System.out.println("moved west");
                        moved = true;
                    }
                }
                break;
            case SOUTHEAST:
                if (choice == 1) {
                    if (tryMove(Direction.SOUTH)) {
                        System.out.println("moved south");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.EAST)) {
                        System.out.println("moved east");
                        moved = true;
                    }
                }
                break;
            case SOUTHWEST:
                if (choice == 1) {
                    if (tryMove(Direction.SOUTH)) {
                        System.out.println("moved south");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.WEST)) {
                        System.out.println("moved west");
                        moved = true;
                    }
                }
                break;

        }
        return moved;
    }

    static int[][] findTeamMessagesInBlockChain() throws GameActionException {
        Transaction[] transactions = rc.getBlock(rc.getRoundNum()-1);
        int[][] allMessages = {
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1}};

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