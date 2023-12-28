import java.util.LinkedList;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.input.KeyEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.animation.AnimationTimer;

public class SnakeGame extends Application {

    private static int SIZE = 14;
    private boolean up, down, left, right, pause, restart;
    private Rectangle[][] backing = new Rectangle[SIZE][SIZE];

    private Snake snake;
    private Board board;
    private boolean gameOver;
    private boolean complete;
    private int highest;


    public void start(Stage stage) {
        int blockSize = 30;
        GridPane root = new GridPane();
        for (int i = 0; i < SIZE; i++) {
            root.getColumnConstraints().add(new ColumnConstraints(blockSize));
        }

        for (int i = 0; i < SIZE; i++) {
            root.getRowConstraints().add(new RowConstraints(blockSize));
        }
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Rectangle rec = new Rectangle(blockSize, blockSize, Color.ORANGE);
                backing[col][row] = rec;
                root.add(rec, col, row);
            }
        }
        Block startingBlock = snake.snakeBlocks.getFirst();
        backing[startingBlock.x][startingBlock.y].setFill(Color.GREEN);
        board.randomApple();

        Label score = new Label(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
        root.add(score, 0, SIZE + 1);
        Label highScore = new Label(String.format("Session Highscore: %d", highest));
        root.add(highScore, 0, SIZE + 2);
        GridPane.setColumnSpan(score, GridPane.REMAINING);
        GridPane.setColumnSpan(highScore, GridPane.REMAINING);

        Scene scene = new Scene(root, SIZE * blockSize, SIZE * blockSize + 20 + 20);

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case UP:
                    case W:
                        if (up != true && down == false && complete) {
                            complete = false;
                            up = true;
                            left = false;
                            right = false;
                        } break;
                    case DOWN:
                    case S:
                        if (down != true && up == false && complete) {
                            complete = false;
                            down = true;
                            left = false;
                            right = false;
                        } break;
                    case LEFT:
                    case A:
                        if (left != true && right == false && complete) {
                            complete = false;
                            up = false;
                            down = false;
                            left = true;
                        } break;
                    case RIGHT:
                    case D:
                        if (right != true && left == false && complete) {
                            complete = false;
                            up = false;
                            down = false;
                            right = true;
                        } break;
                    case SPACE:
                        if (gameOver) {
                            restart = true;
                        } else {
                            pause = !pause;
                        } break;
                }
            }
        });

        stage.setScene(scene);
        stage.show();
        
        AnimationTimer timer = new AnimationTimer() {
            private long last_update = 0;
            @Override
            public void handle(long now) {
                if (now - last_update >= 250_000_000 && !pause && !gameOver) {
                    score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
                    if (up == true) {
                        moveUp();
                        complete = true;
                    } else if (down == true) {
                        moveDown();
                        complete = true;
                    } else if (left == true) {
                        moveLeft();
                        complete = true;
                    } else if (right == true) {
                        moveRight();
                        complete = true;
                    }
                    last_update = now;
                } else if (restart) {
                    resetBoard();
                    snake.length = 1;
                    gameOver = false;
                    restart = false;
                    score.setText(String.format("Current Length: %d - Press SPACE to Pause", snake.length));
                } else if (gameOver) {
                    if (snake.length > highest) {
                        highest = snake.length;
                    }
                    score.setText(String.format("Final Length: %d - Press SPACE to Restart", snake.length));
                    highScore.setText(String.format("Session Highscore: %d", highest));
                } else if (pause) {
                    score.setText(String.format("Current Length: %d - Press SPACE to Unpause", snake.length));
                }
            }
        };
        timer.start();
    }

    private void resetBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                backing[i][j].setFill(Color.ORANGE);
                board.board[i][j].type = blockType.EMPTY;
            }
        }
        snake.snakeBlocks = new LinkedList<>();
        Block startingBlock = board.board[(int) (SIZE / 2)][(int) (SIZE / 2)];
        startingBlock.type = blockType.SNAKE;
        snake.snakeBlocks.add(startingBlock);
        snake.head = startingBlock;
        backing[startingBlock.x][startingBlock.y].setFill(Color.GREEN);
        up = false;
        down = false;
        left = false;
        right = false;
        board.randomApple();
        complete = true;
    }

    private void moveUp() {
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
            }
        }
    }

    private void moveDown() {
        int x = snake.head.x;
        int y = snake.head.y;
        if (y + 1 >= SIZE || board.board[x][y+1].type == blockType.SNAKE) {
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
            }
        }
    }

    private void moveLeft() {
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
            }
        }
    }

    private void moveRight() {
        int x = snake.head.x;
        int y = snake.head.y;
        if (x + 1 >= SIZE || board.board[x+1][y].type == blockType.SNAKE) {
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
            }
        }
    }

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

    enum blockType {
        SNAKE,
        APPLE,
        EMPTY;
    }

    private class Block {
        private int x;
        private int y;
        private blockType type = blockType.EMPTY;

        private Block(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private class Board {
        private Block[][] board;
        public Board() {
            board = new Block[SIZE][SIZE];
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    board[i][j] = new Block(i, j);
                }
            }
        }
        
        private void randomApple() {
            int x = 0;
            int y = 0;
            boolean notPlaced = true;
            while (notPlaced) {
                x = (int) (Math.random() * SIZE);
                y = (int) (Math.random() * SIZE);
                if (board[x][y].type == blockType.EMPTY) {
                    notPlaced = false;
                    board[x][y].type = blockType.APPLE;
                    backing[x][y].setFill(Color.RED);
                }
            }
        }
    }

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

    public SnakeGame() {
        this.board = new Board();
        this.snake = new Snake(board.board[(int) (SIZE / 2)][(int) (SIZE / 2)]);
        this.complete = true;
    }

    public static void main(String[] args) { launch(args); }
}