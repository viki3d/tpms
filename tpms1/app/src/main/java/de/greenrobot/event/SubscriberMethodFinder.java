package de.greenrobot.event;

import android.util.Log;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/* JADX INFO: loaded from: classes.dex */
class SubscriberMethodFinder {
    private static final int BRIDGE = 64;
    private static final int MODIFIERS_IGNORE = 5192;
    private static final String ON_EVENT_METHOD_NAME = "onEvent";
    private static final int SYNTHETIC = 4096;
    private static final Map<Class<?>, List<SubscriberMethod>> methodCache = new HashMap();
    private final Map<Class<?>, Class<?>> skipMethodVerificationForClasses = new ConcurrentHashMap();

    SubscriberMethodFinder(List<Class<?>> skipMethodVerificationForClassesList) {
        if (skipMethodVerificationForClassesList != null) {
            for (Class<?> clazz : skipMethodVerificationForClassesList) {
                this.skipMethodVerificationForClasses.put(clazz, clazz);
            }
        }
    }

    List<SubscriberMethod> findSubscriberMethods(Class<?> subscriberClass) {
        List<SubscriberMethod> subscriberMethods;
        synchronized (methodCache) {
            subscriberMethods = methodCache.get(subscriberClass);
        }
        if (subscriberMethods != null) {
            return subscriberMethods;
        }
        List<SubscriberMethod> subscriberMethods2 = new ArrayList<>();
        HashMap<String, Class> eventTypesFound = new HashMap<>();
        StringBuilder methodKeyBuilder = new StringBuilder();
        for (Class<?> clazz = subscriberClass; clazz != null; clazz = clazz.getSuperclass()) {
            String name = clazz.getName();
            if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("android.")) {
                break;
            }
            try {
                Method[] methods = clazz.getDeclaredMethods();
                filterSubscriberMethods(subscriberMethods2, eventTypesFound, methodKeyBuilder, methods);
            } catch (Throwable th) {
                Method[] methods2 = subscriberClass.getMethods();
                subscriberMethods2.clear();
                eventTypesFound.clear();
                filterSubscriberMethods(subscriberMethods2, eventTypesFound, methodKeyBuilder, methods2);
            }
        }
        if (subscriberMethods2.isEmpty()) {
            throw new EventBusException("Subscriber " + subscriberClass + " has no public methods called " + ON_EVENT_METHOD_NAME);
        }
        synchronized (methodCache) {
            methodCache.put(subscriberClass, subscriberMethods2);
        }
        return subscriberMethods2;
    }

    /* JADX WARN: Code duplicated, block: B:26:0x0097  */
    private void filterSubscriberMethods(List<SubscriberMethod> subscriberMethods, HashMap<String, Class> eventTypesFound, StringBuilder methodKeyBuilder, Method[] methods) {
        ThreadMode threadMode;
        int length = methods.length;
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            Method method = methods[i2];
            String methodName = method.getName();
            if (methodName.startsWith(ON_EVENT_METHOD_NAME)) {
                int modifiers = method.getModifiers();
                Class<?> methodClass = method.getDeclaringClass();
                if ((modifiers & 1) != 0 && (modifiers & MODIFIERS_IGNORE) == 0) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 1 && (threadMode = getThreadMode(methodClass, method, methodName)) != null) {
                        Class<?> eventType = parameterTypes[i];
                        methodKeyBuilder.setLength(i);
                        methodKeyBuilder.append(methodName);
                        methodKeyBuilder.append('>');
                        methodKeyBuilder.append(eventType.getName());
                        String methodKey = methodKeyBuilder.toString();
                        Class methodClassOld = eventTypesFound.put(methodKey, methodClass);
                        if (methodClassOld == null || methodClassOld.isAssignableFrom(methodClass)) {
                            subscriberMethods.add(new SubscriberMethod(method, threadMode, eventType));
                        } else {
                            eventTypesFound.put(methodKey, methodClassOld);
                        }
                    }
                } else if (!this.skipMethodVerificationForClasses.containsKey(methodClass)) {
                    Log.d(EventBus.TAG, "Skipping method (not public, static or abstract): " + methodClass + "." + methodName);
                }
            }
            i2++;
            i = 0;
        }
    }

    private ThreadMode getThreadMode(Class<?> clazz, Method method, String methodName) {
        String modifierString = methodName.substring(ON_EVENT_METHOD_NAME.length());
        if (modifierString.length() == 0) {
            ThreadMode threadMode = ThreadMode.PostThread;
            return threadMode;
        }
        if (modifierString.equals("MainThread")) {
            ThreadMode threadMode2 = ThreadMode.MainThread;
            return threadMode2;
        }
        if (modifierString.equals("BackgroundThread")) {
            ThreadMode threadMode3 = ThreadMode.BackgroundThread;
            return threadMode3;
        }
        if (modifierString.equals("Async")) {
            ThreadMode threadMode4 = ThreadMode.Async;
            return threadMode4;
        }
        if (!this.skipMethodVerificationForClasses.containsKey(clazz)) {
            throw new EventBusException("Illegal onEvent method, check for typos: " + method);
        }
        return null;
    }

    static void clearCaches() {
        synchronized (methodCache) {
            methodCache.clear();
        }
    }
}
