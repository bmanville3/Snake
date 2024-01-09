import java.util.LinkedList;
import java.util.HashSet;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import java.io.FileWriter;

/**
 * SnakeGame is the classic Snake game that can be found on google
 * with all the same rules and mechanisms. Within this game, there are
 * three different subgames to choose from: classic, speed, and crazy apples.
 * 
 * @author Brandon Manville
 * @version 1.0
 */
public class SnakeGame extends Application {

    private static final int BOARD_SIZE = 16;
    private static final int BLOCK_SIZE = 32;
    private static final long REG_WAIT = 180_000_000;
    private static double LENGTH = 1.3 * BOARD_SIZE * BLOCK_SIZE;
    private static double WIDTH = 1.9 * BOARD_SIZE * BLOCK_SIZE;

    private boolean up, down, left, right, pause, gameOver, playing; // Game mechanics
    private boolean speed, crazyApples, maze; // Game modes

    private Snake snake;
    private Board board;
    private Rectangle[][] backing = new Rectangle[BOARD_SIZE][BOARD_SIZE];

    private int highestReg, highestSpeed, highestCrazy, mazesComp;
    private int[] allTHighReg, allTHighSpeed, allTHighCrazy, allTHighMaze;
    private String[] allTHighRegNames, allTHighSpeedNames, allTHighCrazyNames, allTHighMazeNames;
    private long wait;
    private User player;
    private VBox leaderB; // The leaderboard
    private boolean backed; // Whether or not the game has been saved yet

    private static final String HIGH_SCORES = "Highscores.txt";

