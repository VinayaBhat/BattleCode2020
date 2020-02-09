package colin;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

public class DeliveryDrone extends Unit {
    List<MapLocation> waterlc=new ArrayList<>();
    MapLocation enemyHQ;



    public DeliveryDrone(RobotController r) {
        super(r);

    }


    public void takeTurn() throws GameActionException {
        super.takeTurn();
        int[][] messages = comms.findTeamMessagesInBlockChain();
        for (int[] m : messages) {
            if (m[1] == 10) {
                System.out.println("WATER FOUND!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1!!!!!!!!!!!!!!!!");
                if(!waterlc.contains(new MapLocation(m[2], m[3])))
                    waterlc.add(new MapLocation(m[2], m[3]));
            }else if(m[1]==11){
                enemyHQ=new MapLocation(m[2],m[3]);
            }
        }
        if(enemyHQ!=null){
            System.out.println("Near Enemy");
            if(rc.getLocation().isWithinDistanceSquared(enemyHQ,GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED)) {
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
                nav.tryMove(rc.getLocation().directionTo(hqLoc));
            }
        }
        if(!rc.isCurrentlyHoldingUnit()) {
            System.out.println("Enemy location not found");
            RobotInfo[] ri = rc.senseNearbyRobots();
            System.out.println("Number of enemies "+ri.length);
            if (ri.length > 0) {
                if (enemyHQ == null) {
                    for (RobotInfo info : ri) {
                        if (info.type == RobotType.HQ && rc.getTeam().opponent()==info.getTeam()) {
                            enemyHQ = info.getLocation();
                            System.out.println("Enemy Location found");
                            nav.tryMove(rc.getLocation().directionTo(hqLoc));
                            nav.tryMove(rc.getLocation().directionTo(hqLoc));
                            nav.tryMove(rc.getLocation().directionTo(hqLoc));
                            int[] message={comms.teamId,11,enemyHQ.x,enemyHQ.y,0,0,0};
                            if(rc.canSubmitTransaction(message,5))
                                rc.submitTransaction(message,5);
                        }
                    }
                }
                if (ri[0].type != RobotType.HQ && rc.canPickUpUnit(ri[0].getID()) && rc.getTeam().opponent()==ri[0].getTeam() )
                    rc.pickUpUnit(ri[0].getID());
            }
            if(enemyHQ == null) {
                    boolean move=nav.tryMove(rc.getLocation().directionTo(new MapLocation(mapheight - hqLoc.x+Util.randomNumber(), mapwidth - hqLoc.y+Util.randomNumber())));
                   if(!move)
                       nav.tryMove(nav.randomDirection());

            }else{
                nav.tryMove(rc.getLocation().directionTo(
                        new MapLocation(enemyHQ.x+Util.randomNumber(),enemyHQ.y+Util.randomNumber())));
            }
        }


        if (rc.senseFlooding(rc.getLocation())) {
            System.out.println("WATER SENSED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if(!waterlc.contains(rc.getLocation())) {
                waterlc.add(rc.getLocation());
                int[] message={comms.teamId,10,rc.getLocation().x,rc.getLocation().y,0,0,0};
                if(rc.canSubmitTransaction(message,3))
                    rc.submitTransaction(message,3);
            }
        }

        if(rc.isCurrentlyHoldingUnit()){
            if(waterlc.size()>0){
                nav.tryMove(rc.getLocation().directionTo(waterlc.get(0)));
                if(rc.canSenseLocation(waterlc.get(0))){
                    if(rc.canDropUnit(rc.getLocation().directionTo(waterlc.get(0)))){
                        rc.dropUnit(rc.getLocation().directionTo(waterlc.get(0)));
                    }
                }
            }else if(enemyHQ==null){
                nav.tryMove(rc.getLocation().directionTo(new MapLocation(hqLoc.x+Util.randomNumber(),hqLoc.y+Util.randomNumber())));
            }else{
                nav.tryMove(nav.oppositeLocation(rc.getLocation().directionTo(enemyHQ)));
            }
        }
        MapLocation[] soup=rc.senseNearbySoup();
        for(int i=0;i<soup.length;i++){
            int[] message={comms.teamId,2,soup[i].x,soup[i].y,0,0,0};
            if(rc.canSubmitTransaction(message,3))
                rc.submitTransaction(message,3);
        }


        //nav.tryMove(rc.getLocation().directionTo(new MapLocation(mapheight-hqLoc.x,mapwidth-hqLoc.y)));





    }
}

