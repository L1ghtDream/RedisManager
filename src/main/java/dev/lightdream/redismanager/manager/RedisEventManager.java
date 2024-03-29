package dev.lightdream.redismanager.manager;

import dev.lightdream.logger.Logger;
import dev.lightdream.redismanager.annotation.RedisEventHandler;
import dev.lightdream.redismanager.event.RedisEvent;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedisEventManager {

    private final List<EventMethod> eventMethods = Collections.synchronizedList(new ArrayList<>());

    public RedisEventManager() {
        RedisManager.instance().reflections()
                .getMethodsAnnotatedWith(RedisEventHandler.class)
                .forEach(method -> register(method, true, null));
    }

    @SneakyThrows
    @SuppressWarnings("rawtypes")
    private void register(Method method, boolean fromConstructor, Object parentObject) {
        if (!method.isAnnotationPresent(RedisEventHandler.class)) {
            Logger.error("Method " + method.getName() + " from class " + method.getDeclaringClass() +
                    " is not annotated with RedisEventHandler");
            return;
        }

        RedisEventHandler redisEventHandler = method.getAnnotation(RedisEventHandler.class);
        if (!redisEventHandler.autoRegister() && fromConstructor) {
            return;
        }

        RedisManager.instance().debugger().registeringMethod(method.getName(), method.getDeclaringClass().getName());

        if (parentObject == null) {
            Class<?> parentClass = method.getDeclaringClass();

            for (EventMethod eventMethod : eventMethods) {
                if (eventMethod.parentObject.getClass().equals(parentClass)) {
                    parentObject = eventMethod.parentObject;
                }
            }

            if (parentObject == null) {
                parentObject = parentClass.getConstructor().newInstance();
            }
        }

        Class<?>[] params = method.getParameterTypes();

        if (params.length != 1) {
            Logger.error("Method " + method.getName() + " from class " + method.getDeclaringClass() + " has more than one parameter");
            return;
        }

        Class<?> paramClass = params[0];

        if (!RedisEvent.class.isAssignableFrom(paramClass)) {
            Logger.warn("Parameter from method " + method.getName() + " from class " + method.getDeclaringClass() + " is not an instance of RedisEvent");
            return;
        }

        //noinspection unchecked
        Class<? extends RedisEvent> redisEventClass = (Class<? extends RedisEvent>) paramClass;

        EventMethod eventMethod = new EventMethod(parentObject, redisEventClass, method);
        eventMethods.add(eventMethod);
    }

    @SuppressWarnings("unused")
    public void register(Object object) {
        for (Method declaredMethod : object.getClass().getDeclaredMethods()) {
            if (!declaredMethod.isAnnotationPresent(RedisEventHandler.class)) {
                continue;
            }

            register(declaredMethod, false, object);
        }
    }

    @SuppressWarnings("unused")
    public void unregister(Object object) {
        eventMethods.removeIf(eventObject -> eventObject.parentObject.equals(object));
    }

    @SuppressWarnings({"rawtypes", "unused"})
    public void fire(RedisEvent event) {
        eventMethods.sort((o1, o2) -> {
            RedisEventHandler annotation1 = o1.method.getAnnotation(RedisEventHandler.class);
            RedisEventHandler annotation2 = o2.method.getAnnotation(RedisEventHandler.class);
            return annotation1.order() - annotation2.order();
        });

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < eventMethods.size(); i++) {
            EventMethod eventMethod = eventMethods.get(i);

            if (!event.getClass().isAssignableFrom(eventMethod.eventClass)) {
                continue;
            }

            eventMethod.method.setAccessible(true);
            try {
                eventMethod.method.invoke(eventMethod.parentObject, eventMethod.eventClass.cast(event));
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                e.getCause().printStackTrace();
                Logger.error("Error while firing event " + event.getClass().getName());
                Logger.error("parentObject class:" + eventMethod.parentObject.getClass().getName());
                Logger.error("parentObject:" + eventMethod.parentObject);
                Logger.error("eventClass:" + eventMethod.eventClass.getName());
                Logger.error("event:" + event.serialize());
            }
        }
    }

    @SuppressWarnings("rawtypes")
    @AllArgsConstructor
    public static class EventMethod {
        public Object parentObject;
        public Class<? extends RedisEvent> eventClass;
        public Method method;
    }
}