    /**
     * This is the stage for the snake game; all the main interactions happen here
     * 
     * @param stage the stage for the game
     */
    public void start(Stage stage) {
        // Make a grid to represent the game
        GridPane grid = new GridPane();
        for (int i = 0; i < BOARD_SIZE; i++) {
            grid.getColumnConstraints().add(new ColumnConstraints(BLOCK_SIZE));
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            grid.getRowConstraints().add(new RowConstraints(BLOCK_SIZE));
        }
        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Rectangle rec = new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.ORANGE);
                // The backing array is used to access the rectangles later
                backing[col][row] = rec;
                grid.add(rec, col, row);
            }
        }

        // Playing Regular Snake at the start
        Label score = new Label(String.format("Current Length: %d - Press any direction to play", snake.length));
        Label highScore = new Label(String.format("Session Highscore for Snake: %d", highestReg));
        Label gameType = new Label("Playing: Snake"); // Added further down for style purposes
        resetBoard();
        wait = REG_WAIT;

        grid.add(score, 0, BOARD_SIZE + 1);
        grid.add(highScore, 0, BOARD_SIZE + 2);
        GridPane.setColumnSpan(score, GridPane.REMAINING);
        GridPane.setColumnSpan(highScore, GridPane.REMAINING);

        HBox root = new HBox();
        Region space = new Region();
        space.setPrefWidth(30);
        root.getChildren().addAll(grid, space, leaderB);

        Scene scene = new Scene(root, WIDTH, LENGTH);

        Timer timer = new Timer();
        timer.score = score;
        timer.highScore = highScore;

        // Game controls
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case UP:
                    case W:
                        if (maze) {
                            moveUpMaze(score);
                        } else if (up != true && down == false && !gameOver && !pause) {
                            timer.userIsMoving = true;
                            if (!timer.running) {
                                timer.start();
                                score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
                            }
                            if (!moveUp(score, highScore)) {
                                timer.stop();
                                saveSessionScore(highScore);
                                System.out.println("SHOULD SAVE");
                            }
                            up = true;
                            left = false;
                            right = false;
                            timer.waiting = true;
                        } break;
                    case DOWN:
                    case S:
                        if (maze) {
                            moveDownMaze(score);
                        } else if (down != true && up == false && !gameOver && !pause) {
                            timer.userIsMoving = true;
                            if (!timer.running) {
                                timer.start();
                                score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
                            }
                            if (!moveDown(score, highScore)) {
                                timer.stop();
                                saveSessionScore(highScore);
                                System.out.println("SHOULD SAVE");
                            }
                            down = true;
                            left = false;
                            right = false;
                            timer.waiting = true;
                        } break;
                    case LEFT:
                    case A:
                        if (maze) {
                            moveLeftMaze(score);
                        } else if (left != true && right == false && !gameOver && !pause) {
                            timer.userIsMoving = true;
                            if (!timer.running) {
                                timer.start();
                                score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
                            }
                            if (!moveLeft(score, highScore)) {
                                timer.stop();
                                saveSessionScore(highScore);
                                System.out.println("SHOULD SAVE");
                            }
                            up = false;
                            down = false;
                            left = true;
                            timer.waiting = true;
                        } break;
                    case RIGHT:
                    case D:
                        if (maze) {
                            moveRightMaze(score);
                        } else if (right != true && left == false && !gameOver && !pause) {
                            timer.userIsMoving = true;
                            if (!timer.running) {
                                timer.start();
                                score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
                            }
                            if (!moveRight(score, highScore)) {
                                timer.stop();
                                saveSessionScore(highScore);
                                System.out.println("SHOULD SAVE");
                            }
                            up = false;
                            down = false;
                            right = true;
                            timer.waiting = true;
                        } break;
                    case SPACE:
                        if (gameOver) {
                            if (maze) {
                                mazeBoard(score, highScore);
                                showAndHide();
                                gameType.setText("Playing: Random Maze Snake");
                                highScore.setText("You can only see one block around you in any direction");
                            } else {
                                resetBoard();
                                score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
                            }
                        } else if (timer.running && !maze) { // Moving keeps players from pausing at the start
                            pause = true;
                            timer.stop();
                            score.setText(String.format("Current Length: %d - Press SPACE to Unpause", snake.length));
                        } else if (pause && !maze) {
                            pause = false;
                            timer.start();
                            score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
                        } break;
                    case EQUALS:
                    case ADD:
                        LENGTH *= 1.1;
                        WIDTH *= 1.1;
                        stage.setHeight(LENGTH);
                        stage.setWidth(WIDTH);
                        break;
                    case MINUS:
                        LENGTH /= 1.1;
                        WIDTH /= 1.1;
                        stage.setHeight(LENGTH);
                        stage.setWidth(WIDTH);
                        break;
                        
                    default:
                        break;
                }
            }
        });

        // Building other game mode options
        Button regSnake = new Button("Snake");
        regSnake.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 2, BLOCK_SIZE);
        regSnake.setFocusTraversable(false); // Necessary for arrow keys to work in game
        regSnake.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                timer.stop();
                resetBoard(highScore);
                speed = false;
                maze = false;
                crazyApples = false;
                wait = REG_WAIT;
                score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
                gameType.setText("Playing: Snake");
                highScore.setText(String.format("Session Highscore for Snake: %d", highestReg));
            }
        });

        Button speedSnake = new Button("Speed Snake");
        speedSnake.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 2, BLOCK_SIZE);
        speedSnake.setFocusTraversable(false);
        speedSnake.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                timer.stop();
                resetBoard(highScore); 
                maze = false;
                crazyApples = false;
                speed = true;
                wait = (long) (REG_WAIT / 3);
                score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
                gameType.setText("Playing: Speed Snake");
                highScore.setText(String.format("Session Highscore for Speed Snake: %d", highestSpeed));
            }
        });

        Button crazySnake = new Button("Crazy Apples Snake");
        crazySnake.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 2, BLOCK_SIZE);
        crazySnake.setFocusTraversable(false);
        crazySnake.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                timer.stop();
                resetBoard(highScore);
                maze = false;
                speed = false;
                crazyApples = true;
                wait = (long) (REG_WAIT * 4 / 3);
                score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
                gameType.setText("Playing: Crazy Apples Snake");
                highScore.setText(String.format("Session Highscore for Crazy Apples: %d", highestCrazy));
            }
        });

        Button mazeSnake = new Button("Random Maze Snake");
        mazeSnake.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 2, BLOCK_SIZE);
        mazeSnake.setFocusTraversable(false);
        mazeSnake.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                timer.stop();
                mazeBoard(score, highScore);
                maze = true;
                crazyApples = false;
                speed = false;
                showAndHide();
                gameType.setText("Playing: Random Maze Snake");
                highScore.setText("You can only see one block around you in any direction");
            }
        });

        HBox boxUp = new HBox();
        HBox boxDown = new HBox();
        boxUp.getChildren().addAll(regSnake, speedSnake);
        boxDown.getChildren().addAll(crazySnake, mazeSnake);
        grid.add(boxUp, 0, BOARD_SIZE + 3);
        grid.add(boxDown, 0, BOARD_SIZE + 4);
        grid.add(gameType, (int) (BOARD_SIZE / 3), BOARD_SIZE + 5);
        GridPane.setColumnSpan(boxUp, GridPane.REMAINING);
        GridPane.setColumnSpan(boxDown, GridPane.REMAINING);
        GridPane.setColumnSpan(gameType, GridPane.REMAINING);

        Button changeUser = new Button("Change User");
        changeUser.setFocusTraversable(false);
        changeUser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (timer.running && !maze) { // Moving keeps players from pausing at the start
                    pause = true;
                    timer.stop();
                    score.setText(String.format("Current Length: %d - Press SPACE to Unpause", snake.length));
                }
                showUserSelection(highScore, score, gameType);
            }
        });
        changeUser.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 2, BLOCK_SIZE);
        leaderB.getChildren().add(changeUser);
        GridPane.setColumnSpan(changeUser, (int) (BOARD_SIZE * BLOCK_SIZE / 2));

        stage.setScene(scene);
        stage.show();
    }

    private void showUserSelection(Label highScore, Label score, Label gameType) {
        Stage userSelection  = new Stage();
        userSelection.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        userSelection.setTitle("Change User");

        Label selectLabel = new Label("Select a Username (select nothing to play as Guest):");
        ComboBox<String> selectDropDown = new ComboBox<>();
        Button confirmSelect = new Button("Confirm Username Selection");
        selectDropDown.getItems().addAll(player.allUsers);
        confirmSelect.setOnAction(e -> {
            System.out.println(!gameOver);
            System.out.println(playing);

            if (!gameOver && playing) saveSessionScore(highScore);
            if (selectDropDown.getValue() != null) {
                player.setUser(selectDropDown.getValue());
            } else {
                player.setUser("Guest");
            }
            userSelection.close();
            resetBoard();
            speed = false;
            crazyApples = false;
            maze = false;
            score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
            gameType.setText("Playing: Snake");
            highScore.setText(String.format("Session Highscore for Snake: %d", highestReg));
        });
        HBox selectionBoxes = new HBox();
        selectionBoxes.getChildren().addAll(selectLabel, selectDropDown, confirmSelect);

        Label addLabel = new Label(String.format("Add a Username (Max %s Users):", User.MAX_USERS));
        TextField addTextField = new TextField();
        Button confirmAdd = new Button("Confirm New Username");
        confirmAdd.setOnAction(e -> {
            if (player.addUser(addTextField.getText())) {
                selectDropDown.getItems().add(addTextField.getText());
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PLAYER ADDED");
                alert.setHeaderText("ADDED");
                alert.setContentText(String.format("%s Added to the System - Select the Username to Play with the New Account", addTextField.getText()));
                addTextField.clear();
                alert.showAndWait();
            } else if (player.allUsers.size() > User.MAX_USERS) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("MAX USERS REACHED");
                alert.setHeaderText("FAILED ADD");
                alert.setContentText(String.format("MAX USERS REACHED: %d / %d Users", User.MAX_USERS, User.MAX_USERS));
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("PLAYER NAME MUST BE UNIQUE");
                alert.setHeaderText("FAILED ADD");
                alert.setContentText("There cannot be duplicate players in the system");
                alert.showAndWait();
            }
        });
        HBox addBoxes = new HBox();
        addBoxes.getChildren().addAll(addLabel, addTextField, confirmAdd);

        Label delLabel = new Label("Delete a Username (Deleting a username will not delete their score from the leaderboard):");
        TextField delTextField = new TextField();
        Button confirmDel = new Button("Confirm Delete Username");
        confirmDel.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("CONFIRMATION OF DELETE");
            alert.setHeaderText("CONFIRMATION");
            alert.setContentText("Are you sure you want to proceed?");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    if (player.delUser(delTextField.getText())) {
                        if (player.name.equals(delTextField.getText())) {
                            player.setUser("Guest");
                        }
                        Alert alertTwo = new Alert(Alert.AlertType.INFORMATION);
                        alertTwo.setTitle(String.format("%s DELETED - Their scores on the leaderboard remain (if applicable)", delTextField.getText()));
                        alertTwo.setHeaderText("DELETED");
                        alertTwo.setContentText("Their scores on the leaderboard remain (if applicable)");
                        selectDropDown.getItems().remove(delTextField.getText());
                        delTextField.clear();
                        alertTwo.showAndWait();
                    } else {
                        Alert alertTwo = new Alert(Alert.AlertType.INFORMATION);
                        if (delTextField.getText().equals("Valerie") || delTextField.getText().equals("Brandon")) {
                            alertTwo.setTitle("CANNOT DELETE PLAYERS");
                            alertTwo.setHeaderText("FAILED DELETE");
                            alertTwo.setContentText("Valerie and Brandon cannot be deleted");
                            alertTwo.showAndWait();
                        } else if (player.allUsers.size() == 1) {
                            alertTwo.setTitle("TOO LITTLE PLAYERS");
                            alertTwo.setHeaderText("FAILED DELETE");
                            alertTwo.setContentText("There must always be at least one player");
                            alertTwo.showAndWait();
                        } else {
                            alertTwo.setTitle("PLAYER NOT FOUND");
                            alertTwo.setHeaderText("FAILED DELETE");
                            alertTwo.setContentText("The player was not found - no one was deleted");
                            alertTwo.showAndWait();
                        }
                    }
                } 
            });
        });
        HBox delBoxes = new HBox();
        delBoxes.getChildren().addAll(delTextField, confirmDel);

        VBox allBoxes = new VBox(10);
        allBoxes.getChildren().addAll(selectionBoxes, addBoxes, delLabel, delBoxes);

        Scene userScene = new Scene(allBoxes, 800, 200);
        userSelection.setScene(userScene);
        userSelection.show();
    }

    private void createLeaderBoard() {
        leaderB = new VBox();
        Label regB = new Label("SNAKE LEADERBOARD - HIGHEST LENGTH");
        Label regBScores = new Label();
        Separator sep1 = new Separator();
        Region reg1 = new Region();
        reg1.setPrefHeight(20);
        Label speedB = new Label("SPEED SNAKE LEADERBOARD - HIGHEST LENGTH");
        Label speedBScores = new Label();
        Separator sep2 = new Separator();
        Region reg2 = new Region();
        reg2.setPrefHeight(20);
        Label crazyB = new Label("CRAZY APPLES SNAKE LEADERBOARD - HIGHEST LENGTH");
        Label crazyBScores = new Label();
        Separator sep3 = new Separator();
        Region reg3 = new Region();
        reg3.setPrefHeight(20);
        Label mazeB = new Label("RANDOM MAZE LEADERBOARD - MOST MAZES COMPLETED");
        Label mazeBScores = new Label();
        Separator sep4 = new Separator();
        Region reg4 = new Region();
        reg4.setPrefHeight(50);
        Label userBest = new Label();
        Label uScores = new Label();
        leaderB.getChildren().addAll(regB, regBScores, sep1, reg1, speedB, speedBScores, sep2, reg2, crazyB, crazyBScores, sep3, reg3, mazeB, mazeBScores, sep4, reg4, userBest, uScores);
    }

    private void changeLeaderBoard() {
        // leaderB.getChildren().addAll(regB, regBScores, sep1, reg1, speedB, speedBScores, sep2, reg2, crazyB, crazyBScores, sep3, reg3, mazeB, mazeBScores, sep4, reg4, userBest, uScores);
        // ^ Used to see the order of things added so can access by index

        ObservableList<Node> children = leaderB.getChildren();

        int[][] allHighs = new int[][] {allTHighReg, allTHighSpeed, allTHighCrazy, allTHighMaze};
        String[][] allNames = new String[][] {allTHighRegNames, allTHighSpeedNames, allTHighCrazyNames, allTHighMazeNames};
        Label[] hScoreLabels = new Label[] {(Label) children.get(1), (Label) children.get(5), (Label) children.get(9), (Label) children.get(13)};

        for (int i = 0; i < allHighs.length; i++) {
            hScoreLabels[i].setText(String.format("1) %s - %d || 2) %s - %d || 3) %s - %d", allNames[i][0], allHighs[i][0], allNames[i][1], allHighs[i][1], allNames[i][2], allHighs[i][2]));
        }

        try {
            ((Label) children.get(16)).setText(String.format("Playing %s - Best Scores:", player.name));
            if (!player.name.equals("Guest")) {
                ((Label) children.get(17)).setText(String.format("Snake - %d || Speed - %d || Crazy - %d || Mazes - %d", player.uRegHigh, player.uSpeedHigh, player.uCrazyHigh, player.uMazeComp));
            } else {
                ((Label) children.get(17)).setText("Please Sign In to Track Your Scores");
            }
        } catch (Exception e) {
            ((Label) children.get(16)).setText("Playing Guest - Best Scores:");
            ((Label) children.get(17)).setText("Please Sign In to Track Your Scores");
        }
    }

    /**
     * Resets the board for a new game; will clear all stats except high score
     * Not an instance method of class Board because we need to edit the game as a whole
     * Also, save the high score information
     */
    private void resetBoard(Label highScore) {
        if (!gameOver && playing) {
            saveSessionScore(highScore);
        }
        resetBoard();
    }

    private void resetBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                backing[i][j].setFill(Color.ORANGE);
                board.board[i][j].type = blockType.EMPTY;
            }
        }
        snake.snakeBlocks = new LinkedList<>();
        Block startingBlock = board.board[(int) (BOARD_SIZE / 2)][(int) (BOARD_SIZE / 2)];
        startingBlock.type = blockType.SNAKE;
        snake.snakeBlocks.add(startingBlock);
        snake.head = startingBlock;
        backing[startingBlock.x][startingBlock.y].setFill(Color.GREEN);
        up = false;
        down = false;
        left = false;
        right = false;
        gameOver = false;
        pause = false;
        playing = false;
        snake.length = 1;
        board.randomApple();
    }

    /**
     * Turns the board into a maze
     * Call randomMaze() to build the board and resets all stats like resetBoard(highScore)
     * @param score changes one of the game labels in the randomMaze() call
     */
    private void mazeBoard(Label score, Label highScore) {
        if (!gameOver && playing) saveSessionScore(highScore);
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                backing[i][j].setFill(Color.BLACK);
                board.board[i][j].type = blockType.EMPTY;
            }
        }
        snake.snakeBlocks = new LinkedList<>();
        Block startingBlock = board.board[0][0];
        startingBlock.type = blockType.SNAKE;
        snake.snakeBlocks.add(startingBlock);
        snake.head = startingBlock;
        backing[startingBlock.x][startingBlock.y].setFill(Color.GREEN);
        up = false;
        down = false;
        left = false;
        right = false;
        gameOver = false;
        pause = false;
        snake.length = 1;
        score.setText("Make it to the Apple that has been Randomly Placed on the Right Side");
        randomMaze(0, score);
    }

    /**
     * DFS search to check if the maze is solvable
     * @param repeat the number of times the maze has tried to be built and failed
     */
    private void randomMaze(int repeat, Label score) {
        int wallsPlaced = 0;
        int x = -1;
        int y = -1;
        while (wallsPlaced <= (BOARD_SIZE * BOARD_SIZE) / 3) {
            x = (int) (Math.random() * BOARD_SIZE);
            y = (int) (Math.random() * BOARD_SIZE);
            if (board.board[x][y].type == blockType.EMPTY) {
                board.board[x][y].type = blockType.WALL;
                wallsPlaced++;
            }
        }
        x = BOARD_SIZE - 1;
        int count = 0;
        boolean placed = false;
        while (!placed) {
            y = (int) (Math.random() * BOARD_SIZE);
            count++;
            if (board.board[x][y].type == blockType.EMPTY) {
                placed = true;
                board.board[x][y].type = blockType.APPLE;
            }
            if (count == 2 * BOARD_SIZE && !placed) {
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++) {
                        backing[i][j].setFill(Color.BLACK);
                        board.board[i][j].type = blockType.EMPTY;
                    }
                }
                board.board[0][0].type = blockType.SNAKE;
                randomMaze(repeat, score);
                placed = true;
            }
        }

        if (!solvableMaze()) {
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    backing[i][j].setFill(Color.BLACK);
                    board.board[i][j].type = blockType.EMPTY;
                }
            }
            board.board[0][0].type = blockType.SNAKE;
            if (repeat < 200) {
                randomMaze(repeat + 1, score);
            } else {
                score.setText("Sorry, no solvable maze was found. Please retry to play.");
            }
        }
    }

    /**
     * DFS search to check if the maze is solvable
     * @return whether the maze is solvable or not
     */
    private boolean solvableMaze() {
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        LinkedList<Block> stack = new LinkedList<>();
        stack.add(board.board[0][0]);
        while (!stack.isEmpty()) {
            Block v = stack.removeFirst();
            visited[v.x][v.y] = true;
            if (v.type == blockType.APPLE) {
                return true;
            }
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (v.x + i >= 0 && v.x + i < BOARD_SIZE && v.y + j >= 0 && v.y + j < BOARD_SIZE &&
                            (j == 0 || i == 0) && board.board[v.x + i][v.y + j].type != blockType.WALL && !visited[v.x + i][v.y + j]) {
                        stack.addFirst(board.board[v.x + i][v.y + j]);
                    }
                }
            }
        }
        return false;
    }

    /**
     * Moves the snake up; ends the game if an invalid move
     * Grows the snake if the next square is an apple
     * This will be the same for each of the following move*()
     * @param score changes the score of the game
     * @return false if invalid move - makes game end; true if valid
     */
    private boolean moveUp(Label score, Label highScore) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (y - 1 < 0 || board.board[x][y-1].type == blockType.SNAKE) {
            score.setText(String.format("Final Length: %d - Press SPACE to Restart", snake.length));
            gameOver = true;
            return false;
        } else {
            snake.snakeBlocks.addFirst(board.board[x][y-1]);
            snake.head = board.board[x][y-1];
            if (board.board[x][y-1].type != blockType.APPLE) {
                updateColor(backing[x][y-1], x, y - 1, Color.GREEN);
                x = snake.snakeBlocks.getLast().x;
                y = snake.snakeBlocks.getLast().y;
                updateColor(backing[x][y], x, y, Color.ORANGE);
                snake.snakeBlocks.removeLast();
            } else {
                updateColor(backing[x][y-1], x, y - 1, Color.GREEN);
                snake.length++;
                board.randomApple();
                score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
            }
            return true;
        }
    }

    /**
     * This version of move is just for the maze version
     * The main difference is introduction of wall and no death upon hitting yourself
     * All implements showAndHide() methods
     */
    private void moveUpMaze(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (y - 1 >= 0 && board.board[x][y-1].type != blockType.WALL) {
            if (board.board[x][y-1].type == blockType.APPLE) {
                gameOver = true;
                score.setText(String.format("You Made It to the Apple! - Press SPACE to Try Another Random Maze", snake.length));
                showMaze();
                mazesComp = 1;
                saveSessionScore(null);
            }
            snake.snakeBlocks.addFirst(board.board[x][y-1]);
            snake.head = board.board[x][y-1];
            updateColor(backing[x][y-1], x, y - 1, Color.GREEN);
            x = snake.snakeBlocks.getLast().x;
            y = snake.snakeBlocks.getLast().y;
            updateColor(backing[x][y], x, y, Color.ORANGE);
            snake.snakeBlocks.removeLast();
            if (!gameOver) {
                showAndHide();
            }
        }
    }

    private boolean moveDown(Label score, Label highScore) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (y + 1 >= BOARD_SIZE || board.board[x][y+1].type == blockType.SNAKE) {
            score.setText(String.format("Final Length: %d - Press SPACE to Restart", snake.length));
            gameOver = true;
            return false;
        } else {
            snake.snakeBlocks.addFirst(board.board[x][y+1]);
            snake.head = board.board[x][y+1];
            if (board.board[x][y+1].type != blockType.APPLE) {
                updateColor(backing[x][y+1], x, y + 1, Color.GREEN);
                x = snake.snakeBlocks.getLast().x;
                y = snake.snakeBlocks.getLast().y;
                updateColor(backing[x][y], x, y, Color.ORANGE);
                snake.snakeBlocks.removeLast();
            } else {
                updateColor(backing[x][y+1], x, y + 1, Color.GREEN);
                snake.length++;
                board.randomApple();
                score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
            }
            return true;
        }
    }

    private void moveDownMaze(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (y + 1 < BOARD_SIZE && board.board[x][y+1].type != blockType.WALL) {
            if (board.board[x][y+1].type == blockType.APPLE) {
                gameOver = true;
                score.setText(String.format("You Made It to the Apple! - Press SPACE to Try Another Random Maze", snake.length));
                showMaze();
                mazesComp = 1;
                saveSessionScore(null);
            }
            snake.snakeBlocks.addFirst(board.board[x][y+1]);
            snake.head = board.board[x][y+1];
            updateColor(backing[x][y+1], x, y + 1, Color.GREEN);
            x = snake.snakeBlocks.getLast().x;
            y = snake.snakeBlocks.getLast().y;
            updateColor(backing[x][y], x, y, Color.ORANGE);
            snake.snakeBlocks.removeLast();
            if (!gameOver) {
                showAndHide();
            }
        }
    }

    private boolean moveLeft(Label score, Label highScore) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (x - 1 < 0 || board.board[x-1][y].type == blockType.SNAKE) {
            score.setText(String.format("Final Length: %d - Press SPACE to Restart", snake.length));
            gameOver = true;
            return false;
        } else {
            snake.snakeBlocks.addFirst(board.board[x-1][y]);
            snake.head = board.board[x-1][y];
            if (board.board[x-1][y].type != blockType.APPLE) {
                updateColor(backing[x-1][y], x - 1, y, Color.GREEN);
                x = snake.snakeBlocks.getLast().x;
                y = snake.snakeBlocks.getLast().y;
                updateColor(backing[x][y], x, y, Color.ORANGE);
                snake.snakeBlocks.removeLast();
            } else {
                updateColor(backing[x-1][y], x - 1, y, Color.GREEN);
                snake.length++;
                board.randomApple();
                score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
            }
            return true;
        }
    }

    private void moveLeftMaze(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (x - 1 >= 0 && board.board[x-1][y].type != blockType.WALL) {
            if (board.board[x-1][y].type == blockType.APPLE) {
                gameOver = true;
                score.setText(String.format("You Made It to the Apple! - Press SPACE to Try Another Random Maze", snake.length));
                showMaze();
                mazesComp = 1;
                saveSessionScore(null);
            }
            snake.snakeBlocks.addFirst(board.board[x-1][y]);
            snake.head = board.board[x-1][y];
            updateColor(backing[x-1][y], x - 1, y, Color.GREEN);
            x = snake.snakeBlocks.getLast().x;
            y = snake.snakeBlocks.getLast().y;
            updateColor(backing[x][y], x, y, Color.ORANGE);
            snake.snakeBlocks.removeLast();
            if (!gameOver) {
                showAndHide();
            }
        }
    }

    private boolean moveRight(Label score, Label highScore) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (x + 1 >= BOARD_SIZE || board.board[x+1][y].type == blockType.SNAKE) {
            score.setText(String.format("Final Length: %d - Press SPACE to Restart", snake.length));
            gameOver = true;
            return false;
        } else {
            snake.snakeBlocks.addFirst(board.board[x+1][y]);
            snake.head = board.board[x+1][y];
            if (board.board[x+1][y].type != blockType.APPLE) {
                updateColor(backing[x+1][y], x + 1, y, Color.GREEN);
                x = snake.snakeBlocks.getLast().x;
                y = snake.snakeBlocks.getLast().y;
                updateColor(backing[x][y], x, y, Color.ORANGE);
                snake.snakeBlocks.removeLast();
            } else {
                updateColor(backing[x+1][y], x + 1, y, Color.GREEN);
                snake.length++;
                board.randomApple();
                score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
            }
            return true;
        }
    }

    private void moveRightMaze(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (x + 1 < BOARD_SIZE && board.board[x+1][y].type != blockType.WALL) {
            if (board.board[x+1][y].type == blockType.APPLE) {
                gameOver = true;
                score.setText(String.format("You Made It to the Apple! - Press SPACE to Try Another Random Maze", snake.length));
                showMaze();
                mazesComp = 1;
                saveSessionScore(null);
            }
            snake.snakeBlocks.addFirst(board.board[x+1][y]);
            snake.head = board.board[x+1][y];
            updateColor(backing[x+1][y], x + 1, y, Color.GREEN);
            x = snake.snakeBlocks.getLast().x;
            y = snake.snakeBlocks.getLast().y;
            updateColor(backing[x][y], x, y, Color.ORANGE);
            snake.snakeBlocks.removeLast();
            if (!gameOver) {
                showAndHide();
            }
        }
    }

    /**
     * Updates the color of the block and changes the type accordingly
     * @param rec rectangle to update color
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param color rectangle is change to this color
     */
    private void updateColor(Rectangle rec, int x, int y, Color color) {
        rec.setFill(color);
        if (color == Color.GREEN) {
            board.board[x][y].type = blockType.SNAKE;
        } else if (color == Color.ORANGE) {
            board.board[x][y].type = blockType.EMPTY;
        }
    }

    /*
     * For maze game to keep the whole board hidden
     * Shows the color of the squares directly surrounding the player
     * Hides all the other squares
     */
    private void showAndHide() {
        int x = snake.head.x;
        int y = snake.head.y;
        backing[x][y].setFill(Color.GREEN);
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (x + i >= 0 && x + i < BOARD_SIZE && y + j >= 0 && y + j < BOARD_SIZE) {
                    if (board.board[x + i][y + j].type == blockType.WALL) {
                        backing[x + i][y + j].setFill(Color.BLUE);
                    } else if (board.board[x + i][y + j].type == blockType.EMPTY) {
                        backing[x + i][y + j].setFill(Color.ORANGE);
                    } else if (board.board[x + i][y + j].type == blockType.APPLE) {
                        backing[x + i][y + j].setFill(Color.RED);
                    }
                }
            }
        }
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if (x + i >= 0 && x + i < BOARD_SIZE && y + j >= 0 && y + j < BOARD_SIZE && (Math.abs(j) == 2 || Math.abs(i) == 2)) {
                    backing[x + i][y + j].setFill(Color.BLACK);
                }
            }
        }
    }

    private void showMaze() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board.board[i][j].type == blockType.WALL) {
                    backing[i][j].setFill(Color.BLUE);
                } else if (board.board[i][j].type == blockType.EMPTY) {
                    backing[i][j].setFill(Color.ORANGE);
                } else if (board.board[i][j].type == blockType.APPLE) {
                    backing[i][j].setFill(Color.RED);
                } else {
                    backing[i][j].setFill(Color.GREEN);
                }
            }
        }
    }

    private void saveSessionScore(Label highScore) {
        backed = true;
        if (!maze && playing) {
            if (crazyApples) {
                if (snake.length > highestCrazy) {
                    highestCrazy = snake.length;
                    highScore.setText(String.format("Session Highscore for Crazy Apples: %d", highestCrazy));
                }
                if (player.userFound && (snake.length > player.uCrazyHigh || snake.length > allTHighCrazy[2])) {
                    player.updateScoresForUsers();
                } else if (!player.userFound && snake.length > allTHighCrazy[2]) {
                    player.updateScoresForGuest();
                }
            } else if (speed) {
                if (snake.length > highestSpeed) {
                    highestSpeed = snake.length;
                    highScore.setText(String.format("Session Highscore for Speed Snake: %d", highestSpeed));
                }
                if (player.userFound && (snake.length > player.uSpeedHigh || snake.length > allTHighSpeed[2])) {
                    player.updateScoresForUsers();
                } else if (!player.userFound && snake.length > allTHighSpeed[2]) {
                    player.updateScoresForGuest();
                }
            } else {
                if (snake.length > highestReg) {
                    highestReg = snake.length;
                    highScore.setText(String.format("Session Highscore for Snake: %d", highestReg));
                }
                if (player.userFound && (snake.length > player.uRegHigh || snake.length > allTHighReg[2])) {
                    player.updateScoresForUsers();
                } else if (!player.userFound && snake.length > allTHighReg[2]) {
                    player.updateScoresForGuest();
                }
            }
        } else if (mazesComp == 1) {
            if (player.userFound) {
                player.updateScoresForUsers();
            } else {
                mazesComp = 0;
            }
        }
    }

    // These represent the different types of blocks possible
    private static enum blockType {
        SNAKE,
        APPLE,
        EMPTY,
        WALL;
    }

    /**
     * These are the building blocks of the game
     * While these are not actually shown on the screen, they are the backing of what is happening
     */
    private class Block {
        private int x;
        private int y;
        private blockType type;

        private Block(int x, int y) {
            this.x = x;
            this.y = y;
            this.type = blockType.EMPTY;
        }
    }

    /**
     * This is the backing board of the game that consists of blocks
     */
    private class Board {
        private Block[][] board;
        public Board() {
            board = new Block[BOARD_SIZE][BOARD_SIZE];
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    board[i][j] = new Block(i, j);
                }
            }
        }
        
        /**
         * Randomly generates an apple somewhere on screen
         * Will not generate on snake but could generate on another apple in crazy apples
         */
        private void randomApple() {
            int x = 0;
            int y = 0;
            boolean notPlaced = true;
            while (notPlaced) {
                x = (int) (Math.random() * BOARD_SIZE);
                y = (int) (Math.random() * BOARD_SIZE);
                if (board[x][y].type != blockType.SNAKE) { // Allows for apple on apple
                    notPlaced = false;
                    board[x][y].type = blockType.APPLE;
                    backing[x][y].setFill(Color.RED);
                }
            }
        }
    }

    /**
     * This class represents the snake itself
     * It uses a linkedlist as the backing data structure for the snake
     */
    private class Snake {
        private int length;
        private LinkedList<Block> snakeBlocks;
        private Block head;

        private Snake() {
            this.length = 1;
        }
    }

    /**
     * This class is a timer that extends animation timer
     * I made this class since AnimationTimer has no "isRunning()" method (very annoying)
     */
    private class Timer extends AnimationTimer {
        private long last_update = 0;
        private int counter = 0;
        private volatile boolean running;
        private Label score;
        private Label highScore;
        private boolean userIsMoving; // used to mitigate "jumps" from happening in game play
        private boolean waiting; // also used to mitigate "jumps"

        @Override
        public void handle(long now) {
            if (now - last_update >= wait && !userIsMoving) {
                if (up == true) {
                    if (!moveUp(score, highScore)) {
                        this.stop();
                        saveSessionScore(highScore);
                    }
                } else if (down == true) {
                    if (!moveDown(score, highScore)) {
                        this.stop();
                        saveSessionScore(highScore);
                    }
                } else if (left == true) {
                    if (!moveLeft(score, highScore)) {
                        this.stop();
                        saveSessionScore(highScore);
                    }
                } else if (right == true) {
                    if (!moveRight(score, highScore)) {
                        this.stop();
                        saveSessionScore(highScore);
                    }
                }
                last_update = now;
                if (crazyApples) {
                    counter++;
                    if (counter >= 3) {
                        board.randomApple();
                        counter = -1;
                        wait -= 1_500_000;
                    }
                }
            } else if (waiting && now - last_update >= wait) {
                last_update = now;
                userIsMoving = false;
                waiting = false;
            }
        }
        @Override
        public void start() {
            super.start();
            running = true;
            playing = true;
        }

        @Override
        public void stop() {
            super.stop();
            running = false;
        }
    }

    /**
     * This class represents a user
     * A user instance has highscores and a number of mazes complete
     * Users are identified by their names
     * All users and information is stores in Highscores.txt
     */
    private class User {
        private static final int MAX_USERS = 100;

        private String name;
        private int uRegHigh;
        private int uSpeedHigh;
        private int uCrazyHigh;
        private int uMazeComp;
        private boolean userFound;
        private HashSet<String> allUsers;

        private User(String name) {
            this.name = name;
            this.findAllUsers();
            this.updateUserInfo();
        }

        private boolean delUser(String gName) {
            if (allUsers.size() == 1 || gName.equals("Valerie") || gName.equals("Brandon")) {
                return false;
            }
            boolean ret = false;
            try {
                File file = new File(HIGH_SCORES);
                // So whole file won't be deleted if things go wrong
                Scanner safetyScanner = new Scanner(file);
                safetyScanner.useDelimiter("\\A");
                String safety = safetyScanner.next();
                try {
                    // Reads the file into different parts
                    // part1 = highscore part, part2 = parts up to specificed user, part3 = specified user, part 4 = rest
                    boolean part1 = true;
                    boolean part2 = false;
                    StringBuilder content1 = new StringBuilder();
                    StringBuilder content2 = new StringBuilder();
                    StringBuilder content4 = new StringBuilder();
                    Scanner scanner = new Scanner(file);
                    scanner.nextLine();
                    scanner.nextLine();
                    String holder;
                    while (scanner.hasNextLine()) {
                        if (part1) {
                            holder = scanner.nextLine();
                            if (holder.equals("# users.csv")) {
                                scanner.nextLine();
                                part1 = false;
                                part2 = true;
                            } else {
                                content1.append(holder).append("\n");
                            }
                        } else if (part2) {
                            holder = scanner.nextLine();
                            if (holder.substring(0, holder.indexOf(",")).equals(gName)) {
                                part2 = false;
                                allUsers.remove(gName);
                                ret = true;
                            } else {
                                content2.append(holder).append("\n");
                            }                        
                        } else {
                            scanner.useDelimiter("\\A");
                            content4.append(scanner.next());
                        }
                    }
                    scanner.close();

                    // Recreates the file without the delete user
                    BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORES));
                    writer.write("# high_scores.csv\nGame Mode,Rank 1,Rank 2,Rank 3\n");
                    writer.write(content1.toString());
                    writer.write("# users.csv\nName,Snake,Speed,Crazy Apples,Most Mazes Complete\n");
                    writer.write(content2.toString());
                    writer.write(content4.toString());
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    BufferedWriter writerSafety = new BufferedWriter(new FileWriter(HIGH_SCORES));
                    writerSafety.write(safety);
                    writerSafety.close();
                }
                safetyScanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ret;
        }

        private boolean addUser(String gName) {
            if (allUsers.size() > MAX_USERS) {
                return false;
            }
            try {
                // part1 = highscore part
                boolean part1 = true;
                Scanner scanner = new Scanner(new File(HIGH_SCORES));
                scanner.nextLine();
                scanner.nextLine();
                String holder;
                while (scanner.hasNextLine()) {
                    if (part1) {
                        holder = scanner.nextLine();
                        if (holder.equals("# users.csv")) {
                            scanner.nextLine();
                            part1 = false;
                        } 
                    } else {
                        holder = scanner.nextLine();
                        if (holder.substring(0, holder.indexOf(",")).equals(gName)) {
                            return false;
                        }                
                    }
                }
                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                BufferedWriter appender = new BufferedWriter(new FileWriter(HIGH_SCORES, true));
                appender.append(gName + ",0,0,0,0" + "\n");
                appender.close();
                allUsers.add(gName);
                changeLeaderBoard();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        private void findAllUsers() {
            allUsers = new HashSet<>(MAX_USERS * 2 + 1);
            try {
                // part1 = highscore part
                boolean part1 = true;
                Scanner scanner = new Scanner(new File(HIGH_SCORES));
                scanner.nextLine();
                scanner.nextLine();
                String holder;
                int[][] highss = new int[][] {allTHighReg, allTHighSpeed, allTHighCrazy, allTHighMaze};
                String[][] names = new String[][] {allTHighRegNames, allTHighSpeedNames, allTHighCrazyNames, allTHighMazeNames};
                int index = 0;
                while (scanner.hasNextLine()) {
                    if (part1) {
                        holder = scanner.nextLine();
                        if (holder.equals("# users.csv")) {
                            scanner.nextLine();
                            part1 = false;
                        } else if (!holder.equals("")) {
                            String[] holders = holder.split(",");
                            for (int j = 1; j < 4; j++) {
                                highss[index][j - 1] = Integer.parseInt(holders[j].substring(holders[j].length() - 1));
                                names[index][j - 1] = holders[j].substring(0, holders[j].indexOf("-"));
                            }
                            index++;

                        }
                    } else {
                        holder = scanner.nextLine();
                        allUsers.add(holder.substring(0, holder.indexOf(",")));                    
                    }
                }
                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void updateUserInfo() {
            this.userFound = false;
            try {
                // part1 = part up to user
                boolean part1 = true;
                Scanner scanner = new Scanner(new File(HIGH_SCORES));
                String holder = "";
                while (scanner.hasNextLine()) {
                    if (part1) {
                        if (scanner.nextLine().equals("# users.csv")) {
                            scanner.nextLine();
                            part1 = false;
                        }
                    } else {
                        holder = scanner.nextLine();
                        if (holder.substring(0, holder.indexOf(",")).equals(this.name)) {
                            this.userFound = true;
                            break;
                        }                     
                    }
                }
                scanner.close();
                if (this.userFound) {
                    String[] highs = holder.split(",");
                    this.uRegHigh = Integer.parseInt(highs[1]);
                    this.uSpeedHigh = Integer.parseInt(highs[2]);
                    this.uCrazyHigh = Integer.parseInt(highs[3]);
                    this.uMazeComp = Integer.parseInt(highs[4]);
                }
                changeLeaderBoard();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void updateScoresForUsers() {
            try {
                File file = new File(HIGH_SCORES);
                // So whole file won't be deleted if things go wrong
                Scanner safetyScanner = new Scanner(file);
                safetyScanner.useDelimiter("\\A");
                String safety = safetyScanner.next();
                try {
                    // Reads the file into different parts
                    // part1 = highscore part, part2 = parts up to specificed user, part3 = specified user, part 4 = leftover
                    boolean part1 = true;
                    boolean part2 = false;
                    StringBuilder content1 = new StringBuilder();
                    StringBuilder content2 = new StringBuilder();
                    StringBuilder content4 = new StringBuilder();
                    Scanner scanner = new Scanner(file);
                    scanner.nextLine();
                    scanner.nextLine();
                    String holder;
                    while (scanner.hasNextLine()) {
                        if (part1) {
                            holder = scanner.nextLine();
                            if (holder.equals("# users.csv")) {
                                scanner.nextLine();
                                part1 = false;
                                part2 = true;
                            } else {
                                content1.append(holder).append("\n");
                            }
                        } else if (part2) {
                            holder = scanner.nextLine();
                            if (holder.substring(0, holder.indexOf(",")).equals(name)) { // Used to create gap for replacing user
                                part2 = false;
                            } else {
                                content2.append(holder).append("\n");
                            }                        
                        } else {
                            scanner.useDelimiter("\\A");
                            content4.append(scanner.next());
                        }
                    }
                    scanner.close();

                    // Recreates the file starting with highscores
                    BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORES));
                    writer.write("# high_scores.csv\nGame Mode,Rank 1,Rank 2,Rank 3\n");

                    String[] lines = content1.toString().split("\n");
                    // Changes leader boards if necessary
                    if (!maze) {
                        if (crazyApples) { // Crazy Apples Snake
                            if (snake.length > this.uCrazyHigh) {
                                this.uCrazyHigh = snake.length;
                            }
                            writer.write(lines[0] + "\n");
                            writer.write(lines[1] + "\n");
                            if (allTHighCrazy[0] < snake.length) {
                                allTHighCrazy[2] = allTHighCrazy[1];
                                allTHighCrazyNames[2] = allTHighCrazyNames[1];
                                allTHighCrazy[1] = allTHighCrazy[0];
                                allTHighCrazyNames[1] = allTHighCrazyNames[0];
                                allTHighCrazy[0] = snake.length;
                                allTHighCrazyNames[0] = this.name;
                            } else if (allTHighCrazy[1] < snake.length) {
                                allTHighCrazy[2] = allTHighCrazy[1];
                                allTHighCrazyNames[2] = allTHighCrazyNames[1];
                                allTHighCrazy[1] = snake.length;
                                allTHighCrazyNames[1] = this.name;
                            } else if (allTHighCrazy[2] < snake.length) {
                                allTHighCrazy[2] = snake.length;
                                allTHighCrazyNames[2] = this.name;
                            }
                            writer.write(String.format("Crazy Apples,%s-%d,%s-%d,%s-%d\n", allTHighCrazyNames[0], allTHighCrazy[0], allTHighCrazyNames[1], allTHighCrazy[1], allTHighCrazyNames[2], allTHighCrazy[2]));
                            writer.write(lines[3] + "\n");
                        } else if (speed) { // Speed Snake
                            if (snake.length > this.uSpeedHigh) {
                                this.uSpeedHigh = snake.length;
                            }
                            writer.write(lines[0] + "\n");
                            if (allTHighSpeed[0] < snake.length) {
                                allTHighSpeed[2] = allTHighSpeed[1];
                                allTHighSpeedNames[2] = allTHighSpeedNames[1];
                                allTHighSpeed[1] = allTHighSpeed[0];
                                allTHighSpeedNames[1] = allTHighSpeedNames[0];
                                allTHighSpeed[0] = snake.length;
                                allTHighSpeedNames[0] = this.name;
                            } else if (allTHighSpeed[1] < snake.length) {
                                allTHighSpeed[2] = allTHighSpeed[1];
                                allTHighSpeedNames[2] = allTHighSpeedNames[1];
                                allTHighSpeed[1] = snake.length;
                                allTHighSpeedNames[1] = this.name;
                            } else if (allTHighSpeed[2] < snake.length) {
                                allTHighSpeed[2] = snake.length;
                                allTHighSpeedNames[2] = this.name;
                            }
                            writer.write(String.format("Speed,%s-%d,%s-%d,%s-%d\n", allTHighSpeedNames[0], allTHighSpeed[0], allTHighSpeedNames[1], allTHighSpeed[1], allTHighSpeedNames[2], allTHighSpeed[2]));
                            writer.write(lines[2] + "\n");
                            writer.write(lines[3] + "\n");
                        } else { // Regular Snake
                            if (snake.length > this.uRegHigh) {
                                this.uRegHigh = snake.length;
                            }
                            if (allTHighReg[0] < snake.length) {
                                allTHighReg[2] = allTHighReg[1];
                                allTHighRegNames[2] = allTHighRegNames[1];
                                allTHighReg[1] = allTHighReg[0];
                                allTHighRegNames[1] = allTHighRegNames[0];
                                allTHighReg[0] = snake.length;
                                allTHighRegNames[0] = this.name;
                            } else if (allTHighReg[1] < snake.length) {
                                allTHighReg[2] = allTHighReg[1];
                                allTHighRegNames[2] = allTHighRegNames[1];
                                allTHighReg[1] = snake.length;
                                allTHighRegNames[1] = this.name;
                            } else if (allTHighReg[2] < snake.length) {
                                allTHighReg[2] = snake.length;
                                allTHighRegNames[2] = this.name;
                            }
                            writer.write(String.format("Snake,%s-%d,%s-%d,%s-%d\n", allTHighRegNames[0], allTHighReg[0], allTHighRegNames[1], allTHighReg[1], allTHighRegNames[2], allTHighReg[2]));
                            writer.write(lines[1] + "\n");
                            writer.write(lines[2] + "\n");
                            writer.write(lines[3] + "\n");
                        }
                    } else { // Random Maze Snake (or maybe some edge case)
                        writer.write(lines[0] + "\n");
                        writer.write(lines[1] + "\n");
                        writer.write(lines[2] + "\n");
                        this.uMazeComp += mazesComp;
                        mazesComp = 0;
                        if (allTHighMaze[0] < this.uMazeComp) {
                            if (allTHighMazeNames[0].equals(this.name)) {
                                allTHighMaze[0]++;
                            } else if (allTHighMazeNames[1].equals(this.name)) {
                                allTHighMazeNames[1] = allTHighMazeNames[0];
                                allTHighMaze[1] = allTHighMaze[0];
                                allTHighMazeNames[0] = this.name;
                                allTHighMaze[0] = this.uMazeComp;
                            } else {
                                allTHighMaze[2] = allTHighMaze[1];
                                allTHighMazeNames[2] = allTHighMazeNames[1];
                                allTHighMaze[1] = allTHighMaze[0];
                                allTHighMazeNames[1] = allTHighMazeNames[0];
                                allTHighMaze[0] = this.uMazeComp;
                                allTHighMazeNames[0] = this.name;
                            }
                        } else if (allTHighMaze[1] < this.uMazeComp) {
                            if (allTHighMazeNames[1].equals(this.name)) {
                                allTHighMaze[1]++;
                            } else {
                                allTHighMaze[2] = allTHighMaze[1];
                                allTHighMazeNames[2] = allTHighMazeNames[1];
                                allTHighMaze[1] = this.uMazeComp;
                                allTHighMazeNames[1] = this.name;
                            }
                        } else if (allTHighMaze[2] < this.uMazeComp) {
                            if (allTHighMazeNames[2].equals(this.name)) {
                                allTHighMaze[2]++;
                            } else {
                                allTHighMaze[2] = this.uMazeComp;
                                allTHighMazeNames[2] = this.name;
                            }
                        }
                        writer.write(String.format("Most Mazes Complete,%s-%d,%s-%d,%s-%d\n", allTHighMazeNames[0], allTHighMaze[0], allTHighMazeNames[1], allTHighMaze[1], allTHighMazeNames[2], allTHighMaze[2]));
                    }
                    writer.write("\n# users.csv\nName,Snake,Speed,Crazy Apples,Most Mazes Complete\n");
                    writer.write(content2.toString());
                    writer.write(String.format("%s,%d,%d,%d,%d\n", this.name, this.uRegHigh, this.uSpeedHigh, this.uCrazyHigh, this.uMazeComp));
                    if (content4.length() != 0) {
                        writer.write(content4.toString());
                    }
                    writer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    BufferedWriter writerSafety = new BufferedWriter(new FileWriter(HIGH_SCORES));
                    writerSafety.write(safety);
                    writerSafety.close();
                }
                safetyScanner.close();
                changeLeaderBoard();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void updateScoresForGuest() {
            try {
                File file = new File(HIGH_SCORES);
                // So whole file won't be deleted if things go wrong
                Scanner safetyScanner = new Scanner(file);
                safetyScanner.useDelimiter("\\A");
                String safety = safetyScanner.next();
                try {
                    // Reads the file into different parts
                    // part1 = highscore part, part2 = everything else
                    boolean part1 = true;
                    StringBuilder content1 = new StringBuilder();
                    StringBuilder content2 = new StringBuilder();
                    Scanner scanner = new Scanner(file);
                    scanner.nextLine();
                    scanner.nextLine();
                    String holder;
                    while (scanner.hasNextLine()) {
                        if (part1) {
                            holder = scanner.nextLine();
                            if (holder.equals("# users.csv")) {
                                scanner.nextLine();
                                part1 = false;
                            } else {
                                content1.append(holder).append("\n");
                            }
                        } else {
                            scanner.useDelimiter("\\A");
                            content2.append(scanner.next());
                        }
                    }
                    scanner.close();

                    // Recreates the file starting with highscores
                    BufferedWriter writer = new BufferedWriter(new FileWriter(HIGH_SCORES));
                    writer.write("# high_scores.csv\nGame Mode,Rank 1,Rank 2,Rank 3\n");
                    String[] lines = content1.toString().split("\n");
                    if (!maze) {
                        if (crazyApples) { // Crazy Apples Snake
                            writer.write(lines[0] + "\n");
                            writer.write(lines[1] + "\n");
                            if (allTHighCrazy[0] < snake.length) {
                                allTHighCrazy[2] = allTHighCrazy[1];
                                allTHighCrazyNames[2] = allTHighCrazyNames[1];
                                allTHighCrazy[1] = allTHighCrazy[0];
                                allTHighCrazyNames[1] = allTHighCrazyNames[0];
                                allTHighCrazy[0] = snake.length;
                                allTHighCrazyNames[0] = this.name;
                            } else if (allTHighCrazy[1] < snake.length) {
                                allTHighCrazy[2] = allTHighCrazy[1];
                                allTHighCrazyNames[2] = allTHighCrazyNames[1];
                                allTHighCrazy[1] = snake.length;
                                allTHighCrazyNames[1] = this.name;
                            } else if (allTHighCrazy[2] < snake.length) {
                                allTHighCrazy[2] = snake.length;
                                allTHighCrazyNames[2] = this.name;
                            }
                            writer.write(String.format("Crazy Apples,%s-%d,%s-%d,%s-%d\n", allTHighCrazyNames[0], allTHighCrazy[0], allTHighCrazyNames[1], allTHighCrazy[1], allTHighCrazyNames[2], allTHighCrazy[2]));
                            writer.write(lines[3] + "\n");
                        } else if (speed) { // Speed Snake
                            writer.write(lines[0] + "\n");
                            if (allTHighSpeed[0] < snake.length) {
                                allTHighSpeed[2] = allTHighSpeed[1];
                                allTHighSpeedNames[2] = allTHighSpeedNames[1];
                                allTHighSpeed[1] = allTHighSpeed[0];
                                allTHighSpeedNames[1] = allTHighSpeedNames[0];
                                allTHighSpeed[0] = snake.length;
                                allTHighSpeedNames[0] = this.name;
                            } else if (allTHighSpeed[1] < snake.length) {
                                allTHighSpeed[2] = allTHighSpeed[1];
                                allTHighSpeedNames[2] = allTHighSpeedNames[1];
                                allTHighSpeed[1] = snake.length;
                                allTHighSpeedNames[1] = this.name;
                            } else if (allTHighSpeed[2] < snake.length) {
                                allTHighSpeed[2] = snake.length;
                                allTHighSpeedNames[2] = this.name;
                            }
                            writer.write(String.format("Speed,%s-%d,%s-%d,%s-%d\n", allTHighSpeedNames[0], allTHighSpeed[0], allTHighSpeedNames[1], allTHighSpeed[1], allTHighSpeedNames[2], allTHighSpeed[2]));
                            writer.write(lines[2] + "\n");
                            writer.write(lines[3] + "\n");
                        } else { // Regular Snake
                            if (allTHighReg[0] < snake.length) {
                                allTHighReg[2] = allTHighReg[1];
                                allTHighRegNames[2] = allTHighRegNames[1];
                                allTHighReg[1] = allTHighReg[0];
                                allTHighRegNames[1] = allTHighRegNames[0];
                                allTHighReg[0] = snake.length;
                                allTHighRegNames[0] = this.name;
                            } else if (allTHighReg[1] < snake.length) {
                                allTHighReg[2] = allTHighReg[1];
                                allTHighRegNames[2] = allTHighRegNames[1];
                                allTHighReg[1] = snake.length;
                                allTHighRegNames[1] = this.name;
                            } else if (allTHighReg[2] < snake.length) {
                                allTHighReg[2] = snake.length;
                                allTHighRegNames[2] = this.name;
                            }
                            writer.write(String.format("Snake,%s-%d,%s-%d,%s-%d\n", allTHighRegNames[0], allTHighReg[0], allTHighRegNames[1], allTHighReg[1], allTHighRegNames[2], allTHighReg[2]));
                            writer.write(lines[1] + "\n");
                            writer.write(lines[2] + "\n");
                            writer.write(lines[3] + "\n");
                        }
                    } else { // Random Maze Snake
                        writer.write(lines[0] + "\n");
                        writer.write(lines[1] + "\n");
                        writer.write(lines[2] + "\n");
                        writer.write(lines[3] + "\n");
                    }

                    writer.write("\n# users.csv\nName,Snake,Speed,Crazy Apples,Most Mazes Complete\n");
                    writer.write(content2.toString());
                    writer.close();

                    changeLeaderBoard();
                } catch (Exception e) {
                    System.out.println(e.toString());
                    BufferedWriter writerSafety = new BufferedWriter(new FileWriter(HIGH_SCORES));
                    writerSafety.write(safety);
                    writerSafety.close();
                }
                safetyScanner.close();
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }

        /**
         * This is just a debugging method used for not having to open the file directly
         * Prints to the console the file
         */
        private void readAndPrint() {
            try {
                Scanner reader = new Scanner(new File(HIGH_SCORES));
                while (reader.hasNextLine()) {
                    System.out.println(reader.nextLine().toString());
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void setUser(String name) {
            this.name = name;
            highestReg = 0;
            highestSpeed = 0;
            highestCrazy = 0;
            mazesComp = 0;
            this.updateUserInfo();
        }
    }

    /**
     * Makes the snake game and places a snake in the middle of the board with no movement yet
     */
    public SnakeGame() {
        this.allTHighCrazy = new int[3];
        this.allTHighSpeed = new int[3];
        this.allTHighReg = new int[3];
        this.allTHighMaze = new int[3];
        this.allTHighCrazyNames = new String[3];
        this.allTHighSpeedNames = new String[3];
        this.allTHighRegNames = new String[3];
        this.allTHighMazeNames = new String[3];
        this.board = new Board();
        this.snake = new Snake();
        this.createLeaderBoard();
        this.player = new User("Guest");
    }

    public static void main(String[] args) { launch(args); }
}