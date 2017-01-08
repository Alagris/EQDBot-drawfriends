package alagris.gui;

import alagris.Main;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

public class Controller {

    @FXML
    private TextArea logsArea;
    private static final int MAX_LOGS_LENGTH=1024*8;
    /**When count of characters in logsArea exceeds MAX_LOGS_LENGTH, then
     * from the beginning are truncated as many characters as this varaible specifies*/
    private static final int LOGS_CLEAR_LENGTH=1024;
    
    @FXML
    private ScrollPane scrollPane;

    @FXML
    void stop(final ActionEvent event) {
    	Main.stop();
    }
    
    @FXML
    public void initialize() {
    	logsArea.textProperty().addListener(new ChangeListener<String>() {
    		/**We need to keep track of it because we fire the same event from this listener*/
    		boolean loopCall=false;
			@Override
			public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
				if(loopCall){
					loopCall=false;
					return;
				}
				loopCall=true;
				if(newValue.length()>MAX_LOGS_LENGTH){
					final int charactersToDelete=Math.max(LOGS_CLEAR_LENGTH,newValue.length()-MAX_LOGS_LENGTH);
					Platform.runLater(()->{
					logsArea.setText(newValue.substring(charactersToDelete));
					scrollPane.setVvalue( 1.0d);
					});
				}
			}
		});
    }
    
    public void logln(final String text){
    	Platform.runLater(()->logsArea.appendText(text+'\n'));
    }


}
