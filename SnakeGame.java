import java.util.LinkedList;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

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
    private static final int BLOCK_SIZE = 24;
    private static final long REG_WAIT = 180_000_000;
    private boolean up, down, left, right, pause, restart;
    private Rectangle[][] backing = new Rectangle[BOARD_SIZE][BOARD_SIZE];

    private Snake snake;
    private Board board;
    private boolean gameOver;
    private int highestReg;
    private int highestFast;
    private int highestCrazy;
    private boolean crazyApples;
    private long wait;
    private boolean moving;
    private boolean changeDir;
    private boolean maze;

    /**
     * This is the stage for the snake game; all the main interactions happen here
     * 
     * @param stage the stage for the game
     */
    public void start(Stage stage) {
        // Make a grid to represent the game
        GridPane root = new GridPane();
        for (int i = 0; i < BOARD_SIZE; i++) {
            root.getColumnConstraints().add(new ColumnConstraints(BLOCK_SIZE));
        }

        for (int i = 0; i < BOARD_SIZE; i++) {
            root.getRowConstraints().add(new RowConstraints(BLOCK_SIZE));
        }
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Rectangle rec = new Rectangle(BLOCK_SIZE, BLOCK_SIZE, Color.ORANGE);
                // The backing array is used to access the rectangles later
                backing[col][row] = rec;
                root.add(rec, col, row);
            }
        }

        // Initially sets up the game
        Block startingBlock = snake.snakeBlocks.getFirst();
        backing[startingBlock.x][startingBlock.y].setFill(Color.GREEN);
        board.randomApple();
        Label score = new Label(String.format("Current Length: %d - Press any direction to play", snake.length));
        root.add(score, 0, BOARD_SIZE + 1);
        // Playing Regular Snake
        Label highScore = new Label(String.format("Session Highscore for Snake: %d", highestReg));
        root.add(highScore, 0, BOARD_SIZE + 2);
        GridPane.setColumnSpan(score, GridPane.REMAINING);
        GridPane.setColumnSpan(highScore, GridPane.REMAINING);
        Label gameType = new Label("Playing: Snake"); // Added further down for style purposes

        Scene scene = new Scene(root, BOARD_SIZE * BLOCK_SIZE, BOARD_SIZE * (BLOCK_SIZE + 7)); // + num to increase height as needed

        // Game controls
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case UP:
                    case W:
                        if (maze) {
                            moveUpMaze(score);
                        } else if (up != true && down == false && !pause) {
                            changeDir = true;
                            moving = true;
                            up = true;
                            left = false;
                            right = false;
                        } break;
                    case DOWN:
                    case S:
                        if (maze) {
                            moveDownMaze(score);
                        } else if (down != true && up == false && !pause) {
                            changeDir = true;
                            moving = true;
                            down = true;
                            left = false;
                            right = false;
                        } break;
                    case LEFT:
                    case A:
                        if (maze) {
                            moveLeftMaze(score);
                        } else if (left != true && right == false && !pause) {
                            changeDir = true;
                            moving = true;
                            up = false;
                            down = false;
                            left = true;
                        } break;
                    case RIGHT:
                    case D:
                        if (maze) {
                            moveRightMaze(score);
                        } else if (right != true && left == false && !pause) {
                            changeDir = true;
                            moving = true;
                            up = false;
                            down = false;
                            right = true;
                        } break;
                    case SPACE:
                        if (gameOver && !maze) {
                            restart = true;
                        } else if (moving && !maze) { // Moving keeps players from pausing at the start
                            pause = !pause;
                            if (pause) {
                                score.setText(String.format("Current Length: %d - Press SPACE to Unpause", snake.length));
                            } else {
                                score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
                            }
                        } else if (gameOver && maze) {
                            mazeBoard(score);
                            showAndHide();
                            score.setText("Make it to the Apple that has been Randomly Placed on the Right Side");
                            gameType.setText("Playing: Random Maze Snake");
                            highScore.setText("You can only see one block around you in any direction");
                        } break;
                    default:
                        break;
                }
            }
        });

        // The timer that keeps the game running
        AnimationTimer timer = new AnimationTimer() {
            private long last_update = 0;
            private int counter = 0;
            @Override
            public void handle(long now) {
                // Allows for rapid change of direction
                if (changeDir && !gameOver && moving) {
                    score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
                    changeDir = false;
                    if (up == true) {
                        moveUp(score);
                    } else if (down == true) {
                        moveDown(score);
                    } else if (left == true) {
                        moveLeft(score);
                    } else if (right == true) {
                        moveRight(score);
                    }
                    last_update = now;
                    if (crazyApples && counter >= 3) {
                        board.randomApple();
                        counter = -1;
                        wait -= 2_000_000;
                    }
                    counter++;
                } else if (now - last_update >= wait && !pause && !gameOver && moving) {
                    if (up == true) {
                        moveUp(score);
                    } else if (down == true) {
                        moveDown(score);
                    } else if (left == true) {
                        moveLeft(score);
                    } else if (right == true) {
                        moveRight(score);
                    }
                    last_update = now;
                    if (crazyApples && counter >= 3) {
                        board.randomApple();
                        counter = -1;
                        wait -= 1_500_000;
                    }
                    counter++;
                } else if (restart) {
                    resetBoard();
                    score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
                } else if (gameOver) {
                    if (crazyApples) {
                        wait = (long) (REG_WAIT * 4 / 3);
                        if (snake.length > highestCrazy) {
                            highestCrazy = snake.length;
                        }
                        highScore.setText(String.format("Session Highscore for Crazy Apples: %d", highestCrazy));
                    } else if (wait == REG_WAIT) {
                        if (snake.length > highestReg) {
                            highestReg = snake.length;
                        }
                        highScore.setText(String.format("Session Highscore for Snake: %d", highestReg));
                    } else {
                        if (snake.length > highestFast) {
                            highestFast = snake.length;
                        }
                        highScore.setText(String.format("Session Highscore for Speed Snake: %d", highestFast));
                    }
                    moving = false;
                    score.setText(String.format("Final Length: %d - Press SPACE to Restart", snake.length));
                }
            }
        };

        // Building other game mode options
        Button regSnake = new Button("Snake");
        regSnake.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 2, BLOCK_SIZE);
        regSnake.setFocusTraversable(false); // Necessary for arrow keys to work in game
        regSnake.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                timer.stop();
                resetBoard();
                crazyApples = false;
                wait = REG_WAIT;
                score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
                gameType.setText("Playing: Snake");
                highScore.setText(String.format("Session Highscore for Snake: %d", highestReg));
                timer.start();
            }
        });

        Button fastSnake = new Button("Speed Snake");
        fastSnake.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 2, BLOCK_SIZE);
        fastSnake.setFocusTraversable(false);
        fastSnake.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                timer.stop();
                resetBoard();
                crazyApples = false;
                wait = (long) (REG_WAIT / 3);
                score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
                gameType.setText("Playing: Speed Snake");
                highScore.setText(String.format("Session Highscore for Speed Snake: %d", highestFast));
                timer.start();
            }
        });

        Button crazySnake = new Button("Crazy Apples Snake");
        crazySnake.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 2, BLOCK_SIZE);
        crazySnake.setFocusTraversable(false);
        crazySnake.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                timer.stop();
                resetBoard();
                crazyApples = true;
                wait = (long) (REG_WAIT * 4 / 3);
                score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
                gameType.setText("Playing: Crazy Apples Snake");
                highScore.setText(String.format("Session Highscore for Crazy Apples: %d", highestCrazy));
                timer.start();
            }
        });

        Button mazeSnake = new Button("Random Maze Snake");
        mazeSnake.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 2, BLOCK_SIZE);
        mazeSnake.setFocusTraversable(false);
        mazeSnake.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                timer.stop();
                mazeBoard(score);
                showAndHide();
                score.setText("Make it to the Apple that has been Randomly Placed on the Right Side");
                gameType.setText("Playing: Random Maze Snake");
                highScore.setText("You can only see one block around you in any direction");
            }
        });

        HBox boxUp = new HBox();
        HBox boxDown = new HBox();
        boxUp.getChildren().addAll(regSnake, fastSnake);
        boxDown.getChildren().addAll(crazySnake, mazeSnake);
        root.add(boxUp, 0, BOARD_SIZE + 3);
        root.add(boxDown, 0, BOARD_SIZE + 4);
        GridPane.setColumnSpan(boxUp, GridPane.REMAINING);
        GridPane.setColumnSpan(boxDown, GridPane.REMAINING);
        root.add(gameType, (int) (BOARD_SIZE / 3), BOARD_SIZE + 5);
        GridPane.setColumnSpan(gameType, GridPane.REMAINING);

        stage.setScene(scene);
        stage.show();
        timer.start();
    }

    /**
     * Resets the board for a new game; will clear all stats except high score
     * Not an instance method of class Board because we need to edit the game as a whole
     * Also, save the high score information
     */
    private void resetBoard() {
        maze = false;
        // If player changes game while still playing
        if (moving) {
            if (crazyApples) {
                if (snake.length > highestCrazy) {
                    highestCrazy = snake.length;
                }
            } else if (wait == REG_WAIT) {
                if (snake.length > highestReg) {
                    highestReg = snake.length;
                }
            } else {
                if (snake.length > highestFast) {
                    highestFast = snake.length;
                }
            }
        }
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
        board.randomApple();
        snake.length = 1;
        gameOver = false;
        restart = false;
        pause = false;
        moving = false;
    }

    private void mazeBoard(Label score) {
        if (moving) {
            if (crazyApples) {
                if (snake.length > highestCrazy) {
                    highestCrazy = snake.length;
                }
            } else if (wait == REG_WAIT) {
                if (snake.length > highestReg) {
                    highestReg = snake.length;
                }
            } else {
                if (snake.length > highestFast) {
                    highestFast = snake.length;
                }
            }
        }
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                backing[i][j].setFill(Color.BLACK);
                board.board[i][j].type = blockType.EMPTY;
            }
        }
        maze = true;
        snake.snakeBlocks = new LinkedList<>();
        Block startingBlock = board.board[0][0];
        startingBlock.type = blockType.SNAKE;
        snake.snakeBlocks.add(startingBlock);
        snake.head = startingBlock;
        backing[startingBlock.x][startingBlock.y].setFill(Color.GREEN);
        crazyApples = false;
        up = false;
        down = false;
        left = false;
        right = false;
        snake.length = 1;
        gameOver = false;
        restart = false;
        pause = false;
        moving = false;
        randomMaze(0, score);
    }

    /**
     * Creates a random maze by randomly placing walls
     * solvableMaze() will test if the maze is solvable
     * If it is not solvable, it will generate a new random maze and try again
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
        LinkedList<Coord> stack = new LinkedList<>();
        stack.add(new Coord(0,0));
        while (!stack.isEmpty()) {
            Coord coor = stack.removeFirst();
            visited[coor.x][coor.y] = true;
            if (board.board[coor.x][coor.y].type == blockType.APPLE) {
                return true;
            }
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (coor.x + i >= 0 && coor.x + i < BOARD_SIZE && coor.y + j >= 0 && coor.y + j < BOARD_SIZE &&
                            (j == 0 || i == 0) && board.board[coor.x + i][coor.y + j].type != blockType.WALL && !visited[coor.x + i][coor.y + j]) {
                        stack.addFirst(new Coord(coor.x + i, coor.y + j));
                    }
                }
            }
        }
        return false;
    }

    private class Coord {
        private int x;
        private int y;

        private Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Moves the snake up; ends the game if an invalid move
     * Grows the snake if the next square is an apple
     */
    private void moveUp(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (y - 1 < 0 || board.board[x][y-1].type == blockType.SNAKE) {
            gameOver = true;
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
        }
    }

    /**
     * Used for maze game
     */
    private void moveUpMaze(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (y - 1 >= 0 && board.board[x][y-1].type != blockType.WALL) {
            if (board.board[x][y-1].type == blockType.APPLE) {
                gameOver = true;
                score.setText(String.format("You Made It to the Apple! - Press SPACE to Try Another Random Maze", snake.length));
                showMaze();
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

    /**
     * Moves the snake down; ends the game if an invalid move
     * Grows the snake if the next square is an apple
     */
    private void moveDown(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (y + 1 >= BOARD_SIZE || board.board[x][y+1].type == blockType.SNAKE) {
            gameOver = true;
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
        }
    }

    /**
     * Used for maze game
     */
    private void moveDownMaze(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (y + 1 < BOARD_SIZE && board.board[x][y+1].type != blockType.WALL) {
            if (board.board[x][y+1].type == blockType.APPLE) {
                gameOver = true;
                score.setText(String.format("You Made It to the Apple! - Press SPACE to Try Another Random Maze", snake.length));
                showMaze();
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

    /**
     * Moves the snake left; ends the game if an invalid move
     * Grows the snake if the next square is an apple
     */
    private void moveLeft(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (x - 1 < 0 || board.board[x-1][y].type == blockType.SNAKE) {
            gameOver = true;
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
        }
    }

    /**
     * Used for maze game
     */
    private void moveLeftMaze(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (x - 1 >= 0 && board.board[x-1][y].type != blockType.WALL) {
            if (board.board[x-1][y].type == blockType.APPLE) {
                gameOver = true;
                score.setText(String.format("You Made It to the Apple! - Press SPACE to Try Another Random Maze", snake.length));
                showMaze();
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

    /**
     * Moves the snake right; ends the game if an invalid move
     * Grows the snake if the next square is an apple
     */
    private void moveRight(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (x + 1 >= BOARD_SIZE || board.board[x+1][y].type == blockType.SNAKE) {
            gameOver = true;
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
        }
    }

    /**
     * Used for maze game
     */
    private void moveRightMaze(Label score) {
        int x = snake.head.x;
        int y = snake.head.y;
        if (x + 1 < BOARD_SIZE && board.board[x+1][y].type != blockType.WALL) {
            if (board.board[x+1][y].type == blockType.APPLE) {
                gameOver = true;
                score.setText(String.format("You Made It to the Apple! - Press SPACE to Try Another Random Maze", snake.length));
                showMaze();
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
     * For maze game
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
    /**
     * Shows the entire maze once the apple is found
     */
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
     * This class presents the snake itself
     * It uses a linkedlist as the backing data structure for the snake
     */
    private class Snake {
        private int length;
        private LinkedList<Block> snakeBlocks;
        private Block head;

        private Snake(Block start) {
            start.type = blockType.SNAKE;
            this.head = start;
            this.snakeBlocks = new LinkedList<>();
            this.snakeBlocks.add(start);
            this.length = 1;
        }
    }

    /**
     * Makes the snake game and places a snake in the middle of the board with no movement yet
     */
    public SnakeGame() {
        this.board = new Board();
        this.snake = new Snake(board.board[(int) (BOARD_SIZE / 2)][(int) (BOARD_SIZE / 2)]);
        this.wait = REG_WAIT;
    }

    public static void main(String[] args) { launch(args); }
}
