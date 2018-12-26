package com.example.guillemms.flatshare;

public class Task {

    private String date;
    private String taskName;

    public Task(String date, String taskName) {
        this.date = date;
        this.taskName = taskName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
