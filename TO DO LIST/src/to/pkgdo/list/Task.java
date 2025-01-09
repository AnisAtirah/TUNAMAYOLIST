package to.pkgdo.list;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

// Task class
public class Task {

    private final String title;
    private final String description;
    private final String dueDate;
    private final String category;
    private final String priority;
    private final int tasknum;
    private final String catnum = "0";
    private final BooleanProperty completed = new SimpleBooleanProperty(false);

    public Task(String title, String description, String dueDate, String category, String priority, int tasknum) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.category = category;
        this.priority = priority;
        this.tasknum = tasknum;
    }

    public String getDueDate() {
      
        return dueDate;
    }

    public String getCategory() {
        return category;
    }

    public String getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDateString() {
        return dueDate;
    }
    
    public String getCatnum(){
        
        if (priority.equalsIgnoreCase("low")){
            return "0";
        }else if(priority.equalsIgnoreCase("medium")){
            return "1";
        }else{
            return "2";
        }
    } 
    
    public boolean isCompleted() {
        return completed.get();
    }

    public void setCompleted(boolean completed) {
        this.completed.set(completed);
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    public int getCompletion() {
        if (isCompleted()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return title + "\nDue: " + dueDate + "\nPriority:" + priority.toLowerCase()  ;
    }
    
    public String toTitle() {
        return title;
    }
    
    public int gettasknum() {
        return tasknum;
    }

}
