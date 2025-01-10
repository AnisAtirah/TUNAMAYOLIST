package TunaMayo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import static TunaMayo.RecurringTask.actionInDBR;
import static TunaMayo.RecurringTask.taskList;

//This class is used in method to detect cycle of dependencies
class ListNode {
    int val;
    ListNode next;

    // Constructor to create a new node
    ListNode(int val) {
        this.val = val;
        this.next = null;
    }
}

public class TunaMayo {
    protected static int totalDEL;
    public static String recurrenceInterval; //"weekly", "daily", "monthly"
    public static LocalDate nextOccurence;
    public static LocalDate due;
    public static java.sql.Date sqlDue;
    public static java.sql.Date sqlnextOcc;
    public static int i = 1;
    public static int complete;
    public static int numTask;
    
    public static void main(String[] args) throws Exception{
        Scanner input = new Scanner(System.in);
        int option = 0;
        int i = 0;
        boolean taskEXISTEDinDB = false;
        int i_depend = 0;
        int count_Depend = 0;
        int last_num=0;
        int actforDB = 0;
        String recepient= "";
        
        //Creating arraylist to store task information
        ArrayList<Integer> taskNumber = new ArrayList<>();
        ArrayList<String> title = new ArrayList<>();
        ArrayList<String> describe = new ArrayList<>();
        ArrayList<String> date = new ArrayList<>();
        ArrayList<String> category = new ArrayList<>();
        ArrayList<String> priority = new ArrayList<>();
        ArrayList<Integer> status = new ArrayList<>();
        ArrayList<Integer> dependency = new ArrayList<>();
        int [] FetchDatafromDB = new int[2];
        
        //This method is to fetch past tasks that has been stored inside database
        FetchDatafromDB = forNumberingTask(FetchDatafromDB, taskEXISTEDinDB, i, last_num, taskNumber, title, describe, date, category, priority, status, dependency, i_depend);
        recepient = EmailAddressfromDB();
        loadDBR();
        //This method is for continuous checking of task's next occurrence 
        RecurringTask.scheduleRecurringTaskCheck();
        
        i = FetchDatafromDB[0]; 
        i_depend = FetchDatafromDB[1];
        int i_old = i;
        int inc = 0;
        
        //Arrays needed for coding of task dependencies
        int [] taskPreceding = new int[i_depend];
        int [] dependent = new int[i_depend];
        ListNode[] nodes = new ListNode[0]; //Array of ListNode to store the nodes of dependencies

        if (i!=0){
            for (int k=0, j=2; k<i_depend;k++,j+=2){
               dependent[k]=FetchDatafromDB[j]; 
               taskPreceding[k]=FetchDatafromDB[j+1];
            }

            nodes = Arrays.copyOf(nodes, nodes.length + i); //updated array nodes
            for (int j = 0; j<i; j++) {//Create nodes for each task
               nodes[j] = new ListNode(j);
            }
        }
        
        //User select which procedure
        while (option!=10) {
            if (!recepient.equalsIgnoreCase("0")){ dueNoti(title, date, recepient); }
            System.out.print("What do you want to do today ");
            System.out.println("\n1) Create a task");
            System.out.println("2) Add a recurring task");
            System.out.println("3) View all tasks");
            System.out.println("4) Manage task");
            System.out.println("5) Delete task");
            System.out.println("6) Sort Task");
            System.out.println("7) Search Task");
            System.out.println("8) Edit task");
            System.out.println("9) Your tasks analytics");
            System.out.println("10) Exit program");
            System.out.print("Enter number: ");
            option = input.nextInt();
            input.nextLine();
        
            switch (option){
                case 1: //Create a task
                    i = createTask(i, taskNumber, title, describe, date, category, priority, status, dependency, input);
                    int countNewTask = i - i_old; //ni untuk contohnye, sblm ni, user dh penah masuk 3 tasks, so lps tu dia run java again, so dptlah brp byk newTask yg dia create lps tu
                                   
                    nodes = Arrays.copyOf(nodes, nodes.length + countNewTask);
                    for (int j = i_old; j<nodes.length; j++) {
                        nodes[j] = new ListNode(j); //create node for each new task
                    }
                    i_old = i; 
                    break;
                case 2: //Create a recurring task
                    inc = RecurringTask.getNumTaskDB();
                    RecurringTask.addRecurringTask(inc);
                    break;
                case 3:  //Display all tasks
                    System.out.println("");
             
                    actionInDB(4,0, null, null, null, null, null,0,0,null,null,0,0,null);
                    System.out.println("=== View All Recurring Tasks ===");
                    RecurringTask.actionInDBR(5, 0, null, null, null, null,null,0);
                    System.out.println("");
                    break;
                case 4: //Mark task as complete
                    System.out.println("Which type of task do you want to mark complete?\n1. Regular Task\n2. Recurring Task");
                    System.out.print("[ 1 / 2 ] : ");
                    int type = input.nextInt();
                    
                    //Arrays needed during the checking of existence of task dependencies in taskCompletion() method
                    taskPreceding = Arrays.copyOf(taskPreceding, taskPreceding.length);
                    dependent = Arrays.copyOf(dependent, dependent.length);
                    nodes = Arrays.copyOf(nodes, nodes.length);
                        
                    boolean depend = false;
                    if (type == 1) { //For regular tasks
                        taskCompletion(status, taskNumber, title, dependent, taskPreceding, depend, count_Depend, input);
                        System.out.println("");
                    } else if (type == 2) { //For recurring tasks
                        System.out.println("\n=== Mark Task as Complete ===");
                        RecurringTask.actionInDBR(3, 0, null, null, null,null,null, 0);
                        if (taskList.isEmpty()){
                            System.out.println("No tasks available.");
                        break;
                        }
                        System.out.println("");
                        System.out.print("Which task do you want to mark as completed? (Insert number): \n");
                        int numTask = input.nextInt();

                        //Check whether the task has been completed in database
                        boolean taskAlrComp = RecurringTask.taskAlreadyComplete(numTask-1);
                        if (taskAlrComp){
                            System.out.println("Task is already completed.");
                            System.out.println("");
                            break;
                        } else
                            RecurringTask.completeTask(numTask-1);
                    }
                    break;
                case 5: //Deleting a task
                    System.out.println("");
                    System.out.println("========== Delete Task =========="); 
                    System.out.println("Which type of task do you want to delete?\n1. Regular Task\n2. Recurring Task");
                    System.out.print("[ 1 / 2 ] : ");
                    int type2 = input.nextInt();
                    if (type2 == 1){ //For regular tasks
                        deleteTaskArr(taskNumber, title, describe, date, category, priority, status, dependency, input);
                        totalDEL++;
                        actionInDB(13,0, null, null, null, null, null,0,0,null,null,0,0,null);
                        i=i-1;
                        if (i==0){
                            totalDEL = 0;
                            actionInDB(13,0, null, null, null, null, null,0,0,null,null,0,0,null);
                        }
                    } else if (type2 == 2) { //For recurring tasks
                        actionInDBR(3, 0, null, null, null,null,null, 0);
                        System.out.println("");
                        System.out.print("Which task do you want to delete? (Insert number) : ");
                        int taskId = input.nextInt();
                        input.nextLine();

                        if (taskId-1 < 0 || taskId-1 >= taskList.size()){
                            System.out.println("Invalid task ID.\n");
                        } else {
                            RecurringTask.deleteTask(taskId-1);
                        }
                    }
                    System.out.println("");
                    break;
                case 6: //Sorting tasks
                    System.out.println("");
                    System.out.println("========== Sort Task =========="); 
                    System.out.println("Which type of task do you want to search?\n1. Regular Task\n2. Recurring Task");
                    System.out.print("[ 1 / 2 ] : ");
                    int type3 = input.nextInt();
                    if (type3 == 1){ //For regular tasks
                        sortTask(taskNumber, title, describe, date, category, priority, status,input);
                    } else if (type3 == 2) { //For recurring tasks
                        sortTaskR(taskList, input);
                    }
                    break;
                case 7: //Searching tasks by a search key
                    System.out.println("");
                    System.out.println("========== Search Task =========="); 
                    System.out.println("Which type of task do you want to search?\n1. Regular Task\n2. Recurring Task");
                    System.out.print("[ 1 / 2 ] : ");
                    type3 = input.nextInt();
                    if (type3 == 1){ //For regular tasks
                    searchTask(title, describe, date, category, priority, taskNumber, status, dependency, input);
                    } else if (type3 == 2) { //For recurring tasks
                        RecurringTask.actionInDBR(14, 0, null, null, null,null,null, 0);
                    }
                    break;
                case 8: //Editing a task
                    System.out.println("\n=== Edit Task ===");
                    System.out.println("Which type of task do you want to edit?\n1. Regular Task\n2. Recurring Task");
                    System.out.print("[ 1 / 2 ] : ");
                    type3 = input.nextInt();
                    
                    if (type3 == 1){ //For regular tasks
                    System.out.print("Enter the task number you want to edit: ");
                    int taskToEdit = input.nextInt();
                    
                    if (taskToEdit-1 < 0 || taskToEdit-1 >= taskNumber.size()){
                        System.out.println("Task number " + taskToEdit + " not found.");
                    } else {
                        System.out.print("\nWhat would you like to edit? \n1. Title\n2. Description\n3. Due Date\n4. Category\n5. Priority\n6. Set Task Dependency \n7. Cancel \nEnter no.: ");
                        int edit = input.nextInt();
                        while (edit!=7){
                            switch (edit){
                                case 1: //Edit title
                                    System.out.print("Enter the new title: ");
                                    input.nextLine();
                                    String newTitle = input.nextLine();
                                    title.set(taskToEdit-1, newTitle);
                                    System.out.println("New title is: "+ title.get(taskToEdit-1));
                                    actionInDB(8,taskToEdit,newTitle,null ,null,null,null,0,0,null,null,0,0,null); //update task title in database
                                    break;
                                case 2: //Edit description
                                    System.out.print("Enter the new description: ");
                                    input.nextLine();
                                    String newDescription = input.nextLine();
                                    describe.set(taskToEdit-1, newDescription);
                                    actionInDB(9,taskToEdit,null,newDescription,null,null,null,0,0,null,null,0,0,null); //update task desc in database
                                    break;
                                case 3: //Edit due date
                                    System.out.print("Enter the new due date (YYYY-MM-DD): ");
                                    input.nextLine();
                                    String newDue = input.nextLine();
                                    date.set(taskToEdit-1, newDue);
                                    actionInDB(10,taskToEdit,null,null,newDue,null,null,0,0,null,null,0,0,null); //update task due in database
                                    break;
                                case 4: //Edit category
                                    System.out.print("Enter the new category (Homework / Personal / Work) : ");
                                    input.nextLine();
                                    String newCategory = input.nextLine();
                                    category.set(taskToEdit-1, newCategory);
                                    actionInDB(11,taskToEdit,null,null,null,newCategory,null,0,0,null,null,0,0,null); //update task category in database
                                    break;
                                case 5: //Edit priority level
                                    System.out.println("Enter the new priority level (High, Medium, Low)");
                                    input.nextLine();
                                    String newPriority = input.nextLine();
                                    priority.set(taskToEdit-1, newPriority);
                                    actionInDB(12,taskToEdit,null,null ,null,null,newPriority,0,0,null,null,0,0,null); //update task category in database
                                    break;
                                case 6: //Edit dependency
                                    //Arrays needed for coding of task dependencies
                                    taskPreceding = Arrays.copyOf(taskPreceding, taskPreceding.length + 1);
                                    dependent = Arrays.copyOf(dependent, dependent.length + 1);
                                    nodes = Arrays.copyOf(nodes, nodes.length);

                                    taskDependency(i_depend, nodes, dependent, taskPreceding, title, input, taskToEdit);
                                    i_depend++;
                                    break;
                                case 7:
                                    break;
                                default:
                                    System.out.println("Enter valid input");
                                    break;
                                } 
                            System.out.print("\nWhat would you like to edit? \n1. Title\n2. Description\n3. Due Date\n4. Category\n5. Priority\n6. Set Task Dependency \n7. Cancel \nEnter no.: ");
                            edit = input.nextInt();
                            } 
                        }
                    } else if (type3 == 2){ //For recurring tasks
                        RecurringTask.actionInDBR(3, 0, null, null, null,null,null, 0);
                        System.out.println("");
                        System.out.print("Which task do you want to edit? (Insert number): ");
                        int taskToEditR = input.nextInt();
                        input.nextLine();
                        if (taskToEditR-1 < 0 || taskToEditR-1 >= taskList.size()){
                            System.out.println("Invalid task ID.\n");
                        } else {
                            Task task = taskList.get(taskToEditR-1);
                            RecurringTask recurringTask = (RecurringTask) task;
                            System.out.print("\n\nWhat would you like to edit? \n1. Title\n2. Description\n3. Recurrence Interval\n4. Completion\n5. Cancel\nEnter no.: ");
                            int edit1 = input.nextInt();
                        
                            while (edit1!=5){
                                switch (edit1){
                                    case 1: //Edit title
                                        System.out.print("Enter the new title: ");
                                        input.nextLine();
                                        String newTitle = input.nextLine();
                                        task.updateTitle(newTitle); 
                                        System.out.println("Task '"+ RecurringTask.getTitleDB(taskToEditR-1) +"' has been updated to '"+newTitle+"'!");
                                        RecurringTask.actionInDBR(9, taskToEditR-1, newTitle, null, null, null,null,0);
            
                                        break;
                                    case 2: //Edit description
                                        System.out.print("Enter the new description: ");
                                        input.nextLine();
                                        String newDescription = input.nextLine();
                                        task.updateDescription(newDescription);
                                        System.out.println("Task '"+ RecurringTask.getTitleDB(taskToEditR-1) +"' has been updated to a new description '"+newDescription+"'!");
                                        RecurringTask.actionInDBR(10, taskToEditR-1, null, newDescription, null, null,null,0);
                                        break;
                                    case 3: //Edit recurrence interval
                                        System.out.print("Enter the new recurrence interval (Daily / Weekly / Monthly) : ");
                                        input.nextLine();
                                        String newRecurrenceInterval = input.nextLine();
                                        recurringTask.updateRecurrenceInterval(newRecurrenceInterval); //Update recurrence interval, next occurrence, due date
                                        System.out.println("Task '"+ RecurringTask.getTitleDB(taskToEditR-1) +"' has been updated to a new recurrence interval '"+newRecurrenceInterval+"'!");
                                        RecurringTask.actionInDBR(11, taskToEditR-1, null, null, newRecurrenceInterval,null,null, 0);
                                        RecurringTask.actionInDBR(13, taskToEditR-1, null, null, null,null,null, 0);
                                        break;
                                    case 4: //Edit task completion to incomplete
                                        System.out.print("This is to mark the task incomplete. Proceed? (Yes / No) : ");
                                        input.nextLine();
                                        String ans = input.nextLine();
                                        if (ans.equalsIgnoreCase("yes")){
                                            recurringTask.updateCompletion(0);
                                            System.out.println("Task '"+ RecurringTask.getTitleDB(taskToEditR-1) +"' has been updated incomplete!");
                                            RecurringTask.actionInDBR(7, taskToEditR-1, null, null, null,null,null, 0);
                                            RecurringTask.actionInDBR(13, taskToEditR-1, null, null, null, null,null,0);
                                        } else {
                                            break;
                                        }
                                        break;
                                    case 5:
                                        break;
                                    default:
                                        System.out.println("Enter valid input");
                                        break;
                                }
                        System.out.print("\nWhat would you like to edit? \n1. Title\n2. Description\n3. Recurrence Interval\n4. Completion\n5. Cancel \nEnter no.: "); 
                        edit1 = input.nextInt();
                        }
                      }
                    }
                    System.out.println("");
                    break;
                case 9: //Display data analytics
                    dataAnalytics(i,taskNumber,status,category);
                    System.out.println("\n");
                    break;
                case 10: //Exit
                    System.out.println("Before exit, would you like to receive email notification from us? \nWe will help remind you when the due date for your tasks is approaching.");
                    System.out.print("(Enter \"No\" if you have already entered your email address) [Yes/No]: ");
                    if (input.nextLine().equalsIgnoreCase("yes")){
                        System.out.print("\nEnter your email address: ");
                        recepient = input.nextLine();
                        actionInDB(7,0, null, null, null, null, null,0,0,null,null,0,0,recepient);
                        System.out.println("Thank you! We will notify you when a task is due within 24 hours.");
                    }
                    System.out.println("Bye for Now!");
                    break;
                default:
                    System.out.println("Invalid number. Please enter again");
                    System.out.println("");
                    break;    
            }
        } System.exit(0);
    } 
    

