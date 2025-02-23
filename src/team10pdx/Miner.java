package team10pdx;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Dictionary;

public class Miner extends Unit {

    MapLocation HQLocation;
    int maxRefineries = 3;
    ArrayList<MapLocation> refineries = new ArrayList<>();
    ArrayList<MapLocation> soupLocations = new ArrayList<>();
    ArrayList<MapLocation> waterLocations = new ArrayList<>();
    boolean fulfillmentCenterCreated = false;
    int numDesignSchools = 0;
    int maxDesignSchools = 1;
    int numLandscapers = 0;
    int diagonalMovementCount = 0;
    MapLocation closestRefineLocation;
    Direction diagonalDirection;
    int numVaporators = 0;
    int netgun=0;
    int[] buildnetgun={0,0};
    boolean builder;
    MapLocation[] nearbySoupLocations;
    MapLocation myLocation;
    int teamSoup;
    int soupCarrying;
    RobotInfo[] nearbyRobots;

    public Miner(RobotController r){
        super(r);
        builder = rc.getID()%2 == 0;
    }

    public void takeTurn() throws GameActionException {
        nearbySoupLocations = rc.senseNearbySoup();
        myLocation = rc.getLocation();
        teamSoup = rc.getTeamSoup();
        soupCarrying = rc.getSoupCarrying();

        if (HQLocation == null) {
            getHQLocation();
        }

        senseWaterNearby();
        dealWithBlockchainMessages();

        //--Functions for Debugging--
        //printSoupLocations();
        //printRefineries();

        closestRefineLocation = nav.findNearestLocation(rc.getLocation(), getPossibleRefineLocations());
        checkSurroundingsForKnownSoup();

        if(builder){
            runBuilder();
        }
        else {
            runMiner();
        }
    }

    private void runBuilder() throws GameActionException{
        System.out.println("I'm a Builder!");
        System.out.println("soup nearby: " + nearbySoupLocations.length);

        boolean canBuildVaporator=false;
        nearbyRobots = rc.senseNearbyRobots();

        for(RobotInfo robot : nearbyRobots){
            if(robot.getType()==RobotType.HQ || robot.getType()==RobotType.REFINERY){
                canBuildVaporator=true;
            }
        }

        if (soupCarrying == RobotType.MINER.soupLimit) {
           /*
            Robot is full of soup
            */
            System.out.println("Go to closest soup");
            goToClosestDeposit();
            diagonalMovementCount = 0;
        } else if (nearbySoupLocations.length > 0 && teamSoup > 500 && soupCarrying > 4 && nearbySoupLocations.length > 0 && canBuildVaporator) {
                System.out.println("build vap");
                diagonalMovementCount = 0;
                buildVaporator();
        }
        else if (!nav.byRobot(RobotType.REFINERY) && refineries.size() < 1 && teamSoup > 220 && soupCarrying > 20 && nearbySoupLocations.length > 2) {
            System.out.println("In Refinery");
            diagonalMovementCount = 0;
            buildRefinery();
        } else if (nav.distanceTo(myLocation, closestRefineLocation) > 15 && refineries.size() < maxRefineries && teamSoup > 200 && soupCarrying > 20 && nearbySoupLocations.length > 2) {
            System.out.println("Build secondary Refinery");
            diagonalMovementCount = 0;
            buildRefinery();

        } else if (teamSoup > 155 && !fulfillmentCenterCreated && soupCarrying > 3) {
            /*
            Build a Fulfillment Center
             */
            System.out.println("In Fulfillment");
            buildFulfillmentCenter();
            diagonalMovementCount = 0;
        } else if (numDesignSchools < maxDesignSchools && teamSoup > 155 && soupCarrying > 5 && !nav.byRobot(RobotType.DESIGN_SCHOOL) && refineries.size() > 0) {
            System.out.println("In Design School");
            buildDesignSchool();
            diagonalMovementCount = 0;
        } else if (teamSoup > 250 && netgun < 2 && myLocation.x > 10 && myLocation.x < mapheight - 10 && myLocation.y > 10 && myLocation.y < mapwidth - 10 && (buildnetgun[0] == 0 || buildnetgun[1] == 0)) {
            System.out.println("Build net gun");
            buildNetGun();
            diagonalMovementCount = 0;
        } else if (nearbySoupLocations.length > 0) {
            /*
            Soup Nearby!
             */
            System.out.println("Soup I can sense: ");
            for (MapLocation loc : nearbySoupLocations) {
                System.out.println(" [" + loc.x + "," + loc.y + "]");
            }
            goToNearbySoup(nearbySoupLocations);
            diagonalMovementCount = 0;
        }
        if (nearbySoupLocations.length == 0) {
            System.out.println("no nearby soup");
            noNearbySoup();
        }
    }

