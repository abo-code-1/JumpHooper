package com.duddlejump.events;

import com.badlogic.gdx.utils.Array;
import com.duddlejump.entities.Doodle;
import com.duddlejump.entities.Platform;

public final class EventBus {

    private final Array<ContactListener> listeners = new Array<>();

    public void subscribe(ContactListener listener) {
        if (listener != null && !listeners.contains(listener, true)) {
            listeners.add(listener);
        }
    }

    public void unsubscribe(ContactListener listener) {
        listeners.removeValue(listener, true);
    }

    public void publishContact(Doodle doodle, Platform platform) {
        for (int i = 0; i < listeners.size; i++) {
            listeners.get(i).onContact(doodle, platform);
        }
    }

    public int listenerCount() {
        return listeners.size;
    }

    public void clear() {
        listeners.clear();
    }
}
