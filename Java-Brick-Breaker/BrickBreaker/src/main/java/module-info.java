module org.example.brickbreaker {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.example.brickbreaker to javafx.fxml;
    exports org.example.brickbreaker;
}