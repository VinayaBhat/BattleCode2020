package colin

import battlecode.common.MapLocation
import org.junit.Test

class NavigationTest extends GroovyTestCase {
    void setUp() {
        super.setUp()
    }

    void tearDown() {
    }
    @Test
    void testFindNearestLocation() {
        Navigation nav = new Navigation();
        MapLocation location1 = MapLocation(1,1);
        MapLocation location2 = MapLocation(1,2);
        MapLocation location3 = MapLocation(1,3);
        MapLocation[] locations = { location2 location3};

        MapLocation = nav.findNearestLocation(location1, locations);

        assertEquals(true);
    }

    void testDistanceTo() {
    }

    void testInRadius() {
    }
}
