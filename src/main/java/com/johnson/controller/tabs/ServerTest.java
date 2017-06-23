package com.johnson.controller.tabs;

import com.google.common.base.Strings;
import com.johnson.utils.MsgHandler;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Created by johnson on 23/06/2017.
 */
public class ServerTest {
    private final ObservableList<ParamBean> params = FXCollections.observableArrayList(
            new ParamBean("service", "")
    );
    @FXML
    private TextArea resultTextArea;
    @FXML
    private TableView<ParamBean> paramTable;
    @FXML
    private TableColumn<ParamBean, String> name;
    @FXML
    private TableColumn<ParamBean, String> value;

    @FXML
    private TextField paramName;
    @FXML
    private TextField paramValue;

    @FXML
    public void initialize() {
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        value.setCellValueFactory(new PropertyValueFactory<>("value"));
        paramTable.setItems(params);
    }

    @FXML
    private void add() {
        String name = paramName.getText().trim();
        if (Strings.isNullOrEmpty(name)) {
            MsgHandler.showMsg(Alert.AlertType.WARNING, "参数名不能为空");
            return;
        }
        int selectedIndex = paramTable.getSelectionModel().getSelectedIndex();
        params.add(++selectedIndex, new ParamBean(name, paramValue.getText().trim()));
    }

    @FXML
    private void submit() {

    }
}
