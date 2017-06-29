package com.johnson;


import com.johnson.utils.MsgHandler;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.util.Duration;
import sun.plugin.util.NotifierObject;

import javax.management.Notification;
import java.io.IOException;
import java.util.Optional;

import static javafx.scene.control.ButtonType.OK;

/**
 * MIT License
 * <p>
 * Copyright (c) 2017 johnson
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class App extends Application {
    public static Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        App.primaryStage = primaryStage;
        primaryStage.setTitle("v6 test");
        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            closeApp();
        });
        show("views/main.fxml");
        primaryStage.getScene().getStylesheets().add(App.class.getClassLoader().getResource("css/xml-highlighting.css").toExternalForm());
    }

    /**
     * primaryStage 显示 Scene
     *
     * @param view fxml文件的classpath相对地址
     */
    public void show(String view) {
        try {
            Parent parent = FXMLLoader.load(getClass().getClassLoader().getResource(view));
            Scene scene = new Scene(parent);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            MsgHandler.showException("界面描述文件导入失败", e);
        }
    }

    /**
     * 关闭app
     */
    public void closeApp() {
        Optional<ButtonType> result = MsgHandler.confirmAlert(primaryStage, "确定退出吗?");
        result.ifPresent(buttonType -> {
            if (buttonType == OK) {
                Platform.exit();
            }
        });
    }
}
