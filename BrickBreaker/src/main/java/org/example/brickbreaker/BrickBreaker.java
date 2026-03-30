package org.example.brickbreaker;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * BrickBreaker Game
 *
 * A simple JavaFX-based brick breaker game.
 * The player controls a paddle to bounce a ball and destroy all bricks.
 * The game ends when the player clears all bricks (win) or misses the ball (lose).
 * Hunter Boyd, 03.29.26 - Per 4
 */
public class BrickBreaker extends Application {

   /* =========================
      Game Configuration
      ========================= */

    // Number of rows and columns of bricks
    int rows = 5;
    int cols = 5;

    // Dimensions of each brick
    int brickWidth = 100;
    int brickHeight = 30;

   /* =========================
      Game State Variables
      ========================= */

    // Current score (number of bricks destroyed)
    int Score = 0;

    // Flags for game outcome
    public boolean lose;
    public boolean win;

   /* =========================
      Ball Physics Properties
      ========================= */

    // Ball velocity in pixels per second
    Double velocityX = 120.0;
    Double velocityY = 120.0;

    // Multipliers used to reverse direction upon collision
    Double bounceX = -1.0;
    Double bounceY = -1.0;

   /* =========================
      Player Properties
      ========================= */

    // Paddle movement speed (pixels per key press)
    Double playerX = 20.0;

   /* =========================
      Game Objects
      ========================= */

    // Ball (logical representation; rendered via image)
    Circle circle = new Circle(150, 300, 30);

    // Player paddle
    Rectangle rect = new Rectangle(270, 400, 200, 25);

    // Ball image
    Image gball = new Image("/golfball.png");

   /* =========================
      Utility Objects
      ========================= */

    // Random generator for brick colors
    Random rand = new Random();

    // List storing all active bricks
    ArrayList<Rectangle> brickList = new ArrayList<>();

    // Alert dialog used for win/lose messages
    Alert lost = new Alert(Alert.AlertType.INFORMATION);

    @Override
    public void start(Stage stage) {

       /* =========================
          Scene & Canvas Setup
          ========================= */

        // Create canvas for rendering graphics
        Canvas canvas = new Canvas(720, 480);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Root container and scene
        Group root = new Group(canvas);
        Scene scene = new Scene(root, 720, 480);

        // Set visual properties of objects
        circle.setFill(Color.BLUE);
        rect.setFill(Color.BLACK);

        // Configure stage (window)
        stage.setTitle("Brick Breaker");
        stage.setScene(scene);

        // Add paddle to scene (ball is rendered manually)
        root.getChildren().add(rect);

       /* =========================
          Brick Generation
          ========================= */

        // Create a grid of bricks with alternating row offsets (staggered layout)
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                // Offset every other row to create a staggered pattern
                int offset = (i % 2 != 0) ? 50 : 0;

                // Create brick with calculated position
                Rectangle brick = new Rectangle(
                        j * brickWidth + offset + 110,
                        i * brickHeight,
                        brickWidth,
                        brickHeight
                );

                // Assign a random color to the brick
                brick.setFill(Color.rgb(
                        rand.nextInt(256),
                        rand.nextInt(256),
                        rand.nextInt(256)
                ));

                // Store and display brick
                brickList.add(brick);
                root.getChildren().add(brick);
            }
        }

       /* =========================
          Main Game Loop
          ========================= */

        AnimationTimer timer = new AnimationTimer() {

            // Stores previous frame time for deltaTime calculation
            long lastTime = 0;

            @Override
            public void handle(long now) {

                // Initialize lastTime on first frame
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                // Calculate elapsed time (seconds) since last frame
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                // Clear previous frame
                gc.clearRect(0, 0, 720, 480);

                /* -------- Ball Movement -------- */

                // Update ball position using velocity and deltaTime
                circle.setCenterX(circle.getCenterX() + velocityX * deltaTime);
                circle.setCenterY(circle.getCenterY() + velocityY * deltaTime);

                // Render score and ball image
                gc.fillText(String.valueOf(Score), 10, 240);
                gc.drawImage(gball,
                        circle.getCenterX() - 30,
                        circle.getCenterY() - 30,
                        90,
                        90
                );

                /* -------- Paddle Collision -------- */

                // Detect collision with paddle (only when ball is moving downward)
                if (circle.getCenterY() >= rect.getY() - 30 &&
                        circle.getCenterX() >= rect.getX() &&
                        circle.getCenterX() <= rect.getX() + rect.getWidth() &&
                        velocityY > 0) {

                    // Reverse vertical direction
                    velocityY *= bounceY;
                }

                /* -------- Wall Collisions -------- */

                // Left and right wall collision
                if (circle.getCenterX() <= 30 || circle.getCenterX() >= 690) {
                    velocityX *= bounceX;
                }

                // Ceiling collision
                if (circle.getCenterY() <= 30 && velocityY < 0) {
                    velocityY *= bounceY;

                    // Bottom boundary (loss condition)
                } else if (circle.getCenterY() >= 450) {
                    lose = true;
                }

                /* -------- Brick Collision -------- */

                // Iterate through bricks and check for collisions
                Iterator<Rectangle> it = brickList.iterator();
                while (it.hasNext()) {
                    Rectangle brick = it.next();

                    if (circle.getCenterY() >= brick.getY() - 30 &&
                            circle.getCenterX() >= brick.getX() &&
                            circle.getCenterX() <= brick.getX() + brick.getWidth() &&
                            circle.getCenterY() <= brick.getY() + brick.getHeight() + 30) {

                        // Reverse vertical direction on collision
                        velocityY *= bounceY;

                        // Remove brick from game
                        it.remove();
                        root.getChildren().remove(brick);

                        // Increase score
                        Score++;
                    }
                }

                /* -------- Game State Handling -------- */

                // Loss condition
                if (lose) {
                    Platform.runLater(() -> {
                        this.stop();
                        lost.setContentText("YOU LOSE!");
                        lost.showAndWait();
                    });

                    // Win condition (all bricks destroyed)
                } else if (Score >= rows * cols) {
                    win = true;
                }

                // Handle win state
                if (win) {
                    Platform.runLater(() -> {
                        this.stop();
                        lost.setContentText("YOU WIN!");
                        lost.showAndWait();
                    });
                }
            }
        };

       /* =========================
          Input Handling
          ========================= */

        // Move paddle left/right using arrow keys
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                rect.setX(rect.getX() - playerX);
            } else if (event.getCode() == KeyCode.RIGHT) {
                rect.setX(rect.getX() + playerX);
            }
        });

        // Start the animation loop
        timer.start();

        // Show the window
        stage.show();
    }
}