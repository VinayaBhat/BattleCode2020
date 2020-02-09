package colin;

import battlecode.common.*;
import org.omg.PortableInterceptor.DISCARDING;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeliveryDrone extends Unit {
    List<MapLocation> waterlc=new ArrayList<>();
    Team enemy;
    int mapwidth;
    int mapheight;
    MapLocation fulfilmentcenter;
    MapLocation enemyHQ;
    List<MapLocation> souploc=new ArrayList<>();

    public DeliveryDrone(RobotController r) {
        super(r);
         enemy = rc.getTeam().opponent();
         mapwidth=rc.getMapWidth();
         mapheight=rc.getMapHeight();
         RobotInfo[] ri=rc.senseNearbyRobots();
         for(RobotInfo info:ri){
             if(info.type==RobotType.FULFILLMENT_CENTER)
                 fulfilmentcenter=info.getLocation();
         }
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        RobotInfo[] ri = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);
        for (int i = 0; i < ri.length; i++) {
            if (ri[i].type == RobotType.HQ) {
                System.out.println("Enemy HQ at " + ri[i].getLocation());
                enemyHQ=ri[i].getLocation();
                Direction towardsHQ = rc.getLocation().directionTo(ri[i].getLocation());
                for (int j = 0; j < 10; j++) {
                    nav.tryMove(nav.oppositeLocation(towardsHQ));
                }
            }
        }
        if(!rc.isCurrentlyHoldingUnit()) {
            if (ri.length > 0) {
                if (ri[0].type != RobotType.HQ && rc.canPickUpUnit(ri[0].getID()))
                    rc.pickUpUnit(ri[0].getID());
            } else {
               if(!rc.canSenseLocation(enemyHQ)){
                   nav.tryMove(nav.randomDirection());
               }
            }
        }
        int[][] messages = comms.findTeamMessagesInBlockChain();
        for (int[] m : messages) {
            if (m[1] == 10) {
                System.out.println("WATER FOUND!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!1!!!!!!!!!!!!!!!!");
                if(!waterlc.contains(new MapLocation(m[2], m[3])))
                    waterlc.add(new MapLocation(m[2], m[3]));
            }else if(m[1]==2){
                if(!souploc.contains(new MapLocation(m[2],m[3])))
                    souploc.add(new MapLocation(m[2],m[3]));

            }
        }

        if (rc.senseFlooding(rc.getLocation())) {
            System.out.println("WATER SENSED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            if(!waterlc.contains(rc.getLocation()))
                waterlc.add(rc.getLocation());
        }

        if(rc.isCurrentlyHoldingUnit()){
            if(waterlc.size()>0){
                nav.tryMove(rc.getLocation().directionTo(waterlc.get(0)));
            }else if(fulfilmentcenter!=null){
                nav.tryMove(rc.getLocation().directionTo(fulfilmentcenter));
            }else if(souploc.size()>0){
                nav.tryMove(rc.getLocation().directionTo(souploc.get(0)));
            }
        }





    }
}

