package com.frc.isi.museo.menu;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    // Patrón Singleton — inicialización *lazy* vía holder
    private ApplicationContext() {
    }

    private static class Holder {

        static final ApplicationContext INSTANCE = new ApplicationContext();
    }

    public static ApplicationContext getInstance() {
        return Holder.INSTANCE;
    }

    // Key–value store (thread-safe por si crecemos)
    private final Map<String, Object> store = new ConcurrentHashMap<>();

    // API mínima de uso general
    public void put(String key, Object value) {
        store.put(key, value);
    }

    public Object get(String key) {
        return store.get(key);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(store.get(key));
    }

    public Object remove(String key) {
        return store.remove(key);
    }

    public boolean contains(String key) {
        return store.containsKey(key);
    }

    public <S> void registerService(Class<S> entityClass, S service) {
        store.put(entityClass.getName(), service);
    }

    @SuppressWarnings("unchecked")
    public <S> S getService(Class<S> serviceClass) {
        return (S) store.get(serviceClass.getName());
    }

    // NUEVO: reemplazar solo si la clave ya existe
    /**
     * Reemplaza el valor asociado a la clave únicamente si la clave existe.
     *
     * @return true si se reemplazó (la clave existía), false si no se hizo
     *         nada.
     */
    public boolean set(String key, Object value) {
        return store.replace(key, value) != null;
    }

    // (Opcional) variante estricta que lanza excepción si la clave no existe
    public void setOrThrow(String key, Object value) {
        if (store.containsKey(key)) {
            throw new NoSuchElementException("No existe la clave: " + key);
        }
        store.put(key, value);
    }

}
