package com.example.comgroupcapmoney.utils;

public class SingleEvent<T> {
    private final T content;
    private boolean hasBeenHandled = false;

    public SingleEvent(T content) {
        this.content = content;
    }

    public T getContentIfNotHandled() {
        if (hasBeenHandled) {
            return null;
        } else {
            hasBeenHandled = true;
            return content;
        }
    }

    public T peekContent() {
        return content;
    }

    public boolean hasBeenHandled() {
        return hasBeenHandled;
    }
}
