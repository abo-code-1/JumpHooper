package com.duddlejump.managers;

public enum ScoreManager {
    INSTANCE;

    private int current = 0;
    private int high = 0;

    public void reset() {
        current = 0;
    }

    public int current() {
        return current;
    }

    public int getHigh() {
        return high;
    }

    public void add(int n) {
        if (n <= 0) {
            return;
        }
        current += n;
        if (current > high) {
            high = current;
        }
    }

    public void setCurrent(int value) {
        current = Math.max(0, value);
        if (current > high) {
            high = current;
        }
    }
}
