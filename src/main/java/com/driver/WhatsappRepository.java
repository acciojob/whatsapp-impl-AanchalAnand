package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;
    

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.customGroupCount = 0;
        this.messageId = 0;
      
    }

    public String createUser(String name, String mobile) throws Exception {
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
        else {
            User user=new User(name,mobile);
            return "Success";
        }
    }

    // The list contains at least 2 users where the first user is the admin. A group has exactly one admin.
    // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
    // If there are 2+ users, the name of group should be "Group count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
    // Note that a personal chat is not considered a group and the count is not updated for personal chats.
    // If group is successfully created, return group.

    //For example: Consider userList1 = {Alex, Bob, Charlie}, userList2 = {Dan, Evan}, userList3 = {Felix, Graham, Hugh}.
    //If createGroup is called for these userLists in the same order, their group names would be "Group 1", "Evan", and "Group 2" respectively.

    public Group createGroup(List<User> users){

        if(users.size()==2){
            Group group1=new Group();
            group1.setName(users.get(1).getName());
            group1.setNumberOfParticipants(users.size());
            adminMap.put(group1,users.get(0));
            return group1;
        }
        else{
            Group group2=new Group();

            group2.setName("Group"+groupUserMap.size()+1);
            group2.setNumberOfParticipants(users.size());
            groupUserMap.put(group2,users);
            adminMap.put(group2,users.get(0));
            return group2;
        }
     }

    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        Message message=new Message();
        message.setId(this.messageId++);
        message.setContent(content);
        message.setTimestamp(new Date());
        return this.messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.

        if(groupUserMap.containsKey(group)){
            List<User> users=groupUserMap.get(group);
            for(User u:users){
                if(Objects.equals(u.getName(), sender.getName()) && Objects.equals(u.getMobile(), sender.getMobile())){
                    List<Message> messeges = new ArrayList<>();
                    if(!groupMessageMap.containsKey(message)) {
                        messeges.add(message);
                        groupMessageMap.put(group,messeges);
                    }
                    else{
                        messeges=groupMessageMap.get(group);
                        messeges.add(message);
                        groupMessageMap.put(group,messeges);
                    }
                    return messeges.size();
                }
                else
                throw new Exception("You are not allowed to send message");

            }
        }

        throw new Exception("Group does not exist");

    }

    //Throw "Group does not exist" if the mentioned group does not exist
    //Throw "Approver does not have rights" if the approver is not the current admin of the group
    //Throw "User is not a participant" if the user is not a part of the group
    //Change the admin of the group to "user" and return "SUCCESS". Note that at one time there is only one admin and the admin rights are transferred from approver to user.
    public String changeAdmin(User approver, User user, Group group) throws Exception{
        if(groupUserMap.containsKey(group)){
            if(adminMap.containsValue(approver)){
                List<User> users=groupUserMap.get(group);
                for(User u:users){
                    if(u==user){
                        adminMap.put(group,user);
                        return "SUCCESS";
                    }

                }
               throw new Exception("User is not a participant");
             }
            throw new Exception("Approver does not have rights");
          }
         throw new Exception("Group does not exist");
    }

    //This is a bonus problem and does not contains any marks
    //A user belongs to exactly one group
    //If user is not found in any group, throw "User not found" exception
    //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
    //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
    //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
    public int removeUser(User user) throws Exception{
    boolean userfound=false;
    Group usergroup=null;
    for(Group g:groupUserMap.keySet()) {
        List<User> participants = groupUserMap.get(g);
        for (User u : participants) {
            if (u == user) {
                if (adminMap.get(g).equals(user)) {
                    throw new Exception("Cannot remove admin");
                }
                userfound = true;
                usergroup = g;
                break;
            }
            if (userfound == true) break;
        }
        //If user is not the admin, remove the user from the group, remove all its messages
        // from all the databases, and update relevant attributes accordingly.

        if (userfound == true) {
            List<User> users = groupUserMap.get(usergroup);
            List<User> updatedusers = new ArrayList<>();
            for (User u : users) {
                if (u.equals(user)) continue;
                updatedusers.add(u);
            }
            groupUserMap.put(usergroup, updatedusers);

            List<Message> messages = groupMessageMap.get(usergroup);
            List<Message> updatedmessages = new ArrayList<>();
            for (Message mg : messages) {
                if (senderMap.get(mg).equals(user)) continue;
                updatedmessages.add(mg);
            }
            groupMessageMap.put(usergroup, updatedmessages);


            //If user is removed successfully, return (the updated number of users in the group + the updated
            // number of messages in group + the updated number of overall messages)

            HashMap<Message, User> updatedsendermap = new HashMap<>();
            for (Message m : senderMap.keySet()) {
                if (senderMap.get(m).equals(user)) continue;
                updatedsendermap.put(m, senderMap.get(m));
            }
            senderMap = updatedsendermap;
            return updatedusers.size()+updatedmessages.size()+updatedmessages.size();
        }
    }
        throw new Exception("User not found");
    }
}
