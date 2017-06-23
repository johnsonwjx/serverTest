package com.johnson.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Created by johnson on 19/06/2017.
 */
public class MsgHandler {
    public static final Logger logger = LoggerFactory.getLogger("msgLog");

    public static void showMsg(Alert.AlertType alertType, String... params) {
        Alert alert = new Alert(alertType);
        switch (params.length) {
            case 1:
                alert.setHeaderText(params[0]);
                break;
            case 2:
                alert.setTitle(params[0]);
                alert.setHeaderText(params[1]);
                break;
            case 3:
                alert.setTitle(params[0]);
                alert.setHeaderText(params[1]);
                alert.setContentText(params[2]);
                break;
        }
        alert.showAndWait();
    }

    public static void showException(String title, Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText(title);
        alert.setContentText(e.getMessage());
        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("报错调用栈信息");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

// Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
        logger.error(title, e);
    }

    public static Optional<ButtonType> confirmAlert(Stage stage, String title, String text) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        if (title != null) {
            alert.setTitle(title);
        }
        alert.setHeaderText(text);
        if (stage.isShowing()) {
            alert.initOwner(stage);
        }
        return alert.showAndWait();
    }

    public static Optional<ButtonType> confirmAlert(Stage stage, String text) {
        return confirmAlert(stage, null, text);
    }

}
