package ownservlet;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;

public class TronDemo extends Application {
    //Creating the crash sound

    //progress bar
    static Rectangle[] bar1 = new Rectangle[3];
    Group barPlayer1 = new Group();
    static Rectangle[] bar2 = new Rectangle[3];
    Group barPlayer2 = new Group();
    //    SocketClient socketClient;
    Socket socket;
    private static String ipAddr = "localhost";
    private static int port = 8080;
    private static int positionPlayer;
    private static ClientSomething clientSomething;


    //CONSTANTS AND PARAMETERS FOR PROTOCOL
    public static final byte DIRECTION_UP = 1;
    public static final byte DIRECTION_DOWN = 2;
    public static final byte DIRECTION_LEFT = 3;
    public static final byte DIRECTION_RIGHT = 4;
    public static final byte USERNAME = 5;
    public static final byte START = 6;
    public static final byte PAUSE = 7;
    public static final byte JUMP = 8;
    public static final byte START_AGAIN = 9;
    static Connection connection;

    public TronDemo(Connection connection) {
        this.connection = new Connection(ipAddr, port);
    }

    //Builds the GUI
    @Override
    public void start(Stage stage) throws Exception {
        clientSomething = new ClientSomething(ipAddr, port);
//        try {
            connection.sendMessage(new Message(START));
//        } catch (IOException e) {
//            throw new IllegalArgumentException(e);
//        }
        if (clientSomething.read() != null) {
            positionPlayer = Integer.parseInt(clientSomething.read());
        }
        StackPane stack = new StackPane();
        GridPane board = new GridPane();
        Pane[][] grid = new Pane[25][25];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                grid[i][j] = new Pane();
                grid[i][j].setPrefSize(20, 20);
                grid[i][j].setStyle("-fx-background-color: black;");
                board.add(grid[i][j], i, j);
            }
        }

        stack.setStyle("-fx-background-color: #67C8FF;" + "-fx-border-color: grey;");
        stack.setPadding(new Insets(2));
        stack.setMinSize(board.getMinWidth(), board.getMinHeight());

        //styles progress bars (it has to come before GamePane initialization or else it crashes)
        int xOffset = 10;
        for (int i = 0; i < 3; i++) {

            Rectangle r1 = new Rectangle(xOffset, 15, 20, 5);
            r1.setFill(Color.web("#262626"));
            bar1[i] = r1;
            barPlayer1.getChildren().add(r1);

            Rectangle r2 = new Rectangle(xOffset, 15, 20, 5);
            r2.setFill(Color.web("#262626"));
            bar2[i] = r2;
            barPlayer2.getChildren().add(r2);

            xOffset += 22;
        }


        GamePane game = new GamePane(1);
        game.setMinSize(500, 500);

        stack.getChildren().addAll(board, game);

        BorderPane head = new BorderPane();
        InputStream is = new FileInputStream("Client/src/main/java/ownservlet/TronLogo.jpg");
        Image logo = new Image(is, 200, 100, true, true);
        ImageView iv = new ImageView(logo);

        //adds text and progress bars to vboxes (left and right of Head)
        VBox player1info = new VBox(5);

        VBox player2info = new VBox(5);

        head.setStyle("-fx-background-color: rgba(0,0,0,1);");
        head.setLeft(player1info);
        head.setCenter(iv);
        head.setRight(player2info);
        head.setPadding(new Insets(20));

        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: blue;");
        pane.setTop(head);
        pane.setCenter(stack);

        Scene scene = new Scene(pane);
        scene.setOnKeyPressed(game::processKeyPress);
        stage.setTitle("Tron");
        stage.setScene(scene);
        stage.show();
    }


    public static class GamePane extends Pane {
        //Attributes
        public ArrayList<Line> linesP1 = new ArrayList<Line>();
        public ArrayList<Line> linesP2 = new ArrayList<Line>();

        public boolean crash1 = false;
        public boolean crash2 = false;

        public int player1wins = 0;
        private int player2wins = 0;
        private String winner = "";

        private Board game;
        private Circle p1, p2;
        private double p1x, p1y, p2x, p2y;
        private Line tailP1, tailP2;

        private Timeline animation;
        private boolean isPaused = true;
        private boolean winReset = false;

        Label press;

        Label winnerText;

        public GamePane() {

        }

        //Constructor
        public GamePane(int i) {

            press = new Label("Press Space Bar to Start");
            winnerText = new Label(winner);

            p1x = 100.0;
            p1y = 260.0;
            p2x = 400.0;
            p2y = 260.0;


            winnerText.setFont(new Font("Arial", 20));

            game = new Board();
            reset();
            press.setStyle("-fx-background-color: black;" + "-fx-border-color: cyan;" + "-fx-text-fill: white;");
            press.layoutXProperty().bind(this.widthProperty().subtract(press.widthProperty()).divide(2));
            press.layoutYProperty().bind(this.heightProperty().subtract(press.heightProperty()).divide(2));
            press.setPadding(new Insets(5));

            winnerText.setStyle("-fx-background-color: black;" + "-fx-border-color: white;" + "-fx-text-fill: white;");
            winnerText.layoutXProperty().bind(this.widthProperty().subtract(winnerText.widthProperty()).divide(2));
            winnerText.layoutYProperty().bind(this.heightProperty().subtract(winnerText.heightProperty()).divide(2));
            winnerText.setVisible(false);

            reset();

            animation = new Timeline(
                    new KeyFrame(Duration.millis(50), e -> {
                        try {
                            movePlayer();
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }));
            animation.setCycleCount(Timeline.INDEFINITE);
            animation.pause();
        }

        //Resumes game
        public void play() {
            animation.play();
            isPaused = false;
        }

        //Pauses game
        public void pause() {
            animation.pause();
            try {
                connection.sendMessage(new Message(PAUSE));
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
            winnerText.setText("PAUSE");
            isPaused = true;
            winnerText.setVisible(true);
        }

        private String[] coordinate;
        private String coor;
        private double oldX1;
        private double oldX2;
        private double oldY1;
        private boolean playGame = false;
        private double oldY2;

        protected void movePlayer() throws IOException {
            String s = clientSomething.read();
            if (s.equals("pause")) {
                reset();
                isPaused = true;
                playGame = false;
            }
            if (playGame) {
                if (positionPlayer == 1) {
                    if (game.getPlayer(positionPlayer).getDir().equals("l")) {
                        p1x -= 1;
                    } else if (game.getPlayer(positionPlayer).getDir().equals("u")) {
                        p1y -= 1;
                    } else if (game.getPlayer(positionPlayer).getDir().equals("r")) {
                        p1x += 1;
                    } else if (game.getPlayer(positionPlayer).getDir().equals("d")) {
                        p1y += 1;
                    }
                    clientSomething.send(p1x, p1y);
                    coor = clientSomething.read();
                    if (!coor.equals("null") && !coor.equals("play")) {
                        coordinate = clientSomething.read().split(",");
                        p2x = Double.parseDouble(coordinate[0]);
                        p2y = Double.parseDouble(coordinate[1]);
                    }
                } else {
                    if (game.getPlayer(positionPlayer).getDir().equals("l")) {
                        p2x -= 1;
                    } else if (game.getPlayer(positionPlayer).getDir().equals("u")) {
                        p2y -= 1;
                    } else if (game.getPlayer(positionPlayer).getDir().equals("r")) {
                        p2x += 1;
                    } else if (game.getPlayer(positionPlayer).getDir().equals("d")) {
                        p2y += 1;
                    }
                    clientSomething.send(p2x, p2y);
                    coor = clientSomething.read();
                    if (!coor.equals("null") && !coor.equals("play")) {
                        coordinate = clientSomething.read().split(",");
                        p1x = Double.parseDouble(coordinate[0]);
                        p1y = Double.parseDouble(coordinate[1]);
                    }
                }

                linesP1.add(tailP1);
                tailP1 = tail(p1x, p1y, p1x, p1y, 1);
                getChildren().add(tailP1);

                linesP2.add(tailP2);
                tailP2 = tail(p2x, p2y, p2x, p2y, 2);
                getChildren().add(tailP2);

                //прорисовка хвостов
                tailP1.setEndX(p1x);
                tailP1.setEndY(p1y);
                tailP2.setEndX(p2x);
                tailP2.setEndY(p2y);


                Point point1 = new Point(p1x, p1y);
                Point point2 = new Point(p2x, p2y);

                if (Math.abs(point1.getX() - point2.getX()) < 8 && Math.abs(point1.getY() - point2.getY()) < 8) {
                    tie();
                }
                //Проверка на выход за границу карты
                crash1 = checkCrash1(point1);
                if (crash1) {
                    crash(1);
                }
                crash2 = checkCrash1(point2);
                if (crash2) {
                    crash(2);
                }

                //Проверка на пересечение с самим собой
                crash2 = check(linesP2, tailP2);
                crash1 = check(linesP1, tailP1);

                if (crash1) {
                    crash(1);
                }
                if (crash2) {
                    crash(2);
                }

                //Проверка на пересечении одного игрока с линией другого
                crash1 = check2(linesP2, tailP1);
                crash2 = check2(linesP1, tailP2);
                if (crash1) {
                    crash(1);
                }
                if (crash2) {
                    crash(2);
                }
            } else if (s.equals("play")) {
                playGame = true;
                isPaused = false;
                try {
                    connection.sendMessage(new Message(START_AGAIN));
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        public void processKeyPress(KeyEvent e) {
            if (e.getCode() == KeyCode.SPACE && (isPaused || !winReset)) {
                try {
                    connection.sendMessage(new Message(START));
                } catch (IOException ex) {
                    throw new IllegalArgumentException(ex);
                }
                System.out.println("1");
                clientSomething.unpause();
                clientSomething.sendPlay(1);
                play();
                isPaused = false;
                press.setVisible(false);
                winnerText.setVisible(false);
            } else if (e.getCode() == KeyCode.SPACE && !isPaused) {
                try {
                    connection.sendMessage(new Message(PAUSE));
                } catch (IOException ex) {
                    throw new IllegalArgumentException(ex);
                }
                pause();
                isPaused = true;
                playGame = false;
                System.out.println("3");
                clientSomething.sendPause(1);
                pause();
            } else if (positionPlayer == 1) {
                if (e.getCode() == KeyCode.W && !game.getPlayer(positionPlayer).getDir().equals("d")) {
                    game.getPlayer(1).setDir("u");
                    linesP1.add(tailP1);
                    tailP1 = tail(p1x, p1y, p1x, p1y, 1);
                    getChildren().add(tailP1);
                } else if (e.getCode() == KeyCode.A && !game.getPlayer(positionPlayer).getDir().equals("r")) {
                    game.getPlayer(1).setDir("l");
                    linesP1.add(tailP1);
                    tailP1 = tail(p1x, p1y, p1x, p1y, 1);
                    getChildren().add(tailP1);
                } else if (e.getCode() == KeyCode.S && !game.getPlayer(positionPlayer).getDir().equals("u")) {
                    game.getPlayer(1).setDir("d");
                    linesP1.add(tailP1);
                    tailP1 = tail(p1x, p1y, p1x, p1y, 1);
                    getChildren().add(tailP1);
                } else if (e.getCode() == KeyCode.D && !game.getPlayer(positionPlayer).getDir().equals("l")) {
                    game.getPlayer(1).setDir("r");
                    linesP1.add(tailP1);
                    tailP1 = tail(p1x, p1y, p1x, p1y, 1);
                    getChildren().add(tailP1);
                }
            } else {
                if (e.getCode() == KeyCode.W && !game.getPlayer(positionPlayer).getDir().equals("d")) {
                    game.getPlayer(2).setDir("u");
                    linesP2.add(tailP2);
                    tailP2 = tail(p2x, p2y, p2x, p2y, 2);
                    getChildren().add(tailP2);
                } else if (e.getCode() == KeyCode.A && !game.getPlayer(positionPlayer).getDir().equals("r")) {
                    game.getPlayer(2).setDir("l");
                    linesP2.add(tailP2);
                    tailP2 = tail(p2x, p2y, p2x, p2y, 2);
                    getChildren().add(tailP2);
                } else if (e.getCode() == KeyCode.S && !game.getPlayer(positionPlayer).getDir().equals("u")) {
                    game.getPlayer(2).setDir("d");
                    linesP2.add(tailP2);
                    tailP2 = tail(p2x, p2y, p2x, p2y, 2);
                    getChildren().add(tailP2);
                } else if (e.getCode() == KeyCode.D && !game.getPlayer(positionPlayer).getDir().equals("l")) {
                    game.getPlayer(2).setDir("r");
                    linesP2.add(tailP2);
                    tailP2 = tail(p2x, p2y, p2x, p2y, 2);
                    getChildren().add(tailP2);
                }
            }
        }

        public Line tail(double sx, double sy, double ex, double ey, int player) {
            Line l = new Line(sx, sy, ex, ey);
            l.setStrokeWidth(4);
            l.setStrokeLineCap(StrokeLineCap.ROUND);
            if (player == 1)
                l.setStroke(Color.CYAN);
            else
                l.setStroke(Color.HOTPINK);
            return l;
        }

        public int crash(int i, int b) {
            int flag = -1;
            if (i == 1) {
                flag = 1;
                player2wins++;
            } else if (i == 2) {
                player1wins++;
                flag = 2;
            }
            return flag;
        }

        //Tests for crashing
        public void crash(int i) {
            reset();
            isPaused = true;
            clientSomething.pause();
            pause();
            playGame = false;
            if (i == 1) {
                winnerText.setText("PLAYER 2 WINS");
                bar2[player2wins].setFill(Color.HOTPINK);
                player2wins++;
                win();
            } else if (i == 2) {
                winnerText.setText("PLAYER 1 WINS");
                bar1[player1wins].setFill(Color.CYAN);
                player1wins++;
                win();
            }
        }

        //Determines if it is a tie or not
        public void tie() {
            reset();
            winnerText.setText("TIE");
            animation.pause();
            isPaused = true;
            winnerText.setVisible(true);
        }

        public void win() {
            reset();
            p1x = 100.0;
            p1y = 260.0;
            p2x = 400.0;
            p2y = 260.0;
            if (player1wins == 3) {
                winReset = true;
                winnerText.setText("CONGRATULATIONS PLAYER 1! YOU WON!");
                player1wins = 0;
                player2wins = 0;
            } else if (player2wins == 3) {
                winReset = true;
                winnerText.setText("CONGRATULATIONS PLAYER 2! YOU WON!");
                player2wins = 0;
                player1wins = 0;
            }

            winnerText.setVisible(true);
        }

        public void reset() {
            getChildren().clear();
            game.getPlayer(1).setDir("r");
            game.getPlayer(2).setDir("l");

            p1x = 100;
            p1y = 260;
            p2x = 400;
            p2y = 260;

            if (positionPlayer == 1) {
                clientSomething.send(p2x, p2y);
            } else {
                clientSomething.send(p1x, p1y);
            }

            tailP1 = new Line(p1x, p1y, p1x, p1y);
            tailP2 = new Line(p2x, p2y, p2x, p2y);

            tailP1.setStrokeWidth(4);
            tailP1.setStroke(Color.CYAN);
            tailP1.setStrokeLineCap(StrokeLineCap.ROUND);
            tailP2.setStrokeWidth(4);
            tailP2.setStroke(Color.HOTPINK);
            tailP2.setStrokeLineCap(StrokeLineCap.ROUND);

            p1 = new Circle(p1x, p1y, 2);
            p2 = new Circle(p2x, p2y, 2);
            p1.setFill(Color.CYAN);
            p2.setFill(Color.HOTPINK);

            crash1 = false;
            crash2 = false;
            linesP1.clear();
            linesP2.clear();

            getChildren().addAll(p1, p2, tailP1, tailP2, press, winnerText);
        }

        //Crashes into self
        public boolean check(ArrayList<Line> list, Line l) {
            for (int i = 0; i < list.size() - 10; i++) {
                //GetBoundsInLocal возвращает границы узла в своей собственной системе координат.
                if (l.getBoundsInLocal().intersects(list.get(i).getBoundsInLocal())) {
                    return true;
                }
            }
            return false;
        }

        //Crashes into other line
        public boolean check2(ArrayList<Line> list, Line l) {
            for (int i = 0; i < list.size(); i++) {
                if (l.getBoundsInLocal().intersects(list.get(i).getBoundsInLocal())) {
                    return true;
                }
            }
            return false;
        }

        //Checks for out of bounds
        public boolean checkCrash1(Point p) {
            boolean crash = false;
            if (p.getX() < 0 || p.getX() > 500 || p.getY() < 0 || p.getY() > 500) {
                crash = true;
            }
            return crash;
        }

    }

    //Starts game
    public static void main(String[] args) {
        launch(args);
    }
}