    //Fetch past tasks inside database and added to arrayList
    public static int [] forNumberingTask(int [] justEXISTEDtask, boolean taskEXIST, int i, int last_num, ArrayList<Integer> taskNumber, ArrayList<String> title, ArrayList<String> describe, ArrayList<String> date, ArrayList<String> category, ArrayList<String> priority, ArrayList<Integer> status, ArrayList<Integer> dependency, int i_depend) {
        Connection conn1 = null; 
        try {
        String url1 = "jdbc:mysql://localhost:3306/tunamayo_db"; //insert your database name
        String user = "root";
        String password = "";

        //connect with your database
        conn1 = DriverManager.getConnection(url1, user, password);
        if (conn1 != null) {
            int taskEXISTEDinDB = 0;
            int for_i_depend = 2;

            //NAK CARIK APE INDEX TASK LAST DALAM DB, THEN BOLEH FETCH DATA DRI DB MASUK SINI
            String sql4 = "SELECT * FROM task order by numTask desc limit 1";
            var carik = conn1.prepareStatement(sql4);

            var last = carik.executeQuery();
            while (last.next()){
                taskEXIST = true; //if true, maksudnye before this, dah ade task pernah dimasukkan dlm db
                last_num = last.getInt(1);
                i = last_num+1;

                String sql5 = "SELECT * FROM task order by numTask asc";
                var isiArr = conn1.prepareStatement(sql5);

                var fill = isiArr.executeQuery(); //maksud isiArr ni is nak isi array dgn task2 yg dh pernah added dlm db, so that kat main() nnti user boleh keep smbung dia punye to do list even if java is distop runnye byk kali
                while (fill.next()){
                    justEXISTEDtask = Arrays.copyOf(justEXISTEDtask, justEXISTEDtask.length + (i_depend+1));
                    taskNumber.add((fill.getInt(1)+1));
                    title.add(fill.getString(2));
                    describe.add(fill.getString(3));
                    date.add(fill.getString(4));
                    category.add(fill.getString(5));
                    priority.add(fill.getString(6));
                    status.add(fill.getInt(7));
                    dependency.add(fill.getInt(8));
                    
                    if (dependency.get(taskEXISTEDinDB)!=-1){
                        justEXISTEDtask = Arrays.copyOf(justEXISTEDtask, justEXISTEDtask.length + (for_i_depend+2));
                        justEXISTEDtask[for_i_depend]= taskNumber.get(taskEXISTEDinDB)-1;
                        justEXISTEDtask[for_i_depend+1]= dependency.get(taskEXISTEDinDB);
                        for_i_depend+=2;
                        i_depend++;
                        
                    }
                    taskEXISTEDinDB++;
                    String sql = "SELECT * FROM deleted_task";
                    var totDEL = conn1.prepareStatement(sql);

                    var getTotDEL = totDEL.executeQuery();
                    if (getTotDEL.next()){
                    totalDEL = getTotDEL.getInt(1);}
                }
            } 
            if (taskEXIST==false) {
                i = 0;
                i_depend = 0;
                totalDEL = 0;
                actionInDB(13,0, null, null, null, null, null,0,0,null,null,0,0,null);
            } 
            justEXISTEDtask[0] = i;
            justEXISTEDtask[1] = i_depend;
            
        }
        }catch(SQLException ex){
            System.out.println("An error occurred");
            ex.printStackTrace();
        }
        finally {
            if (conn1 != null) {
                try {
                    conn1.close();
                }
                catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return justEXISTEDtask;
    }

    //Fetch past recurring tasks from database and insert to taskList
    public static void loadDBR(){
        Connection conn1 = null; 
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db";
            String user = "root";
            String password = "";
            
            //Connection with your database
            conn1 = DriverManager.getConnection(url1, user, password);
            if (conn1 != null) {
                String sql = "SELECT numTask, Title, Description, RecurrenceInterval, NextOccurence, Due, Completion FROM recurtask"; // Adjust table/column names

                PreparedStatement stmt = conn1.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery(); 
                
                int currentNumTask = RecurringTask.getNumTaskDB();
                while (rs.next()) {
                    numTask = rs.getInt("numTask");
                    String title1 = rs.getString("title");
                    String description1 = rs.getString("description");
                    String recurrence = rs.getString("RecurrenceInterval");
                    java.sql.Date nextocc = rs.getDate("NextOccurence");
                    java.sql.Date duedate = rs.getDate("Due");
                    int isCompleted = rs.getInt("completion");

                    LocalDate nextocc2 = nextocc.toLocalDate();
                    LocalDate duedate2 = duedate.toLocalDate();


                    RecurringTask task = new RecurringTask(currentNumTask, title1, description1, recurrence, nextocc2, duedate2, isCompleted);
                    taskList.add(task); 
                    i = Math.max(i, numTask + 1);
                    currentNumTask++;
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

    //Method to create a task
    public static int createTask(int i, ArrayList<Integer> taskNumber, ArrayList<String> title, ArrayList<String> describe, ArrayList<String> date, ArrayList<String> category, ArrayList<String> priority, ArrayList<Integer> status, ArrayList<Integer> dependency, Scanner input){
        while (true) {
            System.out.println("");
            System.out.println("========== Add a New task ==========");
            System.out.print("Enter task title: ");
            title.add(input.nextLine());
            System.out.print("Enter task description: ");
            describe.add(input.nextLine());
            inputDate:
                while (true){
                    System.out.print("Enter due date (YYYY-MM-DD): ");
                    String DATE = input.nextLine();
                    if (DATE.length()<8 || DATE.matches(".*[a-zA-Z].*")){
                        System.out.println("Invalid input. Make sure the to follow exactly the format (YYYY-MM-DD)");
                        System.out.println("");
                        continue inputDate;
                    } else {
                        date.add(DATE);
                        break inputDate; }
                }
            System.out.print("Enter task category (Homework / Personal / Work): ");
            category.add(input.nextLine());
            inputPriority: 
                while (true){
                    System.out.print("Enter priority level (High / Medium / Low): ");  
                    String PRIORITY = input.nextLine();              
                    if (PRIORITY.equalsIgnoreCase("high")||PRIORITY.equalsIgnoreCase("medium")||PRIORITY.equalsIgnoreCase("low")) {
                        priority.add(PRIORITY);
                        break inputPriority;
                    } else {
                        System.out.println("Invalid priority. Enter the priority again.");
                        System.out.println("");
                        continue inputPriority;
                    }
                }
               
            taskNumber.add((i+1));
            String title1 = title.get(i);
            String describe1 = describe.get(i);
            String  date1 = date.get(i);
            String category1 = category.get(i);
            String priority1 = priority.get(i);
            status.add(0);  
            dependency.add(null);
            
            actionInDB(1,i, title1, describe1, date1, category1, priority1,0,0,null,null,0,0,null);
            System.out.println("Task \"" + title.get(i) + "\" added succesfully!");
            i++; 
            System.out.print("\nAdd more task? (Enter Yes / No): ");
            String choice = input.nextLine();
            if (!choice.equalsIgnoreCase("yes")) {
                System.out.println("");
                return i;
            }
        }
    }

    //Method to delete a task
    public static void deleteTaskArr(ArrayList<Integer> taskNumber, ArrayList<String> title, ArrayList<String> describe, ArrayList<String> date, ArrayList<String> category, ArrayList<String> priority, ArrayList<Integer> status, ArrayList<Integer> dependency, Scanner input){

        System.out.println("\n========== Delete Task ==========");
        
        try {
            if (taskNumber.isEmpty()) {
                System.out.println("No tasks available to delete.");
                return;
            }
            
            // Display current tasks with their numbers
            for (int i = 0; i < taskNumber.size(); i++) {
                System.out.printf("Task %d: %s%n", taskNumber.get(i), title.get(i));
            }
            
            System.out.print("Enter task number: ");
            int inputTaskDEL = input.nextInt();
            input.nextLine();
            
            // Find the index where taskNumber matches inputTaskNum
            int taskDEL = -1;
            for (int i = 0; i < taskNumber.size(); i++) {
                if (taskNumber.get(i) == inputTaskDEL) {
                    taskDEL = i;
                    break;
                }
            }
            
            if (taskDEL == -1) {
                System.out.println("Task number " + inputTaskDEL + " not found.");
                return;
            }
            
            actionInDB(2, inputTaskDEL-1, null, null, null, null, null, 0, 0, null, null, 0, 0, null); //delete task in DB
            System.out.println("Task \"" + title.get(taskDEL) + "\" deleted successfully");
            
            //remove every info berkaitan dengan task
            taskNumber.remove(inputTaskDEL-1);
            title.remove(inputTaskDEL-1);
            describe.remove(inputTaskDEL-1);
            date.remove(inputTaskDEL-1);
            category.remove(inputTaskDEL-1);
            priority.remove(inputTaskDEL-1);
            status.remove(inputTaskDEL-1);
            dependency.remove(inputTaskDEL-1);
            
            // Reindex the taskNumber list
            for (int i = 0; i < taskNumber.size(); i++) {
                taskNumber.set(i, i + 1);
            }
            
            for (int i=0; i < taskNumber.size(); i++) {
                String titlesort = title.get(i);
                String describesort = describe.get(i);
                String datesort = date.get(i);
                String categorysort = category.get(i);
                String prioritysort = priority.get(i);
                int statussort = status.get(i);
              
                actionInDB(6, i, titlesort, describesort , datesort, categorysort, prioritysort, 0, 0, null, null, 0, statussort,null);
            }
        } catch (InputMismatchException e) {
            System.out.println("Please enter a valid number.");
            input.nextLine(); // Clear invalid input
        }
    
}

    //Method to set a task as completed
    public static void taskCompletion(ArrayList<Integer> status, ArrayList<Integer> taskNumber, ArrayList<String> taskTitle, int [] dependent, int [] taskPreceding, boolean depend, int count_Depend, Scanner input){
        System.out.println("\n=== Mark Task as Complete ===");
        System.out.print("Enter the task number you want to mark as complete: ");
        int taskDONE = input.nextInt();
        count_Depend = 0;
        
        // Find the index where taskNumber matches taskDONE
        int taskCOMPLETE = -1;
        for (int i = 0; i < taskNumber.size(); i++) {
            if (taskNumber.get(i) == taskDONE) {
                taskCOMPLETE = i;
                break;
            }
        }

        if (taskCOMPLETE == -1) {
            System.out.println("Task number " + taskDONE + " not found.");
            return;
        }
            
        switch (status.get(taskDONE-1)){  
            case 0:
                for (int j=0;j<dependent.length;j++) { //check whether the task is an dependency task or not 
                    if ((taskDONE-1)==dependent[j]){  
                        depend = true;
                        break;
                    } else {
                        depend = false;
                    }
                    count_Depend++;
                }
                if (depend == false){ //loop for task with no dependency
                    System.out.println("\nTask \"" + taskTitle.get(taskDONE-1) + "\" marked as complete!");
                    status.set(taskDONE-1, 1); //update completion in arraylist
                    actionInDB(3,0,null,null ,null,null,null,0,taskDONE,null,null,0,0,null); //update completion in database
                    break;
                } else { //loop for task with dependency
                    if (status.get(taskPreceding[count_Depend])==0){ //taskStatus[0]==0
                        System.out.println("\nWarning: Task \"" + taskTitle.get(taskDONE-1) + "\" cannot be marked as complete because it depends on \"" + taskTitle.get(taskPreceding[count_Depend])+ "\".");
                        System.out.println("Please complete \"" + taskTitle.get(taskPreceding[count_Depend]) + "\" first."); 
                        break;
                    }else{
                        System.out.println("\nTask \"" + taskTitle.get(taskDONE-1) + "\" marked as complete!");
                        status.set(taskDONE-1, 1); //update completion in arraylist
                        actionInDB(3,0,null,null ,null,null,null,0,taskDONE,null,null,0,0,null); //update completion in database
                        break;
                    }
                }
            case 1:
                System.out.println("\nTask \"" + taskTitle.get(taskDONE-1) + "\" already marked as complete!");
                break;
            default:
                System.out.println("\nNo data for Task " + taskDONE);
        }
    }

    //Method for setting task dependencies
    public static void taskDependency(int i_depend, ListNode[] nodes, int[] dependent, int[] taskPreceding, ArrayList<String> title, Scanner input, int taskToEdit) {
        System.out.println("\n=== Set Task Dependency ===");
        System.out.println("\nEnter task number that depends on another task: " + taskToEdit);

        dependent[i_depend] = (taskToEdit - 1); // Task that depends on another task
        System.out.print("Enter the task number it depends on: ");
        taskPreceding[i_depend] = (input.nextInt() - 1); // Task that is being depended on

        nodes[taskPreceding[i_depend]].next = nodes[dependent[i_depend]]; // Add dependency in the linked list

        // Detect cycle of dependency
        TunaMayo cycleDetector = new TunaMayo();
        if (cycleDetector.hasCycle(nodes[taskPreceding[i_depend]])) {
            System.out.println("\nCycle detected! Dependency not added.");

            // Remove the dependency if cycle is detected
            nodes[taskPreceding[i_depend]].next = null;

            // Reset the last added dependency in tracking array
            dependent[i_depend] = 0;
            taskPreceding[i_depend] = 0;
        } else { // If no cycle detected, update the data and add the dependency
            actionInDB(5, 0, null, null, null, null, null, 0, 0, dependent, taskPreceding, i_depend, 0,null);
            System.out.println("\nTask \"" + title.get(dependent[i_depend]) + "\" now depends on \"" + title.get(taskPreceding[i_depend]) + "\".");
        }
    }

    //Method for sorting tasks
    public static void sortTask(ArrayList<Integer> taskNumber, ArrayList<String> title, ArrayList<String> describe, ArrayList<String> date, ArrayList<String> category, ArrayList<String> priority, ArrayList<Integer> status, Scanner input) {
   
    CatchData(taskNumber, title, describe, date, category, priority, status);
    
    int choice;
    
    do {
    System.out.println("");
    System.out.println("Sort by :");
    System.out.println("1. Due date (ascending)");
    System.out.println("2. Due date (descending)");
    System.out.println("3. Priority (high to low)");
    System.out.println("4. Priority (low to high)");
    System.out.print("Enter the number of your choice : ");
    while (!input.hasNextInt()) { // Handle non-integer inputs
        System.out.print("Invalid input. Please enter a number between 1 and 4. \nEnter new number : ");
        input.nextLine(); // Consume the invalid input
    }
        choice = input.nextInt();
        input.nextLine(); // Clear the buffer
    } while (choice < 1 || choice > 4);
    
    switch (choice) {
    
        case 1 : 
        int n = date.size();

        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < n-i-1; j++) {
                LocalDate date1 = LocalDate.parse(date.get(j));
                LocalDate date2 = LocalDate.parse(date.get(j+1));
         
            if (date1.isAfter(date2)) {
                swap1(title, j, j + 1);
                swap1(describe, j, j + 1);
                swap1(date, j, j + 1);
                swap1(category, j, j + 1);
                swap1(priority, j, j + 1);
                swap2(status, j, j + 1);
            }
            }
        }
        break;

        case 2 :
        int k = date.size();
       
        for (int i = 0; i < k-1; i++) {
            for (int j = 0; j < k-i-1; j++) {
                LocalDate date1 = LocalDate.parse(date.get(j));
                LocalDate date2 = LocalDate.parse(date.get(j+1));

            if (date1.isBefore(date2)) {
                swap1(title, j, j + 1);
                swap1(describe, j, j + 1);
                swap1(date, j, j + 1);
                swap1(category, j, j + 1);
                swap1(priority, j, j + 1);
                swap2(status, j, j + 1);
            }
            }
        }
        break;

        case 3 : //sort by priority from high to low
        int m = priority.size();
        for (int i = 0; i < m-1; i++) {
            for (int j = 0; j < m-i-1; j++) {
                    if (priorityRank(priority.get(j)) < priorityRank(priority.get(j+1))) {
                        swap1(title, j, j + 1);
                        swap1(describe, j, j + 1);
                        swap1(date, j, j + 1);
                        swap1(category, j, j + 1);
                        swap1(priority, j, j + 1);
                        swap2(status, j, j + 1);
                    }
                }
            }
        break;

        case 4 :   //sort by priority from low to high
        int h = priority.size();
        for (int i = 0; i < h-1; i++) {
            for (int j = 0; j < h-i-1; j++) {
                    if (priorityRank(priority.get(j)) > priorityRank(priority.get(j+1))) {
                        swap1(title, j, j + 1);
                        swap1(describe, j, j + 1);
                        swap1(date, j, j + 1);
                        swap1(category, j, j + 1);
                        swap1(priority, j, j + 1);
                        swap2(status, j, j + 1);
                    }
                }
            }
        break;

        default :
        System.out.println("Invalid choice. Please enter 1-4 only.");
            
        } 
    
        for (int i =0; i < taskNumber.size(); i++) {
            String titlesort = title.get(i);
            String describesort = describe.get(i);
            String datesort = date.get(i);
            String categorysort = category.get(i);
            String prioritysort = priority.get(i);
            int statussort = status.get(i);
            
            actionInDB(6, i, titlesort, describesort , datesort, categorysort, prioritysort, 0, 0, null, null, 0, statussort,null);
        }
        
        System.out.println("");
        System.out.println("========Task sorted!========");
        System.out.println("");
        
        for (int i = 0; i < taskNumber.size(); i++) {
                System.out.print((i+1) + ". " + title.get(i) + " - " + describe.get(i) + " - " + date.get(i) + " - " + category.get(i) + " - " + priority.get(i));
                if (status.get(i)==0) {
                    System.out.println(" - incomplete");
                } else {
                    System.out.println(" - completed");
                }
        }
        System.out.println("");
    } 

    //Methods for swapping
    public static void swap1 (ArrayList<String> list, int i, int j){
        String temp1 = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp1);
    }
    
    public static void swap2 (ArrayList<Integer> list, int i, int j){
        int temp2 = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp2);
    }

    // Method to assign value according to its priority
    public static int priorityRank (String priority) {
        //assign value for each priority to make it easier to compare 
            if (priority.equalsIgnoreCase("low")) 
                return 1;
            else if (priority.equalsIgnoreCase("medium"))
                return 2;
            else if (priority.equalsIgnoreCase("high"))
                return 3;
            
            return 0;
    }

    //Sorting recurring tasks
    public static void sortTaskR(ArrayList<Task> taskList, Scanner input) {
   
    taskList.clear();
    loadDBR();
    
    int choice;
    
    do {
    System.out.println("");
    System.out.println("Sort by :");
    System.out.println("1. Next occurence (ascending)");
    System.out.println("2. Next occurence (descending)");
    System.out.println("3. Due date (ascending))");
    System.out.println("4. Due date (descending)");
    System.out.print("Enter the number of your choice : ");
    while (!input.hasNextInt()) { // Handle non-integer inputs
        System.out.print("Invalid input. Please enter a number between 1 and 4. \nEnter new number : ");
        input.nextLine(); // Consume the invalid input
    }
        choice = input.nextInt();
        input.nextLine(); // Clear the buffer
    } while (choice < 1 || choice > 4);
    
    switch (choice) {
    
        case 1 : 
        int n = taskList.size();

        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < n-i-1; j++) {
                Task task1 = taskList.get(j);
                Task task2 = taskList.get(j+1);
                RecurringTask rtask1 = (RecurringTask) task1;
                RecurringTask rtask2 = (RecurringTask) task2;
                LocalDate date1 = rtask1.getNextOccurence();
                LocalDate date2 = rtask2.getNextOccurence();
         
            if (date1.isAfter(date2)) {
                swap3(taskList, j, j + 1);
                }
            }
        }
        break;

        case 2 :
        int k = taskList.size();
       
        for (int i = 0; i < k-1; i++) {
            for (int j = 0; j < k-i-1; j++) {
                Task task1 = taskList.get(j);
                Task task2 = taskList.get(j+1);
                RecurringTask rtask1 = (RecurringTask) task1;
                RecurringTask rtask2 = (RecurringTask) task2;
                LocalDate date1 = rtask1.getNextOccurence();
                LocalDate date2 = rtask2.getNextOccurence();

            if (date1.isBefore(date2)) {
                swap3(taskList, j, j + 1);
                }
            }
        }
        break;

        case 3 : //sort by priority from high to low
            int m = taskList.size();
            for (int i = 0; i < m-1; i++) {
                for (int j = 0; j < m-i-1; j++) {
                    Task task1 = taskList.get(j);
                    Task task2 = taskList.get(j+1);
                    RecurringTask rtask1 = (RecurringTask) task1;
                    RecurringTask rtask2 = (RecurringTask) task2;
                    LocalDate date1 = rtask1.getDueDate();
                    LocalDate date2 = rtask2.getDueDate();

            if (date1.isAfter(date2)) {
                    swap3(taskList, j, j + 1);
                    }
                }
        }
            
        break;

        case 4 :   //sort by priority from low to high
        int h = taskList.size();
        for (int i = 0; i < h-1; i++) {
            for (int j = 0; j < h-i-1; j++) {
                    Task task1 = taskList.get(j);
                    Task task2 = taskList.get(j+1);
                    RecurringTask rtask1 = (RecurringTask) task1;
                    RecurringTask rtask2 = (RecurringTask) task2;
                    LocalDate date1 = rtask1.getDueDate();
                    LocalDate date2 = rtask2.getDueDate();

            if (date1.isBefore(date2)) {
                    swap3(taskList, j, j + 1);
                    }
                }
            }
        break;

        default :
            System.out.println("Invalid choice. Please enter 1-4 only.");
            
        } 
    
