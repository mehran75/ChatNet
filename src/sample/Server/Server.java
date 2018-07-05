package sample.Server;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {

    static LinkedList<ServerThread> socketsList;
    private static Connection connection;
    Thread thread;
    ServerSocket serverSocket;
    int ClientCount = 0;
    static LinkedList<Person> PeopleGraph;
    static LinkedList<Group> Groups;


    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
            socketsList = new LinkedList<>();
            System.out.println("server started on port: " + port);
            System.out.println(serverSocket.getInetAddress().getHostAddress());

            start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stop();
            thread = null;
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (thread != null) {
            System.out.println("Waiting for client...");

            try {
                Socket socket = serverSocket.accept();
                DataOutputStream data = new DataOutputStream(socket.getOutputStream());
                String info = new DataInputStream(socket.getInputStream()).readUTF();


                String[] tmp = info.split("#");

                System.out.println(info);

                if (tmp[0].equals("SIGN IN")) {  //............................... Login request ........................

                    String name = tmp[1].split("&")[0];
                    String number = tmp[1].split("&")[1];
                    Person p = new Person(name);
                    p.addNumber(number);


                    if (PeopleGraph.contains(p)) {
                        data.writeUTF("#### Access Granted ####");
                        data.flush();
                        System.out.println("#### Access Granted ####");
                        addThread(socket, name, number);


                    } else {
                        data.writeUTF("#### Access Denied ####");
                        System.out.println("#### Access Denied ####");
                    }

                } else if (tmp[0].equals("SIGN UP")) { // ...........................SignUp Request ..........................

                    String name = tmp[1].split("&")[0];
                    String number = tmp[1].split("&")[1];
                    Person p = new Person(name);
                    p.addNumber(number);


                    if (!PeopleGraph.contains(p)) {

                        try {
                            PreparedStatement statement = connection.prepareStatement(
                                    "INSERT INTO people_table (name, numbers) VALUES (?,?)");

                            statement.setString(1, name);
                            statement.setString(2, number);
                            statement.execute();
                            PeopleGraph.addFirst(p);
                            data.writeUTF("#### User now available ####");


                        } catch (SQLException e) {
                            e.printStackTrace();
                        }

                    } else {
                        data.writeUTF("#### You already signed up! ####");

                    }
                    data.flush();

                }


            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                stop();
            }
        }
    }

    public void addThread(Socket socket, String name, String number) {
        ServerThread thread = new ServerThread(this, socket);
        thread.name = name;
        thread.number = number;
        socketsList.addFirst(thread);

        try {
            thread.open();
            thread.start();
            ClientCount++;

            Person person = searchPeople(name);


            if (person != null) {

                thread.handle("FRIENDS LIST#" + person.friendListToString());
                thread.handle("GROUPS LIST#" + (getGroupsOfPerson(person)).toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public ServerThread findClient(String name, String number) {

        for (ServerThread thread : socketsList) {
            if (thread.name.equals(name) && thread.number.equals(number)) {
                return thread;
            }
        }
        System.out.println("Not found");


        return null;
    }

    public ServerThread findClient(String name) {
        for (ServerThread thread : socketsList) {
            if (thread.name.equals(name)) {
                return thread;
            }
        }
        System.out.println("Not found");


        return null;
    }

    public synchronized void remove(String name, String number) {
        ServerThread thread = findClient(name, number);

        if (socketsList.size() > 0) {
            socketsList.remove(thread);

            ClientCount--;
            thread.close();
        }

    }

    public static Person searchPeople(String name) {

        for (int i = 0; i < PeopleGraph.size(); i++) {
            if (PeopleGraph.get(i).getName().equals(name))
                return PeopleGraph.get(i);

        }
        return null;


    }

    void search(Person start, Person end) {


        Stack<Person> stack = new Stack<>();


        if (start.getFriends().getFirst() != null) {
            stack.push(start.getFriends().getFirst());
        } else {
            System.out.println("no way");
        }

        while (!stack.isEmpty()) {
            Person p = stack.peek();
            System.out.println(p.getName());

            if (p.equals(end))
                break;

            Person friend;

            try {
                friend = p.getFriends().getFirst();
            } catch (Exception e){
                stack.pop();
                continue;
            }

            if (friend != null) {
                if (!stack.contains(friend) && !stack.excludeContains(friend)) {
                    stack.push(friend);
                } else {
                    stack.exclude(friend);
                    stack.pop();
                }
            } else {

                stack.exclude(stack.pop());
            }
        }
        if (stack.isEmpty())
            System.out.println("No way");
        else
            System.out.println(stack.toString());
    }

    public static LinkedList<Person> suggestion(Person person, int level) {

        Queue<Person> queue = new Queue<>();

        for (Person p : person.getFriends()) {
            queue.enqueue(p);
        }

        int counter = 0;

        LinkedList<Person> list = new LinkedList<>();


        while (counter < level && !queue.isEmpty()) {


            while (!queue.isEmpty()) {

                Person p = queue.dequeue();

                for (Person tmp : p.getFriends()) {
                    if (!tmp.equals(person) && !list.contains(tmp)) {
                        list.addFirst(tmp);
                        queue.enqueue(tmp);
                    }

                }
            }
            counter++;


        }

        for (Person p : person.getFriends()) {
            if (list.contains(p))
                list.remove(p);
        }

        return list;

    }

    public LinkedList<Group> getGroupsOfPerson(Person person) {
        LinkedList<Group> res = new LinkedList<>();

        for (Group g : Groups)
            if (g.getUsers().contains(person))
                res.addFirst(g);


        return res;
    }

    public Group findGroup(String groupName) {
        for (Group g : Groups) {
            if (g.getName().equals(groupName))
                return g;
        }
        return null;
    }

//    ---------------------------------- Modify Database ------------------------------

    //------------------------------------chatDB table--------------------------

    public String getHistory(String name1, String number1, String name2, String number2) {
        String result = "";

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM chatDB WHERE user1 = ? and number1 = ? and user2 = ? and number2 = ?");

            statement.setString(1, name1);
            statement.setString(2, number1);
            statement.setString(3, name2);
            statement.setString(4, number2);

            ResultSet set = statement.executeQuery();
            set.next();
            result = set.getString("PM");

        } catch (SQLException e) {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM chatDB WHERE user1 = ?1 and number1 = ?2 and user2 = ?3 and number2 = ?4");

                statement.setString(3, name1);
                statement.setString(4, number1);
                statement.setString(1, name2);
                statement.setString(2, number2);

                ResultSet set = statement.executeQuery();
                set.next();
                result = set.getString("PM");

            } catch (SQLException ignored) {

            }
        }
        if (result.equals("null"))
            return "";


        return result;
    }

    public void updateHistory(String name1, String number1, String name2, String number2, String pm) {

        String query = "";
        String query2 = "";
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM chatDB WHERE user1 = ?1 and number1 = ?2 and user2 = ?3 and number2 = ?4");

            statement.setString(1, name1);
            statement.setString(2, number1);
            statement.setString(3, name2);
            statement.setString(4, number2);

            ResultSet set = statement.executeQuery();
            set.next();
            query = set.getString("PM");

        } catch (SQLException e) {
            try {
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM chatDB WHERE user1 = ?1 and number1 = ?2 and user2 = ?3 and number2 = ?4");

                statement.setString(3, name1);
                statement.setString(4, number1);
                statement.setString(1, name2);
                statement.setString(2, number2);

                ResultSet set = statement.executeQuery();
                set.next();
                query2 = set.getString("PM");

            } catch (SQLException ignored) {
            }
        }

        if (query.equals("") && query2.equals("")) {
            try {

                System.out.println("insert");

                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO chatDB (user1, number1, user2, number2, PM) VALUES (?,?,?,?,?)");
                statement.setString(1, name1);
                statement.setString(2, number1);
                statement.setString(3, name2);
                statement.setString(4, number2);
                statement.setString(5, pm);

                statement.execute();


            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if (!query.equals("")) {
            try {
                System.out.println("update1");
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE chatDB SET PM = ? WHERE user1 = ? and number1 = ? and user2 = ? and number2 = ?");

                statement.setString(1, query + "," + pm);
                statement.setString(2, name1);
                statement.setString(3, number1);
                statement.setString(4, name2);
                statement.setString(5, number2);

                statement.execute();


            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (!query2.equals("")) {
            try {
                System.out.println("up2");
                PreparedStatement statement = connection.prepareStatement(
                        "UPDATE chatDB SET PM = ? WHERE (user1 = ? and number1 = ? and user2 = ? and number2 = ?)");

                statement.setString(1, query2 + "," + pm);
                statement.setString(4, name1);
                statement.setString(5, number1);
                statement.setString(2, name2);
                statement.setString(3, number2);

                statement.execute();


            } catch (SQLException ignored) {
                ignored.printStackTrace();
            }
        }


    }

    //------------------------------------people_table table--------------------
    public void addNumber(String name, String number) {

        Person person = searchPeople(name);

        String old_number = "";

        try {

            PreparedStatement statement0 = connection.prepareStatement("SELECT * FROM people_table WHERE name = ?");
            statement0.setString(1, person.getName());
            ResultSet set = statement0.executeQuery();
            set.next();
            old_number = set.getString("numbers");


            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE people_table SET numbers = ? WHERE name = ?");

            statement.setString(1, old_number + "," + number);
            statement.setString(2, person.getName());

            statement.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateFriend(String user1, String user2) {

        try {
//            ------------------------------get friends-------------------------------
            PreparedStatement queryStatement1 = connection.prepareStatement("SELECT * FROM people_table WHERE name = ?");
            PreparedStatement queryStatement2 = connection.prepareStatement("SELECT * FROM people_table WHERE name = ?");

            queryStatement1.setString(1, user1);
            queryStatement2.setString(1, user2);

            String friends1 = queryStatement1.executeQuery().getString("friends");
            String friends2 = queryStatement2.executeQuery().getString("friends");

//            ------------------------------update friends---------------------------

            PreparedStatement updateStatement1 = connection.prepareStatement("UPDATE people_table SET friends = ? WHERE name = ?");
            PreparedStatement updateStatement2 = connection.prepareStatement("UPDATE people_table SET friends = ? WHERE name = ?");

            if (!friends1.equals(""))
                updateStatement1.setString(1, friends1 + "," + user2);
            else
                updateStatement1.setString(1, user2);

            if (!friends2.equals(""))
                updateStatement2.setString(1, friends2 + "," + user1);
            else
                updateStatement2.setString(1, user1);

            updateStatement1.execute();
            updateStatement2.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    //------------------------------------Groups table---------------------------
    public void addGroup(String groupName, String users) {
        try {

            PreparedStatement statement = connection.prepareStatement("INSERT INTO Groups (name, users) VALUES (?,?)");

            statement.setString(1, groupName);
            statement.setString(2, users);

            statement.execute();

            LinkedList<Person> list = new LinkedList<>();
            String[] usersName = users.split("&");

            for (String s : usersName) {
                Person person = searchPeople(s.split(" ")[0]);

                list.addFirst(person);
            }

            Groups.addFirst(new Group(groupName, list));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void updateGroupMassage(Group group, String massage) {


        String groupName = group.getName();

        group.updateMassages(massage);
        System.out.println(group.getMassages());

        try {


            PreparedStatement statement = connection.prepareStatement("UPDATE Groups SET massages = ? WHERE name = ?");


            statement.setString(1, group.getMassages());
            statement.setString(2, groupName);

            statement.execute();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    String getGroupMassages(String groupName, String users) {
        try {
            PreparedStatement queryStatement = connection.prepareStatement("SELECT * FROM Groups WHERE name = ?");
            queryStatement.setString(1, groupName);
//            queryStatement.setString(2, users);
            ResultSet set = queryStatement.executeQuery();
            set.next();

            return set.getString("massages");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

//    --------------------------------- Modify Database ----------------------------------

    public static void main(String[] args) {


        PeopleGraph = new LinkedList<>();
        Groups = new LinkedList<>();

// ---------------------------------get people from Database and create graph-----------------------------------
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:"+ Server.class.getResource("/src/chat.sqlite"));
            System.out.println("DB is connected");
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM people_table");

            ResultSet result = statement.executeQuery();
            LinkedList<String> friends = new LinkedList<>();

            while (result.next()) {

                Person person = new Person(result.getString("name"));

                String number = result.getString("numbers");
                if (number.contains(",")) {
                    String[] numbers = number.split(",");
                    for (String s : numbers) {
                        person.addNumber(s);
                    }
                } else {
                    person.addNumber(number);
                }
                String tmp = result.getString("friends");

                if (tmp != null && !tmp.equals("null"))
                    friends.addFirst(tmp);


                PeopleGraph.addFirst(person);

            }

//            friends = friends.reverse();
            int counter = 0;

            while (!friends.isEmpty()) {
                String friendsName = friends.get(0);

                if (!friendsName.equals("")) {
                    if (friendsName.contains(",")) {
                        String[] fr = friendsName.split(",");
                        for (String f : fr) {

                            Person p = searchPeople(f);

                            PeopleGraph.get(counter).addFriend(p);
                        }
                    } else {
                        Person p = searchPeople(friendsName);

                        PeopleGraph.get(counter).addFriend(p);
                    }
                }

                friends.removeFirst();

                counter++;
            }

//--------------------------------------add groups in list----------------------------------------------------
            PreparedStatement statement1 = connection.prepareStatement("SELECT * FROM Groups");

            ResultSet resultSet = statement1.executeQuery();

            while (resultSet.next()) {

                String name = resultSet.getString("name");
                String tmp = resultSet.getString("users");
                String massages = resultSet.getString("massages");


                String[] users = tmp.split("&");

                LinkedList<Person> persons = new LinkedList<>();

                for (String s : users) {
                    Person person = searchPeople(s.split(" ")[0]);
                    persons.addFirst(person);

                }

                Group group = new Group(name, persons);
                group.setMassages(massages);

                Groups.addFirst(group);

            }


        } catch (SQLException e) {
            e.printStackTrace();
        }


//        -----------------------------------start server----------------------------
        new Server(1997);
//        launch(args);


    }


}
