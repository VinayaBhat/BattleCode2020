package colin;

import static org.junit.Assert.*;
import org.junit.Test;
import battlecode.common.*;
import static battlecode.common.Direction.*;

public class PatsNavigationTest {
    RobotController rc;
    Navigation nav = new Navigation(rc);
    Communications comms;
    Robot rob = new Robot(rc);
    Building testRefinery = new Refinery(rc);
    Building testDesignSchool = new DesignSchool(rc);
    Building testFullfillmentCenter = new FulFillmentcenter(rc);
    //Shooter testHQ = new HQ(rc) throws GameActionException;
   // Building testMiner = new Miner(rc);
   // Robot testLandscaper = new Landscaper(rc);
   // Robot testDrone = new DeliveryDrone(rc);

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
    public void testTurnCountIsTrue() throws GameActionException {
        int take_turn_should_work = rob.turnCount;
        assertEquals(0, take_turn_should_work);

    }

    @Test
    public void testTurnCountIsFalse() throws GameActionException {
        boolean take_turn_shouldnt_work = (rob.turnCount == 1);
        assertEquals(false, take_turn_shouldnt_work);
    }
    @Test
    public void testRefineryTurnCountisTrue() throws GameActionException{
        int take_turn_should_work = testRefinery.turnCount;
        assertEquals(0, take_turn_should_work);
        }

    @Test
    public void testRefineryTurnCountIsFalse() throws GameActionException {
        boolean take_turn_shouldnt_work = (testRefinery.turnCount == 1);
        assertEquals(false, take_turn_shouldnt_work);
    }
    @Test
    public void testDesignSchoolCountIsTrue() throws GameActionException{
        int take_turn_should_work = testDesignSchool.turnCount;
        assertEquals(0, take_turn_should_work);
    }

    @Test
    public void testFullfillmentCenterCountIsTrue() throws GameActionException{
        int take_turn_should_work = testFullfillmentCenter.turnCount;
        assertEquals(0, take_turn_should_work);
    }


    }

