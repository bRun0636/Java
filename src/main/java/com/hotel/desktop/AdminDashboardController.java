package com.hotel.desktop;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import okhttp3.*;

import java.io.IOException;

public class AdminDashboardController {
    private static final String API_BASE_URL = "http://localhost:8080/api";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private Stage stage;
    private TableView<RoomData> roomsTable;
    private TableView<BookingData> bookingsTable;

    public void show() {
        stage = new Stage();
        stage.setTitle("Панель администратора");

        TabPane tabPane = new TabPane();

        Tab roomsTab = new Tab("Номера");
        roomsTab.setContent(createRoomsTab());
        tabPane.getTabs().add(roomsTab);

        Tab bookingsTab = new Tab("Бронирования");
        bookingsTab.setContent(createBookingsTab());
        tabPane.getTabs().add(bookingsTab);

        Tab usersTab = new Tab("Пользователи");
        usersTab.setContent(createUsersTab());
        tabPane.getTabs().add(usersTab);

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.show();

        loadRooms();
        loadBookings();
        loadUsers();
    }

    private VBox createRoomsTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label titleLabel = new Label("Управление номерами");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox addRoomBox = new HBox(10);
        TextField roomNumberField = new TextField();
        roomNumberField.setPromptText("Номер");
        TextField capacityField = new TextField();
        capacityField.setPromptText("Вместимость");
        Button addButton = new Button("Добавить номер");
        addRoomBox.getChildren().addAll(roomNumberField, capacityField, addButton);

