package colin;
import battlecode.common.*;
import java.util.ArrayList;
import java.util.Map;

public class Miner extends Unit {

    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};
    static int turnCount;
    static MapLocation HQLocation;
    static int [][] refineries = {
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
    static int numDesignSchools = 0;
    static int maxDesignSchools = 1;
    static int numLandscapers = 0;
    int numFulfillmentCenter = 0;
    int maxFulfillmentCenter = 1;

    public Miner(RobotController r){
        super(r);
    }

    boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    public void takeTurn() throws GameActionException {
        System.out.println("ID: "+rc.getID()+"soup carrying: "+rc.getSoupCarrying());
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
       for(Direction dir: Util.directions){
           if(rc.senseFlooding(rc.adjacentLocation(dir))){
               MapLocation l=rc.adjacentLocation(dir);
               int[] message={comms.teamId, 10,l.x, l.y, 0, 0, 0};
               if(rc.canSubmitTransaction(message,3)){
                   rc.submitTransaction(message,3);
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
        int[][] messages = comms.findTeamMessagesInBlockChain();

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
                        numDesignSchools++;
                        System.out.println("New Design School num: "+numDesignSchools);
                        break;
                    case 5:
                        numLandscapers++;
                        System.out.println("New Landscaper");
                        break;
                    case 8:
                        numFulfillmentCenter++;
                        System.out.println("New Fulfillment Center");
                        break;
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
            MapLocation[] locations;
            if(numLandscapers<1){
                System.out.println("Before landscapers");
                int size = numRefineries+1;
                locations = new MapLocation[size];
                locations[0] = HQLocation;
                for(int i = 1; i<size; i++){
                    MapLocation newLocation = new MapLocation(refineries[i-1][0],refineries[i-1][1]);
                    System.out.println("adding "+newLocation);
                    locations[i] = newLocation;
                }
            }
            else {
                System.out.println("After landscapers");
                locations = new MapLocation[numRefineries];
                for(int i = 0; i<numRefineries; i++){
                    MapLocation newLocation = new MapLocation(refineries[i][0], refineries[i][1]);
                    System.out.println("adding "+newLocation);
                    locations[i] = newLocation;
                }
            }

            //find the direction
            MapLocation closest = nav.findNearestLocation(rc.getLocation(), locations);

            Direction d = rc.getLocation().directionTo(closest);
            if(nav.inRadius(closest, rc.getLocation(), 1)){
                if(tryRefine(d)){
                    System.out.println("mined");
                }
                else{
                    System.out.println("by closest, can't mine");
                }
            }
            else{
                if(nav.tryMove(d)){
                    System.out.println("moving to closest");
                }
                else{
                    if(nav.tryAltMoves(d)){
                        System.out.println("moved alt");
                    }
                    else{
                        System.out.println("could not move alt");
                    }
                }
            }
        }
        else if(rc.getTeamSoup()>155 && numRefineries>0 && numFulfillmentCenter<1 && rc.getSoupCarrying() > 3 ){
            /*
            Build a Fulfillment Center
             */

            if(nav.inRadius(rc.getLocation(), HQLocation, 2) && !nav.inRadius(rc.getLocation(), HQLocation, 1)){
                /*
                Build Fulfillment
                 */

                Direction d = nav.randomDirection();
                if(tryBuild(RobotType.FULFILLMENT_CENTER, d)){
                    System.out.println("built fulfillment");
                    RobotInfo[] robots = rc.senseNearbyRobots();
                    for(RobotInfo robot : robots){
                        if(robot.getType()==RobotType.FULFILLMENT_CENTER){
                            /*
                            Transmit the fulfillment center x and y
                             */
                            MapLocation location = robot.getLocation();
                            int[] message = {comms.teamId, 8, location.x, location.y, 0, 0, 0};
                            rc.submitTransaction(message, 2);
                        }
                    }

                }
            }
            else if(nav.inRadius(rc.getLocation(), HQLocation, 1)){
                /*
                Move away from HQ
                 */
            }
            else {
                /*
                Move to HQ
                 */
                Direction d = rc.getLocation().directionTo(HQLocation);
                if(nav.tryMove(d)){

                }
            }

        }
        else if(numDesignSchools < maxDesignSchools && rc.getTeamSoup()>150 && rc.getSoupCarrying()>5 && !nav.byRobot(RobotType.DESIGN_SCHOOL) && numRefineries>0){
            /*
            Here we build a landscaper
             */
            System.out.println("Current design schools: "+ numDesignSchools +" max: "+ maxDesignSchools);
            if(!nav.inRadius(rc.getLocation(), HQLocation, 3)){
                Direction d = rc.getLocation().directionTo(HQLocation);
                if(nav.tryMove(d)){
                    System.out.println("moved");
                }
                else if(nav.tryAltMoves(d)){
                    System.out.println("moved alty");
                }
                else{
                    System.out.println("completely blocked");
                }
            }
            else{
                Direction d = nav.oppositeLocation(rc.getLocation().directionTo(HQLocation));
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
        else if(!nav.byRobot(RobotType.REFINERY) && numRefineries<maxRefineries && rc.getTeamSoup()>220 && rc.getSoupCarrying()>20){
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
                        if(nav.tryMove(Direction.SOUTH)){
                            System.out.println("moved south");
                        }
                        break;
                    case SOUTH:
                        if(nav.tryMove(Direction.NORTH)){
                            System.out.println("moved north");
                        }
                        break;
                    case EAST:
                        if(nav.tryMove(Direction.WEST)){
                            System.out.println("moved west");
                        }
                        break;
                    case WEST:
                        if(nav.tryMove(Direction.EAST)){
                            System.out.println("moved east");
                        }
                        break;
                    case NORTHEAST:
                        if(nav.tryMove(Direction.SOUTHWEST)){
                            System.out.println("moved southwest");
                        }
                        break;
                    case NORTHWEST:
                        if(nav.tryMove(Direction.SOUTHEAST)){
                            System.out.println("moved southeast");
                        }
                        break;
                    case SOUTHEAST:
                        if(nav.tryMove(Direction.NORTHWEST)){
                            System.out.println("moved northwest");
                        }
                        break;
                    case SOUTHWEST:
                        if(nav.tryMove(Direction.NORTHEAST)){
                            System.out.println("moved northeast");
                        }
                        break;
                }
            }
            else {
                Direction dir = nav.randomDirection();
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
                boolean aroundMe = nav.inRadius(rc.getLocation(), soupLoc, 1);
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
                    if(nav.tryMove(d)){
                        System.out.println("moved "+d);
                    }else if (nav.tryAltMoves(d)){

                    }else {
                        System.out.println("could not move "+d);
                    }
                }
            }
            else{
                /*
                If there are no known soup locations move randomly?
                 */
                Direction rd = nav.randomDirection();
                if (nav.tryMove(rd)) {
                    System.out.println("Robot moved in random direction " + rd);
                } else if (nav.tryAltMoves(rd)) {

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
                            rc.submitTransaction(message, 1);
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
                if (nav.inRadius(rc.getLocation(), loc, 1)) {
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
                    if(nav.tryMove(d)){
                        System.out.println("moved "+d);
                        break;
                    }
                    else{
                        if(nav.tryAltMoves(d)){
                            System.out.println("moved "+d);
                        }
                    }
                }
            }
        }
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
}
