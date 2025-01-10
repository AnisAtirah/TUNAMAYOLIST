package TunaMayo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

public class RecurringTask extends Task {
    public String recurrenceInterval; //"weekly", "daily", "monthly"
    public LocalDate nextOccurence;
    public LocalDate due;
    public static java.sql.Date sqlDue;
    public static java.sql.Date sqlnextOcc;
    public static ArrayList<Task> taskList = new ArrayList<>();
    public static int i;
    public int complete;
    public static int currentNumTask;
    
    public RecurringTask(int numTask, String title, String description, String recurrenceInterval, LocalDate nextOccurence, LocalDate due, int complete) {
    super(numTask, title, description);
    this.i = numTask;
    this.recurrenceInterval = recurrenceInterval;
    this.nextOccurence = nextOccurence;
    this.due = due;
    this.complete = complete;
    }
    
    public static void scheduleRecurringTaskCheck() {
        // Create a ScheduledExecutorService that runs the update task periodically
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Schedule the update task every 1 minute
        scheduler.scheduleAtFixedRate(() -> {
            // Call the method that checks and updates task completion
            actionInDBR(6, 0, null, null, null,null,null, 1);
        }, 0, 1, TimeUnit.MINUTES);  // Initial delay of 0, repeat every 1 minute
        
    }
    
    //This method returns recurrenceInterval
    public String getRecurrenceInterval(){
          return recurrenceInterval;
      }
    
    //This method returns recurrenceInterval from database based on taskId
    public static String getRecurrenceInterval(int taskId){
        Connection conn = null; 
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db"; 
            String user = "root";
            String password = "";
            
            //Connection with database
            conn = DriverManager.getConnection(url1, user, password); 
            if (conn != null) {
                //Selects column 'RecurrenceInterval' from table recurtask where numTask == taskId
                String sql = "SELECT RecurrenceInterval FROM recurtask WHERE numTask = ? "; 
                // Prepare the SQL statement
                var myStat = conn.prepareStatement(sql);
                myStat.setInt(1, taskId);
                ResultSet rs = myStat.executeQuery();
                if(rs.next()){
                String recurInterval = rs.getString("RecurrenceInterval");
                return recurInterval;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (conn != null) 
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            
            }
        }
        return null;
    }  
    
    //This method returns nextOccurence
    public LocalDate getNextOccurence(){
        return nextOccurence;
      }
    
    //This method returns nextOccurence from database based on taskId
    public static java.sql.Date getNextOccurence(int taskId){ //REMAIN KAT LUAR
            Connection conn = null; 
            try {
                String url1 = "jdbc:mysql://localhost:3306/tunamayo_db"; //insert your database name
                String user = "root";
                String password = "";

                //connect with your database
                conn = DriverManager.getConnection(url1, user, password);
                if (conn != null) {
                    String sql = "SELECT NextOccurence FROM recurtask WHERE numTask = ? ";
                    // Prepare the SQL statement
                    var myStat = conn.prepareStatement(sql);
                    myStat.setInt(1, taskId);
                    ResultSet rs = myStat.executeQuery();
                    if(rs.next()){
                    java.sql.Date sqlDate = rs.getDate("NextOccurence");

                    return sqlDate;
                    }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (conn != null) 
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            
            }
        }
        return null;
    }  
        
    public LocalDate getDueDate(){
          return due;
      }
    
    public static java.sql.Date getDueDate(int taskId){ 
        Connection conn = null; 
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db";
            String user = "root";
            String password = "";
            
            //Connection with database
            conn = DriverManager.getConnection(url1, user, password);
            if (conn != null) {
                //Selects column 'Due' from table recurtask where numTask == taskId
                String sql = "SELECT Due FROM recurtask WHERE numTask = ? ";
                // Prepare the SQL statement
                var myStat = conn.prepareStatement(sql);
                myStat.setInt(1, taskId);
                ResultSet rs = myStat.executeQuery();
                if(rs.next()){
                java.sql.Date sqlDate = rs.getDate("Due");
                
                return sqlDate;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (conn != null) 
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            
            }
        }
        return null;
    }  
    
    //This method returns complete
    public int getCompletion(){
          return complete;
      }
        
