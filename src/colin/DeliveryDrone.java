package colin;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

public class DeliveryDrone extends Unit {
    List<MapLocation> waterlc = new ArrayList<>();
    MapLocation enemyHQ;
    boolean cow=false;


    public DeliveryDrone(RobotController r) {
        super(r);

    }


    public void takeTurn() throws GameActionException {
        super.takeTurn();
        System.out.println("ENEMY HQ !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "+enemyHQ);
        int[][] messages = comms.findTeamMessagesInBlockChain();
        for (int[] m : messages) {
            if (m[1] == 10) {
                System.out.println("WATER FOUND!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1!!!!!!!!!!!!!!!!");
                if (!waterlc.contains(new MapLocation(m[2], m[3])))
                    waterlc.add(new MapLocation(m[2], m[3]));
            } else if (m[1] == 11) {
                enemyHQ = new MapLocation(m[2], m[3]);
            }
        }
        if (enemyHQ != null) {
            System.out.println("Near Enemy");
            if (rc.getLocation().isWithinDistanceSquared(enemyHQ, GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED)) {
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
            }
        }

        RobotInfo[] ri = rc.senseNearbyRobots();
        if (!rc.isCurrentlyHoldingUnit()) {
            for (RobotInfo info : ri) {
                if (info.type == RobotType.COW) {
                    if (rc.canPickUpUnit(info.getID())) {
                        rc.pickUpUnit(info.getID());
                        cow=true;
                        break;
                    }
                } else if (info.getTeam() == rc.getTeam().opponent() && info.type == RobotType.HQ) {
                    if (enemyHQ == null) {
                        enemyHQ = info.getLocation();
                        System.out.println("ENEMY FOUND");
                        nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                        nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                        nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                        nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                        nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                        int[] message = {comms.teamId, 11, enemyHQ.x, enemyHQ.y, 0, 0, 0};
                        if (rc.canSubmitTransaction(message, 5))
                            rc.submitTransaction(message, 5);
                        break;
                    } else {
                        nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                        nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                        nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                        nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                        nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                        break;
                    }
                } else if (info.getTeam() == rc.getTeam().opponent() && rc.canPickUpUnit(ri[0].getID())) {
                    rc.pickUpUnit(ri[0].getID());
                    break;
                }
            }
        } else {
            boolean enemypresent=false;
            boolean nearenemyHq=false;
            for(RobotInfo info:ri){
                if(info.getTeam()!=rc.getTeam().opponent() && info.type==RobotType.HQ && enemyHQ==null){
                    enemyHQ=info.getLocation();
                    nearenemyHq=true;
                    enemypresent=true;
                    int[] message = {comms.teamId, 11, enemyHQ.x, enemyHQ.y, 0, 0, 0};
                    if (rc.canSubmitTransaction(message, 5))
                        rc.submitTransaction(message, 5);
                }else if(info.getTeam()!=rc.getTeam().opponent()){
                    enemypresent=true;
                }
            }

            if(nearenemyHq){
                nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
                nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
            }else if(cow && enemypresent){
               if(rc.canDropUnit(Direction.CENTER))
                   rc.dropUnit(Direction.CENTER);
            }else if(enemypresent){
                if(rc.canDropUnit(Direction.CENTER)){
                    rc.dropUnit(Direction.CENTER);
                }
            }else if(!enemypresent || cow){
                System.out.println("MOVING TOWRDS ENEMY !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                if(enemyHQ==null){
                    MapLocation loc=new MapLocation(mapheight-hqLoc.x+Util.randomNumber(),mapwidth-hqLoc.y+Util.randomNumber());
                    nav.tryMove(rc.getLocation().directionTo(loc));
                }else{
                    nav.tryMove(rc.getLocation().directionTo(enemyHQ));
                }
            }
        }

        if (!rc.isCurrentlyHoldingUnit()) {
            MapLocation loc=new MapLocation(mapheight-hqLoc.x+Util.randomNumber(),mapwidth-hqLoc.y+Util.randomNumber());
            if(!nav.tryMove(rc.getLocation().directionTo(loc))){
                nav.tryMove(nav.randomDirection());
            }
        }
    }
}
