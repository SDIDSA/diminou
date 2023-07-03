package org.luke.diminou.abs.components.controls.scratches;

public enum Orientation {
    HORIZONTAL, VERTICAL;

    public Orientation other() {
        switch (this) {
            case HORIZONTAL -> {
                return VERTICAL;
            }
            case VERTICAL -> {
                return HORIZONTAL;
            }
            default -> {
                return null;
            }
        }
    }
}