    //This method returns a numTask that is completed
    public static int getCompletion(int taskId){
        Connection conn = null; 
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db"; 
            String user = "root";
            String password = "";
            
            //Connection with database
            conn = DriverManager.getConnection(url1, user, password);
            if (conn != null) {
                //Selects column 'Completion' from table recurtask where numTask == taskId
                String sql = "SELECT Completion FROM recurtask WHERE numTask = ? ";
                // Prepare the SQL statement
                var myStat = conn.prepareStatement(sql);
                myStat.setInt(1, taskId);
                ResultSet rs = myStat.executeQuery();
                if(rs.next()){
                    int numTask = rs.getInt("Completion");
                return numTask;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (conn != null) 
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            
            }
        }
        return 0;
    }  
    
    //This method returns title from database based on its taskId
    public static String getTitleDB(int taskId){
        Connection conn = null; 
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db"; 
            String user = "root";
            String password = "";
            
            //Connection with database
            conn = DriverManager.getConnection(url1, user, password);
            if (conn != null) {
                //Selects column 'Title' from table recurtask where numTask == taskId
                String sql = "SELECT Title FROM recurtask WHERE numTask = ? ";
                // Prepare the SQL statement
                var myStat = conn.prepareStatement(sql);
                myStat.setInt(1, taskId);
                ResultSet rs = myStat.executeQuery();
                if(rs.next()){
                String title1 = rs.getString("Title");
                
                return title1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (conn != null) 
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            
            }
        }
        return null;
    }  
    
    //This method returns description from database based on taskid
    public static String getDescDB(int taskId){
        Connection conn = null; 
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db";
            String user = "root";
            String password = "";
            
            //Connection with database
            conn = DriverManager.getConnection(url1, user, password);
            if (conn != null) {
                //Selects column 'Description' from table recurtask where numTask == taskId
                String sql = "SELECT Description FROM recurtask WHERE numTask = ? ";
                // Prepare the SQL statement
                var myStat = conn.prepareStatement(sql);
                myStat.setInt(1, taskId);
                ResultSet rs = myStat.executeQuery();
                if(rs.next()){
                String desc1 = rs.getString("Description");
                
                return desc1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (conn != null) 
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            
            }
        }
        return null;
    }  