        roomsTable = new TableView<>();
        TableColumn<RoomData, String> numberCol = new TableColumn<>("Номер");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        TableColumn<RoomData, Integer> capacityCol = new TableColumn<>("Вместимость");
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        TableColumn<RoomData, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setCellFactory(param -> new TableCell<RoomData, Void>() {
            private final Button deleteBtn = new Button("Удалить");
            {
                deleteBtn.setOnAction(event -> {
                    RoomData room = getTableView().getItems().get(getIndex());
                    deleteRoom(room.getId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        roomsTable.getColumns().addAll(numberCol, capacityCol, actionsCol);

        addButton.setOnAction(e -> {
            try {
                int capacity = Integer.parseInt(capacityField.getText());
                addRoom(roomNumberField.getText(), capacity);
                roomNumberField.clear();
                capacityField.clear();
            } catch (NumberFormatException ex) {
                showAlert("Ошибка", "Неверный формат вместимости");
            }
        });

        vbox.getChildren().addAll(titleLabel, addRoomBox, roomsTable);
        return vbox;
    }

    private VBox createBookingsTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label titleLabel = new Label("Все бронирования");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        bookingsTable = new TableView<>();
        TableColumn<BookingData, String> roomCol = new TableColumn<>("Номер");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        TableColumn<BookingData, String> checkInCol = new TableColumn<>("Дата заезда");
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        TableColumn<BookingData, String> checkOutCol = new TableColumn<>("Дата выезда");
        checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        TableColumn<BookingData, Integer> guestsCol = new TableColumn<>("Гостей");
        guestsCol.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));
        TableColumn<BookingData, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<BookingData, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setCellFactory(param -> new TableCell<BookingData, Void>() {
            private final Button cancelBtn = new Button("Отменить");
            {
                cancelBtn.setOnAction(event -> {
                    BookingData booking = getTableView().getItems().get(getIndex());
                    if ("ACTIVE".equals(booking.getStatus())) {
                        cancelBooking(booking.getId());
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BookingData booking = getTableView().getItems().get(getIndex());
                    if ("ACTIVE".equals(booking.getStatus())) {
                        setGraphic(cancelBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        bookingsTable.getColumns().addAll(roomCol, checkInCol, checkOutCol, guestsCol, statusCol, actionsCol);

        vbox.getChildren().addAll(titleLabel, bookingsTable);
        return vbox;
    }

    private VBox createUsersTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label titleLabel = new Label("Зарегистрированные пользователи");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TableView<UserData> usersTable = new TableView<>();
        TableColumn<UserData, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<UserData, String> usernameCol = new TableColumn<>("Имя пользователя");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        TableColumn<UserData, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<UserData, String> firstNameCol = new TableColumn<>("Имя");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        TableColumn<UserData, String> lastNameCol = new TableColumn<>("Фамилия");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        usersTable.getColumns().addAll(idCol, usernameCol, emailCol, firstNameCol, lastNameCol);

        // Load users
        try {
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/admin/users")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonArray usersArray = gson.fromJson(responseBody, JsonArray.class);
                    ObservableList<UserData> users = FXCollections.observableArrayList();
                    for (JsonElement element : usersArray) {
                        JsonObject userJson = element.getAsJsonObject();
                        UserData user = new UserData(
                                userJson.get("id").getAsLong(),
                                userJson.get("username").getAsString(),
                                userJson.get("email").getAsString(),
                                userJson.get("firstName").getAsString(),
                                userJson.get("lastName").getAsString()
                        );
                        users.add(user);
                    }
                    usersTable.setItems(users);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        vbox.getChildren().addAll(titleLabel, usersTable);
        return vbox;
    }

    private void loadRooms() {
        try {
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/rooms")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonArray roomsArray = gson.fromJson(responseBody, JsonArray.class);
                    ObservableList<RoomData> rooms = FXCollections.observableArrayList();
                    for (JsonElement element : roomsArray) {
                        JsonObject roomJson = element.getAsJsonObject();
                        RoomData room = new RoomData(
                                roomJson.get("id").getAsLong(),
                                roomJson.get("number").getAsString(),
                                roomJson.get("capacity").getAsInt()
                        );
                        rooms.add(room);
                    }
                    roomsTable.setItems(rooms);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadBookings() {
        try {
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/admin/bookings")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    JsonArray bookingsArray = gson.fromJson(responseBody, JsonArray.class);
                    ObservableList<BookingData> bookings = FXCollections.observableArrayList();
                    for (JsonElement element : bookingsArray) {
                        JsonObject bookingJson = element.getAsJsonObject();
                        BookingData booking = new BookingData(
                                bookingJson.get("id").getAsLong(),
                                bookingJson.get("roomNumber").getAsString(),
                                bookingJson.get("checkInDate").getAsString(),
                                bookingJson.get("checkOutDate").getAsString(),
                                bookingJson.get("numberOfGuests").getAsInt(),
                                bookingJson.get("status").getAsString()
                        );
                        bookings.add(booking);
                    }
                    bookingsTable.setItems(bookings);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadUsers() {
        try {
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/admin/users")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    // Users will be displayed in the users tab
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addRoom(String number, int capacity) {
        try {
            JsonObject roomJson = new JsonObject();
            roomJson.addProperty("number", number);
            roomJson.addProperty("capacity", capacity);

            RequestBody body = RequestBody.create(
                    roomJson.toString(), MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/rooms")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    loadRooms();
                    showAlert("Успех", "Номер добавлен");
                } else {
                    showAlert("Ошибка", "Не удалось добавить номер");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка при добавлении номера");
        }
    }

    private void deleteRoom(Long id) {
        try {
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/rooms/" + id)
                    .delete()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    loadRooms();
                    showAlert("Успех", "Номер удален");
                } else {
                    showAlert("Ошибка", "Не удалось удалить номер");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка при удалении номера");
        }
    }

    private void cancelBooking(Long id) {
        try {
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/admin/bookings/" + id)
                    .delete()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    loadBookings();
                    showAlert("Успех", "Бронирование отменено");
                } else {
                    showAlert("Ошибка", "Не удалось отменить бронирование");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка при отмене бронирования");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class RoomData {
        private final Long id;
        private final String number;
        private final Integer capacity;

        public RoomData(Long id, String number, Integer capacity) {
            this.id = id;
            this.number = number;
            this.capacity = capacity;
        }

        public Long getId() { return id; }
        public String getNumber() { return number; }
        public Integer getCapacity() { return capacity; }
    }

    public static class BookingData {
        private final Long id;
        private final String roomNumber;
        private final String checkInDate;
        private final String checkOutDate;
        private final Integer numberOfGuests;
        private final String status;

        public BookingData(Long id, String roomNumber, String checkInDate, 
                          String checkOutDate, Integer numberOfGuests, String status) {
            this.id = id;
            this.roomNumber = roomNumber;
            this.checkInDate = checkInDate;
            this.checkOutDate = checkOutDate;
            this.numberOfGuests = numberOfGuests;
            this.status = status;
        }

        public Long getId() { return id; }
        public String getRoomNumber() { return roomNumber; }
        public String getCheckInDate() { return checkInDate; }
        public String getCheckOutDate() { return checkOutDate; }
        public Integer getNumberOfGuests() { return numberOfGuests; }
        public String getStatus() { return status; }
    }

    public static class UserData {
        private final Long id;
        private final String username;
        private final String email;
        private final String firstName;
        private final String lastName;

        public UserData(Long id, String username, String email, String firstName, String lastName) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
    }
}

