package dev.lightdream.redismanager.event.impl;

import dev.lightdream.redismanager.event.RedisEvent;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PingEvent extends RedisEvent<Object> {

    @SuppressWarnings("unused")
    public PingEvent(String target) {
        super(target);
    }
}
