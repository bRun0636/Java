package com.hotel.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class DesktopApplication extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            LoginController loginController = new LoginController();
            loginController.show();
        } catch (Exception e) {
            e.printStackTrace();
            Platform.exit();
        }
    }

    public static void main(String[] args) {
        // Убеждаемся, что JavaFX Toolkit инициализирован
        Application.launch(DesktopApplication.class, args);
    }
}


