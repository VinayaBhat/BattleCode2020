package team10pdx;
import battlecode.common.*;

import java.util.ArrayList;
import java.util.Map;

public class Miner extends Unit {

    MapLocation HQLocation;
    int maxRefineries = 3;
    ArrayList<MapLocation> refineries = new ArrayList<>();
    ArrayList<MapLocation> soupLocations = new ArrayList<>();
    ArrayList<MapLocation> waterLocations = new ArrayList<>();
    boolean fulfillmentCenterCreated = false;
    boolean firstVaporatorCreated = false;
    boolean build_first_vaporator = false;
    boolean build_second_vaporator = false;
    boolean build_third_vaporator = false;
    int [] vaporator_checker = {0, 0, 0};
    int numDesignSchools = 0;
    int maxDesignSchools = 1;
    int numLandscapers = 0;
    int diagonalMovementCount = 0;
    MapLocation closestRefineLocation;
    Direction diagonalDirection;
    int numVaporators = 0;
    int netgun=0;



    public Miner(RobotController r){
        super(r);
    }

    public void takeTurn() throws GameActionException {
        System.out.println("ID: " + rc.getID() + " soup: " + rc.getSoupCarrying());
        System.out.println("Diag: " + diagonalMovementCount);

        //get HQ Location when first made.
        if (HQLocation == null) {
            getHQLocation();
        }

        //Miners trying to sense water
        for (Direction dir : Util.directions) {
            MapLocation loc = rc.getLocation().add(dir);
            if (rc.canSenseLocation(loc))
                if (rc.senseFlooding(loc) && !waterLocations.contains(loc)) {
                    if (rc.getTeamSoup() > 3) {
                        comms.broadcastWaterLocation(loc);
                        waterLocations.add(loc);
                    }
                }
        }


        printSoupLocations();
        printRefineries();

        //always get blockchain messages first
        dealWithBlockchainMessages();

        closestRefineLocation = nav.findNearestLocation(rc.getLocation(), getPossibleRefineLocations());
        checkSurroundingsForKnownSoup();

        MapLocation[] nearbySoupLocations = rc.senseNearbySoup();
        System.out.println("soup nearby: " + nearbySoupLocations.length);

        build_first_vaporator = rc.getTeamSoup() > 550 && rc.getSoupCarrying() > 3 && fulfillmentCenterCreated == true && firstVaporatorCreated == false;
        int vape_count = 0;
        for (int i = 0; i < 3; i++) {
            if (vaporator_checker[i] == -1) {
                vape_count += 1;
            }
        }
        build_second_vaporator = rc.getTeamSoup() > 550 && rc.getSoupCarrying() > 3 && refineries.size() == 1 && vaporator_checker[0] == -1 && vape_count == 1;
        build_third_vaporator = rc.getTeamSoup() > 550 && rc.getSoupCarrying() > 3 && refineries.size() == maxRefineries && vaporator_checker[0] == -1 && vape_count == 2;
        System.out.println("build_first_vaporator is " + build_first_vaporator + "and build_second_vapes = " + build_second_vaporator + " and current soup is " + rc.getSoupCarrying() + "and team is " + rc.getTeamSoup());
        /*
        if the miner has reached its souplimit
        then go to deposit
        */
        if (rc.getSoupCarrying() == RobotType.MINER.soupLimit) {
           /*
            Robot is full of soup
            */
            goToClosestDeposit();
            diagonalMovementCount = 0;
        }
        else if (nav.distanceTo(rc.getLocation(), closestRefineLocation) > 15 && refineries.size() < maxRefineries && refineries.size()!=0 && rc.getTeamSoup() > 400 && rc.getSoupCarrying() > 20 && nearbySoupLocations.length > 2 && firstVaporatorCreated == true) {

            System.out.println("Build secondary Refinery");
            diagonalMovementCount = 0;
            buildRefinery();
        }
        else  if(rc.getTeamSoup()>250 && netgun<2 && rc.getLocation().x >10 && rc.getLocation().x<mapheight-10 && rc.getLocation().y>10 && rc.getLocation().y<mapwidth-10 ) {
            buildNetGun();
        }
        else if (rc.getTeamSoup() > 155 && !fulfillmentCenterCreated && rc.getSoupCarrying() > 3) {
            /*
            Build a Fulfillment Center
             */
            System.out.println("In Fulfillment");
            buildFulfillmentCenter();

            diagonalMovementCount = 0;
        }
        else if (build_first_vaporator == true) {
            /// build first vaporator near HQ
            System.out.println("attempting to build vaporator");
            buildVaporator();

            diagonalMovementCount = 0;
        } else if (numDesignSchools < maxDesignSchools && rc.getTeamSoup() > 155 && rc.getSoupCarrying() > 5 && !nav.byRobot(RobotType.DESIGN_SCHOOL) && refineries.size() > 0) {
            System.out.println("In Design School");
            buildDesignSchool();
            diagonalMovementCount = 0;

        } else if (!nav.byRobot(RobotType.REFINERY) && refineries.size() < 1 && rc.getTeamSoup() > 220 && rc.getSoupCarrying() > 20 && nearbySoupLocations.length > 2 && firstVaporatorCreated == true) {
            System.out.println("In Refinery");
            diagonalMovementCount = 0;
            buildRefinery();
        }
//        else if (build_second_vaporator == true || build_third_vaporator == true) {
//            /// build first vaporator near HQ
//            System.out.println("attempting to build vaporator");
//            buildVaporator();
//            diagonalMovementCount = 0;
//       }
        //there is soup nearby
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
                            netgun++;
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

                        case 12:
                            firstVaporatorCreated = true;
                            numVaporators++;
                            if (mes[4] >= 0) {
                                int cross_off = mes[4];
                                vaporator_checker[cross_off] = -1;
                            }
                            System.out.println("Vaporator created");
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
                    MapLocation soupLoc = rc.getLocation().add(dir);
                    if (!soupLocations.contains(soupLoc) && rc.getTeamSoup() > 4) {
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
            } else if (nav.inRadius(rc.getLocation(), HQLocation, 1)) {
                /*
                Move away from HQ
                 */
                d = nav.oppositeDirection(rc.getLocation().directionTo(HQLocation));
                if (nav.tryMove(d)) {
                }
            } else {
                d = nav.randomDirection();
                if (nav.tryMove(d)) {
                }
            }
        }


