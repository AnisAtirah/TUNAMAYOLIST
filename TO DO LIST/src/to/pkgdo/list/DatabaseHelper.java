package to.pkgdo.list;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/tunamayo_db"; // Replace with your database name
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static List<Task> fetchTasksFromDB() {
        List<Task> tasks = new ArrayList<>();
        List<CompletedTask> completedTasks = new ArrayList<>();

        String query = "SELECT * FROM task WHERE Completion = 0"; // Adjust column names if needed

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Create Task objects from result set
                String title = rs.getString("Title");
                String description = rs.getString("Description");
                String dueDate = rs.getString("Due");
                String category = rs.getString("Category");
                String priority = rs.getString("Priority");
                int completed1 = rs.getInt("Completion"); // Assuming 1 = completed
                int tasknum = rs.getInt("numTask");

                boolean completed;
                if (completed1 == 1) {
                    completed = true;
                } else {
                    completed = false;
                }    

                Task task = new Task(title, description, dueDate, category, priority, tasknum);
                task.setCompleted(completed);
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    public static List<CompletedTask> fetchCompletedTasksFromDB() {
        List<CompletedTask> completedTasks = new ArrayList<>();
        String query = "SELECT * FROM task WHERE Completion = 1";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                CompletedTask task = new CompletedTask(
                        rs.getString("Title"),
                        rs.getString("Description"),
                        rs.getString("Due"),
                        rs.getString("Category"),
                        rs.getString("Priority"),
                        rs.getInt("numTask")
                );
                completedTasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return completedTasks;
    }

    public static int forNumberingTask() {
        String query = "SELECT MAX(numTask) FROM task";  // Change to your actual table name and column name

        String url = "jdbc:mysql://localhost:3306/tunamayo_db";  // Modify with your actual database URL
        String user = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, user, password); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                return rs.getInt(1);  // Get the max task_id
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;  // Return -1 if no task found
    }

}
