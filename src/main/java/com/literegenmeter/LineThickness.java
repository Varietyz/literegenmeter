package com.literegenmeter;

public enum LineThickness {
    SMALL(1, "Small"),
    MEDIUM(2, "Medium"),
    LARGE(3, "Large"),
    EXTRA_LARGE(4, "Extra Large");

    private final int value;
    private final String name;

    LineThickness(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
