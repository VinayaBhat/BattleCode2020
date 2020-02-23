package team10pdx;

import battlecode.common.*;

public class Communications {
    RobotController rc;


    // state related only to communications should go here

    // all messages from our team should start with this so we can tell them apart
    static int teamId = 0;
    // the second entry in every message tells us what kind of message it is. e.g. 0 means it contains the HQ location
    public static final String[] messageType = {
            "HQ loc",
            "Refinery Location",
            "Soup location",
            "Remove Soup Location",
            "New Design School",
            "New Landscaper",
            "Landscaper Message",
            "Landscaper Role Message",
            "Fulfllment Center",
            "Drones",
            "Water",
            "Enemy HQ"
    };

    public Communications(RobotController r) {
        rc = r;
        if(rc.getTeam()==Team.A){
            teamId = 99999;
        }
        else{
            teamId = 88888;
        }
    }

    public boolean sendHqLoc(MapLocation loc) throws GameActionException {
        boolean sent = false;
        int[] message = new int[7];
        message[0] = teamId;
        message[1] = 0;
        message[2] = loc.x; // x coord of HQ
        message[3] = loc.y; // y coord of HQ
        if (rc.canSubmitTransaction(message, 3)) {
            sent=true;
            rc.submitTransaction(message, 3);
        }
        return sent;
    }

    public boolean broadcastRefineryLocation(MapLocation location) throws GameActionException {
        boolean sent=false;
        int[] refineryLocationTransaction = {teamId, 1, location.x, location.y, 0, 0, 0};
        if(rc.canSubmitTransaction(refineryLocationTransaction, 2)){
            sent=true;
            rc.submitTransaction(refineryLocationTransaction, 2);
        }
        return sent;
    }

    public boolean broadcastDesignSchoolLocation(MapLocation location) throws GameActionException {
        boolean sent=false;
        int[] message = {teamId, 4, location.x, location.y, 0,0,0};
        if(rc.canSubmitTransaction(message, 2)){
            sent=true;
            rc.submitTransaction(message, 2);
        }
        return sent;
    }


    public boolean broadcastFulfillmentCenterLocation(MapLocation loc) throws GameActionException {
        boolean sent=false;
        int[] message = {
                teamId,
                8,
                loc.x,
                loc.y,
                0,
                0,
                0 };
        if(rc.canSubmitTransaction(message, 2)){
            sent=true;
            rc.submitTransaction(message, 2);
        }
        return sent;
    }

    public MapLocation getHqLocFromBlockchain() throws GameActionException {
        for (int i = 1; i < rc.getRoundNum(); i++) {
            for (Transaction tx : rc.getBlock(i)) {
                int[] mess = tx.getMessage();
                if (mess[0] == teamId && mess[1] == 0) {
                    System.out.println("found the HQ!");
                    return new MapLocation(mess[2], mess[3]);
                }
            }
        }
        return null;
    }

    public boolean[] broadcastedCreation = new boolean[4];

    public boolean broadcastCreation(MapLocation loc, int cue) throws GameActionException {
        if (broadcastedCreation[cue]) return false; // don't re-broadcast
        {
            int[] message = new int[7];
            message[0] = teamId;
            message[1] = cue;
            message[2] = loc.x; // x coord of HQ
            message[3] = loc.y; // y coord of HQ
            if (rc.canSubmitTransaction(message, 1)) {
                rc.submitTransaction(message, 1);
                broadcastedCreation[cue] = true;
            }
        }
        return broadcastedCreation[cue];
    }

//    // check the latest block for unit creation messages
//    public int getnewBuildingCount(int cue) throws GameActionException {
//        int count = 0;
//        for (Transaction tx : rc.getBlock(rc.getRoundNum() - 1)) {
//            int[] mess = tx.getMessage();
//            if (mess[0] == teamId && mess[1] == cue) {
//                count += 1;
//            }
//        }
//        return count;
//    }

    public boolean broadcastLandscaperRole(int role) throws GameActionException {
        boolean sent=false;
        int[] roleMessage = {teamId, 7, role, 0, 0, 0, 0};
        if(rc.canSubmitTransaction(roleMessage, 1)){
            sent=true;
            rc.submitTransaction(roleMessage, 1);
        }
        return sent;
    }

    public boolean broadcastSoupLocation(MapLocation loc) throws GameActionException {
        boolean sent=false;
        int[] message = new int[7];
        message[0] = teamId;
        message[1] = 2;
        message[2] = loc.x; // x coord of HQ
        message[3] = loc.y; // y coord of HQ
        if (rc.canSubmitTransaction(message, 2)) {
            rc.submitTransaction(message, 2);
            sent=true;
            System.out.println("new soup!" + loc);
        }
        return sent;
    }

    public boolean broadcastWaterLocation(MapLocation loc) throws GameActionException {
        boolean sent=false;
        int[] message = {
                teamId,
                10,
                loc.x,
                loc.y,
                0,
                0,
                0 };
        if(rc.canSubmitTransaction(message, 3)){
            sent=true;
            rc.submitTransaction(message, 3);
        }
        return sent;
    }


    public int[][] findTeamMessagesInBlockChain() throws  GameActionException{
        Transaction[] transactions = rc.getBlock(rc.getRoundNum()-1);
        int[][] allMessages = {
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1},
                {-1,-1,-1,-1,-1,-1,-1}};

        int count = 0;
        System.out.println("Transactions: ");
        for(Transaction transaction : transactions){
            int[] message = transaction.getMessage();
            System.out.println("  "+transaction.getSerializedMessage());
            if(message[0]==teamId){
                allMessages[count] = message;
                count++;
            }
        }
        return allMessages;
    }

    public void broadcastRemoveSoup(MapLocation location, int price) throws GameActionException {
        int[] message = {teamId, 3, location.x, location.y, 0, 0, 0};
        rc.submitTransaction(message, price);
    }





}
