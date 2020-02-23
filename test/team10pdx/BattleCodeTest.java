package team10pdx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import battlecode.common.*;
import battlecode.common.MapLocation;
import org.mockito.Mockito;

import java.util.Arrays;

public class BattleCodeTest {
    RobotController shooter;
    RobotController comm;
    RobotController nav;

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
}
