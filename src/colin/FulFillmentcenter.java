package colin;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class FulFillmentcenter extends Building {
    public FulFillmentcenter(RobotController r) {
        super(r);
    }

    static int maxdrones = 0;

    public void takeTurn() throws GameActionException {
        super.takeTurn();

        /*
        First turn find all the open directions
        to place a landscaper
         */




            for (Direction dir : Util.directions) {
                int[][] messagesfromblockchain=comms.findTeamMessagesInBlockChain();
                for(int[] m:messagesfromblockchain){
                    if(m[1]==8){
                        maxdrones=m[3];
                    }
                }
                if(maxdrones<3) {
                if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
                    System.out.println("Delivery Drone built" + dir);
                    maxdrones++;
                    int[] message = {comms.teamId, 8, maxdrones, 0, 0, 0, 0};
                    rc.submitTransaction(message, 4);
                }

            }
        }


    }
}
