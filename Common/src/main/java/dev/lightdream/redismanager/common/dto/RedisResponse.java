package dev.lightdream.redismanager.common.dto;

import com.google.gson.annotations.Expose;
import dev.lightdream.redismanager.common.manager.CommonRedisManager;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
public class RedisResponse<T> {

    public int id;
    @Expose
    private T response;
    @Expose
    private String responseClassName;
    private boolean finished = false;
    private boolean timeout = false;

    public RedisResponse(int id) {
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

    public void respondUnsafe(String objectJson, String responseClass) {
        this.responseClassName = responseClass;
        T object = CommonRedisManager.fromJson(objectJson, getResponseClassName());
        respond(object, responseClass);
    }

    @SuppressWarnings("unused")
    public T getResponse() {
        return response;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public Class<T> getResponseClassName() {
        if (responseClassName == null) {
            return null;
        }
        return (Class<T>) Class.forName(responseClassName);
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public String toString() {
        return CommonRedisManager.toJson(this);
    }
}