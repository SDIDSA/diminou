package org.luke.diminou.app.pages.settings;

public enum FourMode {
    TEAM_MODE("team_mode"), NORMAL_MODE("normal_mode"), ASK_EVERYTIME("ask_everytime");

    private final String text;

    FourMode(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static String[] names() {
        String[] res = new String[values().length];
        for(int i = 0; i < values().length; i++) {
            res[i] = values()[i].getText();
        }
        return res;
    }

    public static FourMode byText(String s) {
        for(FourMode mode : values()) {
            if(mode.getText().equalsIgnoreCase(s) || mode.name().equalsIgnoreCase(s)) return mode;
        }
        return null;
    }
}