    private void runMiner() throws GameActionException {
        System.out.println("I'm a miner!");

        System.out.println("soup nearby: " + nearbySoupLocations.length);
        if (soupCarrying == RobotType.MINER.soupLimit) {
           /*
            Robot is full of soup
            */
            goToClosestDeposit();
            diagonalMovementCount = 0;
        }
        else if (nearbySoupLocations.length > 0) {
            /*
            Soup Nearby!
             */
            System.out.println("Soup I can sense: ");
            for (MapLocation loc : nearbySoupLocations) {
                System.out.println(" [" + loc.x + "," + loc.y + "]");
            }
            goToNearbySoup(nearbySoupLocations);
            diagonalMovementCount = 0;
        }
        if (nearbySoupLocations.length == 0) {
            noNearbySoup();
        }
    }

    private void printSoupLocations () {
        System.out.println("Global Soup Locations: ");
        for (MapLocation loc : soupLocations) {
            System.out.println(" [" + loc.x + "," + loc.y + "]");
        }
    }

    private void checkSurroundingsForKnownSoup () throws GameActionException {
        for (Direction dir : Util.directions) {
            MapLocation loc = rc.getLocation().add(dir);
            if (soupLocations.contains(loc)) {
                int soupAmount = rc.senseSoup(loc);
                if (soupAmount == 0 && rc.getTeamSoup() > 2) {
                    System.out.println("broadcasting remove soup");
                    comms.broadcastRemoveSoup(loc, 2);
                }
            }
        }
    }

