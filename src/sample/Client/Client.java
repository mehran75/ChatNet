package sample.Client;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


public class Client extends Application implements Runnable {

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Thread thread;
    private VBox chatBox;
    private String LogInName = "", LogInNumber = "";
    private ListView<String> friendsListView;
    private ObservableList<String> list;
    private String contact = "";


    private void connect(String name, String number, int code) {
        try {

            socket = new Socket(InetAddress.getLocalHost(), 1997);

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            dataInputStream = new DataInputStream(is);
            dataOutputStream = new DataOutputStream(os);

            String request = (code == 1 ? "SIGN IN" : "SIGN UP");


            handle(request + "#" + name + "&" + number);


            if (code == 1) {

                if (dataInputStream.readUTF().equals("#### Access Granted ####")) {

                    thread = new Thread(this);
                    thread.start();

                    LogInName = name;
                    LogInNumber = number;

                } else {
                    socket = null;
                    dataInputStream = null;
                    dataOutputStream = null;

                    new Alert(javafx.scene.control.Alert.AlertType.ERROR, "this user doesn't exist!").show();

                }
            } else if (code == 2) {
                if (dataInputStream.readUTF().equals("#### User now available ####")) {
                    new Alert(Alert.AlertType.CONFIRMATION, "Done! \n now click on login").show();
                } else {

                    new Alert(Alert.AlertType.ERROR, "you registered before!").show();

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Server doesn't respond!").show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

//        --------------------------Login page-------------------------------------------
        BorderPane layout = new BorderPane();


        TextField username = new TextField();
        username.setPromptText("your Username");
        username.setStyle("-fx-background-color: powderblue;");
        username.setFont(new Font(13));

        TextField number = new TextField();
        number.setPromptText("your number");
        number.setStyle("-fx-background-color: powderblue;");
        number.setFont(new Font(13));

        number.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    number.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        Label label = new Label("Please enter your username and number to sign you in");
        label.autosize();
        label.setTextFill(Color.WHITE);


        Button login = new Button("Sign-in");
        login.setStyle("-fx-background-color: powderblue;");

        Button signUp = new Button("Sign-up");
        signUp.setStyle("-fx-background-color: powderblue;");

        HBox hBox = new HBox(login, signUp);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);

        VBox vBox = new VBox(20, label, username, number, hBox);

        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10, 10, 10, 10));

        layout.setCenter(vBox);
        layout.setBackground(new Background(new BackgroundImage(new Image(getClass().getResourceAsStream("/sample/Client/loginHeader.jpg")),
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));

        primaryStage.setTitle("Sign-in");
        primaryStage.setScene(new Scene(layout, 350, 450));
        primaryStage.getIcons().add(new Image("file:E:/Programing training/Java/serverApp/src/sample/Client/login.png"));

        primaryStage.show();


//       ------------------------------Buttons Action------------------------------
        login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!username.getText().equals("") && !number.getText().equals("")) {
                    connect(username.getText(), number.getText(), 1);

                    if (thread != null) {
                        primaryStage.close();
                    }
                }
            }
        });

        signUp.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!username.getText().equals("") && !number.getText().equals("")) {
                    connect(username.getText(), number.getText(), 2);
                }
            }
        });
//        -------------------------------------Buttons Action------------------------------


