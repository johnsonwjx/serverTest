package com.johnson.controller;


import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.johnson.utils.ConfigHandler;
import com.johnson.utils.MsgHandler;
import com.johnson.utils.MyBean;
import com.johnson.utils.V6ServiceInvoker;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * Created by johnson on 14/06/2017.
 */
public class Main {
    @FXML
    private TextField url;

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private CheckBox proxy;

    @FXML
    private TreeView<Object> serviceTree;
    @FXML
    private TabPane mainTabPane;

    @FXML
    private void initialize() {
        Properties config = ConfigHandler.INSTANCE.getConfig();
        String urlValue = config.getProperty("url");
        String usernameValue = config.getProperty("username");
        String proxyValue = config.getProperty("proxy");
        url.setText(Strings.nullToEmpty(urlValue));
        username.setText(usernameValue == null ? "admin" : usernameValue);
        proxy.setSelected("true".equals(proxyValue));
        loadTab();
    }


    private Service<TreeItem<Object>> getServicesServices;

    @FXML
    private void login() {
        if (getServicesServices != null) {
            getServicesServices.cancel();
        } else {
            getServicesServices = new Service<TreeItem<Object>>() {
                @Override
                protected Task<TreeItem<Object>> createTask() {
                    return new Task<TreeItem<Object>>() {
                        @Override
                        protected TreeItem<Object> call() throws Exception {
                            //根据 url proxy获取服务并设到系统环境变量
                            List<MyBean> services = V6ServiceInvoker.getServices(url.getText().trim(), proxy.isSelected());
                            TreeItem<Object> root = new TreeItem<>("服务");
                            services.forEach(service -> {
                                TreeItem<Object> myBeanTreeItem = new TreeItem<>(service);
                                root.getChildren().add(myBeanTreeItem);
                                //String value = serviceUrl;//用输入的地址作为服务地址
                                //因为有可能有服务器集群，所以不能用输入的地址作为服务地址
                                String value = "http://" + service.get("addr");
                                if (proxy.isSelected())//使用集群，那样所有服务都用web地址
                                    value = url.getText().trim();
                                String key = service.get("name");
                                if (!Strings.isNullOrEmpty(key)) {
                                    System.setProperty(key, value);
                                }
                            });
                            root.setExpanded(true);
                            //根据 系统环境变量的服务对应url地址，登录系统了,并保存到 config文件
                            V6ServiceInvoker.loginSystem(username.getText().trim(), password.getText().trim(), url.getText().trim(), String.valueOf(proxy.isSelected()));
                            return root;
                        }
                    };
                }
            };
            getServicesServices.setOnSucceeded(event -> {
                serviceTree.setRoot((TreeItem<Object>) event.getSource().getValue());

            });
            getServicesServices.setOnFailed(event -> {
                MsgHandler.showException("建立服务树失败", event.getSource().getException());
                serviceTree.setRoot(null);
            });
        }
        serviceTree.setRoot(new TreeItem<>("加载中...", new ProgressIndicator()));
        getServicesServices.restart();
    }

    private void loadTab() {
        Service<Map<JsonObject, Parent>> service = new Service<Map<JsonObject, Parent>>() {
            @Override
            protected Task<Map<JsonObject, Parent>> createTask() {

                return new Task<Map<JsonObject, Parent>>() {
                    @Override
                    protected Map<JsonObject, Parent> call() throws Exception {

                        InputStreamReader inputStreamReader = null;
                        Map<JsonObject, Parent> tabMap;
                        try {
                            inputStreamReader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("views/tabs/tabs.json"));
                            JsonArray tabJsonArray = new JsonParser().parse(inputStreamReader).getAsJsonArray();
                            tabMap = new TreeMap<>(Comparator.comparingInt(o -> o.get("index").getAsInt()));
                            tabJsonArray.forEach(tabJsonElement -> {
                                JsonObject tabJson = tabJsonElement.getAsJsonObject();
                                URL tabFileUrl = getClass().getClassLoader().getResource("views/tabs/" + tabJson.get("file").getAsString());
                                if (tabFileUrl != null) {
                                    MsgHandler.logger.debug("导入{}", tabFileUrl);
                                    try {
                                        Parent parent = FXMLLoader.load(tabFileUrl);
                                        tabMap.put(tabJson, parent);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        } finally {
                            if (inputStreamReader != null) {
                                inputStreamReader.close();
                            }
                        }
                        return tabMap;
                    }
                };
            }
        };
        service.setOnFailed(event -> {
            MsgHandler.showException("导入tab fxml失败", event.getSource().getException());
        });
        service.setOnSucceeded(event -> {
            Map<JsonObject, Parent> tabMap = (Map<JsonObject, Parent>) event.getSource().getValue();
            tabMap.forEach((tabObject, parent) -> {
                mainTabPane.getTabs().add(new Tab(tabObject.get("name").getAsString(), parent));
            });
        });
        service.start();
    }
}
