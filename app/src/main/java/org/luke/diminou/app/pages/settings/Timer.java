package org.luke.diminou.app.pages.settings;

public enum Timer {
    VERY_SHORT("play_timer_very_short", 20),
    SHORT("play_timer_short", 30),
    DEFAULT("play_timer_default", 40),
    LONG("play_timer_long", 50);

    private final String text;
    private final int duration;
    Timer(String text, int duration) {
        this.text = text;
        this.duration = duration;
    }

    public String getText() {
        return text;
    }

    public int getDuration() {
        return duration;
    }

    public static String[] names() {
        String[] res = new String[values().length];
        for(int i = 0; i < values().length; i++) {
            res[i] = values()[i].getText();
        }
        return res;
    }

    public static Timer byText(String s) {
        for(Timer mode : values()) {
            if(mode.getText().equalsIgnoreCase(s) || mode.name().equalsIgnoreCase(s)) return mode;
        }
        return null;
    }
}
