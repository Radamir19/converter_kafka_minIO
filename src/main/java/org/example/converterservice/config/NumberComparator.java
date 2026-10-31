package org.example.converterservice.config;

import java.util.Comparator;
public class NumberComparator implements Comparator<String> {
    @Override
    public int compare(String path1, String path2) {
        return Integer.compare(number(path1), number(path2));
    }

    private int number(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        return Integer.parseInt(name.replaceFirst(".*", ""));
    }
}
