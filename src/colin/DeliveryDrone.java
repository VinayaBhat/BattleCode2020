package colin;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeliveryDrone extends Unit {
    List<MapLocation> waterlc=new ArrayList<>();
   int turns=10;


    public DeliveryDrone(RobotController r) {
        super(r);
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

            Team enemy = rc.getTeam().opponent();
            if (rc.senseFlooding(rc.getLocation())) {
                waterlc.add(rc.getLocation());
            }
            if (rc.isCurrentlyHoldingUnit()) {
                if (waterlc.size() > 0) {
                    nav.tryMove(rc.getLocation().directionTo(waterlc.get(0)));
                } else {
                    RobotInfo[] ri = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, rc.getTeam());
                    if (ri.length == 0)
                        rc.dropUnit(nav.randomDirection());
                    else
                        nav.tryMove(nav.randomDirection());
                }
                if (rc.canSenseLocation(waterlc.get(0))) {
                    rc.dropUnit(rc.getLocation().directionTo(waterlc.get(0)));
                }
            }
            RobotInfo[] ri = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);
            if (ri.length > 0) {
                if (rc.canPickUpUnit(ri[0].getID()))
                    rc.pickUpUnit(ri[0].getID());
            } else {
                if (turns < 0) {
                    boolean move = nav.tryMove(rc.getLocation().directionTo(new MapLocation(30, 30)));
                    if (move) {
                        turns++;

                    }
                } else {
                    nav.tryMove(nav.randomDirection());
                }
            }




    }





}