//        --------------------------Login page-------------------------------------------


    }

    @Override
    public void run() {
        while (true) {
            try {

                String s = dataInputStream.readUTF();
                System.out.println(s);

                String[] tmp = s.split("#");

                if (tmp[0].equals("CHAT RESULT")) { // -------------------handle users chat-------------

                    if (!tmp[1].equals("OFFLINE")) {

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {

                                String[] senderDetail = tmp[1].split("&");


                                String[] n = friendsListView.getSelectionModel().getSelectedItem().split(" ");

                                if (n[0].equals(senderDetail[0]) && n[1].equals(senderDetail[1])) {
                                    DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                                    Date date = new Date();

                                    Text text = new Text("  " + df.format(date) + " :\"  " + senderDetail[0] + "\" : \n   " + tmp[2]);


                                    HBox hBox = new HBox(10, text);
                                    hBox.setStyle("-fx-background-color: rgba(255,132,77,0.13)");

                                    chatBox.getChildren().add(hBox);


                                } else {

                                    eu.hansolo.enzo.notification.Notification.Notifier.INSTANCE.notifyInfo("new massage", "new massage from "
                                            + senderDetail[0] + " " + senderDetail[1]);

                                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent event) {
                                            eu.hansolo.enzo.notification.Notification.Notifier.INSTANCE.stop();

                                        }
                                    }));

                                    timeline.play();

                                }

                            }
                        });

                        Thread.sleep(100);
                    }

                } else if (tmp[0].equals("HISTORY RESULT")) {
                    try {

                        String[] massages = tmp[1].split(",");

                        String finalResult = "";

                        for (String pm : massages) {
                            String[] details = pm.split("&");

                            String result = "  " + details[0] + " :  ";

                            if (details[1].equals(LogInName)) {
                                result += "\"me\" : \n   ";
                            } else {
                                result += "\"" + details[1] + "\" : \n   ";
                            }
                            result += details[2];

                            finalResult += result + "&";
                        }
                        String finalResult1 = finalResult.substring(0, finalResult.length() - 1);


                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                String[] chats = finalResult1.split("&");

                                for (String pm : chats) {
                                    Text text = new Text(pm);
                                    text.autosize();
                                    text.setWrappingWidth(200);

                                    HBox hBox = new HBox(10, text);
                                    if (pm.contains("\"me\""))
                                        hBox.setStyle("-fx-background-color: rgba(196,0,255,0.13)");
                                    else
                                        hBox.setStyle("-fx-background-color: rgba(255,132,77,0.13)");

                                    chatBox.getChildren().add(hBox);
                                }

                            }
                        });
                        Thread.sleep(100);
                    } catch (ArrayIndexOutOfBoundsException ignored) {
                    }


                } else if (tmp[0].equals("SEARCH RESULT")) { // -------------------handle users search-------------

                    System.out.println(tmp[1]);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            if (!tmp[1].equals("NOT FOUND")) {

                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                                        "Do you want to send friendship request to \"" + tmp[1] + "\"?");
                                alert.setTitle("Friend request");
                                alert.setHeaderText("User found");
                                Optional<ButtonType> optional = alert.showAndWait();

                                if (optional.isPresent() && optional.get().equals(ButtonType.OK))
                                    // request from me to another user
                                    handle("FRIEND REQUEST#" + LogInName + "&" + tmp[1]);

                            } else {
                                new Alert(Alert.AlertType.WARNING, "User doesn't exist!").show();
                            }

                        }
                    });
                    Thread.sleep(100);

                } else if (tmp[0].equals("SUGGESTION RESULT")) { // -------------------handle suggestions-------------
                    try {

                        System.out.println(tmp[1]);
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                showSuggestions(tmp[1]);
                            }
                        });


                    } catch (IndexOutOfBoundsException ignored) {
                    }

                } else if (tmp[0].equals("FRIENDS LIST")) { // -------------------get friends list-------------

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            String[] f;
                            if (tmp.length > 1)
                                f = friendsList(tmp[1]);
                            else
                                f = new String[0];
                            mainPage(new Stage(), f);

                        }
                    });
                    Thread.sleep(400);

                } else if (tmp[0].equals("FRIEND REQUEST")) { // -------------------handle friend request-------------

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "you have request from \""
                                    + tmp[1] + "\".\nDo you Accept?");
                            Optional<ButtonType> optional = alert.showAndWait();
                            if (optional.isPresent() && optional.get().equals(ButtonType.OK)) {
                                handle("FRIEND REQUEST RESULT#" + LogInName + "&" + tmp[1]);
                            }
                        }
                    });

                    Thread.sleep(100);

                } else if (tmp[0].equals("FRIEND REQUEST RESULT")) { // -------------------get friend request result-------------


                    String[] fr = friendsList(tmp[1]);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {

                            for (String x : fr)
                                if (!list.contains(x))
                                    list.add(x);
                        }
                    });
                    Thread.sleep(100);

                } else if (tmp[0].equals("GROUPS LIST")) {
                    try {
                        System.out.println(tmp[1]);
                        String[] names = tmp[1].split(",");

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                for (String g : names) {
                                    list.add("group#" + g);
                                }
                            }
                        });
                        Thread.sleep(100);
                    } catch (Exception ignored) {
                    }

                } else if (tmp[0].equals("GROUP HISTORY RESULT")) {
                    try {
                        if (!tmp[1].equals("null")) {

                            String[] massages = tmp[1].split(",");

                            String finalResult = "";

                            for (String pm : massages) {
                                String[] details = pm.split("&");

                                String result = "  " + details[0] + " :  ";

                                if (details[1].equals(LogInName)) {
                                    result += "\"me\" : \n   ";
                                } else {
                                    result += "\"" + details[1] + "\" : \n   ";
                                }
                                result += details[2];

                                finalResult += result + "&";
                            }
                            String finalResult1 = finalResult.substring(0, finalResult.length() - 1);


                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    String[] chats = finalResult1.split("&");

                                    for (String pm : chats) {
                                        Text text = new Text(pm);
                                        text.autosize();
                                        text.setWrappingWidth(200);

                                        HBox hBox = new HBox(10, text);
                                        if (pm.contains("\"me\""))
                                            hBox.setStyle("-fx-background-color: rgba(196,0,255,0.13)");
                                        else
                                            hBox.setStyle("-fx-background-color: rgba(255,132,77,0.13)");

                                        chatBox.getChildren().add(hBox);
                                    }

                                }
                            });
                            Thread.sleep(100);
                        }
                    } catch (Exception ignored) {
                    }

                } else if (tmp[0].equals("GROUP CHAT RESULT")) {

                    if (!tmp[1].equals("OFFLINE")) {

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {

                                String[] senderDetail = tmp[1].split("&");


                                String n = friendsListView.getSelectionModel().getSelectedItem();

                                if (n.equals("group#" + tmp[3])) {
                                    DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                                    Date date = new Date();

                                    Text text = new Text("  " + df.format(date) + " :\"  " + senderDetail[0] + "\" : \n   " + tmp[2]);


                                    HBox hBox = new HBox(10, text);

                                    hBox.setStyle("-fx-background-color: rgba(255,132,77,0.13)");

                                    chatBox.getChildren().add(hBox);


                                } else {

                                    eu.hansolo.enzo.notification.Notification.Notifier.INSTANCE.notifyInfo("new massage", "new massage from "
                                            + senderDetail[0] + " " + senderDetail[1] + " in group \"" + tmp[3] + "\"");

                                    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent event) {
                                            eu.hansolo.enzo.notification.Notification.Notifier.INSTANCE.stop();

                                        }
                                    }));

                                    timeline.play();

                                }

                            }
                        });

                        Thread.sleep(100);
                    }

                } else if (tmp[0].equals("SENDING FILE")) {

                    String[] receiver = tmp[1].split("&");
                    File dir = new File("E:/Programing training/Java/serverApp/src/sample/Client/received_files/"
                            + tmp[1] + "#" + LogInName + "&" + LogInNumber);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }

                    if (dir.exists()) {
                        try {

                            FileOutputStream fos =
                                    new FileOutputStream("E:/Programing training/Java/serverApp/src/sample/Client/received_files/"
                                            + tmp[1] + "#" + LogInName + "&" + LogInNumber + "/" + tmp[2]);


                            int filesize = Math.toIntExact(Long.parseLong(tmp[3])); // Send file size in separate msg
                            byte[] buffer = new byte[filesize];
                            int read = 0;

                            int remaining = filesize;
                            while ((read = dataInputStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                                remaining -= read;
                                fos.write(buffer, 0, read);
                            }

                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();

                        }
                    }
                }
            } catch (Exception e) {

                e.printStackTrace();

                try {
                    dataInputStream.close();
                    dataOutputStream.close();
                    socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.exit(1);
            }
        }
    }

    private void showSuggestions(String suggests) {

        String[] names = suggests.split(",");

        Button[] buttons = new Button[names.length];

        VBox vBox = new VBox(20);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(10, 10, 10, 10));

        for (int i = 0; i < names.length; i++) {

            buttons[i] = new Button("send request to  \"" + names[i] + "\"");

            HBox hBox = new HBox(50);
            hBox.setAlignment(Pos.CENTER);
            hBox.getChildren().add(buttons[i]);
            HBox.setHgrow(hBox, Priority.ALWAYS);

            vBox.getChildren().add(hBox);

            int finalI = i;

            buttons[i].setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    Client.this.handle("FRIEND REQUEST#" + LogInName + "&" + names[finalI]);
                }
            });

        }


        for (int i = 0; i < names.length; i++) {


        }


        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setFitToWidth(true);


        Stage stage = new Stage();


        Scene scene = new Scene(scrollPane, 200, 300);
        stage.setScene(scene);
        stage.show();

    }

    private String[] friendsList(String friends) {

        String[] f = friends.split(",");

        String res = "";
        for (String s : f) {
            String[] t = s.split("@");
            for (int i = 1; i < t.length; i++) {
                res += t[0] + " " + t[i] + ",";
            }
            res = res.substring(0, res.length() - 1);
            res += ",";
        }
        res = res.substring(0, res.length() - 1);

        return res.split(",");
    }

    private void mainPage(Stage primaryStage, String[] friends) {

        chatBox = new VBox(10);

        primaryStage.setTitle(LogInName + "  (" + LogInNumber + ")");

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundImage(
                new Image(getClass().getResourceAsStream("/sample/Client/chatBackground.png")),
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));


