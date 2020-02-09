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



        int[][] messagesfromblockchain=comms.findTeamMessagesInBlockChain();
        for(int[] m:messagesfromblockchain){
            if(m[1]==9){
                maxdrones=m[2];
            }
        }

        if(maxdrones<4) {
            for(Direction dir:Util.directions) {
                if (rc.canBuildRobot(RobotType.DELIVERY_DRONE, dir)) {
                    rc.buildRobot(RobotType.DELIVERY_DRONE, dir);
                    System.out.println("Delivery Drone built" + dir);
                    maxdrones++;
                    int[] message = {comms.teamId, 9, maxdrones, 0, 0, 0, 0};
                    rc.submitTransaction(message, 4);
                    break;
                }
            }

        }


    }
}
