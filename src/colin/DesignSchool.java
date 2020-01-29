package colin;
import battlecode.common.*;
import colin.Building;
import colin.Util;

import java.util.ArrayList;


public class DesignSchool extends Building {
    public DesignSchool(RobotController r) {
        super(r);
    }

    static int totalnumLandscapers = 0;

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        /*
        First turn find all the open directions
        to place a landscaper
         */
        System.out.println("t soup: "+rc.getTeamSoup()+" soup: "+rc.getSoupCarrying());
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

        System.out.println("num open: "+numOpen);
        for(int i = 0; i<numOpen; i++){
            System.out.println("I: "+i+"\n"+"NumOpenDir: "+numOpen);
            if(rc.getTeamSoup()>150){
                if(tryBuild(RobotType.LANDSCAPER, openDirections[i])){
                    //submit transaction for HQ to broadcast its location
                    int[] message = {comms.teamId, 5, 0, 0, 0, 0, 0};
                    rc.submitTransaction(message, 2);
                    System.out.println("built landscaper "+openDirections[i]);
                }
            }
        }
        
    }
}

