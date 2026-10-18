package org.example.converterservice.service.util;

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
        return name.substring(name.lastIndexOf('/') + 1, name.lastIndexOf('.')).toLowerCase();
    }
}
