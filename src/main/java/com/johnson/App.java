package com.johnson;/**
 * Created by johnson on 14/06/2017.
 */

import com.johnson.utils.MsgHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

import static javafx.scene.control.ButtonType.OK;


public class App extends Application {
    public static final Logger logger = LoggerFactory.getLogger(App.class);
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("v6 test");
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            closeApp();
        });
        show("views/main.fxml");
    }

    public void show(String view) {
        try {
            Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource(view));
            Scene scene = new Scene(parent);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            MsgHandler.showException("界面描述文件导入失败", e);
            logger.error(e.getMessage());
        }
    }

    public void closeApp() {
        Optional<ButtonType> result = MsgHandler.confirmAlert(primaryStage, "确定退出吗?");
        result.ifPresent(buttonType -> {
            if (buttonType == OK) {
                Platform.exit();
            }
        });
    }
}
