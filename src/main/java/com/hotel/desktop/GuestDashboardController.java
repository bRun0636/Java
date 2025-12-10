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
import java.time.LocalDate;

public class GuestDashboardController {
    private static final String API_BASE_URL = "http://localhost:8080/api";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private Stage stage;
    private TableView<RoomData> roomsTable;
    private TableView<BookingData> bookingsTable;

    public void show() {
        stage = new Stage();
        stage.setTitle("Панель гостя");

        TabPane tabPane = new TabPane();

        Tab roomsTab = new Tab("Доступные номера");
        roomsTab.setContent(createRoomsTab());
        tabPane.getTabs().add(roomsTab);

        Tab bookingsTab = new Tab("Мои бронирования");
        bookingsTab.setContent(createBookingsTab());
        tabPane.getTabs().add(bookingsTab);

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.show();

        loadRooms();
        loadBookings();
    }

    private VBox createRoomsTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label titleLabel = new Label("Доступные номера для бронирования");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        roomsTable = new TableView<>();
        TableColumn<RoomData, String> numberCol = new TableColumn<>("Номер");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("number"));
        TableColumn<RoomData, Integer> capacityCol = new TableColumn<>("Вместимость");
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        TableColumn<RoomData, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setCellFactory(param -> new TableCell<RoomData, Void>() {
            private final Button bookBtn = new Button("Забронировать");
            {
                bookBtn.setOnAction(event -> {
                    RoomData room = getTableView().getItems().get(getIndex());
                    showBookingDialog(room);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(bookBtn);
                }
            }
        });
        roomsTable.getColumns().addAll(numberCol, capacityCol, actionsCol);

        vbox.getChildren().addAll(titleLabel, roomsTable);
        return vbox;
    }

    private VBox createBookingsTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label titleLabel = new Label("История бронирований");
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

    private void showBookingDialog(RoomData room) {
        Stage dialog = new Stage();
        dialog.setTitle("Бронирование номера " + room.getNumber());

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        DatePicker checkInPicker = new DatePicker();
        checkInPicker.setPromptText("Дата заезда");
        DatePicker checkOutPicker = new DatePicker();
        checkOutPicker.setPromptText("Дата выезда");
        TextField guestsField = new TextField();
        guestsField.setPromptText("Количество гостей");

        Button bookButton = new Button("Забронировать");
        Button cancelButton = new Button("Отмена");

        bookButton.setOnAction(e -> {
            try {
                LocalDate checkIn = checkInPicker.getValue();
                LocalDate checkOut = checkOutPicker.getValue();
                int guests = Integer.parseInt(guestsField.getText());

                if (checkIn != null && checkOut != null && guests > 0 && guests <= room.getCapacity()) {
                    createBooking(room.getId(), checkIn, checkOut, guests);
                    dialog.close();
                } else {
                    showAlert("Ошибка", "Проверьте введенные данные");
                }
            } catch (NumberFormatException ex) {
                showAlert("Ошибка", "Неверный формат количества гостей");
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(cancelButton, bookButton);

        vbox.getChildren().addAll(
                new Label("Дата заезда:"), checkInPicker,
                new Label("Дата выезда:"), checkOutPicker,
                new Label("Количество гостей:"), guestsField,
                buttonBox
        );

        Scene scene = new Scene(vbox, 300, 250);
        dialog.setScene(scene);
        dialog.show();
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
                    .url(API_BASE_URL + "/bookings/my-bookings")
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

    private void createBooking(Long roomId, LocalDate checkIn, LocalDate checkOut, int guests) {
        try {
            JsonObject bookingJson = new JsonObject();
            bookingJson.addProperty("roomId", roomId);
            bookingJson.addProperty("checkInDate", checkIn.toString());
            bookingJson.addProperty("checkOutDate", checkOut.toString());
            bookingJson.addProperty("numberOfGuests", guests);

            RequestBody body = RequestBody.create(
                    bookingJson.toString(), MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/bookings")
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    loadBookings();
                    showAlert("Успех", "Бронирование создано");
                } else {
                    showAlert("Ошибка", "Не удалось создать бронирование");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Ошибка при создании бронирования");
        }
    }

    private void cancelBooking(Long id) {
        try {
            Request request = new Request.Builder()
                    .url(API_BASE_URL + "/bookings/" + id)
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
}