    private void getHQLocation () {
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

    private void printRefineries () {
        System.out.println("Refineries: ");
        for (MapLocation location : refineries) {
            System.out.println("  x: " + location.x + " y: " + location.y);
        }
    }

    private void dealWithBlockchainMessages () throws GameActionException {
        int[][] messages = comms.findTeamMessagesInBlockChain();

        //loop through messages from our team
        for (int[] mes : messages) {
            //messages is initialized with all -1
            //so if there is a -1 it is the end
            if (mes[0] == -1) {
                //end of messages
                break;
            } else {
                switch (mes[1]) {
                    case 0:
                        //message about the HQ location
                        if (HQLocation == null) {
                            HQLocation = new MapLocation(mes[2], mes[3]);
                            System.out.println("HQ Location: x:" + mes[2] + " y:" + mes[3]);
                        }
                        break;
                    case 13:
                        if(buildnetgun[0]==0)
                            buildnetgun[0]=1;
                        else if(buildnetgun[1]==0)
                            buildnetgun[1]=1;
                        break;
                    case 1:
                        //message about the refinery location
                        MapLocation newLocation = new MapLocation(mes[2], mes[3]);
                        if (!refineries.contains(newLocation)) {
                            System.out.println("New Refinery");
                            refineries.add(newLocation);
                        }
                        break;
                    case 2:
                        //message about soup location
                        MapLocation loc = new MapLocation(mes[2], mes[3]);
                        if (!soupLocations.contains(loc)) {
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
                        System.out.println("New Design School num: " + numDesignSchools);
                        break;
                    case 5:
                        numLandscapers++;
                        System.out.println("New Landscaper");
                        break;
                    case 8:
                        fulfillmentCenterCreated = true;
                        System.out.println("Fulfillment Created");
                        break;
                    case 9:
                        break;
                    case 10:
                        System.out.println("New Water Location");
                        MapLocation newWaterLocation = new MapLocation(mes[2], mes[3]);
                        if (!waterLocations.contains(newWaterLocation)) {
                            waterLocations.add(newWaterLocation);
                        }
                        break;
                }
            }
        }
    }

    private void goToNearbySoup (MapLocation[]nearbySoupLocations) throws GameActionException {
        diagonalMovementCount = 0;

        System.out.println("going to nearby soup");
        boolean bySoup = false;
        for (Direction dir : Util.directions) {
            if (rc.canMineSoup(dir))
                bySoup = true;
            System.out.println("trying to mine in " + dir);
            if (tryMine(dir)) {
                MapLocation soupLoc = myLocation.add(dir);
                if (!soupLocations.contains(soupLoc) && teamSoup > 4) {
                    comms.broadcastSoupLocation(soupLoc);
                    soupLocations.add(soupLoc);
                }
            }
        }

        /*
        Finally, if not by any soup then try and move towards
        the soup that is within the robots radius
         */
        System.out.println("by soup: " + bySoup);
        if (!bySoup) {
            //not by soup so move
            //System.out.println("i should move");
            for (MapLocation loc : nearbySoupLocations) {
                Direction d = rc.getLocation().directionTo(loc);
                if (nav.tryMove(d)) {
                    System.out.println("moved " + d);
                    break;
                } else if (nav.tryAltMoves(d)) {
                    System.out.println("moved " + d);
                } else {
                    if (nav.tryMove(nav.randomDirection()))
                        System.out.println("move randomly");
                }
            }
        }
    }

    private void buildFulfillmentCenter () throws GameActionException {

        Direction d = null;
        boolean within_hq = nav.inRadius(rc.getLocation(), HQLocation, 2) && !nav.inRadius(rc.getLocation(), HQLocation, 1) && numVaporators == 0;
        if (within_hq == true) {
                /*
                Build Fulfillment
                */
            d = nav.oppositeDirection(rc.getLocation().directionTo(HQLocation));
            if (tryBuild(RobotType.FULFILLMENT_CENTER, d)) {

                System.out.println("built fulfillment");
                fulfillmentCenterCreated = true;
                RobotInfo[] robots = rc.senseNearbyRobots();
                for (RobotInfo robot : robots) {
                    if (robot.getType() == RobotType.FULFILLMENT_CENTER) {
                        /*
                        Transmit the fulfillment center x and y
                         */
                        MapLocation location = robot.getLocation();
                        comms.broadcastFulfillmentCenterLocation(location);
                    }
                }

            }
        } else if (nav.inRadius(myLocation, HQLocation, 1)) {
                /*
                Move away from HQ
                 */
            d = nav.oppositeDirection(myLocation.directionTo(HQLocation));
            if (nav.tryMove(d)) {
            }
        } else {
            d = myLocation.directionTo(HQLocation);
            if (nav.tryMove(d)){

            }
            else if(nav.tryAltMoves(d)){

            }
            else{
                nav.tryMove(nav.randomDirection());
            }
        }
    }


    private void buildVaporator () throws GameActionException {
        if(tryBuild(RobotType.VAPORATOR, nav.randomDirection())){
            comms.broadcastCreation(myLocation,12);
        }
        else {
            Direction direction = nav.randomDirection();
            Direction opposite = nav.oppositeDirection(direction);

            if(nav.tryMove(direction)){

            }
            else if(nav.tryAltMoves(direction)){

            }
            else if(nav.tryMove(opposite)){

            }
            else if(nav.tryAltMoves(opposite)){

            }
            else{
                System.out.println("I'm stuck :((");
            }
        }
    }




    private void goToClosestDeposit() throws GameActionException {
        Direction d = myLocation.directionTo(closestRefineLocation);

        if(nav.inRadius(closestRefineLocation, myLocation, 1)){
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
                    nav.tryMove(nav.randomDirection());
                    System.out.println("could not move alt");
                }
            }
        }
    }

