package com.hotel.desktop;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import okhttp3.*;

import java.io.IOException;

public class RegisterController {
    private final OkHttpClient client = new OkHttpClient();
    private Stage stage;

    public void show() {
        stage = new Stage();
        stage.setTitle("Регистрация");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label usernameLabel = new Label("Имя пользователя:");
        TextField usernameField = new TextField();
        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);

        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();
        grid.add(emailLabel, 0, 1);
        grid.add(emailField, 1, 1);

        Label passwordLabel = new Label("Пароль:");
        PasswordField passwordField = new PasswordField();
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);

        Label firstNameLabel = new Label("Имя:");
        TextField firstNameField = new TextField();
        grid.add(firstNameLabel, 0, 3);
        grid.add(firstNameField, 1, 3);

        Label lastNameLabel = new Label("Фамилия:");
        TextField lastNameField = new TextField();
        grid.add(lastNameLabel, 0, 4);
        grid.add(lastNameField, 1, 4);

        Label phoneLabel = new Label("Телефон:");
        TextField phoneField = new TextField();
        grid.add(phoneLabel, 0, 5);
        grid.add(phoneField, 1, 5);

        Button registerButton = new Button("Зарегистрироваться");
        Button cancelButton = new Button("Отмена");
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(cancelButton, registerButton);
        grid.add(buttonBox, 1, 6);

        Label messageLabel = new Label();
        grid.add(messageLabel, 1, 7);

        registerButton.setOnAction(e -> {
            JsonObject userJson = new JsonObject();
            userJson.addProperty("username", usernameField.getText());
            userJson.addProperty("email", emailField.getText());
            userJson.addProperty("password", passwordField.getText());
            userJson.addProperty("firstName", firstNameField.getText());
            userJson.addProperty("lastName", lastNameField.getText());
            userJson.addProperty("phone", phoneField.getText());

            if (register(userJson.toString())) {
                messageLabel.setText("Регистрация успешна!");
                messageLabel.setStyle("-fx-text-fill: green;");
                stage.close();
                LoginController loginController = new LoginController();
                loginController.show();
            } else {
                messageLabel.setText("Ошибка регистрации");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });

        cancelButton.setOnAction(e -> stage.close());

        Scene scene = new Scene(grid, 400, 350);
        stage.setScene(scene);
        stage.show();
    }

    private boolean register(String userJson) {
        try {
            RequestBody body = RequestBody.create(
                    userJson, MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url("http://localhost:8080/api/auth/register")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

