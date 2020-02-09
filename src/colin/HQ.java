package colin;

import battlecode.common.*;
import colin.Navigation;
import colin.Shooter;
import colin.Util;
import colin.Communications;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

class MessageWaiting {
    int[] message;
    int price;

    MessageWaiting(int[] message, int price) {
        this.message = message;
        this.price = price;
    }

}

public class HQ extends Shooter {
    int numMiners = 0;
    int maxMiners = 6;
    ArrayList<MapLocation> refineries = new ArrayList<>();
    ArrayList<Integer> mainLandscapers = new ArrayList<>();
    ArrayList<Integer> secondaryLandscapers = new ArrayList<>();

    Queue<MessageWaiting> q = new LinkedList<>();

    public HQ(RobotController r) throws GameActionException {
        super(r);
        comms.sendHqLoc(rc.getLocation());
    }

    public void takeTurn() throws GameActionException {
        super.takeTurn();
        System.out.println("Location: "+rc.getLocation());
        System.out.println("Soup: "+rc.getTeamSoup()+"soup c:"+rc.getSoupCarrying());

        //Print Landscapers
        //printLandscapers();

        /*
        Submit transactions that may be on the Queue
         */
        if(!q.isEmpty()){
            System.out.println("Size: "+q.size());
            MessageWaiting newTransaction;
            while(!q.isEmpty()){
                newTransaction = q.peek();
                if(rc.getTeamSoup()>newTransaction.price){
                    MessageWaiting mes = q.remove();
                    rc.submitTransaction(mes.message, mes.price);
                }
            }
        }

        /*
        If its not the first round then get the block
        and deal with the messages in the block
         */
        if(rc.getRoundNum()>1){
            dealWithBlockchainMessages();
        }

        if(rc.getTeamSoup()>25 && numMiners<maxMiners){
            createMiner();
        }
    }

    private void createMiner() throws GameActionException {
        boolean minerPlaced = false;
        MapLocation[] soupLocations = rc.senseNearbySoup();

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

    private void dealWithBlockchainMessages() throws GameActionException {
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
                        MapLocation newRefinery = new MapLocation(message[2], message[3]);
                        if(refineries.contains(newRefinery)){
                            System.out.println("Already have refinery");
                        }
                        else{
                            System.out.println("Refinery Location: x:"+ message[2]+" y:"+message[3]);
                            refineries.add(newRefinery);
                        }
                        int[] mes = {comms.teamId, 1, newRefinery.x, newRefinery.y, 0, 0, 0};
                        if(rc.getTeamSoup()>5){
                            rc.submitTransaction(mes, 3);
                        }
                        else {
                            System.out.println("Adding to Q");
                            MessageWaiting messageWaiting = new MessageWaiting(mes, 1);
                            q.add(messageWaiting);
                        }
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
                        if(rc.getTeamSoup()>7){
                            rc.submitTransaction(trans, 3);
                        }else{
                            System.out.println("adding to Q");
                            MessageWaiting messageWaiting = new MessageWaiting(trans, 3);
                            q.add(messageWaiting);
                        }
                        break;
                    case 6:
                        System.out.println("Landscaper Message");
                        for(int i=0; i<message.length; i++){
                            System.out.println(" "+message[i]);
                        }
                        if(mainLandscapers.size()<4){
                            //add to the main landscapers
                            System.out.println("adding main");
                            mainLandscapers.add(message[4]);
                            //transmit its role
                            int[] roleMessage = {comms.teamId, 7, message[4], 0, 0, 0, 0};
                            //make sure you have enough soup
                            if(rc.getTeamSoup()>5){
                                rc.submitTransaction(roleMessage, 2);
                            }
                            else{
                                //add to the Queue if not enough soup
                                System.out.println("adding to Q");
                                MessageWaiting messageWaiting = new MessageWaiting(roleMessage, 3);
                                q.add(messageWaiting);
                            }
                        }
                        else{
                            System.out.println("adding second");
                            secondaryLandscapers.add(message[4]);
                            //transmit its role
                            int[] roleMessage = {comms.teamId, 7, message[4], 1, 0, 0, 0};
                            //make sure you have enough soup
                            if(rc.getTeamSoup()>5){
                                rc.submitTransaction(roleMessage, 2);
                            }
                            else{
                                //add to the Queue if not enough soup
                                System.out.println("adding to Q");
                                MessageWaiting messageWaiting = new MessageWaiting(roleMessage, 3);
                                q.add(messageWaiting);
                            }
                        }
                        break;
                }
            }
        }
    }

    public void printLandscapers() {
        System.out.println("Main Landscaper IDs: ");
        for(int id : mainLandscapers){
            System.out.println(" "+id);
        }

        System.out.println("Secondary Landscaper IDs: ");
        for(int id : secondaryLandscapers){
            System.out.println(" "+id);
        }
    }
}