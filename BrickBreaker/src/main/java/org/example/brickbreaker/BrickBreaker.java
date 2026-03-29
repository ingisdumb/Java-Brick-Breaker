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

public class BrickBreaker extends Application {

    // Game configuration
    int rows = 5;
    int cols = 5;
    int brickWidth = 100;
    int brickHeight = 30;

    // Game state
    int Score = 0;
    public boolean lose;
    public boolean win;

    // Ball movement properties
    Double velocityX = 120.0;
    Double velocityY = 120.0;
    Double bounceX = -1.0; // Multiplier to reverse X direction
    Double bounceY = -1.0; // Multiplier to reverse Y direction

    // Player movement speed
    Double playerX = 20.0;

    // Game objects
    Circle circle = new Circle(150, 300, 30); // Ball
    Rectangle rect = new Rectangle(270, 400, 200, 25); // Paddle
    Image gball = new Image("/golfball.png");

    // Utilities
    Random rand = new Random();
    ArrayList<Rectangle> brickList = new ArrayList<>();

    // Alert dialog for win/lose messages
    Alert lost = new Alert(Alert.AlertType.INFORMATION);

    @Override
    public void start(Stage stage) {

        // Create drawing surface
        Canvas canvas = new Canvas(720, 480);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Root container and scene setup
        Group root = new Group(canvas);
        Scene scene = new Scene(root, 720, 480);

        // Configure object appearance
        circle.setFill(Color.BLUE);
        rect.setFill(Color.BLACK);

        // Stage setup
        stage.setTitle("Brick Breaker");
        stage.setScene(scene);

        // Add game objects to scene
        // UNUSED - root.getChildren().add(circle);
        root.getChildren().add(rect);

        // Generate brick grid with alternating row offsets
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                // Offset every other row for staggered layout
                int offset = (i % 2 != 0) ? 50 : 0;

                Rectangle brick = new Rectangle(
                        j * brickWidth + offset + 110,
                        i * brickHeight,
                        brickWidth,
                        brickHeight
                );

                // Assign random color to each brick
                brick.setFill(Color.rgb(
                        rand.nextInt(256),
                        rand.nextInt(256),
                        rand.nextInt(256)
                ));

                brickList.add(brick);
                root.getChildren().add(brick);
            }
        }



        // Main game loop (runs every frame)
        AnimationTimer timer = new AnimationTimer() {
            //Create lastTime variable
            long lastTime = 0;
            @Override
            public void handle(long now) {

                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }

                //create deltaTime to keep track of seconds between frames
                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;


                // Clear previous frame
                gc.clearRect(0, 0, 720, 480);

                // Update ball position
                circle.setCenterX(circle.getCenterX() + velocityX * deltaTime);
                circle.setCenterY(circle.getCenterY() + velocityY * deltaTime);

                // Render score & ball
                gc.fillText(String.valueOf(Score), 10, 240);
                gc.drawImage(gball, circle.getCenterX() - 30, circle.getCenterY() - 30, 90, 90);

                // Paddle collision detection (only when ball moving downward)
                if (circle.getCenterY() >= rect.getY() - 30 &&
                        circle.getCenterX() >= rect.getX() &&
                        circle.getCenterX() <= rect.getX() + rect.getWidth() &&
                        velocityY > 0) {

                    velocityY *= bounceY;
                }

                // Wall collision (left and right)
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

                // Brick collision detection
                Iterator<Rectangle> it = brickList.iterator();
                while (it.hasNext()) {
                    Rectangle brick = it.next();

                    if (circle.getCenterY() >= brick.getY() - 30 &&
                            circle.getCenterX() >= brick.getX() &&
                            circle.getCenterX() <= brick.getX() + brick.getWidth() &&
                            circle.getCenterY() <= brick.getY() + brick.getHeight() + 30) {

                        // Reverse vertical direction on hit
                        velocityY *= bounceY;

                        // Remove brick from scene and list
                        it.remove();
                        root.getChildren().remove(brick);

                        // Increment score
                        Score++;
                    }
                }

                // Handle loss condition
                if (lose) {
                    Platform.runLater(() -> {
                        this.stop();
                        lost.setContentText("YOU LOSE!");
                        lost.showAndWait();
                    });

                    // Check win condition (all bricks destroyed)
                } else if (Score >= rows * cols) {
                    win = true;
                }

                // Handle win condition
                if (win) {
                    Platform.runLater(() -> {
                        this.stop();
                        lost.setContentText("YOU WIN!");
                        lost.showAndWait();
                    });
                }
            }
        };

        // Handle player input for paddle movement
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                rect.setX(rect.getX() - playerX);
            } else if (event.getCode() == KeyCode.RIGHT) {
                rect.setX(rect.getX() + playerX);
            }
        });

        // Start game loop
        timer.start();

        // Display window
        stage.show();
    }
}