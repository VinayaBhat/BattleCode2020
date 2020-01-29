package colin;

import battlecode.common.*;
import colin.Navigation;
import colin.Shooter;
import colin.Util;
import colin.Communications;
import java.util.ArrayList;

public class HQ extends Shooter {
    static int numMiners = 0;
    static int maxMiners = 5;
    static int[][] refineries = {
            {-1,-1},
            {-1,-1},
            {-1,-1},
            {-1,-1},
            {-1,-1}
    };
    static int numRefineries = 0;
    static int maxRefineries = 2;

    public HQ(RobotController r) throws GameActionException {
        super(r);
        comms.sendHqLoc(rc.getLocation());
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        System.out.println("Soup: "+rc.getTeamSoup());
        MapLocation[] soupLocations = rc.senseNearbySoup();
        /*
        If its not the first round then get the block
        and deal with the messages in the block
         */
        if(rc.getRoundNum()>0){
            int[][] teamMessages = comms.findTeamMessagesInBlockChain();

            //loop through messages from our team
            for(int[] message : teamMessages){
                //messages is initialized with all -1
                //so if there is a -1 it is the end
                if(message[0]==-1){
                    System.out.println("end of messages");
                    break;
                }else{
                    switch(message[1]){
                        case 0:
                            //message about the HQ location
                            //don't need to do anything
                            break;
                        case 1:
                            //message about the refinery location
                            refineries[numRefineries][0] = message[2];
                            refineries[numRefineries][1] = message[3];
                            numRefineries++;
                            System.out.println("Refinery Location: x:"+ message[2]+" y:"+message[3]);
                            break;
                        case 2:
                            //message about soup location
                            System.out.println("Soup Location: x:"+ message[2]+" y:"+message[3]);
                            break;
                        case 3:
                            //Remove soup
                            System.out.println("Removed soup location");
                            break;
                        case 4:
                            //New Design School
                            System.out.println("New Design School");
                            break;
                        case 5:
                            //New Landscaper
                            System.out.println("New Landscaper");
                            //broadcast the HQ location for the landscaper
                            int[] trans = {comms.teamId, 0, rc.getLocation().x, rc.getLocation().y, 0, 0, 0};
                            rc.submitTransaction(trans, 3);
                    }
                }
            }
        }

        if(rc.getTeamSoup()>25 && numMiners<maxMiners){
            boolean minerPlaced = false;
            Direction d = null;
            if(soupLocations.length!=0){
                d = rc.getLocation().directionTo(soupLocations[0]);
            }
            if(d!=null) {
                if (tryBuild(RobotType.MINER, d)) {
                    System.out.println("created miner towards soup");
                    minerPlaced = true;
                    numMiners++;
                }
            }
            if(!minerPlaced) {
                //no miner placed in preferred direction
                //place randomly
                for (int i = 0; i < 7; i++) {
                    if (tryBuild(RobotType.MINER, Util.directions[i])) {
                        System.out.println("created miner randomly");
                        numMiners++;
                        break;
                    }
                }
            }
        }


    }
}