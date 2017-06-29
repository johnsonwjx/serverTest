package com.johnson.controller;

import com.johnson.App;
import com.johnson.utils.MsgHandler;
import javafx.concurrent.Service;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;

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
public class Loading {

    @FXML
    private void cancel() {
        service.cancel();
        dialogStage.close();
    }

    private static Stage dialogStage;
    private static Service service;

    public static void show(Service service) {
        try {
            if (dialogStage == null) {
                dialogStage = new Stage();
                dialogStage.initOwner(App.primaryStage);
                dialogStage.initStyle(StageStyle.UNDECORATED);
                dialogStage.setResizable(false);
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                final Pane parent = FXMLLoader.load(Loading.class.getClassLoader().getResource("views/loading.fxml"));
                Scene scene = new Scene(parent);
                //不能手动移动位置
                scene.setOnDragDropped(event -> event.consume());
                dialogStage.setScene(scene);
                //基于owner居中
                dialogStage.setOnShowing(event -> {
                    Window owner = dialogStage.getOwner();
                    dialogStage.setX(owner.getX() + (owner.getWidth() - parent.getPrefWidth() ) / 2d);
                    dialogStage.setY(owner.getY() + (owner.getHeight() - parent.getPrefHeight()) / 2d);
                });
            }
        } catch (IOException e) {
            MsgHandler.showException("创建load失败", e);
        }
        if (service == null) {
            throw new IllegalArgumentException("服务不能为空");
        }
        Loading.service = service;
        dialogStage.show();
    }

    public static void close() {
        dialogStage.close();
    }
}
