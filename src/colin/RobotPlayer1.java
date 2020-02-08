package colin;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

import static java.lang.StrictMath.abs;

public strictfp class RobotPlayer1 {
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

    static Random random = new Random();
    static int turnCount;
    static boolean firstTurn = true;
    static MapLocation HQLocation;
    static int numMiners;
    static final int maxMiners = 6;
    static Communications1 comms = new Communications1();
    static int[][] refineries = {
            {-1,-1},
            {-1,-1},
            {-1,-1},
            {-1,-1},
            {-1,-1}
    };
    static int numRefineries = 0;
    static int maxRefineries = 2;
    static int stepsAwayFromHQ = 0;
    static ArrayList<MapLocation> soupLocations = new ArrayList<>();
    static ArrayList<MapLocation> designSchoolLocations = new ArrayList<>();
    static int numDesignSchools = 0;
    static int maxDesignSchools = 1;
    static int numLandscapers = 0;
    static int maxLandscapers = 5;
    static Direction[] designSchoolOpenDirections = { null, null, null, null, null, null, null, null };
    static int designSchoolsNumOpenDirections = 0;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer1.rc = rc;

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
                    case REFINERY:           runRefinery();          break;
//                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
//                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
                    case LANDSCAPER:         runLandscaper();        break;
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
        /*
        If its not the first round then get the block
        and deal with the messages in the block
         */
        if(rc.getRoundNum()>0){
            int[][] teamMessages = findTeamMessagesInBlockChain();

            //loop through messages from our team
            for(int[] message : teamMessages){
                //messages is initialized with all -1
                //so if there is a -1 it is the end
                if(message[0]==-1){
                    System.out.println("end of messages");
                    break;
                }else{
                    switch(message[1]){
                        case 0:
                            //message about the HQ location
                            //don't need to do anything
                            break;
                        case 1:
                            //message about the refinery location
                            refineries[numRefineries][0] = message[2];
                            refineries[numRefineries][1] = message[3];
                            numRefineries++;
                            System.out.println("Refinery Location: x:"+ message[2]+" y:"+message[3]);
                            break;
                        case 2:
                            //message about soup location
                            System.out.println("Soup Location: x:"+ message[2]+" y:"+message[3]);
                            break;
                        case 3:
                            //Remove soup
                            System.out.println("Removed soup location");
                            break;
                        case 4:
                            //New Design School
                            System.out.println("New Design School");
                            break;
                        case 5:
                            //New Landscaper
                            System.out.println("New Landscaper");
                            //broadcast the HQ location for the landscaper
                            int[] trans = {comms.teamId, 0, rc.getLocation().x, rc.getLocation().y, 0, 0, 0};
                            rc.submitTransaction(trans, 3);
                    }
                }
            }
        }

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
        /*
        Get the HQ location when first created
         */
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

        /*
        If its not the first round then get the block
        and deal with the messages in the block
         */
        if(rc.getRoundNum()>0){
            int[][] messages = findTeamMessagesInBlockChain();

            //loop through messages from our team
            for(int[] mes : messages){
                //messages is initialized with all -1
                //so if there is a -1 it is the end
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
                            if(!soupLocations.contains(loc)){
                                System.out.println("new location");
                                soupLocations.add(loc);
                            }
                            //System.out.println("Soup Location: x:"+ mes[2]+" y:"+mes[3]);
                            break;
                        case 3:
                            MapLocation loc1 = new MapLocation(mes[2], mes[3]);
                            soupLocations.remove(loc1);
                            System.out.println("Removed soup location");
                            break;
                        case 4:
                            MapLocation location2 = new MapLocation(mes[2], mes[3]);
                            designSchoolLocations.add(location2);
                            numDesignSchools++;
                            System.out.println("New Design School");
                            break;
                        case 5:
                            System.out.println("New Landscaper");
                    }
                }
            }
        }

        MapLocation[] souplocation = rc.senseNearbySoup();
        System.out.println("soups near me: "+souplocation.length);

        /*
        if the miner has reached its souplimit
        then go to the HQ
        */
        if(rc.getSoupCarrying()==RobotType.MINER.soupLimit) {

            //find the closest deposit location
            int size = numRefineries+1;
            MapLocation[] locations = new MapLocation[size];
            locations[0] = HQLocation;
            for(int i = 1; i<size; i++){
                MapLocation newLocation = new MapLocation(refineries[i-1][0],refineries[i-1][1]);
                System.out.println("adding "+newLocation);
                locations[i] = newLocation;
            }

            //find the direction
            MapLocation closest = findNearestLocation(locations);

            Direction d = rc.getLocation().directionTo(closest);
            if(inRadius(closest, rc.getLocation(), 1)){
                if(tryRefine(d)){
                    System.out.println("mined");
                }
                else{
                    System.out.println("by closest, can't mine");
                }
            }
            else{
                if(tryMove(d)){
                    System.out.println("moving to closest");
                }
                else{
                    if(tryAltMoves(d)){
                        System.out.println("moved alt");
                    }
                    else{
                        System.out.println("could not move alt");
                    }
                }
            }
        }
        else if(numDesignSchools < maxDesignSchools && rc.getTeamSoup()>150 && rc.getSoupCarrying()>5){
            /*
            Here we build a landscaper
             */
            System.out.println("Current landscapers: "+ numDesignSchools +" max: "+ maxDesignSchools);
            if(!inRadius(rc.getLocation(), HQLocation, 3)){
                Direction d = rc.getLocation().directionTo(HQLocation);
                if(tryMove(d)){
                    System.out.println("moved");
                }
                else if(tryAltMoves(d)){
                    System.out.println("moved alty");
                }
                else{
                    System.out.println("completely blocked");
                }
            }
            else{
                Direction d = oppositeLocation(rc.getLocation().directionTo(HQLocation));
                if(tryBuild(RobotType.DESIGN_SCHOOL, d)){
                    System.out.println("built design school");
                    RobotInfo[] robots = rc.senseNearbyRobots();
                    for(RobotInfo robot : robots){
                        if(robot.getType()==RobotType.DESIGN_SCHOOL){
                            MapLocation location = robot.getLocation();
                            int[] message = {comms.teamId, 4, location.x, location.y, 0,0,0};
                            rc.submitTransaction(message, 5);
                        }
                    }
                }
            }

        }
        else if(souplocation.length>10 && !inRadius(HQLocation, rc.getLocation(), 6) && !byRobot(RobotType.REFINERY) && numRefineries<maxRefineries && rc.getTeamSoup()>220 && rc.getSoupCarrying()>20){
            /*
            Here is where we build the refinery.
             */
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
                            rc.submitTransaction(refineryLocationTransaction, 20);
                        }
                    }
                }
                else {
                    System.out.println("can't build "+dir);
                }
            }
        }
        else if (souplocation.length == 0) {
            /*
            Here the miner does not detect any soup near it
             */
            //System.out.println("inside no near soup size:"+soupLocations.size());
            if(soupLocations.size()>0){
                /*
                If there is known soup locations then go there
                 */
                MapLocation soupLoc = soupLocations.get(0);
                Direction d = rc.getLocation().directionTo(soupLoc);
                boolean aroundMe = inRadius(rc.getLocation(), soupLoc, 1);
                if(aroundMe){
                    if(tryMine(d)){
                        System.out.println("mined "+d);
                    }
                    else{
                        //location around robot but could not mine.
                        int soupAmount = rc.senseSoup(soupLoc);
                        System.out.println("could not mine "+d+" soup amount: "+soupAmount);
                        if(soupAmount==0){
                            int[] message = {comms.teamId, 3, soupLoc.x, soupLoc.y, 0,0,0};
                            if(rc.getTeamSoup()>4){
                                rc.submitTransaction(message, 3);
                                System.out.println("Submitted transaction to remove soup");
                            }
                        }
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
                /*
                If there are no known soup locations move randomly?
                 */
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
            /*
            Here the robot detects that there is soup
            within its radius
             */
            boolean bySoup=false;
            int step=0;
            //first check if this soup is found already
            //or near other soup we know about
            boolean soupAlreadyKnown = false;

            /*
            Check if any of the soups in radius are not in
            the global variable.
             */
            for(MapLocation seeSoupLoc : souplocation){
                //first check if the soup is known
                for(MapLocation knownSoupLoc : soupLocations){
                    if(seeSoupLoc.x == knownSoupLoc.x && seeSoupLoc.y == knownSoupLoc.y){
                        //we already have this soup
                        soupAlreadyKnown = true;
                    }
                }
                //if soup isn't known then submit transaction
                if(!soupAlreadyKnown){
                    //broadcast the soup to everyone
                    int[] message = {comms.teamId, 2, seeSoupLoc.x, seeSoupLoc.y, 0,0,0};
                    if(!soupLocations.contains(seeSoupLoc)) {
                        if (rc.getTeamSoup() > 2) {
                            rc.submitTransaction(message, 2);
                            System.out.println("Submitted transaction for soup");
                        }
                    }
                }
            }

            /*
            Print out all the global soup locations
            this is good for debugging.

            System.out.println("Known soup locations: ");
            for(MapLocation location : soupLocations){
                System.out.println("Location: x:"+location.x+" y:"+location.y);
            }
             */

            /*
            Check if the soup is within 1 square
            Mine if it is
            Only need to go up to 7 locations because
            souplocation should be ordered by closest? <-- this is an assumption
             */
            for(MapLocation loc: souplocation) {
                if(step>7) break;
                if (inRadius(rc.getLocation(), loc, 1)) {
                    bySoup = true;
                    Direction d = rc.getLocation().directionTo(loc);
                    if(tryMine(d)){
                        System.out.println("mined "+d);
                    }else{
                        //could not mine so check if there is actually soup there?
                    }
                }
                step+=1;
            }

            /*
            Finally, if not by any soup then try and move towards
            the soup that is within the robots radius
             */
            if(!bySoup){
                //not by soup so move
                //System.out.println("i should move");
                for(MapLocation loc: souplocation){
                    Direction d = rc.getLocation().directionTo(loc);
                    if(tryMove(d)){
                        System.out.println("moved "+d);
                        break;
                    }
                    else{
                        if(tryAltMoves(d)){
                            System.out.println("moved "+d);
                        }
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
        /*
        First turn find all the open directions
        to place a landscaper
         */
        System.out.println("t soup: "+rc.getTeamSoup()+" soup: "+rc.getSoupCarrying());
        int numOpen = 0;
        Direction[] openDirections = {null, null, null, null, null, null, null, null};

        //find the good building directions
        for(Direction dir : directions){
            if(rc.canBuildRobot(RobotType.LANDSCAPER, dir)){
                System.out.println("Open direction: "+dir);
                openDirections[numOpen] = dir;
                numOpen++;
            }
            else{
                System.out.println("closed: "+dir);
            }
        }

        System.out.println("num open: "+numOpen);
        for(int i = 0; i<numOpen; i++){
            System.out.println("I: "+i+"\n"+"NumOpenDir: "+numOpen);
            if(rc.getTeamSoup()>150){
                if(tryBuild(RobotType.LANDSCAPER, openDirections[i])){
                    //submit transaction for HQ to broadcast its location
                    int[] message = {comms.teamId, 5, 0, 0, 0, 0, 0};
                    rc.submitTransaction(message, 2);
                    System.out.println("built landscaper "+openDirections[i]);
                }
            }
        }


    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
    }

    static void runLandscaper() throws GameActionException {
        //get the block and the team messages on it
        int[][] teamMessages = findTeamMessagesInBlockChain();

        for(int[] message : teamMessages){
            //messages is initialized with all -1
            //so if there is a -1 it is the end
            if(message[0]==-1){
                System.out.println("end of messages");
                break;
            }else{
                switch(message[1]){
                    case 0:
                        //message about the HQ location
                        System.out.println("HQ Location: x:"+ message[2]+" y:"+message[3]);
                        HQLocation = new MapLocation(message[2], message[3]);
                        break;
                    case 1:
                        //message about the refinery location
                        System.out.println("Refinery Location: x:"+ message[2]+" y:"+message[3]);
                        break;
                    case 2:
                        //message about soup location
                        System.out.println("Soup Location: x:"+ message[2]+" y:"+message[3]);
                        break;
                    case 3:
                        //remove soup location
                        System.out.println("Removed soup location");
                        break;
                    case 4:
                        //new design school
                        System.out.println("New Design School");
                        break;
                    case 5:
                        //another landscaper
                        System.out.println("New Landscaper");
                }
            }
        }

        // first, save HQ by trying to remove dirt from it
        if (HQLocation != null && HQLocation.isAdjacentTo(rc.getLocation())) {
            Direction dirtohq = rc.getLocation().directionTo(HQLocation);
            if(rc.canDigDirt(dirtohq)){
                rc.digDirt(dirtohq);
            }
        }

        if(rc.getDirtCarrying() == 0){
            if(tryDig()){
                System.out.println("dug");
            };
        }

        MapLocation bestPlaceToBuildWall = null;
        // find best place to build
        if(HQLocation != null) {
            int lowestElevation = 9999999;
            for (Direction dir : directions) {
                MapLocation tileToCheck = HQLocation.add(dir);
                if(rc.getLocation().distanceSquaredTo(tileToCheck) < 4
                        && rc.canDepositDirt(rc.getLocation().directionTo(tileToCheck))) {
                    if (rc.senseElevation(tileToCheck) < lowestElevation) {
                        lowestElevation = rc.senseElevation(tileToCheck);
                        bestPlaceToBuildWall = tileToCheck;
                    }
                }
            }
        }

        if (Math.random() < 0.8){
            // build the wall
            if (bestPlaceToBuildWall != null) {
                rc.depositDirt(rc.getLocation().directionTo(bestPlaceToBuildWall));
                rc.setIndicatorDot(bestPlaceToBuildWall, 0, 255, 0);
                System.out.println("building a wall");
            }
        }

        // otherwise try to get to the hq
        if(HQLocation != null){
            if(goTo(HQLocation)){
                System.out.println("moving to HQ");
            }
        } else {
            if(goTo(randomDirection())){
                System.out.println("random move");
            }
        }
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

    static boolean tryDig() throws GameActionException {
        Direction dir;
        if(HQLocation == null){
            dir = randomDirection();
        } else {
            dir = HQLocation.directionTo(rc.getLocation());
        }
        if(rc.canDigDirt(dir)){
            rc.digDirt(dir);
            rc.setIndicatorDot(rc.getLocation().add(dir), 255, 0, 0);
            return true;
        }
        return false;
    }

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

    static Direction oppositeLocation(Direction d){
        Direction newDirection = randomDirection();
        switch (d){
            case EAST:
                return Direction.WEST;
            case WEST:
                return Direction.EAST;
            case NORTH:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.NORTH;
            case NORTHEAST:
                return Direction.SOUTHWEST;
            case NORTHWEST:
                return Direction.SOUTHEAST;
            case SOUTHEAST:
                return Direction.NORTHWEST;
            case SOUTHWEST:
                return Direction.NORTHEAST;
        }
        return newDirection;
    }

    static MapLocation findNearestLocation(MapLocation[] locations){
        int count = 0;
        MapLocation closest = new MapLocation(1,1);
        MapLocation myLocation = rc.getLocation();
        System.out.println("Refinery Locations: ");
        for(MapLocation location : locations){
            System.out.println("- "+location.x+"- -"+location.y+"-");
            if(count==0){
                closest = location;
            }
            else{
                if(distanceTo(myLocation,location)<distanceTo(myLocation,closest)){
                    System.out.println("new closest: "+location);
                    closest = location;
                }
            }
            count++;
        }
        return closest;
    }

    static boolean moveToHQ() throws GameActionException {
        boolean moved = false;
        Direction d = rc.getLocation().directionTo(HQLocation);
        if(tryMove(d)){
            moved = true;
        }
        return moved;
    }

    static boolean byRobot(RobotType type){
        boolean found = false;
        RobotInfo[] robots = rc.senseNearbyRobots();
        for(RobotInfo robot : robots){
            if(robot.getType()==type){
                found = true;
            }
        }
        return found;
    }


    static boolean tryAltMoves(Direction d) throws GameActionException {
        boolean flip = (rc.getRoundNum()%2==0);
        boolean moved = false;
        switch(d) {
            case EAST:
                if (flip) {
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
                return moved;
            case WEST:
                if (flip) {
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
                return moved;
            case NORTH:
                if (flip) {
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
                return moved;
            case SOUTH:
                if (flip) {
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
                return moved;
            case NORTHEAST:
                if (flip) {
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
                return moved;
            case NORTHWEST:
                if (flip) {
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
                return moved;
            case SOUTHEAST:
                if (flip) {
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
                return moved;
            case SOUTHWEST:
                if (flip) {
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
                return moved;
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

    static int distanceTo(MapLocation location1, MapLocation location2){
        int xDifference = abs(location1.x-location2.x);
        int yDifference = abs(location1.y - location2.y);
        if(xDifference>yDifference){
            return xDifference;
        }
        else if(yDifference>xDifference){
            return yDifference;
        }
        else{
            return xDifference;
        }
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

class Communications1 {
    public Communications1(){}
    public int teamId = 5682394;
    public String[] messageType = {"HQ Location", "Refinery Location", "Add Soup Location", "Remove Soup Location", "New Design School", "New Landscaper"};
}

