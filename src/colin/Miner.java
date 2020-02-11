package colin;
import battlecode.common.*;

import java.util.ArrayList;

public class Miner extends Unit {

    MapLocation HQLocation;
    int maxRefineries = 2;
    ArrayList<MapLocation> refineries = new ArrayList<>();
    ArrayList<MapLocation> soupLocations = new ArrayList<>();
    boolean fulfillmentCenterCreated = false;
    int numDesignSchools = 0;
    int maxDesignSchools = 1;
    int numLandscapers = 0;
    int diagonalMovementCount = 0;
    Direction diagonalDirection;

    public Miner(RobotController r){
        super(r);
    }

    public void takeTurn() throws GameActionException {
        System.out.println("ID: "+rc.getID()+" soup: "+rc.getSoupCarrying());

        //get HQ Location when first made.
        if(HQLocation==null){
            getHQLocation();
        }

        //Miners trying to sense water
       for(Direction dir:Util.directions){
           if(rc.canSenseLocation(rc.getLocation().add(dir)))
               if(rc.senseFlooding(rc.getLocation().add(dir))){
                   int[] message={comms.teamId,10,rc.getLocation().add(dir).x,rc.getLocation().add(dir).y,0,0,0};
                   if(rc.canSubmitTransaction(message,3)){
                       rc.submitTransaction(message,3);
                   }
               }
       }

        printRefineries();

        //always get blockchain messages first
        dealWithBlockchainMessages();

        /*
        Next an else if for buildlings
        This forces priority over soup
         */

        MapLocation[] nearbySoupLocations = rc.senseNearbySoup();
        System.out.println("soup nearby: "+nearbySoupLocations.length);
        /*
        if the miner has reached its souplimit
        then go to deposit
        */
        if(rc.getSoupCarrying()==RobotType.MINER.soupLimit) {
           /*
            Robot is full of soup
            */
           findClosestDepositLocation();
           diagonalMovementCount = 0;
        }
        else if(rc.getTeamSoup()>155 && !fulfillmentCenterCreated && rc.getSoupCarrying() > 3 ){
            /*
            Build a Fulfillment Center
             */
            buildFulfillmentCenter();
            diagonalMovementCount = 0;
        }
        else if(numDesignSchools < maxDesignSchools && rc.getTeamSoup()>155 && rc.getSoupCarrying()>5 && !nav.byRobot(RobotType.DESIGN_SCHOOL) && refineries.size()>0){
            buildDesignSchool();
            diagonalMovementCount = 0;

        }
        else if(!nav.byRobot(RobotType.REFINERY) && refineries.size()<maxRefineries && rc.getTeamSoup()>220 && rc.getSoupCarrying()>20){
            diagonalMovementCount = 0;
            if(refineries.size()>0){
                //check if far away from other refineries
                for(MapLocation refinery : refineries){
                    if(!nav.inRadius(refinery, rc.getLocation(), 10) && nearbySoupLocations.length>2){
                        buildRefinery();
                    }
                }
            }
            else{
                buildRefinery();
            }
        }
        //there is soup nearby
        else if(nearbySoupLocations.length>0) {
            /*
            Soup Nearby!
             */
            goToNearbySoup(nearbySoupLocations);
            diagonalMovementCount = 0;
        }

        if (nearbySoupLocations.length == 0) {
            noNearbySoup();
        }
    }

