package org.example.converterservice.convert;

import org.springframework.stereotype.Component;


@Component
public class FileNames {
    public static String extensionOf(String name) {
        if(name == null) {
            return "";
        }
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }

    public static String withoutExtension(String name) {
        if(name == null) {
            return "";
        }
        return name.substring(0, name.lastIndexOf('.')).toLowerCase();
    }
}