        private void buildVaporator () throws GameActionException {
            Direction d = null;
            boolean within_hq = nav.inRadius(rc.getLocation(), HQLocation, 2) && !nav.inRadius(rc.getLocation(), HQLocation, 1) && firstVaporatorCreated == false;
            boolean within_first_refinery = false;
            boolean within_second_refinery = false;
            MapLocation first_refinery = null;
            MapLocation second_refinery = null;
            int vapecount = 0;
            for (int i = 0; i < 3; i++){
                if (vaporator_checker[i] == - 1){
                    vapecount += 1;
                }
            }
            int checker = -1;

            if (refineries.size() == 1) {
                first_refinery = refineries.get(0);
                within_first_refinery = numVaporators > 0 && nav.inRadius(rc.getLocation(), refineries.get(0), 2) && !nav.inRadius(rc.getLocation(), refineries.get(0), 1) && vapecount == 1;

            } else if (refineries.size() == maxRefineries) {
                second_refinery = refineries.get(1);
                within_second_refinery = numVaporators > 0 && nav.inRadius(rc.getLocation(), refineries.get(1), 2) && !nav.inRadius(rc.getLocation(), refineries.get(1), 1) && vapecount == 2;
            }
                if (within_hq == true && fulfillmentCenterCreated == true) {
                d = nav.oppositeDirection(rc.getLocation().directionTo(HQLocation));
                checker = 0;
            } else if (refineries.size() == 1 && within_first_refinery == true  && firstVaporatorCreated == true) {
                d = nav.oppositeDirection(rc.getLocation().directionTo(first_refinery));
                checker = 1;
            } else if (refineries.size() == maxRefineries && within_second_refinery == true  && firstVaporatorCreated == true) {
                d = nav.oppositeDirection(rc.getLocation().directionTo(second_refinery));
                checker = 2;
            }

            if (d != null && tryBuild(RobotType.VAPORATOR, d)) {
                System.out.println("built vaporator");
                if (numVaporators == 0) {
                   firstVaporatorCreated = true;
                   build_first_vaporator=false;
                }
                RobotInfo[] robots = rc.senseNearbyRobots();
                for (RobotInfo robot : robots) {
                    if (robot.getType() == RobotType.VAPORATOR) {
                            /*
                            Transmit the vaporator x and y
                             */
                        MapLocation location = robot.getLocation();
                        int[] message = {comms.teamId, 12, location.x, location.y, checker, robot.getID(), 0};
                        if (robot.getID() != 0) {
                            System.out.println("vapeys id is " + robot.getID());

                            rc.submitTransaction(message, 2);
                        }
                    }
                }
            }
            else if (nav.inRadius(rc.getLocation(), HQLocation, 1) && firstVaporatorCreated == false) {
                /*
                Move away from HQ
                 */
                d = nav.oppositeDirection(rc.getLocation().directionTo(HQLocation));
                if (nav.tryMove(d)) {
                }
            }
            else if (first_refinery != null && nav.inRadius(rc.getLocation(), first_refinery, 1) && vaporator_checker[1] != -1) {
                /*
                Move away from HQ
                 */
                d = nav.oppositeDirection(rc.getLocation().directionTo(first_refinery));
                if (nav.tryMove(d)) {
                }
            } else if (second_refinery != null && nav.inRadius(rc.getLocation(), second_refinery, 1) && vaporator_checker[2] != -1) {
                /*
                Move away from HQ
                 */
                d = nav.oppositeDirection(rc.getLocation().directionTo(second_refinery));
                if (nav.tryMove(d)) {
                }
            }
            else {
                d = nav.randomDirection();
                if (nav.tryMove(d)) {
                }
            }

            }




    private void goToClosestDeposit() throws GameActionException {
        Direction d = rc.getLocation().directionTo(closestRefineLocation);

        if(nav.inRadius(closestRefineLocation, rc.getLocation(), 1)){
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
        if(!nav.inRadius(rc.getLocation(), HQLocation, 3)){
            Direction d = rc.getLocation().directionTo(HQLocation);
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
            Direction d = nav.oppositeDirection(rc.getLocation().directionTo(HQLocation));
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
                        comms.broadcastRefineryLocation(location);
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
        boolean canbuild=true;
        RobotInfo[] ri=rc.senseNearbyRobots();
        for(RobotInfo r:ri){
            if(rc.getType()==RobotType.HQ || rc.getType()==RobotType.NET_GUN){
                canbuild=false;
                break;
            }
        }

        if(canbuild) {
            for(Direction dir:Util.directions){
            if (tryBuild(RobotType.NET_GUN, dir)) {
                System.out.println("Netgun built");
                comms.broadcastnetgun();
                netgun ++;
                break;
            }
        }
        }
    }
}

