package dev.lightdream.redismanager.dto;

import dev.lightdream.redismanager.event.impl.ResponseEvent;
import dev.lightdream.redismanager.manager.RedisManager;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.util.List;

@NoArgsConstructor
@Getter
public class RedisResponse<T> implements ISerializable {

    private long id;
    private T response;
    private String responseClassName;

    // State
    private boolean finished = false;
    private boolean timeout = false;

    public RedisResponse(long id) {
        this.id = id;
    }

    public void markAsFinished() {
        finished = true;
    }

    public void timeout() {
        timeout = true;
    }

    @SuppressWarnings("unused")
    public boolean hasTimeout() {
        return timeout;
    }

    public void respond(T object, String responseClass) {
        this.response = object;
        this.responseClassName = responseClass;
        markAsFinished();
    }

    public void respond(ResponseEvent response) {
        if (response.getResponse().isEmpty() || response.getResponseClassName().isEmpty()) {
            respond(null, response.getResponseClassName());
            return;
        }

        if (response.getResponseClass().isAssignableFrom(List.class)) {
            //noinspection unchecked
            T object = (T) response.deserialize();
            respond(object, response.getResponseClassName());
            return;
        }

        //noinspection unchecked
        T object = (T) RedisManager.instance().gsonSettings().gson()
                .fromJson(response.getResponse(), response.getResponseClass());
        respond(object, response.getResponseClassName());
    }

    @SuppressWarnings("unused")
    public T getResponse() {
        return response;
    }

    @SuppressWarnings({"unchecked", "unused"})
    @SneakyThrows
    public Class<T> getResponseClassName() {
        if (responseClassName == null) {
            return null;
        }
        return (Class<T>) Class.forName(responseClassName);
    }

    @Override
    public String toString() {
        return serialize();
    }

}