//        ----------------------------Contacts listView-------------------------------------

        List<String> items = new ArrayList<>();
        for (String s : friends)
            items.add(s);


        list = FXCollections.observableList(items);


        friendsListView = new ListView<>();

        friendsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {  //  on listView item clicked
            @Override
            public void handle(MouseEvent event) {
                String item = friendsListView.getSelectionModel().getSelectedItem();
                if (!contact.equals(item)) {

                    if (item.contains("group#")) {
                        Client.this.handle("GROUP HISTORY#" + item.split("#")[1]);
                        contact = item;
                    } else {
                        String[] user = item.split(" ");
                        Client.this.handle("HISTORY#" + LogInName + "&" + LogInNumber + "#" + user[0] + "&" + user[1]);

                        contact = user[0] + " " + user[1];
                    }
                    chatBox.getChildren().clear();
                }
            }
        });
        friendsListView.setItems(list);

        if (friendsListView.getItems().size() != 0) {
            friendsListView.getSelectionModel().select(0);
        }
//        ----------------------------Left side-------------------------------------
        TextField search_tf = new TextField();
        search_tf.setPromptText("search people");

        Button search_btn = new Button("find");
        search_btn.setStyle("-fx-background-color: plum;");

        Button createGroup_btn = new Button("create group");
        createGroup_btn.setStyle("-fx-background-color: plum;");

        createGroup_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                createGroupPage();
            }
        });

