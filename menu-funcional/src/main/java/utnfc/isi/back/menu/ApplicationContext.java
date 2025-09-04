package utnfc.isi.back.menu;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    // 1. El mapa interno para almacenar valores por clave
    private final Map<String, Object> store = new ConcurrentHashMap<>();

    // 2. Constructor privado para evitar instancias externas
    private ApplicationContext() {}

    // 3. Holder estático para lazy initialization (thread-safe)
    private static class Holder {
        private static final ApplicationContext instancia = new ApplicationContext();
    }

    // 4. Método público para obtener la instancia única
    public static ApplicationContext getInstance() {
        return Holder.instancia;
    }

    // 5. Inserta o actualiza un valor (upsert)
    public void put(String key, Object value) {
        store.put(key, value);
    }

    // 6. Recupera el valor bruto (sin cast)
    public Object get(String key) {
        return store.get(key);
    }

    // 7. Recupera y castea el valor centralizadamente
    public <T> T get(String key, Class<T> type) {
        Object val = store.get(key);
        if (val == null) return null;
        if (!type.isInstance(val)) {
            throw new ClassCastException("Valor guardado con clave '" + key + "' no es de tipo " + type.getName());
        }
        return type.cast(val);
    }

    // 8. Elimina un valor
    public void remove(String key) {
        store.remove(key);
    }

    // 9. Verifica si existe una clave
    public boolean contains(String key) {
        return store.containsKey(key);
    }

    // 10. Reemplaza el valor solo si existe, si no lanza excepción
    public void set(String key, Object newValue) {
        if (!store.containsKey(key)) {
            throw new IllegalArgumentException("La clave '" + key + "' no existe.");
        }
        store.put(key, newValue);
    }
    
}
