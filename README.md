# About the Game
This program is the classic Snake game you can find on Google or any other site. There are three game modes: Snake, Speed Snake, Crazy Apples Snake. Snake is the classic gam mode. Speed Snake is just 3x as fast. Crazy Apples Snake spawns apples every 3 blocks moved
and get progressively faster the longer you stay alive. These game modes can be toggled while playing in the GUI by simply clicking the buttons. A player can move using W, S, A, D or UP, DOWN, LEFT, RIGHT. A player can pause using SPACE.
Once a player dies by touching the border or themselves, they can press space to restart. The game will keep track of the highest session score for each game mode. Simply compile and run the file to play. Once the game is started, press any direction to play.

# Goal of this Project and the Implementation Choices
The goal of this project was to create a functioning GUI and practice my OOP skills. I thought making a game would be a fun way to do both of these. I chose Java as my language since I recently did a project in Python and another in MySQL.
To make the GUI, I used JavaFX. The playing field itself is simply a GridPane of Rectangles that change colors. I used AnimationTimer in JavaFX to move the game forward and implemented a KeyListener to take commands from the player. For more details, scroll through
the code, but these were the highlights.

The OOP part comes in with the backing structure of the game. I am sure the visual struture and backing structure could have easily been combined into one, but I chose to make them separate so I would have direct access to a lot of moving parts and have a lot
of the code written by me directly. This also allows for tampering with the game without visually showing it if the programmer desires. Within my implementation, I have private classes Block, Board, and Snake and public class SnakeGame. 
Each class has some direct interaction with the game and helps keep chunks of the game orangized. The Blocks make up a board and have a specific type: EMPTY, APPLE, SNAKE. These types can be found in the enum blockType. The Snake class represents the snake
and the blocks it takes up on the board via a LinkedList. The Board itself is a main part of the backing structure for the game and where the action takes place.

This was just a quick and dirty overview of the code. Hope you enjoy the game!
