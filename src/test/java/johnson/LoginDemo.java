package johnson;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Created by johnson on 22/06/2017.
 */
public class LoginDemo  extends Application{
    @Override
    public void start(Stage primaryStage) throws Exception {
         Dialog<Boolean> loginDialog=new Dialog<>();
        DialogPane dialogPane = loginDialog.getDialogPane();
        Field[] declaredFields = ButtonType.class.getDeclaredFields();
        Arrays.stream(declaredFields).forEach(field -> {
            if(field.getType().equals(ButtonType.class)){
                try {
                    dialogPane.getButtonTypes().add((ButtonType) field.get(ButtonType.class));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
        loginDialog.setOnCloseRequest(event -> {
            event.consume();
        });
        loginDialog.showAndWait();
        Parent root=new StackPane(new Label("Hello"));
        Scene scene =new Scene(root,400,400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
