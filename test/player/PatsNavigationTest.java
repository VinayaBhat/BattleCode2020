package player;

import static org.junit.Assert.*;
import org.junit.Test;
import battlecode.common.*;
import battlecode.common.MapLocation;

public class PatsNavigationTest {
    RobotController rc;
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

}
