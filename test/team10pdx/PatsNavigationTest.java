package team10pdx;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import battlecode.common.*;
import battlecode.common.MapLocation;

public class PatsNavigationTest {
    RobotController rc;
    @Before
    public void setup(){

    }

    Navigation nav = new Navigation(rc);

    @Test
    public void testNearestLocation() {
        MapLocation location1 = new MapLocation(1, 1);
        MapLocation location2 = new MapLocation(1, 2);
        MapLocation location3 = new MapLocation(1, 3);
        MapLocation[] locations = {location2, location3};
        MapLocation nearest = nav.findNearestLocation(location1, locations);
        assertEquals(true, nearest.y == 2);
    }
    @Test
    public void testIsInRadius(){
        MapLocation location1 = new MapLocation(0, 0);
        MapLocation location2 = new MapLocation (4, 2);
        boolean should_be_in_radius = nav.inRadius(location1, location2, 4);
        assertEquals(true, should_be_in_radius);
    }

    @Test
    public void testIsNotInRadius(){
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

    Robot rob = new Robot(rc);
    Building refinerytester = new Refinery(rc);
    Building designschooltester = new DesignSchool(rc);
    Building fulfillmentcentertester = new FulFillmentcenter(rc);

    @Test
    public void RobotTakeTurnCount(){
        int taketurn = rob.turnCount;
        assertEquals(0, taketurn);
    }

    @Test
    public void refineryTakeTurnCount(){
        int taketurn = refinerytester.turnCount;
        assertEquals(0, taketurn);
    }
    @Test
    public void designSchoolTakeTurnCount(){
        int taketurn = designschooltester.turnCount;
        assertEquals(0, taketurn);
    }
    @Test
    public void fulfillmentCenterTakeTurnCount(){
        int taketurn = fulfillmentcentertester.turnCount;
        assertEquals(0, taketurn);
    }

   @Test(expected = Exception.class)
    public void Robot() throws GameActionException {
        RobotController rc1=new RobotController() {
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
                return new MapLocation(3,4);
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

        Robot rb=new Robot(rc1);
        rb.takeTurn();
        rb.tryBuild(RobotType.HQ,Direction.SOUTHWEST);
        Miner m=new Miner(rc1);
        m.takeTurn();
        m.buildRefinery();
        m.buildDesignSchool();

   }

}
