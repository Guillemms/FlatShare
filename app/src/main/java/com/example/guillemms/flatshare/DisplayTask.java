package com.example.guillemms.flatshare;

public class DisplayTask {

    private String taskName;
    private String userName;
    private String weekStr;
    private boolean isWeekDisplayed;
    private String taskId;

    public DisplayTask(String taskName, String userName, String weekStr, boolean isWeekDisplayed, String taskId) {
        this.taskName = taskName;
        this.userName = userName;
        this.weekStr = weekStr;
        this.isWeekDisplayed = isWeekDisplayed;
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getUserName() {
        return userName;
    }

    public String getWeekStr() {
        return weekStr;
    }

    public boolean isWeekDisplayed() {
        return isWeekDisplayed;
    }

    public String getTaskId() {
        return taskId;
    }
}
