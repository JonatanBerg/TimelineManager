package timelineManager.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import timelineManager.helpClasses.TimelineViewer;
import timelineManager.model.Task;
import timelineManager.model.Timeline;

/**
 * This is a controller class for the AddTimelineView.
 * it holds the variables and logics needed to create a new timeline from GUI
 */

public class AddTaskController extends AbstractController implements Initializable{   
    
    
    @FXML
    private JFXTextField titleField;
    
    @FXML
    private JFXDatePicker startDate;
    
    @FXML
    private JFXDatePicker endDate;
    
    @FXML
    private JFXTextArea descriptionField;
    
    @FXML
    private JFXButton saveButton;
    
    @FXML
    private JFXButton cancelButton;
    
    private Callback<DatePicker, DateCell> dayCellFactory;
    private boolean isTestMode = false; // Used for JUnit tests
    
    String title,desc,priority;
    LocalDate start,end;
    
    /**
     * The constructor calls the super modelAccess which makes access to same reference to modelAcess for all controllers
     * that use the same technique
     * @param modelAccess uses the superClass to give same reference for the model to all controllers
     */
    public AddTaskController(ModelAccess modelAccess, TimelineViewer timelineViewer) {
        super(modelAccess, timelineViewer);
    }
    
    /**
     * A method for adding a task to the model, it uses the selected timeline from the model to know
     * what timeline to assign it to.
     * @param e an ActionEvent
     */
    public void addTheTask(ActionEvent e) throws ClassNotFoundException, SQLException, Exception{
        title = titleField.getText();
        desc = descriptionField.getText();
        start = startDate.getValue();
        end = endDate.getValue();
        
        // Tries to add the Task to the selected timeline
        try {
        	errorCheck(); // Throws exception if there's any invalid or missing information
        	
        	Task task = new Task(title, desc, start, end, getModelAccess().getSelectedTimeline());
        	getModelAccess().getSelectedTimeline().addTask(task);
                
                 getModelAccess().database.connectToDatabase();
                int id=(int) getModelAccess().getSelectedTimeline().getId();
                int tasksId=(int) task.getId();
                getModelAccess().database.addTask(tasksId, title, desc, start.toString(), end.toString(), id);
                
                // Closes the connection
                getModelAccess().database.getConnection().close();
                
        	super.timelineViewer.update(super.getModelAccess().timelineModel);
                
                
                
                
        	// If check is needed for JUnit tests
            if(!isTestMode) {
                // Window closes itself after user clicks the Save button
                final Node source = (Node) e.getSource();
                final Stage stage = (Stage) source.getScene().getWindow();
                stage.close();
            } 
        }
        catch (RuntimeException exception) {
        	/*if(!isTestMode) {
        		Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning ");
                alert.setHeaderText(exception.getMessage());
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.showAndWait();
        	} else {
        	    exception.printStackTrace();
        		throw exception;
        	}
        	*/
        }
		if(!isTestMode)
        {
            if(title.isEmpty())
                titleField.setTooltip(new Tooltip("Please insert title"));
            else if(start == null)
                startDate.setTooltip(new Tooltip("Select start date"));
            else if(end == null)
                endDate.setTooltip(new Tooltip("Select end date"));
            else if(end.isBefore(start))
                endDate.setTooltip(new Tooltip("End date cannot be before start date"));
        }
        	else
			throw new RuntimeException("Something went wrong");
        	 
        	
    }
    
    public void cancelTask(){
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    // Disables the dates in the date pickers that are before or after the timeline dates.
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Timeline currentTimeline = getModelAccess().getSelectedTimeline();
    	
        dayCellFactory = new Callback<DatePicker, DateCell>() {
        	public DateCell call(final DatePicker datePicker) {
        		return new DateCell() {
        			@Override public void updateItem(LocalDate item, boolean empty) {
        				super.updateItem(item, empty);
        				
        				if(item.isBefore(currentTimeline.getStartTime()) || item.isAfter(currentTimeline.getEndTime())) {
        					setDisable(true);
        				}
        			}
        		};
        	}
        };
        	
	 	startDate.setDayCellFactory(dayCellFactory);
		endDate.setDayCellFactory(dayCellFactory);
	}
	
    // Setters
    public void setTitle(String title) {
        titleField.setText(title);
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate.setValue(startDate);
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate.setValue(endDate);
    }
    
    public void setDescription(String description) {
        descriptionField.setText(description);
    }
    
    public void setTestMode(boolean isTestMode){
    	this.isTestMode = isTestMode;
    }
	
    // Private methods
	// Checks for any invalid or missing information and throws and exception if found
  /*  private void errorCheck() {
    	boolean errorFound = true;
    	String errorMessage = "";

    	if(title.trim().isEmpty()){
    		errorMessage = "Please select a title";
    	} else if(start == null){
    		errorMessage = "Please select start date";
    	} else if(end == null) {
    		errorMessage = "Please select end date";
    	} else if(end.isBefore(start)) {
    		errorMessage = "End date cannot be before start date";
    	} else if(end.isAfter(getModelAccess().getSelectedTimeline().getEndTime())) {
    		errorMessage = "Task end date cannot be after timeline end date";
    	} else if(start.isBefore(getModelAccess().getSelectedTimeline().getStartTime())) {
    		errorMessage = "Task start date cannot be before timeline start date";
    	} else {
    		errorFound = false;
    	}

    	if(errorFound) {
    		throw new RuntimeException(errorMessage);
    	}
    }	*/
    private void errorCheck() {
    	boolean errorFound = true;
    	
    	if(title.trim().isEmpty())
    			titleField.setStyle("-fx-border-color: orangered;"+"-fx-border-width: 3;");
    		else if (start==null)
    			startDate.setStyle("-fx-border-color: orangered;"+"-fx-border-width: 3;");
    		else if (end==null || end.isBefore(start))
    			endDate.setStyle("-fx-border-color: orangered;"+"-fx-border-width: 3;");
    		else 
        		errorFound = false;

    	if(errorFound) 
    		throw new RuntimeException();
    }
}
