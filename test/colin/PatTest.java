package colin;
import static org.junit.Assert.*;
import org.junit.Test;
import colin.Util;
import battlecode.common.*;
import battlecode.common.MapLocation;

class PatTest {

    // void setUp() {
   //     super.setUp();
    //}

    //void tearDown() {
    //}
    @Test
    void testFindNearestLocation() {
        RobotController rc = new RobotController;
        Navigation nav = new Navigation(rc);
        MapLocation location1 = new MapLocation(1,1);
        MapLocation location2 = new MapLocation(1,2);
        MapLocation location3 = new MapLocation(1,3);
        MapLocation[] locations = { location2, location3};

        MapLocation nearest = nav.findNearestLocation(location1, locations);

        assertEquals(true, nearest.y == 2);
   }

    void testDistanceTo() {
    }

    void testInRadius() {
    }
}
