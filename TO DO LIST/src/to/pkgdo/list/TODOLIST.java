package to.pkgdo.list;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.time.LocalDate;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static javafx.application.Application.launch;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class TODOLIST extends Application {

    private ListView<Task> taskListView = new ListView<>();
    private ListView<CompletedTask> completedTasks = new ListView<>();  // List for completed tasks

    private int tasknum = 0;
    
    private int tasknum(){
       return DatabaseHelper.forNumberingTask();
    }

    @Override
    public void start(Stage primaryStage) {
        
        
        //Layout
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10; -fx-spacing: 10;");

        //Intro
        Label introlbl = new Label("TU-DU LIST");
        introlbl.getStyleClass().add("label-intro");
        introlbl.setStyle("-fx-alignment: center; -fx-padding: 10;");
        ImageView introImage = new ImageView(new Image(getClass().getResourceAsStream("/tunamayo.png")));
        introImage.setFitWidth(50);
        introImage.setFitHeight(50);
        /*ImageView introImage2 = new ImageView(new Image(getClass().getResourceAsStream("/tunamayo.png")));
        introImage2.setFitWidth(50);
        introImage2.setFitHeight(50);*/

        // Create an HBox to hold the label and image
        HBox introContainer = new HBox(1, introlbl, introImage); // 10 is spacing between image and text
        introContainer.setAlignment(Pos.CENTER);
        root.getChildren().add(introContainer);

        //input field and button
        Label addlbl = new Label("Add Task                                   ");
        addlbl.getStyleClass().add("label-add-task");
        Button addButton = new Button("+");

        ChoiceBox<String> sort = new ChoiceBox<String>();
        Label sortlbl = new Label("Sort Task");
        sortlbl.getStyleClass().add("label-add-task");

        //Retrieving the observable list
        ObservableList<String> sortlist = sort.getItems();
        //Adding items to the list
        sortlist.addAll("Due Date (Ascending)", "Due Date (Descending)", "Priority (High to Low)", "Priority (Low to High)");

        //setting label
        Button sortButton = new Button("SORT");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox sortgrp = new HBox(5, sortlbl, sort, spacer, sortButton);
        sortgrp.setAlignment(Pos.CENTER_LEFT);

        sortButton.setOnAction(event -> {
            String selectedSort = sort.getValue();
            if (selectedSort == null) {
                return;
            }
            switch (selectedSort) {
                case "Due Date (Ascending)":
                    taskListView.getItems().sort(Comparator.comparing(Task::getDueDate));
                    break;
                case "Due Date (Descending)":
                    taskListView.getItems().sort(Comparator.comparing(Task::getDueDate).reversed());
                    break;
                case "Priority (High to Low)":
                    taskListView.getItems().sort(Comparator.comparing(Task::getCatnum).reversed());
                    break;
                case "Priority (Low to High)":
                    taskListView.getItems().sort(Comparator.comparing(Task::getCatnum));
                    break;
                default:
                    break;
            }
        });

        // Context menu for task actions
        ContextMenu contextMenu = new ContextMenu();

        taskListView.setCellFactory(lv -> {
            ListCell<Task> cell = new ListCell<>() {

                @Override
                protected void updateItem(Task task, boolean empty) {
                    super.updateItem(task, empty);

                    if (task == null || empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        
                        //display task
                        Label tasklbl = new Label(task.toString());
                        
                        //display cbx and btn 
                        CheckBox complete = new CheckBox();
                        Button delete = new Button("DELETE");
                        complete.setSelected(false);
                        
                        Button viewlbl = new Button("View");
                        
                        viewlbl.setOnAction(event -> {
                            viewTask(task.toTitle(),task.getDescription(), task.getDueDate(), task.getCategory(), task.getPriority());
                        });

                        // Action for CheckBox
                        complete.setOnAction(event -> {
                            task.setCompleted(complete.isSelected());
                            updateTaskCompletionInDatabase(task); // Update completion in DB

                            if (task.isCompleted()) {
                                // Check if the task is already in the completed list
                                boolean isAlreadyCompleted = completedTasks.getItems()
                                        .stream()
                                        .anyMatch(completedTask -> completedTask.toTitle().equals(task.toTitle()));

                                if (!isAlreadyCompleted) {
                                    // Create a CompletedTask object
                                    CompletedTask completedTask = new CompletedTask(task.toTitle(), task.getDescription(), task.getDueDateString(), task.getCategory(), task.getPriority(), task.gettasknum());
                                    // Add the completed task to the completedTasks ListView
                                    completedTasks.getItems().add(completedTask);

                                    //Optionally, remove the task from the main task list
                                    taskListView.getItems().remove(task);
                                }
                            }
                        });

                        delete.setOnAction(event -> {
                            taskListView.getItems().remove(task); //remove from list
                            deleteTaskFromDatabase(task);  // Remove from database
                        });

                        // Layout for the ListCell content (task label and checkbox)
                        HBox cellLayout = new HBox(10, tasklbl, complete, delete, viewlbl);
                        cellLayout.setStyle("-fx-alignment: center-left;");
                        setGraphic(cellLayout);

                        // Attach the context menu to the cell
                        setContextMenu(contextMenu);
                    }
                }
            };
            return cell;
        }
        );

        // Context menu for task actions
        ContextMenu contextMenu1 = new ContextMenu();
        
        completedTasks = new ListView<>();
        Label title = new Label("Completed Task");
        title.getStyleClass().add("label-completed-task");

        completedTasks.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CompletedTask task, boolean empty) {
                super.updateItem(task, empty);
                if (task == null || empty) {
                    setText(null);
                } else {
                    Label taskLabel = new Label(task.toString());
                    Button deleteButton = new Button("DELETE");

                    // Delete Button Action
                    deleteButton.setOnAction(event -> {
                        completedTasks.getItems().remove(task);
                        deleteCompletedTaskFromDatabase(task); // Remove from database
                    });

                    HBox cellLayout = new HBox(10, taskLabel, deleteButton);
                    setGraphic(cellLayout);

                    // Attach the context menu to the cell
                    setContextMenu(contextMenu1);
                }

            }

        });

        // Add task action
        addButton.setOnAction(event -> {
              createTask(taskListView);
              tasknum = tasknum();
              });

        // Add components to layout
        HBox inputArea = new HBox(5, addlbl, addButton);
        root.getChildren().addAll(inputArea, taskListView, title, completedTasks, sortgrp);

        Scene scene = new Scene(root, 400, 600);
        scene.getStylesheets().add(getClass().getResource("/stylesheet.css").toExternalForm());
        primaryStage.setTitle("TUNA MAYO LIST");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load tasks from the database when the application starts
        loadTasksFromDB();
        loadCompletedTasksFromDB();
    }

    private void createTask(ListView<Task> taskListView) {
        Stage createTask = new Stage();
        createTask.initModality(Modality.APPLICATION_MODAL); // Block interaction with other windows
        createTask.setTitle("New Task");
        Label newTask = new Label("       New Task");
        newTask.getStyleClass().add("label-intro");
        newTask.setStyle("-fx-alignment: center; -fx-padding: 10;");

        //input field and button
        TextField taskTitle = new TextField();
        taskTitle.setPromptText("Enter a new Task Title");

        //input field and button
        TextField taskDesc = new TextField();
        taskDesc.setPromptText("Enter Description");

        //input field and button
        TextField taskDue = new TextField();
        taskDue.setPromptText("Enter Due Date (YYYYMMDD)");

        //input field and button
        TextField taskCategory = new TextField();
        taskCategory.setPromptText("Enter Category");

        //input field and button
        TextField taskPriority = new TextField();
        taskPriority.setPromptText("Enter Priority(low/medium/high)");

        tasknum = tasknum() + 1;

        // Buttons
        Button saveButton = new Button("SAVE");

        // Save button action
        saveButton.setOnAction(e -> {
            String title = taskTitle.getText().trim();
            String description = taskDesc.getText().trim();
            String dueDate = taskDue.getText().trim();
            String category = taskCategory.getText().trim();
            String priority = taskPriority.getText().trim();
            

            if (!title.isEmpty()) {
                taskListView.getItems().add(new Task(title, description, dueDate, category, priority, tasknum));
                addTaskToDatabase(title, description, dueDate, category, priority, tasknum);

                createTask.close(); // Close the pop-up
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Task Title cannot be empty!", ButtonType.OK);
                alert.showAndWait();
            }

            createTask.close();
        });

        VBox Layout = new VBox(10, newTask, taskTitle, taskDesc, taskDue, taskCategory, taskPriority);
        VBox createTaskLayout = new VBox(10, Layout, saveButton);
        createTaskLayout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Scene createTaskScene = new Scene(createTaskLayout, 400, 300);
        createTaskScene.getStylesheets().add(getClass().getResource("/stylesheet.css").toExternalForm());
        createTask.setScene(createTaskScene);

        // Show the pop-up window
        createTask.showAndWait(); // Wait until this window is closed
    }
   
        
    private void viewTask(String title, String description, String dueDate, String category, String priority) {
        
        Stage createTask = new Stage();
        createTask.initModality(Modality.APPLICATION_MODAL); // Block interaction with other windows
        createTask.setTitle("Task Details");
        
        Label titleTitle = new Label(title);
        titleTitle.getStyleClass().add("label-sort");
        titleTitle.setStyle("-fx-padding: 5;");
        
        
        Label titleDescription = new Label("Description: " + description);
        
        
        Label titledueDate = new Label("Due Date: " + dueDate);
      
        
        Label titleCategory = new Label("Category: " + category);

        
        Label titlePriority = new Label("Priority: " + priority);
     
        
        // Buttons
        Button closeButton = new Button("close");

        // Save button action
        closeButton.setOnAction(e -> {
        createTask.close();
        });
        
        VBox details = new VBox (titleDescription,titledueDate,titleCategory,titlePriority);
        details.setStyle("-fx-background-color: #ffffff;" + "-fx-border-color: #d3d3d3;" + "-fx-border-width : 1px;" + "-fx-padding: 10px;" +   "-fx-spacing: 10px;");
        details.getStyleClass().add("vbox-task-details");
        
        VBox createTaskLayout = new VBox(10, titleTitle, details, closeButton);
        createTaskLayout.setStyle("-fx-padding: 20;");
       
        Scene createTaskScene = new Scene(createTaskLayout, 400, 250);
        createTaskScene.getStylesheets().add(getClass().getResource("/stylesheet.css").toExternalForm());
        createTask.setScene(createTaskScene);

        // Show the pop-up window
        createTask.showAndWait(); // Wait until this window is closed
    }

    private void loadTasksFromDB() {
        // Fetch tasks from the database
        List<Task> tasks = DatabaseHelper.fetchTasksFromDB();
        System.out.println("Fetched Tasks: " + tasks);

        // Update the ListView
        taskListView.getItems().clear(); // Clear current tasks
        taskListView.getItems().addAll(tasks); // Add tasks from the database
    }

    
    private void addTaskToDatabase(String title, String description, String dueDate, String category, String priority, int tasknum) {
        String query = "INSERT INTO task (Title, Description, Due, Category, Priority, Completion, numTask) VALUES (?, ?, ?, ?, ?, 0, ?)";

        // Importing and registering drivers
        String url1 = "jdbc:mysql://localhost:3306/tunamayo_db"; //insert your database name
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url1, user, password); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, title);
            stmt.setString(2, description);
            stmt.setString(3, dueDate);
            stmt.setString(4, category);
            stmt.setString(5, priority);
            stmt.setInt(6, tasknum);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteTaskFromDatabase(Task task) {
        String query = "DELETE FROM task WHERE numTask = ? ";  // Assuming Title and Due Date are unique identifiers

        // Database connection parameters
        String url = "jdbc:mysql://localhost:3306/tunamayo_db";  // Modify with your actual database URL
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password); PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, task.gettasknum());  // Using the task's number 
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateTaskCompletionInDatabase(Task task) {
        String query = "UPDATE task SET Completion = ? WHERE numTask = ?";

        // Database connection parameters
        String url = "jdbc:mysql://localhost:3306/tunamayo_db";  // Modify with your actual database URL
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password); PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the completion value (1 for completed, 0 for not completed)
            stmt.setInt(1, task.isCompleted() ? 1 : 0);  // Use 1 for true, 0 for false
            stmt.setInt(2, task.gettasknum());  // Use task title to identify which task to update
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteCompletedTaskFromDatabase(CompletedTask task) {
        String query = "DELETE FROM task WHERE numTask = ? ";
        String url = "jdbc:mysql://localhost:3306/tunamayo_db"; // Replace with your DB details
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, task.gettasknum());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadCompletedTasksFromDB() {
        // Fetch tasks from the database
        List<CompletedTask> tasks = DatabaseHelper.fetchCompletedTasksFromDB();
        System.out.println("Fetched Tasks: " + tasks);

        // Update the ListView
        completedTasks.getItems().clear(); // Clear current tasks
        completedTasks.getItems().addAll(tasks); // Add tasks from the database
    }



    public static void main(String[] args) {
        launch(args);
    }

}
