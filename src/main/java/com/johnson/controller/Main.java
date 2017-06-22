package com.johnson.controller;


import com.google.common.base.Strings;
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
import youngfriend.common.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by johnson on 14/06/2017.
 */
public class Main {
    @FXML
    private TextField url;

    @FXML
    private TextField username;

    @FXML
    private CheckBox proxy;

    @FXML
    private TreeView<Object> serviceTree;
    @FXML
    private Tab serverTestTab;

    @FXML
    private void initialize() {
        Properties config = ConfigHandler.INSTANCE.getConfig();
        String urlValue = config.getProperty("url");
        String usernameValue = config.getProperty("username");
        String proxyValue = config.getProperty("proxy");
        url.setText(Strings.nullToEmpty(urlValue));
        username.setText(usernameValue == null ? "admin" : usernameValue);
        proxy.setSelected("true".equals(proxyValue));
    }

    private void loadTab() {
        File tabDir = new File(getClass().getClassLoader().getResource("views/tabs").getFile());
        File[] tabfxmls = tabDir.listFiles();
        Service<Parent[]> service = new Service<Parent[]>() {
            @Override
            protected Task<Parent[]> createTask() {
                return new Task<Parent[]>() {
                    @Override
                    protected Parent[] call() throws Exception {
                        return (Parent[]) Arrays.stream(tabfxmls).map(tabfxml -> {
                            try {
                                return (Parent) FXMLLoader.load(tabfxml.toURI().toURL());
                            } catch (IOException e) {
                                MsgHandler.showException("导入tab报错", e);
                                throw new RuntimeException(e);
                            }
                        }).toArray();
                    }
                };
            }
        };
    }


    public Main() {

    }

    private Service<TreeItem<Object>> getServicesServices;

    public void login() {
        if (getServicesServices != null) {
            getServicesServices.cancel();
        } else {
            getServicesServices = new Service<TreeItem<Object>>() {
                @Override
                protected Task<TreeItem<Object>> createTask() {
                    return new Task<TreeItem<Object>>() {
                        @Override
                        protected TreeItem<Object> call() throws Exception {
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
                                if (!StringUtils.nullOrBlank(key)) {
                                    System.setProperty(key, value);
                                }

                            });
                            root.setExpanded(true);
                            return root;
                        }
                    };
                }
            };
            getServicesServices.setOnSucceeded(event -> {
                serviceTree.setRoot((TreeItem<Object>) event.getSource().getValue());
                Properties config = ConfigHandler.INSTANCE.getConfig();
                config.setProperty("url", url.getText().trim());
                config.setProperty("username", username.getText().trim());
                config.setProperty("proxy", String.valueOf(proxy.isSelected()));
                ConfigHandler.INSTANCE.saveConfig();
            });
            getServicesServices.setOnFailed(event -> {
                MsgHandler.showException("建立服务树失败", event.getSource().getException());
            });
        }
        serviceTree.setRoot(new TreeItem<>("加载中...", new ProgressIndicator()));
        getServicesServices.restart();
    }

}
