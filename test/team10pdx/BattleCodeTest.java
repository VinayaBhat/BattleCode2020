package team10pdx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import battlecode.common.*;
import battlecode.common.MapLocation;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BattleCodeTest {
    RobotController shooter;
    RobotController comm;
    RobotController nav;
    RobotController drone;
    RobotController drone1;
    RobotController unit;
    RobotController unit1;
    RobotController ds;
    RobotController fulfilment;

    @Before
    public void setUpFulFilment(){
        fulfilment=mock(RobotController.class);
    }

    @Before
    public void setUpDesignSchool(){
        ds=mock(RobotController.class);
    }

    @Before
    public void setupShooter() {
        shooter=mock(RobotController.class);
        when(shooter.getTeam()).thenReturn(Team.A);
        RobotInfo[] enemy = new RobotInfo[2];
        enemy[0] = new RobotInfo(111, Team.B, RobotType.DELIVERY_DRONE, 0, false, 0, 0, 0, new MapLocation(2, 2));
        enemy[1] = new RobotInfo(112, Team.B, RobotType.DELIVERY_DRONE, 0, false, 0, 0, 0, new MapLocation(3, 3));
        when(shooter.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, Team.B)).thenReturn(enemy);
        when(shooter.canShootUnit(111)).thenReturn(true);
        when(shooter.canShootUnit(112)).thenReturn(true);
    }
    @Before
    public void setupUnit() {
        unit=mock(RobotController.class);
        when(unit.getMapHeight()).thenReturn(64);
        when(unit.getMapWidth()).thenReturn(64);
        unit1=mock(RobotController.class);
        when(unit1.getMapHeight()).thenReturn(64);
        when(unit1.getMapWidth()).thenReturn(64);
    }

    @Before
    public void setupcommunicaiton() throws GameActionException {
        comm=mock(RobotController.class);
        when(comm.getTeam()).thenReturn(Team.A);
        when(comm.canSubmitTransaction(new int[]{99999,0,0,0,0,0,0},3)).thenReturn(true);
        when(comm.canSubmitTransaction(new int[]{99999,1,1,2,0,0,0},2)).thenReturn(true);
        when(comm.canSubmitTransaction(new int[]{99999,4,1,3,0,0,0},2)).thenReturn(true);
        when(comm.canSubmitTransaction(new int[]{99999,8,1,4,0,0,0},2)).thenReturn(true);
        when(comm.canSubmitTransaction(new int[]{99999,0,0,0,0,0,0},1)).thenReturn(true);
        when(comm.canSubmitTransaction(new int[]{99999,7,1,0,0,0,0},1)).thenReturn(true);
        when(comm.canSubmitTransaction(new int[]{99999,2,4,4,0,0,0},2)).thenReturn(true);
        when(comm.canSubmitTransaction(new int[]{99999,10,5,5,0,0,0},3)).thenReturn(true);
        when(comm.canSubmitTransaction(new int[]{99999,10,5,5,0,0,0},3)).thenReturn(true);
        when(comm.getRoundNum()).thenReturn(2);
        Transaction tx=new Transaction(2,new int[]{99999,0,0,0,0,0,0},2);
        Transaction[] tx1=new Transaction[1];
        tx1[0]=tx;
        when(comm.getBlock(1)).thenReturn(tx1);
    }

    @Before
    public void setNav(){
        nav=mock(RobotController.class);
        when(nav.canMove(Direction.NORTH)).thenReturn(true);
        when(nav.canMove(Direction.NORTHEAST)).thenReturn(true);
        when(nav.canMove(Direction.NORTHWEST)).thenReturn(true);
        when(nav.canMove(Direction.SOUTH)).thenReturn(true);
        when(nav.canMove(Direction.SOUTHEAST)).thenReturn(true);
        when(nav.canMove(Direction.SOUTHWEST)).thenReturn(true);
    }
    @Before
    public void setDrone(){
        drone=mock(RobotController.class);
        drone1=mock(RobotController.class);
    }

    @Test
    public void shooterTest() throws GameActionException {
        Shooter shoot= new Shooter(shooter);
        Shooter shootspy=Mockito.spy(shoot);
        shootspy.takeTurn();
        assertEquals(shootspy.shooting(),true);

    }

    @Test
    public void commTest() throws GameActionException {
        Communications com=new Communications(comm);
        Communications commspy=Mockito.spy(com);
        assertEquals(commspy.sendHqLoc(new MapLocation(0,0)),true);
        assertEquals(commspy.broadcastRefineryLocation(new MapLocation(1,2)),true);
        assertEquals(commspy.broadcastDesignSchoolLocation(new MapLocation(1,3)),true);
        assertEquals(commspy.broadcastFulfillmentCenterLocation(new MapLocation(1,4)),true);
        assertEquals(commspy.getHqLocFromBlockchain(),new MapLocation(0,0));
        assertEquals(commspy.broadcastCreation(new MapLocation(0,0),0),true);
        assertEquals(commspy.broadcastLandscaperRole(1),true);
        assertEquals(commspy.broadcastSoupLocation(new MapLocation(4,4)),true);
        assertEquals(commspy.broadcastWaterLocation(new MapLocation(5,5)),true);
        assertEquals(commspy.getnewBuildingCount(0),1);
        int[][] allMessages = {
                {99999,0,0,0,0,0,0},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1}};
        assertEquals(commspy.findTeamMessagesInBlockChain(),allMessages);
        commspy.broadcastRemoveSoup(new MapLocation(4,4),1);
    }

    @Test
    public void NavTest(){
        Navigation n=new Navigation(nav);
        assertEquals(Arrays.asList(Util.directions).contains(n.getRandomDiagonal()),true);
        assertEquals(n.getNextDiagonal(Direction.SOUTHWEST),Direction.SOUTHEAST);
        assertEquals(n.getNextDiagonal(Direction.SOUTHEAST),Direction.SOUTHWEST);
        assertEquals(n.getNextDiagonal(Direction.NORTHWEST),Direction.NORTHEAST);
        assertEquals(n.getNextDiagonal(Direction.NORTHEAST),Direction.NORTHWEST);
        when(nav.canMove(Direction.SOUTH)).thenReturn(false);
        assertEquals(n.getNextDiagonal(Direction.SOUTHWEST),Direction.NORTHWEST);
        assertEquals(n.getNextDiagonal(Direction.SOUTHEAST),Direction.NORTHEAST);
        when(nav.canMove(Direction.SOUTH)).thenReturn(true);
        when(nav.canMove(Direction.NORTH)).thenReturn(false);
        assertEquals(n.getNextDiagonal(Direction.NORTHWEST),Direction.SOUTHWEST);
        assertEquals(n.getNextDiagonal(Direction.NORTHEAST),Direction.SOUTHEAST);
        when(nav.canMove(Direction.SOUTH)).thenReturn(false);
        when(nav.canMove(Direction.NORTH)).thenReturn(false);
        assertEquals(Arrays.asList(Util.directions).contains(n.getNextDiagonal(Direction.NORTHWEST)),true);
        assertEquals(Arrays.asList(Util.directions).contains(n.getNextDiagonal(Direction.NORTHEAST)),true);
        assertEquals(Arrays.asList(Util.directions).contains(n.getNextDiagonal(Direction.SOUTHEAST)),true);
        assertEquals(Arrays.asList(Util.directions).contains(n.getNextDiagonal(Direction.SOUTHWEST)),true);
        assertEquals(Arrays.asList(Util.directions).contains(n.getNextDiagonal(Direction.EAST)),true);

    }

    @Test
    public void UnitTestwithNoNearbyrobots() throws GameActionException {
        when(unit.getTeam()).thenReturn(Team.A);
        Unit u=new Unit(unit);
        RobotInfo[] ri1=new RobotInfo[0];
        when(unit.senseNearbyRobots()).thenReturn(ri1);
        assertEquals(u.findHQ(),isNull());
    }
    @Test
    public void UnitTestwithNearbyRobot() throws GameActionException {
        when(unit1.getTeam()).thenReturn(Team.A);
        RobotInfo[] ri=new RobotInfo[1];
        ri[0] = new RobotInfo(121, Team.A, RobotType.HQ, 0, false, 0, 0, 0, new MapLocation(2, 2));
        when(unit1.senseNearbyRobots()).thenReturn(ri);
        Unit u1=new Unit(unit1);
        assertEquals(u1.findHQ(),new MapLocation(2,2));
        u1.takeTurn();

    }

    @Test
    public void DeliveryDronetest() throws GameActionException {
        when(drone.getTeam()).thenReturn(Team.A);
        DeliveryDrone d=new DeliveryDrone(drone);
        RobotInfo[] ri=new RobotInfo[1];
        ri[0] = new RobotInfo(111, Team.B, RobotType.COW, 0, false, 0, 0, 0, new MapLocation(2, 2));
        when(drone.isCurrentlyHoldingUnit()).thenReturn(false);
        when(drone.canPickUpUnit(111)).thenReturn(true);
        assertEquals(d.canPickUpRobotandEnemyPresent(ri),false);

        Navigation n=new Navigation(nav);
        RobotInfo[] ri2=new RobotInfo[2];
        ri2[0] = new RobotInfo(111, Team.B, RobotType.MINER, 0, false, 0, 0, 0, new MapLocation(2, 2));
        ri2[1] = new RobotInfo(111, Team.B, RobotType.HQ, 0, false, 0, 0, 0, new MapLocation(2, 2));
        when(drone.isCurrentlyHoldingUnit()).thenReturn(false);
        when(drone.canPickUpUnit(111)).thenReturn(true);
        int[] message = {99999, 11, 10, 10, 0, 0, 0};
        when(drone.canSubmitTransaction(message, 5)).thenReturn(true);
        assertEquals(d.canPickUpRobotandEnemyPresent(ri2),true);
        when(drone.isCurrentlyHoldingUnit()).thenReturn(true);
        when(drone.canPickUpUnit(111)).thenReturn(false);
        assertEquals(d.canPickUpRobotandEnemyPresent(ri2),true);

        when(drone.getLocation()).thenReturn(new MapLocation(4,4));
        when(drone.senseFlooding(new MapLocation(5,4))).thenReturn(true);
        when(drone.canSenseLocation(new MapLocation(5,4))).thenReturn(true);
        List<MapLocation> water=new ArrayList<>();
        water.add(new MapLocation(5,4));
        assertEquals(d.sensewaterlocation(),water);

        when(drone.isCurrentlyHoldingUnit()).thenReturn(false);
        when(drone.getLocation()).thenReturn(new MapLocation(4,4));
        assertEquals(d.ifnotholdingunitandenemyHQnotfound(Direction.NORTH),false);

        when(drone.isCurrentlyHoldingUnit()).thenReturn(true);
        when(drone.canDropUnit(Direction.NORTH)).thenReturn(true);
        assertEquals(d.isHoldingCow(true,Direction.NORTH),false);
        d.cow=true;
        assertEquals(d.isHoldingCow(false,Direction.NORTH),true);
    }
    @Test
    public void Dronetest2() throws GameActionException {
        when(drone.getTeam()).thenReturn(Team.A);
        DeliveryDrone d=new DeliveryDrone(drone);
        int[][] allMessages = {
                {99999,11,2,2,0,0,0},
                {99999,10,1,1,-0,0,0},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1}};
        assertEquals(d.findwaterlocations(allMessages),allMessages);

    }

    @Test
    public void DesignSchoolTest() throws GameActionException {
        DesignSchool school=new DesignSchool(ds);
        assertEquals(school.buildLandscapers(),false);
        when(ds.canBuildRobot(RobotType.LANDSCAPER,Direction.NORTH)).thenReturn(true);
        when(ds.getTeamSoup()).thenReturn(200);
        when(school.tryBuild(RobotType.LANDSCAPER,Direction.NORTH)).thenReturn(true);
        assertEquals(school.buildLandscapers(),true);
        school.takeTurn();
    }

    @Test
    public void FulfilmentCenterTest() throws GameActionException {
        FulFillmentcenter f=new FulFillmentcenter(fulfilment);
        int[][] allMessages = {
                {99999,9,2,2,0,0,0},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1}};

        assertEquals(f.findmaxdrones(allMessages),2);
        when(fulfilment.canBuildRobot(RobotType.DELIVERY_DRONE,Direction.NORTH)).thenReturn(true);
        assertEquals(f.builddrones(),3);
    }

    @Test
    public void DroneTest() throws GameActionException {
        when(drone1.getTeam()).thenReturn(Team.A);
        DeliveryDrone dr=new DeliveryDrone(drone1);
        dr.hqLoc=new MapLocation(2,2);
        when(drone1.isCurrentlyHoldingUnit()).thenReturn(true);

        int[][] allMessages = {
                {99999,11,2,2,0,0,0},
                {99999,10,1,1,-0,0,0},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1}};
        assertEquals(dr.findwaterlocations(allMessages),allMessages);
        when(drone1.getLocation()).thenReturn(new MapLocation(2,2));
        when(drone1.canSenseLocation(new MapLocation(1,1))).thenReturn(true);
        when(drone1.canDropUnit(drone1.getLocation().directionTo(new MapLocation(1,1)))).thenReturn(true);
        assertEquals(dr.isHoldingEnemyUnit(),true);

    }

    @Test
    public void DroneTest2() throws GameActionException {
        when(drone1.getTeam()).thenReturn(Team.A);
        DeliveryDrone d=new DeliveryDrone(drone1);
        assertEquals(d.notHoldingUnit(true,Direction.NORTH),true);
        d.movetowardsenemyafterdropping=true;
        assertEquals(d.notHoldingUnit(false,Direction.NORTH),true);
        d.movetowardsenemyafterdropping=false;
        d.enemyHQ=new MapLocation(2,2);
        when(drone1.getLocation()).thenReturn(new MapLocation(1,1));
        Navigation nav=new Navigation(drone1);
        when( nav.tryMove(nav.oppositeDirection(drone1.getLocation().directionTo(d.enemyHQ)))).thenReturn(true);
        assertEquals(d.notHoldingUnit(false,Direction.NORTH),true);
        when(drone1.isCurrentlyHoldingUnit()).thenReturn(true);
        when(nav.tryMove(Direction.NORTH)).thenReturn(true);
        assertEquals(d.notHoldingUnit(false,Direction.NORTH),true);



    }
}
