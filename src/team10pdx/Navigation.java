package team10pdx;
import battlecode.common.*;

import java.util.Random;

import static battlecode.common.Direction.*;
import static java.lang.StrictMath.abs;

public class Navigation {
    RobotController rc;

    // state related only to navigation should go here

    public Navigation(RobotController r) {
        rc = r;
    }

    public Direction getRandomDiagonal(){
        int choice = (int) (Math.random() * Util.diagonals.length); //random int 0-3
        return Util.diagonals[choice];
    }

    public Direction getNextDiagonal(Direction dir){
        int choice = (int) (Math.random() * Util.diagonals.length);
        switch(dir){
            case SOUTHEAST:
                if(rc.canMove(SOUTH) && rc.canMove(SOUTHWEST)){
                    return SOUTHWEST;
                }
                else if(rc.canMove(NORTH) && rc.canMove(NORTHEAST)){
                    return NORTHEAST;
                }
                else{
                    return Util.diagonals[choice];
                }
            case SOUTHWEST:
                if(rc.canMove(SOUTH) && rc.canMove(SOUTHEAST)){
                    return SOUTHEAST;
                }
                else if(rc.canMove(NORTH) && rc.canMove(NORTHWEST)){
                    return NORTHWEST;
                }
                else{
                    return Util.diagonals[choice];
                }
            case NORTHWEST:
                if(rc.canMove(NORTH) && rc.canMove(NORTHEAST)){
                    return NORTHEAST;
                }
                else if(rc.canMove(SOUTH) && rc.canMove(SOUTHWEST)){
                    return SOUTHWEST;
                }
                else{
                    return Util.diagonals[choice];
                }
            case NORTHEAST:
                if(rc.canMove(NORTH) && rc.canMove(NORTHWEST)){
                    return NORTHWEST;
                }
                else if(rc.canMove(SOUTH) && rc.canMove(SOUTHEAST)){
                    return SOUTHEAST;
                }
                else{
                    return Util.diagonals[choice];
                }
        }
        return Util.diagonals[choice];
    }

    MapLocation findNearestLocation(MapLocation myLocation, MapLocation[] locations){
        int count = 0;
        MapLocation closest = new MapLocation(1,1);
        for(MapLocation location : locations){
            if(count==0){
                //first one starts as closest
                closest = location;
            }
            else{
                if(distanceTo(myLocation,location)<distanceTo(myLocation,closest)){
                    //new closest
                    closest = location;
                }
            }
            count++;
        }
        return closest;
    }

    int distanceTo(MapLocation location1, MapLocation location2){
        int xDifference = abs(location1.x-location2.x);
        int yDifference = abs(location1.y - location2.y);
        if(xDifference>yDifference){
            return xDifference;
        }
        else if(yDifference>xDifference){
            return yDifference;
        }
        else{
            return xDifference;
        }
    }

    boolean tryMove(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
            rc.move(dir);
            return true;
        } else return false;
    }

    // tries to move in the general direction of dir
    boolean goTo(Direction dir) throws GameActionException {
        Direction[] toTry = {dir, dir.rotateLeft(), dir.rotateRight(), dir.rotateLeft().rotateLeft(), dir.rotateRight().rotateRight()};
        for (Direction d : toTry){
            if(tryMove(d))
                return true;
        }
        return false;
    }

    // navigate towards a particular location
    boolean goTo(MapLocation destination) throws GameActionException {
        return goTo(rc.getLocation().directionTo(destination));
    }

    boolean inRadius(MapLocation loc1, MapLocation loc2, int radius){
        return (abs(loc1.x-loc2.x) < radius+1 && abs(loc1.y-loc2.y) < radius+1);
    }

    boolean tryAltMoves(Direction d) throws GameActionException {
        boolean flip = (rc.getRoundNum()%2==0);
        boolean moved = false;
        switch(d) {
            case EAST:
                if (flip) {
                    if (tryMove(Direction.NORTHEAST)) {
                        System.out.println("moved northeast");
                        moved = true;
                    }

                } else {
                    if (tryMove(SOUTHEAST)) {
                        System.out.println("moved southeast");
                        moved = true;
                    }
                }
                return moved;
            case WEST:
                if (flip) {
                    if (tryMove(Direction.NORTHWEST)) {
                        System.out.println("moved northwest");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.SOUTHWEST)) {
                        System.out.println("moved southwest");
                        moved = true;
                    }
                }
                return moved;
            case NORTH:
                if (flip) {
                    if (tryMove(Direction.NORTHEAST)) {
                        System.out.println("moved northeast");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.NORTHWEST)) {
                        System.out.println("moved northwest");
                        moved = true;
                    }
                }
                return moved;
            case SOUTH:
                if (flip) {
                    if (tryMove(SOUTHEAST)) {
                        System.out.println("moved southeast");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.SOUTHWEST)) {
                        System.out.println("moved southwest");
                        moved = true;
                    }
                }
                return moved;
            case NORTHEAST:
                if (flip) {
                    if (tryMove(Direction.NORTH)) {
                        System.out.println("moved north");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.EAST)) {
                        System.out.println("moved east");
                        moved = true;
                    }
                }
                return moved;
            case NORTHWEST:
                if (flip) {
                    if (tryMove(Direction.NORTH)) {
                        System.out.println("moved north");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.WEST)) {
                        System.out.println("moved west");
                        moved = true;
                    }
                }
                return moved;
            case SOUTHEAST:
                if (flip) {
                    if (tryMove(Direction.SOUTH)) {
                        System.out.println("moved south");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.EAST)) {
                        System.out.println("moved east");
                        moved = true;
                    }
                }
                return moved;
            case SOUTHWEST:
                if (flip) {
                    if (tryMove(Direction.SOUTH)) {
                        System.out.println("moved south");
                        moved = true;
                    }
                } else {
                    if (tryMove(Direction.WEST)) {
                        System.out.println("moved west");
                        moved = true;
                    }
                }
                return moved;
        }
        return moved;
    }

    Direction oppositeDirection(Direction d){
        Direction newDirection = randomDirection();
        switch (d){
            case EAST:
                return Direction.WEST;
            case WEST:
                return Direction.EAST;
            case NORTH:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.NORTH;
            case NORTHEAST:
                return Direction.SOUTHWEST;
            case NORTHWEST:
                return SOUTHEAST;
            case SOUTHEAST:
                return Direction.NORTHWEST;
            case SOUTHWEST:
                return Direction.NORTHEAST;
        }
        return newDirection;
    }

    Direction randomDirection() {
        return Util.directions[(int) (Math.random() * Util.directions.length)];
    }

    boolean byRobot(RobotType type){
        boolean found = false;
        RobotInfo[] robots = rc.senseNearbyRobots();
        for(RobotInfo robot : robots){
            if(robot.getType()==type){
                found = true;
            }
        }
        return found;
    }
}