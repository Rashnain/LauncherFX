module com.github.rashnain.launcherfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.github.rashnain.launcherfx.controller to javafx.fxml;
    exports com.github.rashnain.launcherfx;
}