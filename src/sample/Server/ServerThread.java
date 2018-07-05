package sample.Server;


import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class ServerThread extends Thread {
    private Socket socket;
    private DataInputStream DIS;
    private DataOutputStream DOS;
    private Server server;
    String number = "";
    String name = "";
//    String password = "";

    public ServerThread(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

    }

    public void run() {
        while (true) {
            try {

                String data = DIS.readUTF();

                System.out.println(data);

                String[] tmp = data.split("#");

                if (tmp[0].equals("CHAT")) {              //-------------------------handle chat-----------------------
                    String[] sender = tmp[1].split("&");
                    String[] receiver = tmp[3].split("&");
                    String massage = tmp[2];

                    ServerThread thread = server.findClient(receiver[0], receiver[1]);

                    DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    Date date = new Date();

                    server.updateHistory(sender[0], sender[1],
                            receiver[0], receiver[1],
                            df.format(date) + "&" + sender[0] + "&" + massage);


                    if (thread != null) {
                        thread.handle("CHAT RESULT#" + sender[0] + "&" + sender[1] + "#" + massage);
                    } else {
                        handle("CHAT RESULT#" + "OFFLINE");
                    }


                } else if (tmp[0].equals("SEARCH")) {       //-------------------------handle search-----------------------
                    if (!tmp[1].equals(name)) {
                        Person p = Server.searchPeople(tmp[1]);

//                        server.search(Server.searchPeople(name), p);
                        if (p.getFriends().contains(Server.searchPeople(name))){
                            System.out.println("he/she is a friend");
                        } else {
                            System.out.println("not a friend");
                        }
                        if (p != null) {
                            handle("SEARCH RESULT#" + tmp[1]);
                        } else {
                            handle("SEARCH RESULT#" + "NOT FOUND");
                        }
                    }

                } else if (tmp[0].equals("SUGGESTION")) {     //-------------------------handle suggestions-------------------

                    String[] details = tmp[1].split("&");
                    handle("SUGGESTION RESULT#" +
                            Server.suggestion(Server.searchPeople(details[0]), Integer.parseInt(details[1])).toString());

                } else if (tmp[0].equals("HISTORY")) {       //------------------------History from database------------------

                    String[] contact1 = tmp[1].split("&");
                    String[] contact2 = tmp[2].split("&");

                    String massages = server.getHistory(contact1[0], contact1[1], contact2[0], contact2[1]);

                    handle("HISTORY RESULT#" + massages);

                } else if (tmp[0].equals("ADD NUMBER")) {        //----------------------handle add new number----------------

                    String[] details = tmp[1].split("&");

                    server.addNumber(details[0], details[1]);
                    Server.searchPeople(details[0]).addNumber(details[1]);


                } else if (tmp[0].equals("FRIEND REQUEST")) {        //-------------------------send friend request-----------------------

                    String sender = tmp[1].split("&")[0];
                    String receiver = tmp[1].split("&")[1];

                    Person person = Server.searchPeople(receiver);

                    if (person != null) {

                        ServerThread t = server.findClient(person.getName());
                        // save requests in database
                        if (t != null) {
                            t.handle("FRIEND REQUEST#" + sender);
                        }

                    }

                } else if (tmp[0].equals("FRIEND REQUEST RESULT")) {        //-------------------------handle friend request-----------------------

                    String user1 = tmp[1].split("&")[0]; // sender
                    String user2 = tmp[1].split("&")[1]; // receiver

                    Person p1 = Server.searchPeople(user1);
                    Person p2 = Server.searchPeople(user2);

                    p1.addFriend(p2);

                    server.updateFriend(user1, user2); // update friends in database

                    ServerThread th1 = server.findClient(user1);
                    ServerThread th2 = server.findClient(user2);

                    th1.handle("FRIEND REQUEST RESULT#" + p1.friendListToString());
                    th2.handle("FRIEND REQUEST RESULT#" + p2.friendListToString());

                } else if (tmp[0].equals("CLOSE")) {        //-------------------------close thread-----------------------

                    server.remove(name, number);
                    break;

                } else if (tmp[0].equals("SENDING FILE")) {
//                    -----------------save file--------------------
                    String[] receiver = tmp[1].split("&");
                    File dir = new File("E:/Programing training/Java/serverApp/src/sample/temp files/"
                            + tmp[1] + "#" + name + "&" + number);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }

                    if (dir.exists()) {
                        try {

                            FileOutputStream fos =
                                    new FileOutputStream("E:/Programing training/Java/serverApp/src/sample/temp files/"
                                            + tmp[1] + "#" + name + "&" + number + "/" + tmp[2]);


                            int filesize = Math.toIntExact(Long.parseLong(tmp[3])); // Send file size in separate msg
                            byte[] buffer = new byte[filesize];
                            int read = 0;

                            int remaining = filesize;
                            while ((read = DIS.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                                remaining -= read;
                                fos.write(buffer, 0, read);
                            }

                            fos.close();


                            ServerThread st = server.findClient(receiver[0], receiver[1]);

                            if (st != null) {

                                File file = new File("E:/Programing training/Java/serverApp/src/sample/temp files/"
                                        + tmp[1] + "#" + name + "&" + number + "/" + tmp[2]);

                                FileInputStream fileInputStream =
                                        new FileInputStream(file);

                                st.handle("SENDING FILE#" + name + "&" + number + "#" + file.getName() + "#" + file.length());
                                byte[] buffer1 = new byte[Math.toIntExact(file.length())];


                                while (fileInputStream.read(buffer1) > 0)
                                    st.handle(buffer1);
                            }
                        } catch (IOException ignored){}
                    }


                } else if (tmp[0].equals("CREATE GROUP")) {

                    String groupName = tmp[1];
                    String users = tmp[2];

                    server.addGroup(groupName, users);

                    handle("GROUPS LIST#" + server.getGroupsOfPerson(Server.searchPeople(name)).toString());

                    String[] usersArray = users.split("&");

                    for (String s : usersArray) {
                        String temp = s.split(" ")[0];
                        if (!temp.equals(name)) {
                            server.findClient(temp).handle("GROUPS LIST#" + server.getGroupsOfPerson(Server.searchPeople(name)).toString());
                        }

                    }


                } else if (tmp[0].equals("GROUP HISTORY")) {

                    handle("GROUP HISTORY RESULT#" + server.getGroupMassages(tmp[1], ""));

                } else if (tmp[0].equals("GROUP CHAT")) {


                    Group group = server.findGroup(tmp[3]);


                    DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    Date date = new Date();

                    for (Person p : group.getUsers()) {
                        if (!p.getName().equals(name)) {
                            ServerThread t = server.findClient(p.getName());


                            if (t != null) {
                                t.handle("GROUP CHAT RESULT#" + tmp[1] + "#" + tmp[2].split("&")[0] + "#" + tmp[3]);
                            } else {
                                handle("GROUP CHAT RESULT#OFFLINE");
                            }
                        }
                    }

                    server.updateGroupMassage(group, df.format(date) + "&" + tmp[1] + "&" + tmp[2]);


                } else {        //-------------------------wrong request-----------------------
                    System.out.println("WRONG REQUEST!");
                }


            } catch (IOException e) {
                e.printStackTrace();
                close();
                stop();
            }
        }

    }

    public void open() throws IOException {

        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        DIS = new DataInputStream(inputStream);
        DOS = new DataOutputStream(outputStream);
    }

    void close() {
        try {
            DIS.close();
            DOS.close();
            socket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    public void handle(String data) {
        try {

            DOS.writeUTF(data);
            DOS.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle(byte[] bytes) {
        try {

            DOS.write(bytes);
            DOS.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

