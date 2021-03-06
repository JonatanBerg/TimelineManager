package timelineManager.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import timelineManager.helpClasses.DateViewer;
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
	
	private boolean isTestMode = false; // Used for JUnit tests
	
	String title,desc,priority;
	LocalDate start,end;
	
	/**
	 * The constructor calls the super modelAccess which makes access to same reference to modelAcess for all controllers
	 * that use the same technique
	 * @param modelAccess The model access of the Timeline Manager
	 * @param timelineViewer The timelineViewer that shows all Timelines and tasks on the main window
	 * @param timelineTable The timelineTable which shows all timelines as a list on the main window
	 * @param dateViewer The dateviewer that shows all dates in the main window
	 */
	public AddTaskController(ModelAccess modelAccess, TimelineViewer timelineViewer,
							 DateViewer dateViewer, TableView<Timeline> timelineTable) {
		super(modelAccess, timelineViewer, timelineTable, dateViewer);
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
		
		if(!errorCheck()) { // Checks if there's any missing or invalid information in the fields.
			Task task = new Task(title, desc, start, end, getModelAccess().getSelectedTimeline());
			getModelAccess().getSelectedTimeline().addTask(task);
			
			// If check is needed for JUnit tests
			if(!isTestMode) {
				getModelAccess().database.connectToDatabase();
				int id = (int) task.getTimeline().getId();
				int tasksId = (int) task.getId();
				getModelAccess().database.addTask(tasksId, title, desc, start.toString(), end.toString(), id);
				
				// Closes the connection
				getModelAccess().database.getConnection().close();
				
				super.getTimelineViewer().update(super.getModelAccess().timelineModel);
				
				// Window closes itself after user clicks the Save button
				final Node source = (Node) e.getSource();
				final Stage stage = (Stage) source.getScene().getWindow();
				stage.close();
			}
		}
	}
	
	/**
	 * this method closes the stage.
	 */
	public void cancelTask(){
		Stage stage = (Stage) cancelButton.getScene().getWindow();
		stage.close();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		disableDates();
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
	
	// Getters
	public JFXTextField getTitleField() {
		return titleField;
	}
	
	public JFXTextArea getDescField() {
		return descriptionField;
	}
	
	public JFXDatePicker getStartDate() {
		return startDate;
	}
	
	public JFXDatePicker getEndDate() {
		return endDate;
	}
	
	
	// Private methods
    /*
          This method checks if there is any errors in the inputs, if it is the fields will be marked
          and a tooltip added to show what the user need to change
     */
	private boolean errorCheck() {
		boolean errorFound = true;
		
		if(title.trim().isEmpty()) {
			Tooltip tooltip = new Tooltip("Please insert title");
			setTooltipStyle(tooltip);
			titleField.setStyle("-fx-border-color: orangered;"+"-fx-border-width: 3;");
			titleField.setTooltip(tooltip);
		}else if ((!title.isEmpty()) && (start == null)) {
			titleField.setStyle("-fx-border-color: transparent;");
			Tooltip tooltip = new Tooltip("Select start date");
			setTooltipStyle(tooltip);
			startDate.setStyle("-fx-border-color: orangered;"+"-fx-border-width: 3;");
			startDate.setTooltip(tooltip);
		} else if ((!title.isEmpty()) && (start != null) && (end == null)) {
			titleField.setStyle("-fx-border-color: transparent;");
			startDate.setStyle("-fx-border-color: transparent;");
			Tooltip tooltip = new Tooltip("Select end date");
			setTooltipStyle(tooltip);
			endDate.setStyle("-fx-border-color: orangered;"+"-fx-border-width: 3;");
			endDate.setTooltip(tooltip);
		} else if((!title.isEmpty()) && (start != null) && (end!=null) && (end.isBefore(start))) {
			titleField.setStyle("-fx-border-color: transparent;");
			startDate.setStyle("-fx-border-color: transparent;");
			endDate.setStyle("-fx-border-color: transparent;");
			Tooltip tooltip = new Tooltip("End date cannot be before start date");
			setTooltipStyle(tooltip);
			endDate.setStyle("-fx-border-color: orangered;"+"-fx-border-width: 3;");
			endDate.setTooltip(tooltip);
			
		} else {
			errorFound = false;
		}
		
		return errorFound;
	}
	
	// Disables dates in the date pickers that are before/after the timeline start/end
	private void disableDates() {
		Callback<DatePicker, DateCell> dayCellFactory;
		
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
	/**
	 * This method set style to red (error message) of the input tooltip
	 * @param tooltip the Tooltip to be edited
	 */
	private void setTooltipStyle(Tooltip tooltip)
	{
		tooltip.setStyle("-fx-background: rgb(30,30,30);" +
				"-fx-background-color: rgba(255,0,0,0.3);" +
				"-fx-text-fill: orange;" +
				"-fx-background-radius: 6px;" +
				"-fx-background-insets: 0;" +
				"-fx-padding: 0.667em 0.75em 0.667em 0.75em;" +
				"-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.5) , 10, 0.0 , 0 , 3 );" +
				"-fx-font-size: 0.85em;");
	}
}
