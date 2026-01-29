package com.tpg.Model;

public class CustomCase {
    private String id;
    private String condition;
    private String message;

    public CustomCase(String id, String condition, String message) {
        this.id = id;
        this.condition = condition;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public String getCondition() {
        return condition;
    }

    public String getMessage() {
        return message;
    }

}