    private void getHQLocation() {
        /*
        Get the HQ location when first created
         */
        if (HQLocation == null) {
            RobotInfo[] searchRobot = rc.senseNearbyRobots();
            for (RobotInfo robot : searchRobot) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    HQLocation = robot.location;
                    System.out.println("HQ location " + HQLocation);
                }
            }
        }
    }

    private void printRefineries() {
        System.out.println("Refineries: ");
        for(MapLocation location : refineries ){
            System.out.println("  x: " + location.x + " y: " + location.y );
        }
    }

    private void dealWithBlockchainMessages() throws GameActionException {
        int[][] messages = comms.findTeamMessagesInBlockChain();

        //loop through messages from our team
        for(int[] mes : messages){
            //messages is initialized with all -1
            //so if there is a -1 it is the end
            if(mes[0]==-1){
                //end of messages
                break;
            }else{
                switch(mes[1]){
                    case 0:
                        //message about the HQ location
                        if(HQLocation==null){
                            HQLocation = new MapLocation(mes[2],mes[3]);
                            System.out.println("HQ Location: x:"+ mes[2]+" y:"+mes[3]);
                        }
                        break;
                    case 1:
                        //message about the refinery location
                        MapLocation newLocation = new MapLocation(mes[2], mes[3]);
                        if(!refineries.contains(newLocation)){
                            System.out.println("New Refinery");
                            refineries.add(newLocation);
                        }
                        break;
                    case 2:
                        //message about soup location
                        MapLocation loc = new MapLocation(mes[2], mes[3]);
                        if(!soupLocations.contains(loc)){
                            System.out.println("New Soup Location");
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
                        fulfillmentCenterCreated = true;
                        System.out.println("Fulfillment Created");
                        break;
                }
            }
        }
    }

    private void goToNearbySoup(MapLocation[] nearbySoupLocations) throws GameActionException {
        diagonalMovementCount = 0;

        boolean bySoup = false;
        for (Direction dir : Util.directions) {
            if (tryMine(dir)) {
                bySoup = true;
                MapLocation soupLoc = rc.getLocation().add(dir);
                if (!soupLocations.contains(soupLoc)) {
                    int[] message = {comms.teamId, 2, soupLoc.x, soupLoc.y, 0,0,0};
                    soupLocations.add(soupLoc);
                    rc.submitTransaction(message, 1);
                }
            }
        }

        /*
        Finally, if not by any soup then try and move towards
        the soup that is within the robots radius
         */
        if(!bySoup){
            //not by soup so move
            //System.out.println("i should move");
            for(MapLocation loc: nearbySoupLocations){
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

    private void buildFulfillmentCenter() throws GameActionException {
        if(nav.inRadius(rc.getLocation(), HQLocation, 2) && !nav.inRadius(rc.getLocation(), HQLocation, 1)){
            /*
            Build Fulfillment
             */
            Direction d = nav.oppositeDirection(rc.getLocation().directionTo(HQLocation));
            if(tryBuild(RobotType.FULFILLMENT_CENTER, d)){
                System.out.println("built fulfillment");
                fulfillmentCenterCreated = true;
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

    private void findClosestDepositLocation() throws GameActionException {
        //find the closest deposit location
        MapLocation[] locations;
        if(numLandscapers<1){
                /*
                Before landscapers
                 */
            int size = refineries.size()+1;
            locations = new MapLocation[size];
            locations[0] = HQLocation;
            int slot = 1;
            for(MapLocation refinery : refineries){
                locations[slot] = refinery;
                slot++;
            }
        }
        else {
            /*
            After Landscapers
             */
            int slot = 0;
            locations = new MapLocation[refineries.size()];
            for(MapLocation refinery : refineries){
                locations[slot] = refinery;
                slot++;
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

    private void noNearbySoup() throws GameActionException{
        /*
        Here the miner does not detect any soup near it
         */
        System.out.println("in nearby soup");
        if(soupLocations.size()>0){
            /*
            If there is known soup locations then go there
             */
            goToKnownSoup();

            //Print known soup
            System.out.println("soup: ");
            for(MapLocation soup : soupLocations){
                System.out.println(" ["+soup.x+","+soup.y+"]");
            }
        }
        else{
            /*
            If there are no known soup locations move randomly?
             */

            /*Direction rd = nav.randomDirection();
            if (nav.tryMove(rd)) {
                System.out.println("Robot moved in random direction " + rd);
            } else if (nav.tryAltMoves(rd)) {

            }
            else {
                System.out.println("Robot could not move");
            }*/

            doScreenSaverMovement();
        }
    }

    private void doScreenSaverMovement() throws GameActionException {
        diagonalMovementCount++;

        if(diagonalDirection==null || diagonalMovementCount==0){
            diagonalDirection = nav.getRandomDiagonal();
        }
        else{
            //move diagonally
            if(nav.tryMove(diagonalDirection)){
                System.out.println("Moving Diagonally");
            }
            else{
                System.out.println("blocked, getting new direction");
                diagonalDirection = nav.getNextDiagonal(diagonalDirection);
                if(nav.tryMove(diagonalDirection)){
                    System.out.println("moved new direction");
                }
            }
        }
    }

    private void goToKnownSoup() throws GameActionException {
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

    public void buildDesignSchool() throws GameActionException {
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
            Direction d = nav.oppositeDirection(rc.getLocation().directionTo(HQLocation));
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

    public void buildRefinery() throws GameActionException {
        Direction dir = nav.randomDirection();
        if(nav.inRadius(rc.getLocation(), HQLocation, 3)){
            //move away from HQ
            moveAwayFromHQ();
        }
        else {
            if (tryBuild(RobotType.REFINERY, dir)) {
                System.out.println("built");
                RobotInfo[] r = rc.senseNearbyRobots();
                System.out.println("length: " + r.length);
                for (RobotInfo robot : r) {
                    if (robot.type == RobotType.REFINERY) {
                        System.out.println("sending refinery location");
                        MapLocation location = robot.getLocation();
                        int[] refineryLocationTransaction = {comms.teamId, 1, location.x, location.y, 0, 0, 0};
                        rc.submitTransaction(refineryLocationTransaction, 20);
                    }
                }
            } else {
                System.out.println("can't build " + dir);
            }
        }
    }

    private void moveAwayFromHQ() throws GameActionException {
        Direction d = rc.getLocation().directionTo(HQLocation);
        Direction opposite = nav.oppositeDirection(d);
        if(nav.tryMove(opposite)){
            System.out.println("Moved away from HQ");
        }
        else if (nav.tryAltMoves(opposite)){
            System.out.println("Moved away from HQ alt");
        }
        else{
            System.out.println("can't move at all");
        }
    }

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
}
