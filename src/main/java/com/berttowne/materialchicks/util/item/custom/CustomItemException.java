package com.berttowne.materialchicks.util.item.custom;

public class CustomItemException extends Exception {

    private final String message;
    private CustomItem item;

    public CustomItemException(CustomItem item, String message) {
        this.item = item;
        this.message = message;
    }

    public CustomItemException(String message) {
        this.message = message;
    }

    public CustomItem getItem() {
        return item;
    }

    @Override
    public String getMessage() {
        return message;
    }

}