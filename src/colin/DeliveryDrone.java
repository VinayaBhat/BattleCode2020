package colin;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

public class DeliveryDrone extends Unit {
    List<MapLocation> waterlc = new ArrayList<>();
    MapLocation enemyHQ;
    Team enemy;
    boolean alreadycarriedow = false;
    boolean cow = false;
    List<MapLocation> forbiddenloc = new ArrayList<>();
    static MapLocation nearHQ;
    boolean movetowardsenemyafterdropping = false;


    public DeliveryDrone(RobotController r) {
        super(r);
        enemy = rc.getTeam().opponent();
    }


    public void takeTurn() throws GameActionException {
        super.takeTurn();
        Direction loc = rc.getLocation().directionTo(new MapLocation(mapheight - hqLoc.x + Util.randomNumber(), mapwidth - hqLoc.y + Util.randomNumber()));
//Finding water location and enemy HQ
        int[][] messages = comms.findTeamMessagesInBlockChain();
        getwaterlocationandenemyHQ(messages);

        //Trying to sense water location
        for (Direction dir : Util.directions) {
            if (rc.canSenseLocation(rc.getLocation().add(dir)) && rc.senseFlooding(rc.getLocation().add(dir))) {
                if(!waterlc.contains(rc.getLocation().add(dir)))
                    waterlc.add(rc.getLocation().add(dir));
            }
        }

        boolean enemypresent = false;
        RobotInfo[] ri = rc.senseNearbyRobots();
//         If nearby robot is
//         1.cow then carry it.
//         2.enemy robot then carry it.
//         3.enemyHQ found then broadcast it
        for (RobotInfo info : ri) {
            if (info.type == RobotType.COW && !rc.isCurrentlyHoldingUnit() && !alreadycarriedow) {
                if (rc.canPickUpUnit(info.getID())) {
                    rc.pickUpUnit(info.getID());
                    alreadycarriedow = true;
                    cow = true;
                    break;
                }
            } else if (info.getTeam() == enemy && !rc.isCurrentlyHoldingUnit()) {
                enemypresent = true;
                if (rc.canPickUpUnit(info.getID())) {
                    rc.pickUpUnit(info.getID());
                    break;
                }
            } else if (info.getType() == RobotType.HQ && info.getTeam() == enemy && enemyHQ == null) {
                System.out.println("ENEMY HQ FOUND");
                enemyHQ = info.getLocation();
                System.out.println("ENEMY LOCATION FOUND " + enemyHQ);
                for (int i = 0; i < 10; i++) {
                    nav.tryMove(rc.getLocation().directionTo(hqLoc));
                }
                int[] message = {comms.teamId, 11, enemyHQ.x, enemyHQ.y, 0, 0, 0};
                if (rc.canSubmitTransaction(message, 5))
                    rc.submitTransaction(message, 5);
                forbiddenloc.add(new MapLocation(enemyHQ.x + 10, enemyHQ.y + 10));
                forbiddenloc.add(new MapLocation(enemyHQ.x - 10, enemyHQ.y + 10));
                forbiddenloc.add(new MapLocation(enemyHQ.x - 10, enemyHQ.y - 10));
                forbiddenloc.add(new MapLocation(enemyHQ.x + 10, enemyHQ.y - 10));
            } else if (info.getTeam() == enemy) {
                enemypresent = true;
            }
        }
//        If not carrying anything and enemyHQ is found. try to find the danger zone
        if (!rc.isCurrentlyHoldingUnit() && enemyHQ != null) {
            boolean forbidden = false;
            for (MapLocation mp : forbiddenloc) {
                if (rc.getLocation().isWithinDistanceSquared(mp, GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED + 10)) {
                    forbidden = true;
                }
            }
            if (!forbidden) {
                nav.tryMove(loc);
            } else {
                nav.tryMove(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
            }
        }
//        If carrying cow and near enemy location then drop it
        if (rc.isCurrentlyHoldingUnit() && cow) {
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
//       If carrying enemy
//       if water location found then drop the enemy in water
//       else move towards our HQ
        System.out.println("HOWWWWWWWWWWWWWWWWWW MAAAANYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY WAAAAAAAAAAAAAAAAATERRRRRRRRRRRRRR " + waterlc.size());
        if (rc.isCurrentlyHoldingUnit() && !cow) {
            if (waterlc.size() > 0) {
                int min = Integer.MAX_VALUE;
                MapLocation water = waterlc.get(0);
                for (MapLocation temp : waterlc) {
                    if (rc.getLocation().distanceSquaredTo(temp) < min) {
                        min = rc.getLocation().distanceSquaredTo(temp);
                        water = temp;
                    }
                }
                System.out.println("MOVING TOWARDS WATER TO DROP!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                nav.tryMove(rc.getLocation().directionTo(water));
                if (rc.canSenseLocation(water)) {
                    if (rc.canDropUnit(rc.getLocation().directionTo(water))) {
                        rc.dropUnit(rc.getLocation().directionTo(water));
                        movetowardsenemyafterdropping = true;
                    }
                }
            } else {
                if (nearHQ == null) {
                    nearHQ = new MapLocation(hqLoc.x + Util.randomNumber(), hqLoc.y + Util.randomNumber());
                }
                System.out.println("Moving towards HQ TO DROP !!!!!!!!!!!!!!!!!!!!!!!!!");

                if (!nav.tryMove(rc.getLocation().directionTo(nearHQ)))
                    nav.tryMove(nav.randomDirection());

                if (rc.canSenseLocation(nearHQ)) {
                    for (Direction dir : Util.directions) {
                        if (rc.canDropUnit(dir))
                            rc.dropUnit(dir);
                    }
                }

                for (Direction dir : Util.directions) {
                    if (rc.canSenseLocation(rc.getLocation().add(dir)) && rc.senseFlooding(rc.getLocation().add(dir))) {
                        waterlc.add(rc.getLocation().add(dir));
//                            if(rc.canDropUnit(dir)){
//                                rc.dropUnit(dir);
//                                movetowardsenemyafterdropping=true;
//                            }
                    }
                }
            }

        }

//            if(!rc.isCurrentlyHoldingUnit() && enemyHQ==null && movetowardsenemyafterdropping)
//                nav.tryMove(loc);
//            else if (!rc.isCurrentlyHoldingUnit() && enemyHQ==null && !movetowardsenemyafterdropping && !rc.canSenseLocation(hqLoc))
//                nav.tryMove(nav.randomDirection());


        //If not holding anything then move towards enemy location
        loc = rc.getLocation().directionTo(new MapLocation(mapheight - hqLoc.x + Util.randomNumber(), mapwidth - hqLoc.y + Util.randomNumber()));
        if (!rc.isCurrentlyHoldingUnit()) {
            nav.tryMove(loc);
        }else if(!rc.isCurrentlyHoldingUnit() && enemyHQ!=null && rc.getLocation().isWithinDistanceSquared(enemyHQ,GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED)){
            nav.tryMove(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
            nav.tryMove(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
            nav.tryMove(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
            nav.tryMove(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
        }

    }

    public void getwaterlocationandenemyHQ(int[][] messages){
        for (int[] m : messages) {
            if (m[1] == 11 && enemyHQ == null) {
                enemyHQ = new MapLocation(m[2], m[3]);
                forbiddenloc.add(new MapLocation(enemyHQ.x + 15, enemyHQ.y + 15));
                forbiddenloc.add(new MapLocation(enemyHQ.x - 15, enemyHQ.y + 15));
                forbiddenloc.add(new MapLocation(enemyHQ.x - 15, enemyHQ.y - 15));
                forbiddenloc.add(new MapLocation(enemyHQ.x + 15, enemyHQ.y - 15));
                System.out.println("ENEMY HQ FROM BLOCKCHAIN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }else if(m[1]==10){
                if(!waterlc.contains(new MapLocation(m[2],m[3])))
                    waterlc.add(new MapLocation(m[2],m[3]));
            }
        }
    }


}
