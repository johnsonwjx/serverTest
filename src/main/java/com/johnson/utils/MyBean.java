package com.johnson.utils;

import com.google.common.base.Joiner;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by johnson on 15/06/2017.
 */
public class MyBean extends HashMap<String, String> {
    private final String[] toStringKey;
    private String toString;

    public MyBean(int initCapacity, String... toStringKey) {
        super(initCapacity);
        this.toStringKey = toStringKey;
    }

    @Override
    public String toString() {
        return toString;
    }

    public void updateToString() {
        toString = Joiner.on("-").join(Arrays.stream(toStringKey).map(key -> get(key)).toArray());
    }
}
