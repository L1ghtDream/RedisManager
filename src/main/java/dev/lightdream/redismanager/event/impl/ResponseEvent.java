package dev.lightdream.redismanager.event.impl;

import dev.lightdream.redismanager.Statics;
import dev.lightdream.redismanager.event.RedisEvent;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ResponseEvent extends RedisEvent<Object> {

    public String response;
    public String responseClassName;

    public ResponseEvent(RedisEvent<?> command, Object response) {
        super(command.originator);
        this.id = command.id;
        this.response = Statics.getMain().getGson().toJson(response);
        this.responseClassName = response.getClass().getName();
    }

}
