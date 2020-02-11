package player;
import battlecode.common.*;


public class DesignSchool extends Building {
    public DesignSchool(RobotController r) {
        super(r);
    }

    static int totalnumLandscapers = 0;
    static int maxLandscapers = 4;

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        /*
        First turn find all the open directions
        to place a landscaper
         */
        int numOpen = 0;
        Direction[] openDirections = {null, null, null, null, null, null, null, null};

        //find the good building directions
        for(Direction dir : Util.directions){
            if(rc.canBuildRobot(RobotType.LANDSCAPER, dir)){
                System.out.println("Open direction: "+dir);
                openDirections[numOpen] = dir;
                numOpen++;
            }
            else{
                System.out.println("closed: "+dir);
            }
        }

        for(int i = 0; i<numOpen; i++){
            System.out.println("I: "+i+"\n"+"NumOpenDir: "+numOpen);
            if(rc.getTeamSoup()>150 && totalnumLandscapers<maxLandscapers){
                if(tryBuild(RobotType.LANDSCAPER, openDirections[i])){
                    //submit transaction for HQ to broadcast its location
                    totalnumLandscapers++;

                    int[] message = {comms.teamId, 5, totalnumLandscapers, 0, 0, 0, 0};
                    rc.submitTransaction(message, 4);
                    System.out.println("built landscaper "+openDirections[i]);
                }
            }
        }
        
    }
}

