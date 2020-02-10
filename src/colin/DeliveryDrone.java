package colin;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

public class DeliveryDrone extends Unit {
    List<MapLocation> waterlc = new ArrayList<>();
    MapLocation enemyHQ;
    Team enemy;
    boolean alreadycarriedow=false;
    List<MapLocation> forbiddenloc=new ArrayList<>();


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
        boolean enemypresent=false;
        RobotInfo[] ri = rc.senseNearbyRobots();
        for(RobotInfo info:ri){
            if(info.type==RobotType.COW && !rc.isCurrentlyHoldingUnit() && !alreadycarriedow) {
                if (rc.canPickUpUnit(info.getID())) {
                    rc.pickUpUnit(info.getID());
                    alreadycarriedow=true;
                    break;
                }
            }
//          else if(info.getTeam()==enemy && !rc.isCurrentlyHoldingUnit()){
//                enemypresent=true;
//                if(rc.canPickUpUnit(info.getID())){
//                    rc.pickUpUnit(info.getID());
//                    break;
//                }}
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
               if(rc.getLocation().isWithinDistanceSquared(mp,5)){
                   forbidden=true;
               }
           }
           if(!forbidden) {
               nav.tryMove(loc);
           }else{
               nav.tryMove(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
           }
        }
        if(!rc.isCurrentlyHoldingUnit() && enemyHQ==null){
                nav.tryMove(loc);
        }

        if(rc.isCurrentlyHoldingUnit()) {
            if(enemypresent){
               for(Direction dir:Util.directions){
                   if(rc.canDropUnit(dir)){
                       rc.dropUnit(dir);
                   }
               }
            }else{
                nav.tryMove(loc);
            }
        }


    }
}
