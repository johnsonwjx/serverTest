package com.johnson.controller.tabs;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by johnson on 23/06/2017.
 * <p>
 * 参数对象
 */
public class ParamBean {
    private final SimpleStringProperty name;
    private final SimpleStringProperty value;

    public ParamBean(String name, String value) {
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleStringProperty(value);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getValue() {
        return value.get();
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }


}
