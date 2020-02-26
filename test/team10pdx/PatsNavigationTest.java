package team10pdx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import battlecode.common.*;
import battlecode.common.MapLocation;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

public class PatsNavigationTest {
    RobotController rcHQ;
    RobotController rcMiner;
    RobotController rc;
    RobotController rcnull;
    RobotController rcLandScaper;
    @Before
    public void setupHQ() {
        rcHQ = new RobotController() {
            @Override
            public int getRoundNum() {
                return 1;
            }

            @Override
            public int getTeamSoup() {
                return 250;
            }

            @Override
            public int getMapWidth() {
                return 64;
            }

            @Override
            public int getMapHeight() {
                return 64;
            }

            @Override
            public int getID() {
                return 1234;
            }

            @Override
            public Team getTeam() {
                return Team.A;
            }

            @Override
            public RobotType getType() {
                return RobotType.HQ;
            }

            @Override
            public MapLocation getLocation() {
                return new MapLocation(3, 4);
            }

            @Override
            public int getSoupCarrying() {
                return 100;
            }

            @Override
            public int getDirtCarrying() {
                return 100;
            }

            @Override
            public boolean isCurrentlyHoldingUnit() {
                return true;
            }

            @Override
            public int getCurrentSensorRadiusSquared() {
                return 10;
            }

            @Override
            public boolean onTheMap(MapLocation mapLocation) {
                return true;
            }

            @Override
            public boolean canSenseLocation(MapLocation mapLocation) {
                return true;
            }

            @Override
            public boolean canSenseRadiusSquared(int i) {
                return true;
            }

            @Override
            public boolean isLocationOccupied(MapLocation mapLocation) throws GameActionException {
                return true;
            }

            @Override
            public RobotInfo senseRobotAtLocation(MapLocation mapLocation) throws GameActionException {
                return null;
            }

            @Override
            public boolean canSenseRobot(int i) {
                return true;
            }

            @Override
            public RobotInfo senseRobot(int i) throws GameActionException {
                return null;
            }

            @Override
            public RobotInfo[] senseNearbyRobots() {
                RobotInfo r=new RobotInfo(9999, Team.A, RobotType.MINER, 0, false, 0, 0, 0, new MapLocation(1,1));
                RobotInfo[] ri={r};
                return ri;
            }

            @Override
            public RobotInfo[] senseNearbyRobots(int i) {
                return new RobotInfo[0];
            }

            @Override
            public RobotInfo[] senseNearbyRobots(int i, Team team) {
                return new RobotInfo[0];
            }

            @Override
            public RobotInfo[] senseNearbyRobots(MapLocation mapLocation, int i, Team team) {
                return new RobotInfo[0];
            }

            @Override
            public MapLocation[] senseNearbySoup() {
                return new MapLocation[0];
            }

            @Override
            public MapLocation[] senseNearbySoup(int i) {
                return new MapLocation[0];
            }

            @Override
            public MapLocation[] senseNearbySoup(MapLocation mapLocation, int i) {
                return new MapLocation[0];
            }

            @Override
            public int senseSoup(MapLocation mapLocation) throws GameActionException {
                return 0;
            }

            @Override
            public int sensePollution(MapLocation mapLocation) throws GameActionException {
                return 0;
            }

            @Override
            public int senseElevation(MapLocation mapLocation) throws GameActionException {
                return 0;
            }

            @Override
            public boolean senseFlooding(MapLocation mapLocation) throws GameActionException {
                return true;
            }

            @Override
            public MapLocation adjacentLocation(Direction direction) {
                return null;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public float getCooldownTurns() {
                return 0;
            }

            @Override
            public boolean canMove(Direction direction) {
                return true;
            }

            @Override
            public void move(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canBuildRobot(RobotType robotType, Direction direction) {
                return true;
            }

            @Override
            public void buildRobot(RobotType robotType, Direction direction) throws GameActionException {

            }

            @Override
            public boolean canMineSoup(Direction direction) {
                return true;
            }

            @Override
            public void mineSoup(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canDepositSoup(Direction direction) {
                return true;
            }

            @Override
            public void depositSoup(Direction direction, int i) throws GameActionException {

            }

            @Override
            public boolean canDigDirt(Direction direction) {
                return true;
            }

            @Override
            public void digDirt(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canDepositDirt(Direction direction) {
                return true;
            }

            @Override
            public void depositDirt(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canPickUpUnit(int i) {
                return true;
            }

            @Override
            public void pickUpUnit(int i) throws GameActionException {

            }

            @Override
            public boolean canDropUnit(Direction direction) {
                return true;
            }

            @Override
            public void dropUnit(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canShootUnit(int i) {
                return true;
            }

            @Override
            public void shootUnit(int i) throws GameActionException {

            }

            @Override
            public void disintegrate() {

            }

            @Override
            public void resign() {

            }

            @Override
            public boolean canSubmitTransaction(int[] ints, int i) {
                return true;
            }

            @Override
            public void submitTransaction(int[] ints, int i) throws GameActionException {

            }

            @Override
            public Transaction[] getBlock(int i) throws GameActionException {
                return new Transaction[0];
            }

            @Override
            public void setIndicatorDot(MapLocation mapLocation, int i, int i1, int i2) {

            }

            @Override
            public void setIndicatorLine(MapLocation mapLocation, MapLocation mapLocation1, int i, int i1, int i2) {

            }
        };


    }
    @Before
    public void setupLandScaper() {
        rcLandScaper = new RobotController() {
            @Override
            public int getRoundNum() {
                return 1;
            }

            @Override
            public int getTeamSoup() {
                return 250;
            }

            @Override
            public int getMapWidth() {
                return 64;
            }

            @Override
            public int getMapHeight() {
                return 64;
            }

            @Override
            public int getID() {
                return 1234;
            }

            @Override
            public Team getTeam() {
                return Team.A;
            }

            @Override
            public RobotType getType() {
                return RobotType.HQ;
            }

            @Override
            public MapLocation getLocation() {
                return new MapLocation(3, 4);
            }

            @Override
            public int getSoupCarrying() {
                return 100;
            }

            @Override
            public int getDirtCarrying() {
                return 100;
            }

            @Override
            public boolean isCurrentlyHoldingUnit() {
                return true;
            }

            @Override
            public int getCurrentSensorRadiusSquared() {
                return 10;
            }

            @Override
            public boolean onTheMap(MapLocation mapLocation) {
                return true;
            }

            @Override
            public boolean canSenseLocation(MapLocation mapLocation) {
                return true;
            }

            @Override
            public boolean canSenseRadiusSquared(int i) {
                return true;
            }

            @Override
            public boolean isLocationOccupied(MapLocation mapLocation) throws GameActionException {
                return true;
            }

            @Override
            public RobotInfo senseRobotAtLocation(MapLocation mapLocation) throws GameActionException {
                return null;
            }

            @Override
            public boolean canSenseRobot(int i) {
                return true;
            }

            @Override
            public RobotInfo senseRobot(int i) throws GameActionException {
                return null;
            }

            @Override
            public RobotInfo[] senseNearbyRobots() {
                RobotInfo r=new RobotInfo(9999, Team.A, RobotType.MINER, 0, false, 0, 0, 0, new MapLocation(1,1));
                RobotInfo[] ri={r};
                return ri;
            }

            @Override
            public RobotInfo[] senseNearbyRobots(int i) {
                return new RobotInfo[0];
            }

            @Override
            public RobotInfo[] senseNearbyRobots(int i, Team team) {
                return new RobotInfo[0];
            }

            @Override
            public RobotInfo[] senseNearbyRobots(MapLocation mapLocation, int i, Team team) {
                return new RobotInfo[0];
            }

            @Override
            public MapLocation[] senseNearbySoup() {
                return new MapLocation[0];
            }

            @Override
            public MapLocation[] senseNearbySoup(int i) {
                return new MapLocation[0];
            }

            @Override
            public MapLocation[] senseNearbySoup(MapLocation mapLocation, int i) {
                return new MapLocation[0];
            }

            @Override
            public int senseSoup(MapLocation mapLocation) throws GameActionException {
                return 0;
            }

            @Override
            public int sensePollution(MapLocation mapLocation) throws GameActionException {
                return 0;
            }

            @Override
            public int senseElevation(MapLocation mapLocation) throws GameActionException {
                return 0;
            }

            @Override
            public boolean senseFlooding(MapLocation mapLocation) throws GameActionException {
                return true;
            }

            @Override
            public MapLocation adjacentLocation(Direction direction) {
                return null;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public float getCooldownTurns() {
                return 0;
            }

            @Override
            public boolean canMove(Direction direction) {
                return true;
            }

            @Override
            public void move(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canBuildRobot(RobotType robotType, Direction direction) {
                return true;
            }

            @Override
            public void buildRobot(RobotType robotType, Direction direction) throws GameActionException {

            }

            @Override
            public boolean canMineSoup(Direction direction) {
                return true;
            }

            @Override
            public void mineSoup(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canDepositSoup(Direction direction) {
                return true;
            }

            @Override
            public void depositSoup(Direction direction, int i) throws GameActionException {

            }

            @Override
            public boolean canDigDirt(Direction direction) {
                return true;
            }

            @Override
            public void digDirt(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canDepositDirt(Direction direction) {
                return true;
            }

            @Override
            public void depositDirt(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canPickUpUnit(int i) {
                return true;
            }

            @Override
            public void pickUpUnit(int i) throws GameActionException {

            }

            @Override
            public boolean canDropUnit(Direction direction) {
                return true;
            }

            @Override
            public void dropUnit(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canShootUnit(int i) {
                return true;
            }

            @Override
            public void shootUnit(int i) throws GameActionException {

            }

            @Override
            public void disintegrate() {

            }

            @Override
            public void resign() {

            }

            @Override
            public boolean canSubmitTransaction(int[] ints, int i) {
                return true;
            }

            @Override
            public void submitTransaction(int[] ints, int i) throws GameActionException {

            }

            @Override
            public Transaction[] getBlock(int i) throws GameActionException {
                return new Transaction[0];
            }

            @Override
            public void setIndicatorDot(MapLocation mapLocation, int i, int i1, int i2) {

            }

            @Override
            public void setIndicatorLine(MapLocation mapLocation, MapLocation mapLocation1, int i, int i1, int i2) {

            }
        };


    }

    @Before
    public void setupMiner() {
        rcMiner = new RobotController() {
            @Override
            public int getRoundNum() {
                return 1;
            }

            @Override
            public int getTeamSoup() {
                return 250;
            }

            @Override
            public int getMapWidth() {
                return 64;
            }

            @Override
            public int getMapHeight() {
                return 64;
            }

            @Override
            public int getID() {
                return 1234;
            }

            @Override
            public Team getTeam() {
                return Team.A;
            }

            @Override
            public RobotType getType() {
                return RobotType.MINER;
            }

            @Override
            public MapLocation getLocation() {
                return new MapLocation(3, 4);
            }

            @Override
            public int getSoupCarrying() {
                return 100;
            }

            @Override
            public int getDirtCarrying() {
                return 100;
            }

            @Override
            public boolean isCurrentlyHoldingUnit() {
                return true;
            }

            @Override
            public int getCurrentSensorRadiusSquared() {
                return 10;
            }

            @Override
            public boolean onTheMap(MapLocation mapLocation) {
                return true;
            }

            @Override
            public boolean canSenseLocation(MapLocation mapLocation) {
                return true;
            }

            @Override
            public boolean canSenseRadiusSquared(int i) {
                return true;
            }

            @Override
            public boolean isLocationOccupied(MapLocation mapLocation) throws GameActionException {
                return true;
            }

            @Override
            public RobotInfo senseRobotAtLocation(MapLocation mapLocation) throws GameActionException {
                return null;
            }

            @Override
            public boolean canSenseRobot(int i) {
                return true;
            }

            @Override
            public RobotInfo senseRobot(int i) throws GameActionException {
                return null;
            }

            @Override
            public RobotInfo[] senseNearbyRobots() {
                return new RobotInfo[0];
            }

            @Override
            public RobotInfo[] senseNearbyRobots(int i) {
                return new RobotInfo[0];
            }

            @Override
            public RobotInfo[] senseNearbyRobots(int i, Team team) {
                return new RobotInfo[0];
            }

            @Override
            public RobotInfo[] senseNearbyRobots(MapLocation mapLocation, int i, Team team) {
                return new RobotInfo[0];
            }

            @Override
            public MapLocation[] senseNearbySoup() {
                return new MapLocation[0];
            }

            @Override
            public MapLocation[] senseNearbySoup(int i) {
                return new MapLocation[0];
            }

            @Override
            public MapLocation[] senseNearbySoup(MapLocation mapLocation, int i) {
                return new MapLocation[0];
            }

            @Override
            public int senseSoup(MapLocation mapLocation) throws GameActionException {
                return 0;
            }

            @Override
            public int sensePollution(MapLocation mapLocation) throws GameActionException {
                return 0;
            }

            @Override
            public int senseElevation(MapLocation mapLocation) throws GameActionException {
                return 0;
            }

            @Override
            public boolean senseFlooding(MapLocation mapLocation) throws GameActionException {
                return false;
            }

            @Override
            public MapLocation adjacentLocation(Direction direction) {
                return null;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public float getCooldownTurns() {
                return 0;
            }

            @Override
            public boolean canMove(Direction direction) {
                return true;
            }

            @Override
            public void move(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canBuildRobot(RobotType robotType, Direction direction) {
                return true;
            }

            @Override
            public void buildRobot(RobotType robotType, Direction direction) throws GameActionException {

            }

            @Override
            public boolean canMineSoup(Direction direction) {
                return true;
            }

            @Override
            public void mineSoup(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canDepositSoup(Direction direction) {
                return true;
            }

            @Override
            public void depositSoup(Direction direction, int i) throws GameActionException {

            }

            @Override
            public boolean canDigDirt(Direction direction) {
                return true;
            }

            @Override
            public void digDirt(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canDepositDirt(Direction direction) {
                return true;
            }

            @Override
            public void depositDirt(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canPickUpUnit(int i) {
                return true;
            }

            @Override
            public void pickUpUnit(int i) throws GameActionException {

            }

            @Override
            public boolean canDropUnit(Direction direction) {
                return true;
            }

            @Override
            public void dropUnit(Direction direction) throws GameActionException {

            }

            @Override
            public boolean canShootUnit(int i) {
                return true;
            }

            @Override
            public void shootUnit(int i) throws GameActionException {

            }

            @Override
            public void disintegrate() {

            }

            @Override
            public void resign() {

            }

            @Override
            public boolean canSubmitTransaction(int[] ints, int i) {
                return true;
            }

            @Override
            public void submitTransaction(int[] ints, int i) throws GameActionException {

            }

            @Override
            public Transaction[] getBlock(int i) throws GameActionException {
                return new Transaction[0];
            }

            @Override
            public void setIndicatorDot(MapLocation mapLocation, int i, int i1, int i2) {

            }

            @Override
            public void setIndicatorLine(MapLocation mapLocation, MapLocation mapLocation1, int i, int i1, int i2) {

            }
        };


    }




    @Test
    public void testNearestLocation() {
        Navigation nav = new Navigation(rc);
        MapLocation location1 = new MapLocation(1, 1);
        MapLocation location2 = new MapLocation(1, 2);
        MapLocation location3 = new MapLocation(1, 3);
        MapLocation[] locations = {location2, location3};
        MapLocation nearest = nav.findNearestLocation(location1, locations);
        assertEquals(true, nearest.y == 2);
    }
    @Test
    public void testIsInRadius(){
        Navigation nav = new Navigation(rc);
        MapLocation location1 = new MapLocation(0, 0);
        MapLocation location2 = new MapLocation (4, 2);
        boolean should_be_in_radius = nav.inRadius(location1, location2, 4);
        assertEquals(true, should_be_in_radius);
    }

    @Test
    public void testIsNotInRadius(){
        Navigation nav = new Navigation(rc);
        MapLocation location1 = new MapLocation(0, 0);
        MapLocation location2 = new MapLocation (4, 2);
        boolean should_not_be_in_radius = nav.inRadius(location1, location2, 2);
        assertEquals(false, should_not_be_in_radius);
    }

   @Test
    public void randomDirectiontest(){
        Util u=new Util();
        Util.randomNumber();
        Util.randomDirection();
   }
@Test
    public void testRobotTryBuild() throws GameActionException {
        Robot rHQ=new Robot(rcHQ);
        assertEquals(rHQ.tryBuild(RobotType.HQ,Direction.NORTH),true);
   }

   @Test
    public void getHQLocation() throws GameActionException {
        Communications c=new Communications(rcHQ);
        assertEquals(c.getHqLocFromBlockchain(),null);
   }

    @Test
    public void getBuildingCount() throws GameActionException {
        Communications c=new Communications(rcHQ);
        assertEquals(c.getnewBuildingCount(1),0);
    }

    @Test
    public void findmessageinBlockChain() throws GameActionException {
        Communications c=new Communications(rcHQ);
        int[][] allMessages = {
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1}};
        assertEquals(c.findTeamMessagesInBlockChain(),allMessages);
    }
    @Test
    public void getrandomDiagonal() throws GameActionException {
       Navigation n=new Navigation(rcHQ);
       n.getRandomDiagonal();
    }

    @Test
    public void getNextDiagonal() throws GameActionException {
        Navigation n=new Navigation(rcHQ);
        n.getNextDiagonal(Direction.NORTH);
    }

    @Test
    public void nearestLocation() throws GameActionException {
        Navigation n=new Navigation(rcHQ);
       n.findNearestLocation(new MapLocation(1,1), new MapLocation[]{new MapLocation(2, 2)});
    }

    @Test
    public void tryMove() throws GameActionException {
        Navigation n=new Navigation(rcHQ);
        assertEquals(n.tryMove(Direction.NORTH),false);
    }

    @Test
    public void goTo() throws GameActionException {
        Navigation n=new Navigation(rcHQ);
        assertEquals(n.goTo(new MapLocation(1,1)),false);
    }
    @Test
    public void goToDirection() throws GameActionException {
        Navigation n=new Navigation(rcHQ);
        assertEquals(n.goTo(Direction.NORTH),false);
    }

    @Test
    public void randomDir() throws GameActionException {
        Navigation n=new Navigation(rcHQ);
       n.randomDirection();
    }

    @Test
    public void oppositeDirection() throws GameActionException {
        Navigation n=new Navigation(rcHQ);
        assertEquals(n.oppositeDirection(Direction.NORTH),Direction.SOUTH);
        assertEquals(n.oppositeDirection(Direction.SOUTH),Direction.NORTH);
        assertEquals(n.oppositeDirection(Direction.WEST),Direction.EAST);
        assertEquals(n.oppositeDirection(Direction.EAST),Direction.WEST);
        assertEquals(n.oppositeDirection(Direction.NORTHEAST),Direction.SOUTHWEST);
        assertEquals(n.oppositeDirection(Direction.NORTHWEST),Direction.SOUTHEAST);
        assertEquals(n.oppositeDirection(Direction.SOUTHEAST),Direction.NORTHWEST);
        assertEquals(n.oppositeDirection(Direction.SOUTHWEST),Direction.NORTHEAST);
    }

    @Test
    public void foundRobot() throws GameActionException {
        Navigation n=new Navigation(rcHQ);
       assertEquals(n.byRobot(RobotType.MINER),true);
    }

    @Test
    public void tryToMine() throws GameActionException {
        Miner m=new Miner(rcMiner);
        assertEquals(m.tryMine(Direction.SOUTHWEST),true);
    }

    @Test
    public void tryRefine() throws GameActionException {
        Miner m=new Miner(rcMiner);
        assertEquals(m.tryRefine(Direction.SOUTHWEST),true);
    }

    @Test
    public void tryDig() throws GameActionException {
        Landscaper m=new Landscaper(rcLandScaper);
        assertEquals(m.tryDig(),true);
    }



}
