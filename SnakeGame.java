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

        Scene scene = new Scene(root, BOARD_SIZE * BLOCK_SIZE, BOARD_SIZE * (BLOCK_SIZE + 6));

        // Game controls
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case UP:
                    case W:
                        if (up != true && down == false && !pause) {
                            changeDir = true;
                            moving = true;
                            up = true;
                            left = false;
                            right = false;
                        } break;
                    case DOWN:
                    case S:
                        if (down != true && up == false && !pause) {
                            changeDir = true;
                            moving = true;
                            down = true;
                            left = false;
                            right = false;
                        } break;
                    case LEFT:
                    case A:
                        if (left != true && right == false && !pause) {
                            changeDir = true;
                            moving = true;
                            up = false;
                            down = false;
                            left = true;
                        } break;
                    case RIGHT:
                    case D:
                        if (right != true && left == false && !pause) {
                            changeDir = true;
                            moving = true;
                            up = false;
                            down = false;
                            right = true;
                        } break;
                    case SPACE:
                        if (gameOver) {
                            restart = true;
                        } else if (moving) { // Moving keeps players from pausing at the start
                            pause = !pause;
                            if (pause) {
                                score.setText(String.format("Current Length: %d - Press SPACE to Unpause", snake.length));
                            } else {
                                score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
                            }
                        } break;
                    default:
                        break;
                }
            }
        });

        // Building other game mode options
        Button regSnake = new Button("Snake");
        regSnake.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 3, BLOCK_SIZE);
        regSnake.setFocusTraversable(false); // Necessary for arrow keys to work in game
        regSnake.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                resetBoard();
                crazyApples = false;
                wait = REG_WAIT;
                score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
                gameType.setText("Playing: Snake");
                highScore.setText(String.format("Session Highscore for Snake: %d", highestReg));
            }
        });
        Button fastSnake = new Button("Speed Snake");
        fastSnake.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 3, BLOCK_SIZE);
        fastSnake.setFocusTraversable(false);
        fastSnake.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                resetBoard();
                crazyApples = false;
                wait = (long) (REG_WAIT / 3);
                score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
                gameType.setText("Playing: Speed Snake");
                highScore.setText(String.format("Session Highscore for Speed Snake: %d", highestFast));
            }
        });
        Button crazySnake = new Button("Crazy Apples Snake");
        crazySnake.setMinSize((BOARD_SIZE * BLOCK_SIZE) / 3, BLOCK_SIZE);
        crazySnake.setFocusTraversable(false);
        crazySnake.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                resetBoard();
                crazyApples = true;
                wait = (long) (REG_WAIT * 4 / 3);
                score.setText(String.format("Current Length: %d - Press any direction to play", snake.length));
                gameType.setText("Playing: Crazy Apples Snake");
                highScore.setText(String.format("Session Highscore for Crazy Apples: %d", highestCrazy));
            }
        });
        HBox box = new HBox();
        box.getChildren().addAll(regSnake, fastSnake, crazySnake);
        root.add(box, 0, BOARD_SIZE + 3);
        GridPane.setColumnSpan(box, GridPane.REMAINING);
        root.add(gameType, (int) (BOARD_SIZE / 3), BOARD_SIZE + 4);
        GridPane.setColumnSpan(gameType, GridPane.REMAINING);

        stage.setScene(scene);
        stage.show();
        
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
        timer.start();
    }

    /**
     * Resets the board for a new game; will clear all stats except high score
     * Not an instance method of class Board because we need to edit the game as a whole
     * Also, save the high score information
     */
    private void resetBoard() {
        // If player changes game while still playing
        if (moving) {
            if (crazyApples) {
                wait = (long) (REG_WAIT * 4 / 3);
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
        } else {
            board.board[x][y].type = blockType.APPLE;
        }
    }

    // These represent the different types of blocks possible
    private static enum blockType {
        SNAKE,
        APPLE,
        EMPTY;
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
