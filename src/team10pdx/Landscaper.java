package team10pdx;
import battlecode.common.*;

public class Landscaper extends Unit {

    int totalNumLandscapers = 0;
    boolean transmittedLocation = false;
    int role = 0;

    public Landscaper(RobotController r) {
        super(r);
    }
    /*
            The landscaper is broken into main code and phase code.
            Main code:
                -Run by all landscapers
                -Run before phase code
            Phase code;
                -Situation dependent code
                -All changes should be done in main
    */
    public void takeTurn() throws GameActionException {
        super.takeTurn();
        //first run the main code
        main();

        switch (role){
            case 0:
                mainLandscapers();
                break;
            case 1:
                secondaryLandscapers();
                break;
        }
    }

    void main() throws GameActionException{
        System.out.println("ID: "+rc.getID());
        MapLocation myLocation = rc.getLocation();
        if(!transmittedLocation && rc.getTeamSoup()>5){
            System.out.println("submitted location");
            int[] message = {comms.teamId, 6, myLocation.x, myLocation.y, rc.getID(), 0, 0};
            rc.submitTransaction(message, 3);
            transmittedLocation = true;
        }

        //get the block and the team messages on it
        int[][] teamMessages = comms.findTeamMessagesInBlockChain();

        //deal with team messages
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
                        hqLoc = new MapLocation(message[2], message[3]);
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
                        totalNumLandscapers = message[2];
                        System.out.println("New Landscaper");
                    case 6:
                        System.out.println("Landscaper Message");
                        break;
                    case 7:
                        System.out.println("Landscaper Role Message");
                        System.out.println("id: "+message[2]+" role: "+message[3]);
                        if(rc.getID()==message[2]){
                            role = message[3];
                        }
                        break;
                }
            }
        }
    }
    /*
    This phase is when the main landscapers are being made
     */
    void mainLandscapers() throws GameActionException{
        // first, save HQ by trying to remove dirt from it
        if (hqLoc != null && hqLoc.isAdjacentTo(rc.getLocation())) {
            Direction dirtohq = rc.getLocation().directionTo(hqLoc);
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
        if(hqLoc != null) {
            int lowestElevation = 9999999;
            for (Direction dir : Util.directions) {
                MapLocation tileToCheck = hqLoc.add(dir);
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
        if(hqLoc != null){
            if(nav.goTo(hqLoc)){
                System.out.println("moving to HQ");
            }else{
                nav.tryMove(nav.randomDirection());
            }
        } else {
            if(nav.goTo(nav.randomDirection())){
                System.out.println("random move");
            }
        }
    }

    void secondaryLandscapers() throws GameActionException {
//        if(role==0){
//            mainLandscapers();
//        }
//        else if(role==1){
//            //do stuff
//            System.out.println("I'm a secondary");
//        }
        // first, save HQ by trying to remove dirt from it
        if (hqLoc != null && hqLoc.isAdjacentTo(rc.getLocation())) {
            Direction dirtohq = rc.getLocation().directionTo(hqLoc);
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
        if(hqLoc != null) {
            int lowestElevation = 9999999;
            for (Direction dir : Util.directions) {
                MapLocation tileToCheck = hqLoc.add(dir);
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
        if(hqLoc != null){
            if(nav.goTo(hqLoc)){
                System.out.println("moving to HQ");
            }else{
                nav.tryMove(nav.randomDirection());
            }
        } else {
            if(nav.goTo(nav.randomDirection())){
                System.out.println("random move");
            }
        }
    }

    boolean tryDig() throws GameActionException {
        Direction dir;
        if(hqLoc == null){
            dir = Util.randomDirection();
        } else {
            dir = hqLoc.directionTo(rc.getLocation());
        }
        if(rc.canDigDirt(dir)){
            rc.digDirt(dir);
            return true;
        }
        return false;
    }



}