        for (int i =0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            RecurringTask rtask = (RecurringTask) task;
            String titlesort = rtask.getTitle();
            String describesort = rtask.getDescription();
            String recursort = rtask.getRecurrenceInterval();
            LocalDate nextoccsort = rtask.getNextOccurence();
            LocalDate duedatesort = rtask.getDueDate();
            java.sql.Date sqlnextoccsort = java.sql.Date.valueOf(nextoccsort);
            java.sql.Date sqlduedatesort = java.sql.Date.valueOf(duedatesort);
            int completesort = rtask.getCompletion();
            if (i == 0)
            RecurringTask.actionInDBR(15, 0, titlesort, describesort, recursort, sqlnextoccsort, sqlduedatesort, completesort);
            else
               RecurringTask.actionInDBR(15, i, titlesort, describesort, recursort, sqlnextoccsort, sqlduedatesort, completesort);

        }
   
        System.out.println("");
        System.out.println("========Recurring Task sorted!========");
        System.out.println("");
        
        for (int i = 0; i < taskList.size(); i++) {
            Task task = taskList.get(i);
            RecurringTask rtask = (RecurringTask) task;
                System.out.print((i+1) + ". " + rtask.getTitle() + " - " + rtask.getDescription() + " - " + rtask.getRecurrenceInterval() + " - " + rtask.getNextOccurence() + " - " + rtask.getDueDate());
                if (rtask.getCompletion()==0) {
                    System.out.println(" - incomplete");
                } else {
                    System.out.println(" - completed");
                }
        }
        System.out.println("\n");
    } 

    //Method for swapping tasks
    public static void swap3 (ArrayList<Task> taskList, int i, int j){
        Task temp1 = taskList.get(i);
        taskList.set(i, taskList.get(j));
        taskList.set(j, temp1);
    }

    //Search tasks
    public static void searchTask(ArrayList<String> title, ArrayList<String> describe, ArrayList<String> date, ArrayList<String> category, ArrayList<String> priority, ArrayList<Integer> taskNumber, ArrayList<Integer> status, ArrayList<Integer> dependency, Scanner input) {
    
    CatchData(taskNumber, title, describe, date, category, priority, status);
    
    System.out.println("");  
    System.out.print("Enter your search key : ");
    String keyword = input.nextLine().toLowerCase();
   
    boolean match = false;
    boolean status_key = false;
    
    System.out.println("");
    
    for (int i = 0; i < title.size(); i++) {
        if (keyword.equalsIgnoreCase("incomplete") || keyword.equalsIgnoreCase("unfinished")) { //so that user can search using status jugak
        status_key = (status.get(i) == 0);
        }
        if (keyword.equalsIgnoreCase("complete")  || keyword.equalsIgnoreCase("done") || keyword.equalsIgnoreCase("finish")) {
        status_key = (status.get(i) == 1);
        }
        
        if (title.get(i).toLowerCase().contains(keyword) || 
            describe.get(i).toLowerCase().contains(keyword) || 
            date.get(i).toLowerCase().contains(keyword) ||
            category.get(i).toLowerCase().contains(keyword) ||
            priority.get(i).toLowerCase().contains(keyword) ||
            taskNumber.get(i).toString().toLowerCase().contains(keyword) ||
            status_key ||
            dependency.get(i).toString().toLowerCase().contains(keyword)) {
                match = true;
                System.out.print((i+1) + ". " + title.get(i) + " - " + describe.get(i) + " - " + date.get(i) + " - " + category.get(i) + " - " + priority.get(i));
                if (status.get(i)==0) {
                    System.out.println(" - incomplete");
                } else {
                    System.out.println(" - completed");
                }
        }          
        }
        if (match == false) {
            System.out.println("");
            System.out.println("Sorry, no match found.");
        }  
        System.out.println("");
    }

    //Retrieving email address from database
    public static String EmailAddressfromDB(){
        Connection conn1 = null; 
        String address = "";
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db";
            String user = "root";
            String password = "";
            conn1 = DriverManager.getConnection(url1, user, password);
            if (conn1 != null) {
                String sql = "SELECT * FROM user_email";
                var email = conn1.prepareStatement(sql);

                var getEmail = email.executeQuery();
                if (getEmail.next()){
                address = getEmail.getString(1);}
            }
            conn1.close();
        }catch(SQLException ex){
            System.out.println("An error occurred");
            ex.printStackTrace();
        }
        return address;
    }

    //Method for notifying through email if due date 24 hours away
    public static void dueNoti(ArrayList<String> title, ArrayList<String> date, String recepient) throws Exception{
        LocalDateTime currentDate = LocalDateTime.now().withSecond(0).withNano(0); // Current date, hours and minutes
         
        DateTimeFormatter format = DateTimeFormatter.ofPattern("[yyyyMMdd]" + "[yyyy-MM-dd]" + "[yyyy/MM/dd]" + "[yyyy.MM.dd]" + "[yyyy,MM,dd]");
        for (int i_date=0;i_date<date.size();i_date++){
            LocalDate duedate = LocalDate.parse(date.get(i_date), format);
            LocalDateTime due = duedate.atStartOfDay(); 
            if (due.minusDays(1).compareTo(currentDate)==0){ //Check if it is already 00:00 of a day before the due date.
                System.out.println("=== Email Notification ===");
                System.out.println("Sending reminder email for task "+ title.get(i_date) + " due in 24 hours.");
                TunaMayowithEmail.sendMailNoti(recepient, title, i_date);
                System.out.println("");
            }
        }
    }

    //Method for data analytics
    public static void dataAnalytics(int i, ArrayList<Integer> taskNumber, ArrayList<Integer> status, ArrayList<String> category){
        System.out.println("\n=== Analytics Dashboard ===");
        int taskInR = RecurringTask.getNumTaskDB();
        int totalTask = i+taskInR;
        System.out.println("> Total task: " + (totalTask-totalDEL));
        int count=0, hw=0, personal=0, work=0, other=0;
        for (int tm=0; tm<taskNumber.size();tm++){
            if (status.get(tm)==1){
                count++;
            }
        }
        int completeInR = RecurringTask.getTotalComplete();
        int totalComplete = count + completeInR;
        System.out.println("> Completed: " + totalComplete);
        System.out.println("> Pending: " + (totalTask-totalComplete-totalDEL));
        System.out.printf("> Completion Rate: %.2f" , (((double)totalComplete/(totalTask-totalDEL))*100.0));
        for (int tm=0;tm<category.size();tm++){
            if (category.get(tm).equalsIgnoreCase("homework")){
                hw++;
            } else if (category.get(tm).equalsIgnoreCase("personal")){
                personal++;
            } else if (category.get(tm).equalsIgnoreCase("work")){
                work++;
            } else {
                other++;
            }   
        }
        System.out.print("% \n> Task Categories:- Homework: "+ hw +", Personal: "+ personal +", Work: "+ work + ", Recurring Tasks: "+ taskInR + ", Others: "+ other);
    }
    
    //Method for actions inside database
    public static void actionInDB(int actforDB, int i, String title, String describe , String date, String category, String priority, int taskDEL, int taskDONE, int [] dependent, int [] taskPreceding, int i_depend, int status, String recepient){
        Connection conn1 = null; 
        try {
            String url1 = "jdbc:mysql://localhost:3306/tunamayo_db";
            String user = "root";
            String password = "";
            
            //Connection with your database
            conn1 = DriverManager.getConnection(url1, user, password);
            if (conn1 != null) {
                switch (actforDB){
                    case 1: //Inserting task into database
                        String sql1 = "insert into task(numTask, Title, Description, Due, Category, Priority) values (?,?,?,?,?,?)";
                        var myStat = conn1.prepareStatement(sql1);

                        myStat.setInt(1, i);
                        myStat.setString(2, title);
                        myStat.setString(3, describe);
                        myStat.setString(4, date);
                        myStat.setString(5, category);
                        myStat.setString(6, priority);

                        myStat.executeUpdate();
                        break;
                    case 2: //Delete task from database 
                        String sql2 = "DELETE FROM task WHERE numTask=?";
                        myStat = conn1.prepareStatement(sql2);

                        myStat.setInt(1, i);
                        
                        myStat.execute();
                        break;
                    case 3: //Update column 'Completion' when task is complete 
                        String sql3 = "UPDATE task SET Completion=1 WHERE numTask=?";
                        var update = conn1.prepareStatement(sql3);

                        update.setInt(1, (taskDONE-1));

                        update.executeUpdate();
                        break;
                    case 4: //Display all tasks 
                        System.out.println("=== View All Tasks ===");
                        String sql4 = "SELECT * FROM task ORDER BY numTask ASC";
                        var check = conn1.prepareStatement(sql4);
                        
                        var results = check.executeQuery();
                        boolean haveTask = false;
                        String statusTask = "";
                        while (results.next()){
                            haveTask = true;
                            if (results.getInt(7)==0){
                                statusTask = "Incomplete";
                            } else {
                                statusTask = "Completed";
                            }
                            
                            if (results.getString(2).equals("none")){
                                continue;
                            } else {
                                if (results.getInt(8) == -1){ //output for task with no dependency
                                    System.out.println((results.getInt(1)+1) + ". [" + statusTask +  "] " + results.getString(2) + " - Due: " + results.getDate(4));    
                                } else { //output for task with dependency
                                    System.out.println((results.getInt(1)+1) + ". [" + statusTask +  "] " + results.getString(2) + " - Due: " + results.getDate(4) + " (Depends on Task " + (results.getInt(8)+1) + ")");    
                                }
                            }
                        } 
                        if (haveTask == false){
                            System.out.println("No regular tasks to view.");
                        }
                        
                        System.out.println("");
                        break;
                    case 5: //Update column 'Dependency' 
                        String sql5 = "UPDATE task SET Dependency=? WHERE numTask=?";
                        update = conn1.prepareStatement(sql5);

                        update.setInt(1, taskPreceding[i_depend]);
                        update.setInt(2, dependent[i_depend]);

                        update.executeUpdate();
                        break;
                    
                    case 6 : //Update columns 'numTask', 'Description', 'Due', 'Category', 'Priority' and 'Completion' according to the task's title
                        String sql6 = "UPDATE task SET numTask =?, Description = ?, Due = ?, Category = ?, Priority = ?, Completion = ? WHERE Title = ?";
                        update = conn1.prepareStatement(sql6);
                
                        update.setInt(1, i);
                        update.setString(2, describe);
                        update.setString(3, date);
                        update.setString(4, category);
                        update.setString(5, priority);
                        update.setInt(6, status);
                        update.setString(7, title);

                        update.executeUpdate();
                        break;
                    case 7: //Update column emailAddress from table user_email 
                        String sql7 = "UPDATE user_email SET emailAddress=?";
                        update = conn1.prepareStatement(sql7);

                        update.setString(1, recepient);

                        update.executeUpdate();
                        break;
                    case 8: //Edit title of a task
                        String sql8 = "UPDATE task SET Title = ? WHERE numTask = ?";
                        update = conn1.prepareStatement(sql8);
                
                        update.setString(1, title);
                        update.setInt(2, i);
                        update.executeUpdate();
                        break;
                    case 9: //Edit description of a task
                        String sql9 = "UPDATE task SET Description = ? WHERE numTask = ?";
                        update = conn1.prepareStatement(sql9);
                
                        update.setString(1, describe);
                        update.setInt(2, i);
                        update.executeUpdate();
                        break;
                    case 10: //Edit due date of a task
                        String sql10 = "UPDATE task SET Due = ? WHERE numTask = ?";
                        update = conn1.prepareStatement(sql10);
                
                        update.setString(1, date);
                        update.setInt(2, i);
                        update.executeUpdate();
                        break;
                    case 11: //Update category of a task
                        String sql11 = "UPDATE task SET Category = ? WHERE numTask = ?";
                        update = conn1.prepareStatement(sql11);
                
                        update.setString(1, category);
                        update.setInt(2, i);
                        update.executeUpdate();
                        break;
                    case 12: //Update priority of a task
                        String sql12 = "UPDATE task SET Priority = ? WHERE numTask = ?";
                        update = conn1.prepareStatement(sql12);
                
                        update.setString(1, priority);
                        update.setInt(2, i);
                        update.executeUpdate();
                        break;
                    case 13: //Update total_deleted_task from table deleted_task
                        String sql13 = "UPDATE deleted_task SET total_deleted_task=?";
                        update = conn1.prepareStatement(sql13);

                        update.setInt(1, totalDEL);

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

    //Method to fetch past data/tasks from database and add to arraylist
    public static void CatchData(ArrayList<Integer> taskNumber, ArrayList<String> title, ArrayList<String> describe, ArrayList<String> date, ArrayList<String> category, ArrayList<String> priority, ArrayList<Integer> status){
       // To make sure the arraylist  is cleared to prevent duplication
        taskNumber.clear();
        title.clear();
        describe.clear();
        date.clear();
        category.clear();
        priority.clear();
        status.clear();
    
        Connection conn1 = null;
        PreparedStatement p = null;
        ResultSet rs = null; 
     
        try {
        
        // Importing and registering drivers
        String url1 = "jdbc:mysql://localhost:3306/tunamayo_db"; //insert your database name
        String user = "root";
        String password = "";

        //Connect with your database
        conn1 = DriverManager.getConnection(url1, user, password);
        
        //Command to select data
        String sql = "SELECT * from task";
        p = conn1.prepareStatement(sql);
        rs = p.executeQuery();
        
        //Fetch until there is no data
        while (rs.next()) {
            
            //Update data to arraylist
            taskNumber.add (rs.getInt("numTask")+1);
            title.add (rs.getString("Title"));
            describe.add (rs.getString("Description"));
            date.add (rs.getString("Due"));
            category.add (rs.getString("Category"));
            priority.add (rs.getString("Priority"));
            status.add (rs.getInt("Completion"));
        }
        }catch (SQLException e) {
            System.out.println(e);
     
        }
    }
    
    // Method to detect cycle of dependencies using the slow and fast pointer approach
    public static boolean hasCycle(ListNode head) {
        ListNode fast = head;
        ListNode slow = head;

        // Traverse the linked list using fast and slow pointers
        while (fast != null && fast.next != null) {
            fast = fast.next.next; // Move fast pointer two steps
            slow = slow.next; // Move slow pointer one step

            // If fast and slow pointers meet, a cycle is detected
            if (fast == slow) {
                return true;
            }
        }
        // No cycle detected
        return false;
    }
}
    