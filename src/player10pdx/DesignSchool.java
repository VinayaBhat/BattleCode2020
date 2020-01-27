package player10pdx;
import battlecode.common.*;

import java.util.ArrayList;


public class DesignSchool extends Building {
    public DesignSchool(RobotController r) {
        super(r);
    }

    static int totalnumLandscapers = 0;

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        int numLandscapers = 0;
        comms.broadcastCreation(rc.getLocation(), 1);
        int landscapercount = 0;
        ArrayList<Direction> gooddirections = new ArrayList<Direction>();
        for (Direction dir : Util.directions)
            if (rc.canBuildRobot(RobotType.LANDSCAPER, dir)) {
                landscapercount += 1;
                gooddirections.add(dir);
            }
            System.out.println("num landscapers = " + totalnumLandscapers);
            if (totalnumLandscapers < 4 && landscapercount > 0) {
                for (Direction dir : gooddirections) {
                    rc.buildRobot(RobotType.LANDSCAPER, dir);
                    numLandscapers += 1;
                    totalnumLandscapers += 1;
                }
                gooddirections = null;
            }
        }
    }
