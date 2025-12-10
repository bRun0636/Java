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
import okhttp3.Cookie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginController {
    private static final String API_BASE_URL = "http://localhost:8080/api";
    private final OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new SimpleCookieJar())
            .build();
    private final Gson gson = new Gson();
    private Stage stage;
    
    // Простой CookieJar для сохранения cookies
    private static class SimpleCookieJar implements CookieJar {
        private final List<Cookie> cookies = new ArrayList<>();
        
        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            this.cookies.addAll(cookies);
        }
        
        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            return cookies;
        }
    }

    public void show() {
        stage = new Stage();
        stage.setTitle("Вход в систему");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Label usernameLabel = new Label("Имя пользователя:");
        TextField usernameField = new TextField();
        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);

        Label passwordLabel = new Label("Пароль:");
        PasswordField passwordField = new PasswordField();
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        Button loginButton = new Button("Войти");
        Button registerButton = new Button("Регистрация");
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.BOTTOM_RIGHT);
        buttonBox.getChildren().addAll(registerButton, loginButton);
        grid.add(buttonBox, 1, 2);

        Label messageLabel = new Label();
        grid.add(messageLabel, 1, 3);

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            if (login(username, password)) {
                messageLabel.setText("Вход выполнен успешно");
                messageLabel.setStyle("-fx-text-fill: green;");
                checkUserRoleAndShowDashboard(username);
            } else {
                messageLabel.setText("Ошибка входа");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });

        registerButton.setOnAction(e -> {
            RegisterController registerController = new RegisterController();
            registerController.show();
        });

        Scene scene = new Scene(grid, 350, 200);
        stage.setScene(scene);
        stage.show();
    }

    private boolean login(String username, String password) {
        try {
            RequestBody formBody = new FormBody.Builder()
                    .add("username", username)
                    .add("password", password)
                    .build();

            Request request = new Request.Builder()
                    .url("http://localhost:8080/login")
                    .post(formBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                // CookieJar автоматически сохранит cookies
                return response.isSuccessful();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void checkUserRoleAndShowDashboard(String username) {
        try {
            // CookieJar автоматически добавит сохраненные cookies
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/user/profile")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                System.out.println("Profile request response code: " + response.code());
                
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    System.out.println("Response body: " + responseBody);
                    JsonObject userJson = gson.fromJson(responseBody, JsonObject.class);
                    
                    // Проверяем роли пользователя
                    boolean isAdmin = false;
                    if (userJson.has("roles") && userJson.get("roles").isJsonArray()) {
                        var rolesArray = userJson.getAsJsonArray("roles");
                        for (var role : rolesArray) {
                            String roleStr = role.getAsString();
                            System.out.println("User role: " + roleStr);
                            if (roleStr.equals("ADMIN")) {
                                isAdmin = true;
                                break;
                            }
                        }
                    }
                    
                    final boolean finalIsAdmin = isAdmin;
                    // Закрываем окно входа и открываем панель в JavaFX Application Thread
                    javafx.application.Platform.runLater(() -> {
                        stage.close();
                        
                        if (finalIsAdmin) {
                            System.out.println("Opening admin dashboard");
                            AdminDashboardController adminDashboard = new AdminDashboardController();
                            adminDashboard.show();
                        } else {
                            System.out.println("Opening guest dashboard");
                            GuestDashboardController guestDashboard = new GuestDashboardController();
                            guestDashboard.show();
                        }
                    });
                } else {
                    String errorBody = response.body() != null ? response.body().string() : "No error body";
                    System.out.println("Error response: " + errorBody);
                    // Если запрос не удался, показываем ошибку
                    final int statusCode = response.code();
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Ошибка");
                        alert.setHeaderText("Не удалось получить информацию о пользователе");
                        alert.setContentText("Код ответа: " + statusCode + "\nПопробуйте войти через веб-интерфейс для проверки.");
                        alert.showAndWait();
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Показываем ошибку пользователю
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Произошла ошибка");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            });
        }
    }
}

