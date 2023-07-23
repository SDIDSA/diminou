package org.luke.diminou.abs.utils;

public class NotificationAction {
    private int icon;
    private String text;
    private Runnable action;

    public NotificationAction(int icon, String text, Runnable action) {
        this.icon = icon;
        this.text = text;
        this.action = action;
    }

    public int getIcon() {
        return icon;
    }

    public String getText() {
        return text;
    }

    public Runnable getAction() {
        return action;
    }
}
