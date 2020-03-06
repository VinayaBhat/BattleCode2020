package team10pdx;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DeliveryDrone extends Unit {
    LimitedQueue<MapLocation> waterlc = new LimitedQueue<>(10);
    MapLocation enemyHQ;
    Team enemy;
    boolean alreadycarriedow = false;
    boolean cow = false;
    List<MapLocation> forbiddenloc = new ArrayList<>();
    static MapLocation nearHQ;
    boolean movetowardsenemyafterdropping = false;
    public class LimitedQueue<E> extends LinkedList<E> {

        private int limit;

        public LimitedQueue(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean add(E o) {
            boolean added = super.add(o);
            while (added && size() > limit) {
                super.remove();
            }
            return added;
        }
    }



    public DeliveryDrone(RobotController r) {
        super(r);
        enemy = rc.getTeam().opponent();
    }


    public void takeTurn() throws GameActionException {
        super.takeTurn();
        Direction loc = rc.getLocation().directionTo(new MapLocation(mapheight - hqLoc.x + Util.randomNumber(), mapwidth - hqLoc.y + Util.randomNumber()));
//Finding water location and enemy HQ
        int[][] messages = comms.findTeamMessagesInBlockChain();
        findwaterlocations(messages);
        //Trying to sense water location
        sensewaterlocation();

        boolean enemypresent = false;
        RobotInfo[] ri = rc.senseNearbyRobots();
//         If nearby robot is
//         1.cow then carry it.
//         2.enemy robot then carry it.
//         3.enemyHQ found then broadcast it
        enemypresent=canPickUpRobotandEnemyPresent(ri);

        ifnotholdingunitandenemyHQnotfound(loc);
//        If not carrying anything and enemyHQ is found. try to find the danger zone

//        If carrying cow and near enemy location then drop it
        isHoldingCow(enemypresent,loc);
//       If carrying enemy
//       if water location found then drop the enemy in water
//       else move towards our HQ
        isHoldingEnemyUnit();



        //If not holding anything then move towards enemy location
        notHoldingUnit(enemypresent,loc);


    }

    public int[][] findwaterlocations(int[][] messages) throws GameActionException {
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
        return messages;
    }

    public List<MapLocation> sensewaterlocation() throws GameActionException {
        for (Direction dir : Util.directions) {
            if (rc.canSenseLocation(rc.getLocation().add(dir)) && rc.senseFlooding(rc.getLocation().add(dir))) {
                if (!waterlc.contains(rc.getLocation().add(dir)))
                    waterlc.add(rc.getLocation().add(dir));
            }
        }
        for(MapLocation water:waterlc){
            System.out.println("WATER  FOUND AT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! "+water);
        }
        return waterlc;
    }

    public boolean canPickUpRobotandEnemyPresent(RobotInfo[] ri) throws GameActionException {
        boolean enemypresent=false;
        for (RobotInfo info : ri) {
            if (info.getType() == RobotType.HQ && info.getTeam() == enemy && enemyHQ == null) {
                System.out.println("ENEMY HQ FOUND");
                enemyHQ = info.getLocation();
                System.out.println("ENEMY LOCATION FOUND " + enemyHQ);
                if(hqLoc!=null) {
                    for (int i = 0; i < 10; i++) {
                        nav.tryMoveDrone(rc.getLocation().directionTo(hqLoc));
                    }
                }
                int[] message = {comms.teamId, 11, enemyHQ.x, enemyHQ.y, 0, 0, 0};
                if (rc.canSubmitTransaction(message, 5))
                    rc.submitTransaction(message, 5);
                forbiddenloc.add(new MapLocation(enemyHQ.x + 15, enemyHQ.y + 15));
                forbiddenloc.add(new MapLocation(enemyHQ.x - 15, enemyHQ.y + 15));
                forbiddenloc.add(new MapLocation(enemyHQ.x - 15, enemyHQ.y - 15));
                forbiddenloc.add(new MapLocation(enemyHQ.x + 15, enemyHQ.y - 15));
            }
            if (info.type == RobotType.COW && !rc.isCurrentlyHoldingUnit() && !alreadycarriedow) {
                if (rc.canPickUpUnit(info.getID())) {
                    rc.pickUpUnit(info.getID());
                    alreadycarriedow = true;
                    cow = true;
                }
            }
            else if (info.getTeam() == enemy && !rc.isCurrentlyHoldingUnit()) {
                enemypresent = true;
                if (rc.canPickUpUnit(info.getID())) {
                    rc.pickUpUnit(info.getID());
                }
            }  else if (info.getTeam() == enemy) {
                enemypresent = true;
            }
        }
        return enemypresent;
    }

    public boolean ifnotholdingunitandenemyHQnotfound(Direction loc) throws GameActionException {
        boolean forbidden = false;
        if (!rc.isCurrentlyHoldingUnit() && enemyHQ != null) {
            for (MapLocation mp : forbiddenloc) {
                if (rc.getLocation().isWithinDistanceSquared(mp, GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED )) {
                    forbidden = true;
                }
            }
            if (!forbidden) {
                nav.tryMoveDrone(loc);
            } else {
                nav.tryMoveDrone(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
            }
        }
        return forbidden;
    }

    public boolean isHoldingCow(boolean enemypresent,Direction loc) throws GameActionException {
        if (rc.isCurrentlyHoldingUnit() && cow) {
            if (enemypresent) {
                for (Direction dir : Util.directions) {
                    if (rc.canDropUnit(dir)) {
                        rc.dropUnit(dir);
                        cow = false;
                    }
                }
            } else {
                nav.tryMoveDrone(loc);
            }
        }
        return cow;
    }

    public boolean isHoldingEnemyUnit() throws GameActionException {
        if (rc.isCurrentlyHoldingUnit() && !cow) {
            for(Direction dir:Util.directions) {
                if (rc.senseFlooding(rc.getLocation().add(dir)) && rc.onTheMap(rc.getLocation().add(dir))) {
                    if (rc.canDropUnit(dir)) {
                        rc.dropUnit(dir);
                    }
                }
            }

            if (waterlc.size() > 0 && rc.isCurrentlyHoldingUnit()) {
                MapLocation water = waterlc.get(0);
                int min = Integer.MAX_VALUE;
                for (MapLocation temp : waterlc) {
                    if (rc.getLocation().distanceSquaredTo(temp) < min) {
                        min = rc.getLocation().distanceSquaredTo(temp);
                        water = temp;
                    }
                }
                nav.tryMoveDrone(rc.getLocation().directionTo(water));
                if (rc.canSenseLocation(water)) {
                    System.out.println("TRYING TO DROP NEAR   !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!        " + rc.getLocation());
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

                if (!nav.tryMoveDrone(rc.getLocation().directionTo(nearHQ)))
                    nav.tryMoveDrone(nav.randomDirection());

                if (rc.canSenseLocation(nearHQ)) {
                    for (Direction dir : Util.directions) {
                        if (rc.canDropUnit(dir))
                            rc.dropUnit(dir);
                    }
                }


            }

        }
        return true;
    }

    public boolean notHoldingUnit(boolean enemypresent,Direction loc) throws GameActionException {
        boolean moved=false;
        if(!rc.isCurrentlyHoldingUnit() && enemypresent) {
            nav.tryMoveDrone(nav.randomDirection());
            moved=true;
        }else if (!rc.isCurrentlyHoldingUnit() && movetowardsenemyafterdropping && enemyHQ==null) {
            loc = rc.getLocation().directionTo(new MapLocation(mapheight - hqLoc.x + Util.randomNumber(), mapwidth - hqLoc.y + Util.randomNumber()));
            nav.tryMoveDrone(loc);
            moved=true;
        }else if (!rc.isCurrentlyHoldingUnit() && movetowardsenemyafterdropping && enemyHQ!=null) {
            loc = rc.getLocation().directionTo(new MapLocation(enemyHQ.x + Util.randomNumber(), enemyHQ.y + Util.randomNumber()));
            nav.tryMoveDrone(loc);
            moved = true;
        }else if(!rc.isCurrentlyHoldingUnit() && enemyHQ!=null && rc.getLocation().isWithinDistanceSquared(enemyHQ,GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED)){
            nav.tryMoveDrone(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
            nav.tryMoveDrone(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
            nav.tryMoveDrone(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
            nav.tryMoveDrone(nav.oppositeDirection(rc.getLocation().directionTo(enemyHQ)));
            moved=true;
        }else{
            loc = rc.getLocation().directionTo(new MapLocation(mapheight - hqLoc.x + Util.randomNumber(), mapwidth - hqLoc.y + Util.randomNumber()));
            nav.tryMoveDrone(loc);
            moved=true;
        }
        return moved;
    }

}




