package fr.vmarchaud.mineweb.bridge.util;

import java.lang.reflect.Field;

public class VolatileField {

    private final Field field;
    private final Object instance;
    private final boolean accessible;
    private Object savedValue;

    public VolatileField(Field field, Object instance, boolean forceAccess) {
        this.field = field;
        this.instance = instance;
        this.accessible = field.isAccessible();
        if (forceAccess) {
            field.setAccessible(true);
        }
    }

    public Object getValue() {
        try {
            return field.get(instance);
        } catch (Exception e) {
            return null;
        }
    }

    public void setValue(Object value) {
        try {
            if (savedValue == null) {
                savedValue = getValue();
            }
            field.set(instance, value);
        } catch (Exception ignored) {
        }
    }

    public VolatileField toSynchronized() {
        return this;
    }

    public void revertValue() {
        try {
            if (savedValue != null) {
                field.set(instance, savedValue);
                savedValue = null;
            }
        } catch (Exception ignored) {
        }
        field.setAccessible(accessible);
    }
}