    private MapLocation[] getPossibleRefineLocations() {
        MapLocation[] locations;
        if(numLandscapers<1 && refineries.size()==0){
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
        return locations;
    }

    private void noNearbySoup() throws GameActionException{
        /*
        Here the miner does not detect any soup near it
         */
        System.out.println("in no nearby soup");
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
                System.out.println("blocked"+diagonalDirection+" getting new direction");
                diagonalDirection = nav.getNextDiagonal(diagonalDirection);
                System.out.println("New direction: "+diagonalDirection);
                if(nav.tryMove(diagonalDirection)){
                    System.out.println("moved new direction");
                }
            }
        }
    }

    private void goToKnownSoup() throws GameActionException {
        MapLocation soupLoc = soupLocations.get(0);
        Direction d = myLocation.directionTo(soupLoc);
        boolean aroundMe = nav.inRadius(myLocation, soupLoc, 1);
        if(aroundMe){
            if(tryMine(d)){
                System.out.println("mined "+d);
            }
            else{
                //location around robot but could not mine.
                int soupAmount = rc.senseSoup(soupLoc);
                if(soupAmount==0){
                    if(rc.getTeamSoup()>4){
                        comms.broadcastSoupLocation(soupLoc);
                        System.out.println("Submitted transaction to remove soup");
                    }
                }
            }
        }else{
            if(nav.tryMove(d)){
                System.out.println("moving to known soup at ["+soupLoc.x+","+ soupLoc.y+"]");
            }else if (nav.tryAltMoves(d)){

            }else {
                nav.tryMove(nav.randomDirection());
                System.out.println("could not move "+d);
            }
        }
    }

    public void buildDesignSchool() throws GameActionException {
        System.out.println("Current design schools: "+ numDesignSchools +" max: "+ maxDesignSchools);
        if(!nav.inRadius(myLocation, HQLocation, 3)){
            Direction d = myLocation.directionTo(HQLocation);
            if(nav.tryMove(d)){
                System.out.println("moved");
            }
            else if(nav.tryAltMoves(d)){
                System.out.println("moved alty");
            }
            else{
                nav.tryMove(nav.randomDirection());
                System.out.println("completely blocked");
            }
        }
        else{
            Direction d = nav.oppositeDirection(myLocation.directionTo(HQLocation));
            if(tryBuild(RobotType.DESIGN_SCHOOL, d)){
                System.out.println("built design school");
                RobotInfo[] robots = rc.senseNearbyRobots();
                for(RobotInfo robot : robots){
                    if(robot.getType()==RobotType.DESIGN_SCHOOL){
                        MapLocation location = robot.getLocation();
                        comms.broadcastDesignSchoolLocation(location);
                    }
                }
            }
        }
    }

    public void buildRefinery() throws GameActionException {
        Direction dir = nav.randomDirection();
        if(nav.inRadius(myLocation, HQLocation, 3)){
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
                        comms.broadcastRefineryLocation(location);
                    }
                }
            } else {
                System.out.println("can't build " + dir);
            }
        }
    }

    private void moveAwayFromHQ() throws GameActionException {
        Direction d = myLocation.directionTo(HQLocation);
        Direction opposite = nav.oppositeDirection(d);
        for(int i=0;i<5;i++) {
            if (nav.tryMove(opposite)) {
                System.out.println("Moved away from HQ");
            } else if (nav.tryAltMoves(opposite)) {
                System.out.println("Moved away from HQ alt");
            } else {
                nav.tryMove(nav.randomDirection());
                System.out.println("can't move at all");
            }
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

    void buildNetGun() throws GameActionException {
        nav.tryMove(nav.randomDirection());
        RobotInfo[] ri=rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED);
        boolean canbuild=true;
        for(RobotInfo r :ri){
            if(r.getType()==RobotType.HQ || r.getType()==RobotType.NET_GUN){
                canbuild=false;
            }
        }


        if(buildnetgun[0]==0 && canbuild){
            for(Direction dir:Util.directions){
                if(tryBuild(RobotType.NET_GUN,dir)){
                    buildnetgun[0]=1;
                    comms.broadcastnetgun();
                    break;
                }
            }
        }else if(buildnetgun[1]==0 && canbuild ){
            for(Direction dir:Util.directions){
                if(tryBuild(RobotType.NET_GUN,dir)){
                    buildnetgun[1]=1;
                    comms.broadcastnetgun();
                    break;
                }
            }
        }
    }


    public void senseWaterNearby() throws GameActionException {
        //Miners trying to sense water
        for (Direction dir : Util.directions) {
            MapLocation loc = myLocation.add(dir);
            if (rc.canSenseLocation(loc))
                if (rc.senseFlooding(loc) && !waterLocations.contains(loc)) {
                    if (rc.getTeamSoup() > 3) {
                        comms.broadcastWaterLocation(loc);
                        waterLocations.add(loc);
                    }
                }
        }
    }
}

