package com.johnson.controller.tabs;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.johnson.controller.Loading;
import com.johnson.utils.CommonUtil;
import com.johnson.utils.MsgHandler;
import com.johnson.utils.V6ServiceInvoker;
import com.johnson.utils.XMLEditor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.util.Map;

/**
 * Created by johnson on 23/06/2017.
 * <p>
 * 服务测试Tab面板
 */
public class ServerTest {
    //表格数据监控List
    private final ObservableList<ParamBean> params = FXCollections.observableArrayList();

    @FXML
    private TableView<ParamBean> paramTable;
    @FXML
    private TableColumn<ParamBean, String> nameCol;
    @FXML
    private TableColumn<ParamBean, String> valueCol;

    @FXML
    private TextField paramName;
    @FXML
    private TextField paramValue;

    @FXML
    private Tab form;

    @FXML
    private Tab raw;
    @FXML
    private TextArea rawTextArea;

    @FXML
    private BorderPane mainPnl;

    private CodeArea codeArea;

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        paramTable.setItems(params);


        //创建结果显示editor

        codeArea = new XMLEditor();
        codeArea.setWrapText(true);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        mainPnl.setCenter(new VirtualizedScrollPane<>(codeArea));

    }


    /**
     * 添加参数
     */
    @FXML
    private void add() {
        if (!form.isSelected()) {
            return;
        }
        String name = paramName.getText().trim();
        if (Strings.isNullOrEmpty(name)) {
            MsgHandler.showMsg(Alert.AlertType.WARNING, "参数名不能为空");
            return;
        }
        int selectedIndex = paramTable.getSelectionModel().getSelectedIndex();
        params.add(++selectedIndex, new ParamBean(name, paramValue.getText().trim()));
        paramTable.getSelectionModel().select(selectedIndex);
        paramName.clear();
        paramValue.clear();
        paramName.requestFocus();
    }

    /**
     * 删除参数
     */
    @FXML
    private void remove() {
        if (!form.isSelected()) {
            return;
        }
        ParamBean selectedItem = paramTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        params.remove(selectedItem);
    }

    /**
     * 清空参数
     */
    @FXML
    private void clean() {
        if (raw.isSelected()) {
            codeArea.clear();
        } else {
            params.clear();
        }
    }

    private Service<String> service = new Service<String>() {
        @Override
        protected Task<String> createTask() {
            return new Task<String>() {
                @Override
                protected String call() throws Exception {
                    String paramsStr;
                    if (form.isSelected()) {
                        StringBuilder paramStringBuilder = new StringBuilder();
                        params.forEach(param -> {
                            paramStringBuilder.append(param.getName()).append(":=").append(param.getValue()).append("\n");
                        });
                        paramsStr = paramStringBuilder.toString();
                    } else {
                        paramsStr = rawTextArea.getText().replaceAll(":", ":=");
                    }
                    return V6ServiceInvoker.httpGet(paramsStr);
                }
            };
        }

    };

    {
        service.setOnSucceeded(event -> {
            Loading.close();
            codeArea.clear();
            String value = service.getValue();
            Map<String, String> stringStringMap = V6ServiceInvoker.v6String2Map(value);

            stringStringMap.forEach((s, s2) -> {
                codeArea.appendText(s);
                codeArea.appendText("->");
                if (s2.startsWith("<?")) {
                    try {
                        s2 = CommonUtil.formatXML(s2);
                    } catch (Exception e) {
                        MsgHandler.showException("格式化xml失败", e);
                    }
                }
                codeArea.appendText(s2);
                codeArea.appendText("\n");
            });
        });
        service.setOnFailed(event -> {
            Loading.close();
            MsgHandler.showException("服务报错", service.getException());
        });
    }

    /**
     * 提交参数
     */
    @FXML
    private void submit() {
        Loading.show(service);
        service.restart();
    }

    /**
     * form raw 面板切换
     * <p>
     * - 切换到form, raw string 转换为ParamBean
     * - 琴换到 raw, from ParamBean 转换为 raw string
     */
    @FXML
    private void rawTabSelect() {
        if (raw.isSelected()) {
            rawTextArea.setText(Joiner.on("\n").join(Collections2.transform(params, bean -> bean.getName() + ":" + bean.getValue())));
        } else {
            String text = rawTextArea.getText();
            params.clear();
            if (Strings.isNullOrEmpty(text)) {
                return;
            }
            Splitter.on("\n").split(text).forEach(s -> {
                String[] split = s.split(":", 2);
                params.add(new ParamBean(split[0], split[1]));
            });
        }
    }

    @FXML
    private void paramEnter(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER:
                add();
                break;
        }
    }

    @FXML
    private void shortcut(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER:
                if (event.isControlDown()) {
                    submit();
                }
                break;
        }
    }
}
