package alagris.gui;
	
import alagris.Main;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class MainGUI extends Application {
	public static MainGUI instance=null;
	public static Controller controller=null;
	private static boolean isReady=false;
	public MainGUI() {
		if(instance==null){
			MainGUI.instance=this;
//		}else{
			//i would like to throw here runtime exception
			//but I hope javaFX knows what it's doing
		}
	}
	
	@Override
	public void stop() throws Exception {
		Main.disableLogging();
		Main.stop();
		super.stop();
	}
	@Override
	public void start(final Stage primaryStage) {
		try {
			final FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
			final Parent root = loader.load();
			final Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			if(controller==null){
				controller=loader.getController();
//			}else{
				//i would like to throw here runtime exception
				//but I hope javaFX knows what it's doing
			}
		} catch(final Exception e) {
			e.printStackTrace();
		}
		setReady(true);
	}

	public static boolean isReady() {
		return isReady;
	}

	private static void setReady(final boolean isReady) {
		MainGUI.isReady = isReady;
	}
}
