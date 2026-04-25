package com.duddlejump.events;

import com.duddlejump.entities.Doodle;
import com.duddlejump.entities.Platform;

public interface ContactListener {
    void onContact(Doodle doodle, Platform platform);
}
