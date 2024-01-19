# About the Game
This program is the classic Snake game you can find on Google or any other site. There are four game modes: Snake, Speed Snake, Crazy Apples Snake, and Random Maze Snake (maze generation discussed at the end). Snake is the classic game mode you are used to. Speed Snake is just 3x as fast as Snake. Crazy Apples Snake spawns apples every 3 blocks moved and gets progressively faster the longer you stay alive (note not based on your size). Random Maze Snake is less of a snake game and more of just solving a random maze (more later). These game modes can be toggled in the GUI by simply clicking the buttons. 

A player can move using W, S, A, D or UP, DOWN, LEFT, RIGHT. A player can pause using SPACE. Once a player dies by touching the border or themselves, they can press space to restart. The game will keep track of the highest session score for each game mode. The game also has a leaderboard and a username system. Players can sign in and keep track of their all-time highs. Simply compile and run the file to play or download the .jar file (requires at least JRE installed). Once the game is started, press any direction to play.

# Running the Game
Snake.jar should work for everyone. If you run it from the command prompt, JavaFX will give its classic warning; there are no problems, so just ignore it. In the case where Snake.jar does not work. You will need to have JavaFX downloaded. 

### For IntelliJ
The Main.java and SnakeGame.java are the files you will need to run if you are using IntelliJ. Main just runs SnakeGame. Place both the files in a new folder called SnakeGame inside of src. You need to add JavaFX to your library: File>Project Stucture>Libraries>"+"> find wherever JavaFX lib downloaded and add base, graphics, and controls to the library. You should now be able to run it in IntelliJ. If you want to create your own .jar, "Export JavaFX 11, 15 or 17 projects into an executable jar file with IntelliJ [2022]" by Random Code can help with that (make sure to add base, graphics, and controls in the artifact if you do not have them there already when making the .jar).

### For Command Prompt
You will need to edit the SnakeGame file. Simply just delete or comment out the very top line package SnakeGame. You can then do "javac --module-path [Your Path Here] --add-modules javafx.controls,javafx.base,javafx.graphics SnakeGame.java" replacing Your Path Here with the path to your JavaFX lib file.

# Goal of this Project and the Implementation Choices
The goal of this project was to create a functioning GUI and practice my OOP skills. I thought making a game would be a fun way to do both of these. I chose Java as my language since I recently did a project in Python and another in MySQL and had not done one in Java. Because of the project, I now have much more experience with building GUIs with JavaFX and improved my knowledge of Java. To make the GUI, I used JavaFX. The playing field itself is a GridPane of Rectangles that change colors. I used AnimationTimer in JavaFX to move the game forward and implemented a KeyListener to take commands from the player.

The OOP part comes in with the backing structure of the game. I am sure the visual and backing structures could easily be combined into one for the first three games, but I chose to make them separate. This was so I have direct access to a lot of moving parts, have more of the code written by me directly, and tamper with the game mechanics without visually showing it (which is important for Random Maze Snake). Within my implementation, I have some private classes (Block, Board, Snake, and User) and the main public class, SnakeGame. The Blocks make up a board and have a specific type: EMPTY, APPLE, or SNAKE. These types can be found in the enum blockType. The Snake class represents the snake and the blocks it takes up on the board via a LinkedList. The Board itself is a main part of the backing structure for the game and where the action takes place. The User class is where the leaderboard and scores are kept.

I chose to make all classes private for three reasons: 1) I wanted to have direct access to everything. I did not want to have to call getters and setters for such a small project. Also, the private classes only interact directly with the backing mechanics of the game itself 2) most of the classes are small (except the User class) 3) I wanted to have all the code in one spot. I did not want to have to sort through many files.

# Random Maze Snake Play and Implementation
Random Maze Snake is quite different from the other three game modes in play and in code. The game always starts with the player in the top left corner of the board. The player must navigate a randomly generated maze to find the apple on the right side (where the specific location is changed each game). This has a catch: the player can only see one square away in any direction. Therefore, they must almost blindly traverse the maze to find the apple. Once the apple is found, the maze is fully revealed and players are free to roam the maze or press SPACE to start a new maze.

There are some coding highlights from Random Maze Snake I want to cover. This game mode does not use a JavaFX AnimatoinTimer, so the player does not constantly move. Instead, movement is done by player input. The program will try to generate a random maze by placing random walls across the map; it turns 1/3 of the total blocks into walls. Of course, this could lead to unsolvable mazes, so I have implemented a maze solver. This solver uses a stack-based Depth-First Search (DFS) to traverse the maze and terminates when the apple is found or the stack is empty. If the apple is not found, a new random maze is generated. This is done a maximum of 200 times. As of now if there are somehow 200 mazes generated with no solution (which is HIGHLY unlikely), an error message pops up in the GUI saying a maze could not be generated and instructs the user to try to restart the game. I would like to add a default maze to use in this case in the future, but I do not think it is super important since at least half the games generated are solvable (from a little testing).

# Leaderboard
The leaderboard is a recent implementation. It has a private class User that backs it. It allows users to select an account, add new accounts, or delete old accounts. This allows users to keep track of their high scores. There is also a leaderboard of the highest scores from each type of game. I chose to keep track of everything in Highscore.txt which is formatted like a .csv file. Just download the file and set it in the same place as the .jar file (or .java if .jar does not work for some reason).

I have played around with the leaderboard and fixed most of the bugs. It should be very functional, but it was actually harder to implement than the game itself and has more edge cases. So, if there are any bugs I missed, please let me know!

This was just a quick overview of the code. Hope you enjoy the game!