//        ------------------------ Top of friends list----------------------------
        search_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String requestMassage = "SEARCH#";
                if (!search_tf.getText().equals("")) {
                    requestMassage += search_tf.getText();
                    Client.this.handle(requestMassage);
                } else {
                    new Alert(Alert.AlertType.ERROR, "search field is Empty!").show();
                }

            }
        });

        HBox topHBox = new HBox(2, search_tf, search_btn, createGroup_btn);
        HBox.setHgrow(search_tf, Priority.ALWAYS);
//        ------------------------ Bottom of friends list----------------------------

        Button addNumber_btn = new Button("Add Number");
        addNumber_btn.setStyle("-fx-background-color: plum;");

        Button suggestion_btn = new Button("Suggestions");
        suggestion_btn.setStyle("-fx-background-color: plum;");

        TextField number_tf = new TextField();
        number_tf.setPromptText("add new number");


        // force the field to be numeric only
        number_tf.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (!newValue.matches("\\d*")) {
                    number_tf.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        addNumber_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!number_tf.getText().equals(""))
                    Client.this.handle("ADD NUMBER#" + LogInName + "&" + number_tf.getText());
            }
        });

        suggestion_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Client.this.handle("SUGGESTION#" + LogInName + "&" + 2);
            }
        });

        HBox bottomHBox = new HBox(2, number_tf, addNumber_btn, suggestion_btn);
        bottomHBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(bottomHBox, Priority.ALWAYS);


        VBox leftSideBox = new VBox(4, topHBox, friendsListView, bottomHBox);
        leftSideBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(topHBox, Priority.ALWAYS);
        VBox.setVgrow(friendsListView, Priority.ALWAYS);

