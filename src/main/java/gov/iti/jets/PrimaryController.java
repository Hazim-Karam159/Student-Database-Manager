package gov.iti.jets;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.*;

public class PrimaryController {

    @FXML
    private TableView<Data> tableView;

    @FXML
    private TableColumn<Data, Integer> idColumn;

    @FXML
    private TableColumn<Data, String> firstNameColumn;

    @FXML
    private TableColumn<Data, String> middleNameColumn;

    @FXML
    private TableColumn<Data, String> lastNameColumn;

    @FXML
    private TableColumn<Data, String> emailColumn;

    @FXML
    private TableColumn<Data, String> phoneColumn;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField middleNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private Button firstButton;

    @FXML
    private Button previousButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button lastButton;

    @FXML
    private Button addButton;

    @FXML
    private Button updateButton;

    @FXML
    private Button deleteButton;

    private ObservableList<Data> dataList;
    private int currentIndex = 0;

    @FXML
    public void initialize() {
        // Initialize the columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        middleNameColumn.setCellValueFactory(new PropertyValueFactory<>("middleName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // Fetch data from the database
        dataList = fetchDataFromDatabase();
        tableView.setItems(dataList);

        // Select the first user by default
        if (!dataList.isEmpty()) {
            tableView.getSelectionModel().selectFirst();
            displayUser(currentIndex);
        }

        // Button actions
        firstButton.setOnAction(e -> displayFirstUser());
        previousButton.setOnAction(e -> displayPreviousUser());
        nextButton.setOnAction(e -> displayNextUser());
        lastButton.setOnAction(e -> displayLastUser());
        addButton.setOnAction(e -> addNewUser());
        updateButton.setOnAction(e -> updateUser());
        deleteButton.setOnAction(e -> deleteUser());

        // Listen for table row selection changes
        tableView.getSelectionModel().selectedIndexProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.intValue() >= 0) {
                currentIndex = newSelection.intValue();
                displayUser(currentIndex);
            }
        });
    }

    private ObservableList<Data> fetchDataFromDatabase() {
        ObservableList<Data> dataList = FXCollections.observableArrayList();
        try (Connection connection = DatabaseUtil.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM students")) {

            while (resultSet.next()) {
                Data data = new Data(
                        resultSet.getInt("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("middle_name"),
                        resultSet.getString("last_name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone")
                );
                dataList.add(data);
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to fetch data from the database: " + e.getMessage());
        }
        return dataList;
    }

    private void displayUser(int index) {
        if (index >= 0 && index < dataList.size()) {
            Data user = dataList.get(index);
            firstNameField.setText(user.firstNameProperty().get());
            middleNameField.setText(user.middleNameProperty().get());
            lastNameField.setText(user.lastNameProperty().get());
            emailField.setText(user.emailProperty().get());
            phoneField.setText(user.phoneProperty().get());
            tableView.getSelectionModel().select(index);
        }
    }

    private void displayFirstUser() {
        if (!dataList.isEmpty()) {
            currentIndex = 0;
            displayUser(currentIndex);
        }
    }

    private void displayPreviousUser() {
        if (currentIndex > 0) {
            currentIndex--;
            displayUser(currentIndex);
        }
    }

    private void displayNextUser() {
        if (currentIndex < dataList.size() - 1) {
            currentIndex++;
            displayUser(currentIndex);
        }
    }

    private void displayLastUser() {
        if (!dataList.isEmpty()) {
            currentIndex = dataList.size() - 1;
            displayUser(currentIndex);
        }
    }

    private void addNewUser() {
        String firstName = firstNameField.getText();
        String middleName = middleNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();

        if (!firstName.isEmpty() && !lastName.isEmpty() && !email.isEmpty() && !phone.isEmpty()) {
            try (Connection connection = DatabaseUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO students (first_name, middle_name, last_name, email, phone) VALUES (?, ?, ?, ?, ?)",
                         Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, firstName);
                statement.setString(2, middleName);
                statement.setString(3, lastName);
                statement.setString(4, email);
                statement.setString(5, phone);
                statement.executeUpdate();

                // Refresh the data list
                dataList = fetchDataFromDatabase();
                tableView.setItems(dataList);
                displayLastUser(); // Display the newly added user
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to add new user: " + e.getMessage());
            }
        } else {
            showAlert("Input Error", "Please fill in all required fields (First Name, Last Name, Email, Phone).");
        }
    }

    private void updateUser() {
        Data selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            String firstName = firstNameField.getText();
            String middleName = middleNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();

            try (Connection connection = DatabaseUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE students SET first_name = ?, middle_name = ?, last_name = ?, email = ?, phone = ? WHERE id = ?")) {

                statement.setString(1, firstName);
                statement.setString(2, middleName);
                statement.setString(3, lastName);
                statement.setString(4, email);
                statement.setString(5, phone);
                statement.setInt(6, selectedUser.idProperty().get());
                statement.executeUpdate();

                // Refresh the data list
                dataList = fetchDataFromDatabase();
                tableView.setItems(dataList);
                displayUser(currentIndex); // Refresh the displayed user
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to update user: " + e.getMessage());
            }
        }
    }

    private void deleteUser() {
        Data selectedUser = tableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            try (Connection connection = DatabaseUtil.getConnection();
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM students WHERE id = ?")) {

                statement.setInt(1, selectedUser.idProperty().get());
                statement.executeUpdate();

                // Refresh the data list
                dataList = fetchDataFromDatabase();
                tableView.setItems(dataList);

                if (currentIndex >= dataList.size()) {
                    currentIndex = dataList.size() - 1;
                }
                displayUser(currentIndex); // Display the next user
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to delete user: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}