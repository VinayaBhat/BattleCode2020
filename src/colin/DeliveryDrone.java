package colin;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

public class DeliveryDrone extends Unit {
    List<MapLocation> waterlc = new ArrayList<>();
    MapLocation enemyHQ;
    Team enemy;
    boolean alreadycarriedow=false;
    boolean cow=false;
    List<MapLocation> forbiddenloc=new ArrayList<>();
    static MapLocation nearHQ;



    public DeliveryDrone(RobotController r) {
        super(r);
        enemy=rc.getTeam().opponent();
    }


    public void takeTurn() throws GameActionException {
        super.takeTurn();
        Direction loc=rc.getLocation().directionTo(new MapLocation(mapheight-hqLoc.x+Util.randomNumber(),mapwidth-hqLoc.y+Util.randomNumber()));

        int[][] messages = comms.findTeamMessagesInBlockChain();
        for (int[] m : messages) {
            if (m[1] == 10) {
                System.out.println("WATER FOUND!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1!!!!!!!!!!!!!!!!");
                if(!waterlc.contains(new MapLocation(m[2], m[3])))
                    waterlc.add(new MapLocation(m[2], m[3]));
            }else if(m[1]==11 && enemyHQ==null){
                enemyHQ=new MapLocation(m[2],m[3]);
                forbiddenloc.add(new MapLocation(enemyHQ.x+15,enemyHQ.y+15));
                forbiddenloc.add(new MapLocation(enemyHQ.x-15,enemyHQ.y+15));
                forbiddenloc.add(new MapLocation(enemyHQ.x-15,enemyHQ.y-15));
                forbiddenloc.add(new MapLocation(enemyHQ.x+15,enemyHQ.y-15));
                System.out.println("ENEMY HQ FROM BLOCKCHAIN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
        for(Direction dir:Util.directions){
            if(rc.senseFlooding(rc.getLocation())){
                waterlc.add(rc.getLocation());
            }
        }
        boolean enemypresent=false;
        RobotInfo[] ri = rc.senseNearbyRobots();
        for(RobotInfo info:ri){
            if(info.type==RobotType.COW && !rc.isCurrentlyHoldingUnit() && !alreadycarriedow) {
                if (rc.canPickUpUnit(info.getID())) {
                    rc.pickUpUnit(info.getID());
                    alreadycarriedow=true;
                    cow=true;
                    break;
                }
            }
          else if(info.getTeam()==enemy && !rc.isCurrentlyHoldingUnit()){
                enemypresent=true;
                if(rc.canPickUpUnit(info.getID())){
                    rc.pickUpUnit(info.getID());
                    break;
                }}
            else if(info.getType()==RobotType.HQ && info.getTeam()==enemy && enemyHQ==null){
                System.out.println("ENEMY HQ FOUND");
                enemyHQ=info.getLocation();
                System.out.println("ENEMY LOCATION FOUND "+enemyHQ);
                for(int i=0;i<10;i++){
                    nav.tryMove(rc.getLocation().directionTo(hqLoc));
                }
                int[] message={comms.teamId,11,enemyHQ.x,enemyHQ.y,0,0,0};
                if(rc.canSubmitTransaction(message,5))
                    rc.submitTransaction(message,5);
                forbiddenloc.add(new MapLocation(enemyHQ.x+15,enemyHQ.y+15));
                forbiddenloc.add(new MapLocation(enemyHQ.x-15,enemyHQ.y+15));
                forbiddenloc.add(new MapLocation(enemyHQ.x-15,enemyHQ.y-15));
                forbiddenloc.add(new MapLocation(enemyHQ.x+15,enemyHQ.y-15));
            }
            else if(info.getTeam()==enemy){
                enemypresent=true;
            }
        }

        if(!rc.isCurrentlyHoldingUnit() && enemyHQ!=null){
            boolean forbidden=false;
           for(MapLocation mp:forbiddenloc){
               if(rc.getLocation().isWithinDistanceSquared(mp,GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED+10)){
                   forbidden=true;
               }
           }
           if(!forbidden) {
               nav.tryMove(loc);
           }else{
               nav.tryMove(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
           }
        }

        if(rc.isCurrentlyHoldingUnit() && cow) {
            if (enemypresent) {
                for (Direction dir : Util.directions) {
                    if (rc.canDropUnit(dir)) {
                        rc.dropUnit(dir);
                        cow = false;
                    }
                }
            } else {
                nav.tryMove(loc);
            }
        }
            if(rc.isCurrentlyHoldingUnit() && !cow) {
                if(waterlc.size()>0){
                    int min=Integer.MAX_VALUE;
                    MapLocation water=waterlc.get(0);
                    for(MapLocation temp:waterlc){
                        if(rc.getLocation().distanceSquaredTo(temp)<min){
                            min=rc.getLocation().distanceSquaredTo(temp);
                            water=temp;
                        }
                    }
                    System.out.println("MOVING TOWARDS WATER TO DROP");
                    nav.tryMove(rc.getLocation().directionTo(water));
                    if(rc.canSenseLocation(water)){
                        for(Direction dir:Util.directions) {
                            if (rc.canDropUnit(dir)) {
                                rc.dropUnit(dir);
                            }
                        }
                    }
                }else{
                    if(nearHQ==null){
                        nearHQ=new MapLocation(hqLoc.x+Util.randomNumber(),hqLoc.y+Util.randomNumber());
                    }
                    System.out.println("Moving towards HQ TO DROP !!!!!!!!!!!!!!!!!!!!!!!!!");
                    for(int i=0;i<15;i++) {
                        nav.tryMove(rc.getLocation().directionTo(nearHQ));
                    }
                        for(Direction dir:Util.directions) {
                            if (rc.canDropUnit(dir)) {
                                rc.dropUnit(dir);
                            }
                        }

                }
            }

            if(!rc.isCurrentlyHoldingUnit()){
                nav.tryMove(loc);
            }

        }



}