//        -----------------------------chat screen------------------------------------------


        chatBox.setPadding(new Insets(10, 10, 10, 10));
        chatBox.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(chatBox, Priority.ALWAYS);


        TextArea massage_tf = new TextArea();
        massage_tf.setPromptText("write your massage");
        massage_tf.setPrefHeight(15);

        Button send_btn = new Button("Send");
        send_btn.setStyle("-fx-background-color: plum;");

        Button file_btn = new Button("send file");
        file_btn.setStyle("-fx-background-color: plum;");

        send_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String massage = massage_tf.getText();
                if (!massage.equals("")) {
                    String item = friendsListView.getSelectionModel().getSelectedItem();

                    if (!item.contains("group#")) {

                        String[] receiverDetail = friendsListView.getSelectionModel().getSelectedItem().split(" ");

                        Client.this.handle("CHAT#" + LogInName + "&" + LogInNumber + "#" + massage + "#" +
                                receiverDetail[0] + "&" + receiverDetail[1]);


                    } else {

                        Client.this.handle("GROUP CHAT#"
                                + LogInName + "&" + LogInNumber + "#" + massage + "#" +/*group name*/ (item.split("#")[1]));

                    }

                    massage_tf.clear();

                    DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
                    Date date = new Date();

                    Text text = new Text("   " + df.format(date) + " :  \"me\" : \n   " + massage);
                    text.setTextAlignment(TextAlignment.CENTER);
                    text.autosize();
                    text.setWrappingWidth(200);


                    HBox hBox = new HBox(10, text);
                    hBox.setStyle("-fx-background-color: rgba(196,0,255,0.13)");


                    chatBox.getChildren().addAll(hBox);
                }
            }
        });

        file_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();

                handleFileStream(fileChooser.showOpenDialog(primaryStage));

            }
        });


        HBox hBox = new HBox(2, massage_tf, send_btn, file_btn);
        hBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(massage_tf, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(chatBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.vvalueProperty().bind(chatBox.heightProperty());


        VBox pm_box = new VBox(2, scrollPane, hBox);
        pm_box.setStyle("-fx-background-color: transparent");
        pm_box.setAlignment(Pos.BOTTOM_CENTER);
        pm_box.setPadding(new Insets(5, 0, 0, 5));


        root.setCenter(pm_box);
        root.setPadding(new Insets(10, 10, 10, 10));
        root.setLeft(leftSideBox);


        primaryStage.setScene(new Scene(root, 1000, 500));
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/sample/Client/icon.png")));
        primaryStage.show();

        System.out.println("client is ready");


//        -----------------------on close action--------------------------

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {

                try {
                    Client.this.handle("CLOSE#");
                    thread.stop();
                    dataInputStream.close();
                    dataOutputStream.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void createGroupPage() {

        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        TextField groupName_tf = new TextField();
        groupName_tf.setStyle("-fx-background-color: powderblue;");
        groupName_tf.setPromptText("Group name");

        ObservableList<String> list = this.list;

        for (String s : list) {
            if (s.contains("group#"))
                list.remove(s);
        }

        ListView<String> listView = new ListView<>(list);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Button create_btn = new Button("Done");
        create_btn.setStyle("-fx-background-color: powderblue;");

        create_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!groupName_tf.getText().equals("")) {
                    ObservableList<String> l = listView.getSelectionModel().getSelectedItems();
                    String list = "";
                    for (String s : l) {
                        list += s + "&";
                    }
                    list += LogInName + " " + LogInNumber;
                    Client.this.handle("CREATE GROUP#" + groupName_tf.getText() + "#" + list);

                    stage.close();
                }
            }
        });

        HBox hBox = new HBox(10, groupName_tf, create_btn);
        hBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(listView, hBox);

        stage.setScene(new Scene(root));
        stage.show();
    }

    private void handleFileStream(File file) {

        if (file != null) {

            if (!friendsListView.getSelectionModel().getSelectedItem().contains("group#")) {
                String[] receiver = friendsListView.getSelectionModel().getSelectedItem().split(" ");

                handle("SENDING FILE#" + receiver[0] + "&" + receiver[1] + "#" +
                        file.getName() + "#" + file.length());

                try {
                    byte[] buffer = new byte[Math.toIntExact(file.length())];

                    FileInputStream fileInputStream = new FileInputStream(file);


                    while (fileInputStream.read(buffer) > 0)
                        handle(buffer);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private void handle(String data) {

        try {
            dataOutputStream.writeUTF(data);
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void handle(byte[] bytes) {
        try {

            dataOutputStream.write(bytes);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch();
    }

}