    //This method returns the total numTask from database
    public static int getNumTaskDB(){
        Connection conn = null; 
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db";
            String user = "root";
            String password = "";
            int numTask = 0;
            int total = -1;
            //Connection with database
            conn = DriverManager.getConnection(url1, user, password);
            if (conn != null) {
                //Selects column 'numTask' from table recurtask where there is a row of data inside the table
                String sql = "SELECT numTask FROM recurtask WHERE ? ";
                // Prepare the SQL statement 
                var myStat = conn.prepareStatement(sql);
                myStat.setInt(1, 1);
                ResultSet rs2 = myStat.executeQuery();
                while (rs2.next()) {
                    numTask = rs2.getInt("numTask");
                    total++;
                }
                if (total == -1)
                    return 0;
                else
                    return total+1;
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (conn != null) 
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            
            }
        }
        return 0;
    }
    
    //This method returns the total tasks that are complete
    public static int getTotalComplete(){
        Connection conn = null; 
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db"; 
            String user = "root";
            String password = "";
            int numTask = 0;
            int total = -1;
            //Connection with database
            conn = DriverManager.getConnection(url1, user, password);
            if (conn != null) {
                //Selects column 'Completion' from table recurtask where there is a row of data inside the table
                String sql = "SELECT Completion FROM recurtask WHERE ? ";
                // Prepare the SQL statement
                var myStat = conn.prepareStatement(sql);
                myStat.setInt(1, 1);
                ResultSet rs2 = myStat.executeQuery();
                while (rs2.next()) {
                    numTask = rs2.getInt("Completion");
                    if (numTask == 1)
                        total++;
                }
                if (total == -1)
                    return 0;
                else
                    return total+1;      
        }
    } catch (SQLException e) {
        e.printStackTrace();
    } finally {
        // Close resources
        try {
            if (conn != null) 
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }
        return 0;
    } 
       
    //This method is for adding a recurringtask into database
    public static void addRecurringTask(int numTask){
        Scanner scanner = new Scanner(System.in);
        String interval;
        int taskcomplete = 0;

        //Prompt user to input title
        System.out.println("Enter task title: ");
        String title = scanner.nextLine();

        //Prompt user to input description
        System.out.println("Enter task description: ");
        String description = scanner.nextLine();

        //Prompt user to input recurrence interval
        inputInterval: 
            while (true){
            System.out.println("Enter task recurrence interval (daily, weekly, monthly): ");
            interval = scanner.next();
            if (interval.equalsIgnoreCase("daily")||interval.equalsIgnoreCase("weekly")||interval.equalsIgnoreCase("monthly"))
                break inputInterval;
            else { //This is for when users input the interval wrongly and loops the process
               System.out.println("Invalid interval.");
               continue inputInterval;
                }
        }
        
        //These are for declaring nextOccurence and due dates
        LocalDate nextOccurence = LocalDate.now();
        LocalDate due = null;

        //This switch case is for calculating the due date based on the task's recurrence interval
        switch (interval.toLowerCase()){
            case "daily":
                due = nextOccurence.plusDays(1);
                break;
            case "weekly":
                due = nextOccurence.plusWeeks(1);
                break;
            case "monthly":
                due = nextOccurence.plusMonths(1);
                break;
            default:
                throw new IllegalArgumentException ("Invalid recurrence interval.");
        }

        //Conversion of nextOccurence and due to be added inside database
        sqlnextOcc = java.sql.Date.valueOf(nextOccurence);
        sqlDue = java.sql.Date.valueOf(due);

        //Creating a new RecurringTask object
        RecurringTask recurringTask = new RecurringTask(numTask, title, description, interval, nextOccurence, due, taskcomplete);
        //Adding the object inside taskList array
        taskList.add(recurringTask);
        //Calling the method actionInDBR for adding the task inside database
        actionInDBR(2, numTask, title, description, interval, sqlnextOcc, sqlDue, 0);

        //Prompting user the task is created successfully
        System.out.println("\nRecurring Task '"+ title +"' created successfully!\n");
    } 

    //This method is for users to mark its task complete
    public static void completeTask(int taskId){
        //Error handling if the given taskId is wrong
        if (taskId < 0 || taskId >= taskList.size()){
            System.out.println("Invalid task ID.\n");
            return;
        }
        
        //Creating a Task object
        Task task = taskList.get(taskId);
        
        if (task instanceof RecurringTask) {
            RecurringTask recurringTask = (RecurringTask) task; 
            int taskcomplete = 1;//Marking the task to complete as 1
            recurringTask.updateCompletion(taskcomplete);//Update the task completion in taskList
            actionInDBR(4, taskId, null, null, null,null,null, taskcomplete); //Calling the method actionInDBR for updating the task  as completed
            System.out.println("\nRecurring Task '" + getTitleDB(taskId) + "' completed!\n"); //Prompting users that the task is completed
        } else {
            System.out.println("\nTask '" + task.getTitle() +"' completed!\n");
            taskList.remove(taskId); //Non-recurring tasks are removed
        }
    } 
    
    //This method is users to edit the recurrence interval of a task
    public void updateRecurrenceInterval(String newRecurrenceInterval) {
        this.recurrenceInterval = newRecurrenceInterval;
        this.nextOccurence = LocalDate.now(); // Reset next occurrence to today's date
        switch (newRecurrenceInterval.toLowerCase()) { //Recalculate the due of the task
            case "daily":
                this.due = nextOccurence.plusDays(1);
                break;
            case "weekly":
                this.due = nextOccurence.plusWeeks(1);
                break;
            case "monthly":
                this.due = nextOccurence.plusMonths(1);
                break;
        }
        //Conversion of nextOccurence and due
        this.sqlnextOcc = java.sql.Date.valueOf(nextOccurence);
        this.sqlDue = java.sql.Date.valueOf(due);
    }
    
    //This method is for users to edit the completion of the task
    public void updateCompletion(int newCompletion) {
        this.complete = newCompletion;
    }

    //This method is an error-handling process when users want to mark a task as complete, but previously already marked complete
    public static boolean taskAlreadyComplete(int numTask){
         Connection conn = null; 
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db";
            String user = "root";
            String password = "";
            
            //Connection with database
            conn = DriverManager.getConnection(url1, user, password);
            if (conn != null) {
                //Selects column 'Completion' from table recurtask where numTask == numTask
                String sql8 = "SELECT Completion FROM recurtask WHERE numTask = ?";
                // Prepare the SQL statement
                var myStat2 = conn.prepareStatement(sql8);
                myStat2.setInt(1, numTask);
                ResultSet rs = myStat2.executeQuery();

                if (rs.next()) {
                    int isCompleted = rs.getInt("Completion");
                    if (isCompleted == 1){
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (conn != null) 
                    conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            
            }
        } return false;
    }  
    
    //This method is for deleting a recurring task based on its taskId
    public static void deleteTask(int taskId){
        //Creating a Task object from taskList
        Task task = taskList.get(taskId);
        String title = getTitleDB(taskId); //Retrieving the title of the task
        actionInDBR(8,taskId,null,null ,null,null,null, 0); //Calling the method actionInDBR for deleting the task from database

        if (task instanceof RecurringTask) {
            RecurringTask recurringTask = (RecurringTask) task;
            System.out.println("\nRecurring Task '" + title + "' deleted!\n"); //Prompting users that the task is deleted
        } 
        //For removing the task from taskList 
        for (int j = 0; j < taskList.size(); j++) {
            if (taskList.get(j).getNumTask() == taskId) {
                taskList.remove(j);
                break;
            }
        }
        //This method is for reindexing the numTask inside database
        actionInDBR(12, 0, null, null, null, null, null, 0);
    }
      
    //This method is for checking whether the input is in valid form
    public static boolean isValidDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate.parse(dateStr, formatter);// Try parsing the input date string
            return true;  // If parsing is successful, the date is valid
        } catch (DateTimeParseException e) {
            return false;  // If parsing fails, the date is invalid
        }
    }

    //This method consists of all SQL instructions for inserting, updating, selecting, and deleting
    public static void actionInDBR(int actforDB, int taskId, String title, String description, String interval, java.sql.Date sqlnextOcc, java.sql.Date sqldue, int taskcomplete){
        Connection conn1 = null; 
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db";
            String user = "root";
            String password = "";
            
            //Connection with database
            conn1 = DriverManager.getConnection(url1, user, password);
            if (conn1 != null) {
                switch (actforDB){
                    case 1: //This case is for loading all tasks from database 
                        //Selecting all collumns from table recurtask
                        String sql = "SELECT * FROM recurtask";
                        // Prepare the SQL statement
                        PreparedStatement stmt = conn1.prepareStatement(sql);
                        //Execute the SQL statement
                        ResultSet rs = stmt.executeQuery(); 

                        //Retrieving all information to be added inside taskList array
                        while (rs.next()) {
                            int numTask = rs.getInt("numTask");
                            String title1 = rs.getString("title");
                            String description1 = rs.getString("description");
                            String recurrence = rs.getString("RecurrenceInterval");
                            java.sql.Date nextocc = rs.getDate("NextOccurence");
                            java.sql.Date duedate = rs.getDate("Due");
                            int isCompleted = rs.getInt("completion");

                            LocalDate nextocc2 = nextocc.toLocalDate();
                            LocalDate duedate2 = duedate.toLocalDate();

                            int j = 1;
                            //Creating a RecurringTask object based on data retrieved from database
                            RecurringTask task = new RecurringTask(j, title1, description1, recurrence, nextocc2, duedate2, isCompleted); // Assuming Task has this constructor
                            taskList.add(task);  //Adding the object insdie taskList array
                    
                            //Updating i and j for numTask
                            i = Math.max(i, numTask + 1);
                            j++;
                        }
                        System.out.println("Tasks loaded from database successfully!"); //Prompting users that the tasks are loaded
                        break;
                    case 2: //Inserting task inside database
                        LocalDate nextOccurence = LocalDate.now();
                        LocalDate due = null;
                        //Calculating the due date based on recurrence interval
                        switch (interval.toLowerCase()){
                            case "daily":
                                due = nextOccurence.plusDays(1);
                                break;
                            case "weekly":
                                due = nextOccurence.plusWeeks(1);
                                break;
                            case "monthly":
                                due = nextOccurence.plusMonths(1);
                                break;
                            default:
                                throw new IllegalArgumentException ("Invalid recurrence interval.");
                        }

                        //Conversion nextOccurence and due for inserting inside database
                        sqlDue = java.sql.Date.valueOf(due);
                        sqlnextOcc = java.sql.Date.valueOf(nextOccurence);

                        //This is for inserting the task inside the table recurtask with all the columns based on information retrieved from the parameter
                        String sql2 = "INSERT into recurtask(numTask, Title, Description, RecurrenceInterval, NextOccurence, Due, Completion) values (?,?,?,?,?,?,?)";
                        //Prepare the SQL statement
                        var myStat = conn1.prepareStatement(sql2);
                        //Set the parameter values
                        myStat.setInt(1, taskId);
                        myStat.setString(2, title);
                        myStat.setString(3, description);
                        myStat.setString(4, interval);
                        myStat.setDate(5, sqlnextOcc);
                        myStat.setDate(6, sqlDue);
                        myStat.setInt(7, taskcomplete);
                        //Execute the update
                        myStat.executeUpdate();

                        break;
                    case 3: //This case is for displaying all recurring task titles
                        //Prompting users that there is no tasks if taskList is empty
                        if (taskList.isEmpty()){
                            System.out.println("No tasks available.");
                        return;
                        }
                        //Selects column 'numTask' and 'Title' from table recurtask in database
                        String sql3 = "SELECT numTask, Title FROM recurtask ORDER BY numTask asc";
                        //Prepare the SQL statement
                        PreparedStatement stmt1 = conn1.prepareStatement(sql3);
                        //Execute the SQL statement
                        ResultSet rs1 = stmt1.executeQuery();

                        while (rs1.next()) {
                            //Retrieving numTask and title of a task from database
                            int numTask = rs1.getInt("numTask");
                            String title2 = rs1.getString("title");
                            //Displaying titles
                            System.out.println("");
                            System.out.print(numTask+1 + ". " + title2);
                            }
                        System.out.println("");
                        break;
                    case 4: //This case is for updating the next occurence after a task is completed
                        java.sql.Date sqlDate = getNextOccurence(taskId); //Retrieving the next occurence of a task
                        //Convert sqlDate to LocalDate
                        LocalDate nextOcc = sqlDate.toLocalDate();
                        LocalDate dueDate;
                        //Creating objects Task and RecurringTask based on its taskId
                        Task task = taskList.get(taskId); 
                        RecurringTask recurringTask = (RecurringTask) task;
                        
                        //Calculating the new next occurence and due date
                        switch (recurringTask.getRecurrenceInterval(taskId).toLowerCase()){
                            case "daily":
                                nextOcc = nextOcc.plusDays(1);
                                dueDate = nextOcc.plusDays(1);
                                break;
                            case "weekly":
                                nextOcc = nextOcc.plusWeeks(1);
                                dueDate = nextOcc.plusWeeks(1);
                                break;
                            case "monthly":
                                nextOcc = nextOcc.plusMonths(1);
                                dueDate = nextOcc.plusMonths(1);
                                break;
                            default:
                                throw new IllegalArgumentException ("Invalid recurrence interval.");
                        }
                        
                        //Convert the date of the new next occurence and due date to java.sql.Date
                        sqlnextOcc = java.sql.Date.valueOf(nextOcc);
                        sqlDue = java.sql.Date.valueOf(dueDate);
                        //Update columns 'NextOccurence', 'Due', 'Completion' where numTask is  equals taskId inside the recurtask table
                        String sql4 = "UPDATE recurtask SET NextOccurence = ?, Due = ?, Completion = ? WHERE numTask = ?";

                        //Prepare the SQL statement
                        PreparedStatement preparedStatement = conn1.prepareStatement(sql4);

                        //Set the parameter values
                        preparedStatement.setDate(1, sqlnextOcc);
                        preparedStatement.setDate(2, sqlDue); 
                        preparedStatement.setInt(3, taskcomplete); 
                        preparedStatement.setInt(4, taskId); 

                        //Execute the update
                        preparedStatement.executeUpdate();
                        break;
                    case 5: //This case is for displaying all tasks with its due and completion
                        if (taskList.isEmpty()){
                            System.out.println("No tasks available."); //Prompt the user there is no task when taskList is empty
                        return;
                        }
                        //Select columns 'numTask', 'Title', 'Due', 'Completion' according to ascending order of numTask from table recurtask 
                        String sql5 = "SELECT numTask, Title, Due, Completion FROM recurtask ORDER BY numTask asc"; // Adjust table/column names
                        // Prepare the SQL statement
                        PreparedStatement stmt2 = conn1.prepareStatement(sql5);
                        //Execute the SQL statement
                        ResultSet rs2 = stmt2.executeQuery();

                        while (rs2.next()) {
                            //Retrieving information of a task from database
                            int numTask = rs2.getInt("numTask");
                            String title3 = rs2.getString("title");
                            java.sql.Date duedate = rs2.getDate("Due");
                            int isCompleted = rs2.getInt("completion");
                            //Convert dueDate to LocalDate
                            LocalDate duedate2 = duedate.toLocalDate();
                            //Display all tasks with its details
                            System.out.println("");
                            System.out.print(numTask+1 + ". ");
                            if (isCompleted == 0)
                                System.out.print("[Incomplete] "+title3+" - Due: "+duedate2);
                            else if (isCompleted == 1)
                                System.out.print("[Complete] "+title3+" - Due: "+duedate2);
                        }
                        System.out.println("\n");
                        break;
                    case 6: //This case is for selecting a task to update its completion to incomplete (0) if today's date is equals to its next occurence
                        LocalDate today = LocalDate.now();
                        //Select columns 'numTask', 'NextOccurence' and 'Completion' from table recurtask
                        String sql7 = "SELECT numTask, NextOccurence, Completion FROM recurtask";
                        //Prepare the SQL statement
                        var myStat1 = conn1.prepareStatement(sql7);
                        //Execute the SQL statement
                        ResultSet rs3 = myStat1.executeQuery();

                        while (rs3.next()) {
                            //Retrieving the information of a task from database
                            int numTask = rs3.getInt("numTask");
                            java.sql.Date nextocc = rs3.getDate("NextOccurence");
                            int isCompleted = rs3.getInt("Completion");

                            LocalDate nextocc2 = nextocc.toLocalDate();
                            
                            //Checking if today's date is equals to the task's next occurence and the completion is completed (1)
                            if (nextocc2.isEqual(today) && isCompleted == 1){
                                isCompleted = 0; //Reset the completion to incomplete (0)
                                actionInDBR(7, numTask, null, null, null, null, null, isCompleted); //Calling the method actionInDBR to update the completion
                            }
                        }
                        break;
                    case 7: //This case is to update completion when today is equals to the task's next occurence
                        //Update column 'Completion' where numTask is equals to taskId from the recurtask table
                        String sql6 = "UPDATE recurtask SET Completion = ? WHERE numTask = ?";
                        // Prepare the SQL statement
                        preparedStatement = conn1.prepareStatement(sql6);
                        // Set the parameter values
                        preparedStatement.setInt(1, taskcomplete);
                        preparedStatement.setInt(2, taskId); 
                        // Execute the update
                        preparedStatement.executeUpdate();  
                        break;
                    case 8: //This case is to delete a task from database
                        String sql9 = "DELETE FROM recurtask WHERE numTask = ?";
                        //Prepare the SQL statement
                        myStat = conn1.prepareStatement(sql9);
                        //Set the parameter value
                        myStat.setInt(1, taskId);
                        //Execute the SQL statement
                        myStat.execute();
                        break;
                    case 9: //This case is to update the title of a task to a new title
                        //Update column 'Title' where numTask is equals to taskId
                        String sql8 = "UPDATE recurtask SET Title = ? WHERE numTask = ?";
                        //Prepare the SQL statement
                        var update = conn1.prepareStatement(sql8);
                        //Set the parameter values
                        update.setString(1, title);
                        update.setInt(2, taskId);
                        //Execute the update
                        update.executeUpdate();
                        break;
                    case 10: //This case is to update the description of a task to a new description
                        //Update column 'Description' where numTask is equals to taskId
                        String sql10 = "UPDATE recurtask SET Description = ? WHERE numTask = ?";
                        //Prepare the SQL statement
                        var update10 = conn1.prepareStatement(sql10);
                        //Set the parameter valeus
                        update10.setString(1, description);
                        update10.setInt(2, taskId);
                        //Execute the update
                        update10.executeUpdate();
                        break;
                    case 11: //This case is to update the recurrence interval, next occurence and due of a task
                        //Retrieving information of next occurence based on the task's numTask
                        java.sql.Date DateNextOcc = getNextOccurence(taskId);
                        //Conversion of sqlDate to LocalDate
                        LocalDate newNextOcc = DateNextOcc.toLocalDate();
                        LocalDate newDue = null;
                        //Calculating the new due based on the edited recurrence interval
                        switch (interval.toLowerCase()) {
                            case "daily":
                                newDue = newNextOcc.plusDays(1);
                                break;
                            case "weekly":
                                newDue = newNextOcc.plusWeeks(1);
                                break;
                            case "monthly":
                                newDue = newNextOcc.plusMonths(1);
                                break;
                            }
                            
                        //Conversion of LocalDate to sqlDate
                        java.sql.Date sqlnewNextOcc = java.sql.Date.valueOf(newNextOcc);
                        java.sql.Date sqlnewDue = java.sql.Date.valueOf(newDue);
                            
                        //Update columns 'RecurrenceInterval', 'NextOccurence', and 'Due' where numTask is equals to the task's taskId
                        String sql11 = "UPDATE recurtask SET RecurrenceInterval = ?, NextOccurence = ?, Due = ? WHERE numTask = ?";
                        //Prepare the SQL statement
                        var update11 = conn1.prepareStatement(sql11);
                        //Set the parameter values
                        update11.setString(1, interval);
                        update11.setDate(2,sqlnewNextOcc);
                        update11.setDate(3,sqlnewDue);
                        update11.setInt(4, taskId);
                        //Execute the update
                        update11.executeUpdate();
                        break;
                       
                    case 12: //This case is for reindex the numTask to ascending order
                        //Select column 'numTask' from table recurtask
                        String select = "SELECT numTask FROM recurtask ORDER BY numTask ASC";
                        //Prepare the SQL statement
                        PreparedStatement stmt4 = conn1.prepareStatement(select);
                        //Execute the SQL statement
                        ResultSet rs4 = stmt4.executeQuery();
                        //Update column 'numTask' where numTask is equals to the task's taskId from recurtask table
                        String updateQuery = "UPDATE recurtask SET numTask = ? WHERE numTask = ?";
                        //Prepare the SQL statement
                        PreparedStatement stmt3 = conn1.prepareStatement(updateQuery);
                        int inc = 0;
                        while (rs4.next()) {
                            //Retrieving numTask from database
                            int numTask = rs4.getInt("numTask");
                            //Creating a task object
                            Task task1 = taskList.get(inc);
                            //Set numTask in the taskList to 0-based index
                            task1.updateNumTask(inc); 
                            //Update the database
                            stmt3.setInt(1, inc);
                            if (numTask > inc)//New numTask value (0-based)
                                stmt3.setInt(2, inc+1); //Current numTask in the database
                            else
                                stmt3.setInt(2, inc);
                            stmt3.executeUpdate();
                            inc++;
                        }   
                    break;
                case 13: //This case is for updating the task's next occurence, due, and completion when a task has been edited its recurrence interval
                    LocalDate restart = LocalDate.now();
                    LocalDate dueRestart = null;
                    //Recalculate the due date based on its edited recurrence interval
                    switch(getRecurrenceInterval(taskId).toLowerCase()){
                        case "daily":
                            dueRestart = restart.plusDays(1);
                            break;
                        case "weekly":
                            dueRestart = restart.plusWeeks(1);
                            break;
                        case "monthly":
                            dueRestart = restart.plusMonths(1);
                            break;
                        }
                    //Conversion of LocalDate to sqlDate
                    java.sql.Date sqlRestart = java.sql.Date.valueOf(restart);
                    java.sql.Date sqldueRestart = java.sql.Date.valueOf(dueRestart);
                    //Update columns 'NextOccurence', 'Due' and 'Completion' where numTask is equals to task's taskId from recurtask table
                    String sql14 = "UPDATE recurtask SET NextOccurence = ?, Due = ?, Completion = ? WHERE numTask = ?";
                    //Prepare the SQL statement
                    var update1 = conn1.prepareStatement(sql14);
                    //Set the parameter values
                    update1.setDate(1, sqlRestart);
                    update1.setDate(2, sqldueRestart);
                    update1.setInt(3, taskcomplete);
                    update1.setInt(4, taskId);
                    //Execute the update
                    update1.executeUpdate();
                    break;
                case 14: //This case is for searching a task based on its title, description, next occurence, due, completion 
                    Scanner input = new Scanner(System.in);
                    String keyword;
                    boolean match = false;
                    LocalDate inputDate = null;
                    java.sql.Date sqlInputDate = null;
                    
                    if (taskList.isEmpty()){
                        System.out.println("No recurring tasks available."); //Prompt the user that there is no tasks if taskList is empty
                    return;
                    }
                    //Asks the user for what detail they want to search for
                    System.out.println("\nEnter what you want to search for (Title, Description, Next Occurence, Due, Completion)");
                    keyword = input.nextLine().toLowerCase();
                    //This switch case is to differentiate date and other details since date requires a specific formatting of input
                    switch (keyword){
                        case "title":
                        case "description":
                        case "completion":
                            System.out.print("Enter your search key: "); //User enters their search key
                            keyword = input.nextLine().toLowerCase();
                            break;
                        case "next occurence":
                        case "due":
                            System.out.print("Enter your search key: (Follow the format YYYY-MM-DD): "); //User enters their search key following the format of date
                            keyword = input.nextLine().toLowerCase();
                            if (isValidDate(keyword)) { //Calling the method isValidDate to ensure the input follows the format
                                inputDate = LocalDate.parse(keyword); //Converting the search key to LocalDate
                                sqlInputDate = java.sql.Date.valueOf(inputDate); //Conversion of LocalDate to sqlDate
                            } else if (!isValidDate(keyword)){ //Else if the input does not follow the format
                                //Prompt the user to re-enter the search key following the dateformat
                                System.out.println("Invalid input format for date. Please follow the format (YYYY-MM-DD): ");
                            }
                            break;
                        default:
                            System.out.println("Invalid.");
                    }
                    
                    //Select all columns from recurtask table
                    String sql15 = "SELECT * FROM recurtask ORDER BY numTask ASC";
                    //Prepare the SQL statement
                    PreparedStatement stmt5 = conn1.prepareStatement(sql15);
                    //Exeecute the SQL statement
                    ResultSet rs5 = stmt5.executeQuery();

                    while (rs5.next()) {
                        //Retrieving information of a task from database
                        int numTask = rs5.getInt("numTask");
                        String title3 = rs5.getString("Title");
                        String desc3 = rs5.getString("Description");
                        java.sql.Date nextocc = rs5.getDate("NextOccurence");
                        java.sql.Date duedate = rs5.getDate("Due");
                        int isCompleted = rs5.getInt("Completion");
                        //Checks whether the search key matches with any details of the task
                        if (keyword.equalsIgnoreCase(title3) || keyword.equalsIgnoreCase(desc3) || (sqlInputDate != null && sqlInputDate.equals(duedate) || (sqlInputDate != null && sqlInputDate.equals(nextocc)))) {
                            //The match has been found
                            match = true;
                            //Converting sqlDate to LocalDate
                            LocalDate nextocc2 = nextocc.toLocalDate();
                            LocalDate duedate2 = duedate.toLocalDate();
                            //Display the task 
                            System.out.println("");
                            System.out.print(numTask+1 + ". " + title3 + " - " + desc3 + " - " + nextocc2 + " - " + duedate2 + " - ");
                            if (isCompleted == 0)
                                System.out.print("Incomplete");
                            else if (isCompleted == 1)
                                System.out.print("Complete");
                        } else if (keyword.equalsIgnoreCase("Incomplete") || keyword.equalsIgnoreCase("Unfinished") || keyword.equalsIgnoreCase("Unfinish")) { //This is for checking if the search key is Incomplete etc.
                            int completekeyword = 0;
                            if (completekeyword == isCompleted){ //Checking completekeyword is equals to completion from database
                                //The match has been found
                                match = true;
                                //Conversion of sqlDate to LocalDate
                                LocalDate duedate2 = duedate.toLocalDate();
                                LocalDate nextocc2 = nextocc.toLocalDate();
                                //Display the task
                                System.out.println("");
                                System.out.print(numTask+1 + ". " + title3 + " - " + desc3 + " - " + nextocc2 + " - " + duedate2 + " - Incomplete");
                            } 
                        }  else if (keyword.equalsIgnoreCase("Complete") || keyword.equalsIgnoreCase("Finished") || keyword.equalsIgnoreCase("Finish")) {  //This is for checking if the search key is Complete etc.
                            int completekeyword = 1;
                            if (completekeyword == isCompleted){ //Checking completekeyword is equals to completion from database
                                //The match has been found
                                match = true;
                                //Conversion of sqlDate to LocalDate
                                LocalDate duedate2 = duedate.toLocalDate();
                                LocalDate nextocc2 = nextocc.toLocalDate();
                                //Display the task
                                System.out.println("");
                                System.out.println(numTask+1 + ". " + title3 + " - " + desc3 + " - " + duedate2 + " - " + nextocc2 + " - Complete");
                            }
                        }
                    } 
                    System.out.println("");
                    //Check if the match has not been found
                    if (match == false){
                        System.out.println("");
                        System.out.println("Sorry, no match found."); //Prompt the user that the match is not found
                    }  
                    System.out.println("");
                    break;
                case 15: //This case is for sorting the tasks according to the user's desire
                    //Update columns 'numTask', 'Description', 'RecurrenceInterval', 'NextOccurence', 'Due' and 'Completion' where Title is equals to the task's title from recurtask table
                    String sql16 = "UPDATE recurtask SET numTask = ?, Description = ?, RecurrenceInterval = ?, NextOccurence = ?, Due = ?, Completion = ? WHERE Title = ?";
                    //Prepare the SQL statement
                    update = conn1.prepareStatement(sql16);
                    //Set the parameter values
                    update.setInt(1, taskId);
                    update.setString(2, description);
                    update.setString(3, interval);
                    update.setDate(4, sqlnextOcc);
                    update.setDate(5, sqldue);
                    update.setInt(6, taskcomplete);
                    update.setString(7, title);
                    //Execute the update
                    update.executeUpdate();
                    break;
                } 
            }
        } catch(SQLException ex){
            System.out.println("An error occurred");
            ex.printStackTrace();
        } finally {
            if (conn1 != null) {
                try {
                    conn1.close();
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        } 
    }
}