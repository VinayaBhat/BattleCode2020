package player10pdx;
import battlecode.common.*;


public class DesignSchool extends Building {
    public DesignSchool(RobotController r) {
        super(r);
    }

    static int numLandscapers = 0;

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        // will only actually happen if we haven't already broadcasted the creation
        comms.broadcastDesignSchoolCreation(rc.getLocation());
        System.out.println("number of landscapers is " + numLandscapers);
        if (numLandscapers < ) {
            for (Direction dir : Util.directions) {
                if (tryBuild(RobotType.LANDSCAPER, dir)) {
                    System.out.println("made a landscaper");
                    numLandscapers++;
                }
            }
        }
    }
}