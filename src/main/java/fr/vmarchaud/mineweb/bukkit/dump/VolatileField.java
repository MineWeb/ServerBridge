/*
 *  ProtocolLib - Bukkit server library that allows access to the Minecraft protocol.
 *  Copyright (C) 2012 Kristian S. Stangeland
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */
package fr.vmarchaud.mineweb.bukkit.dump;

import java.lang.reflect.Field;

import com.comphenix.protocol.ProtocolLogger;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.FieldAccessor;

/**
 * Represents a field that will revert to its original state when this class is garbaged collected.
 *
 * @author Kristian
 */
public class VolatileField {
    private final FieldAccessor accessor;
    private final Object container;
    private Object previous;
    private Object current;
    private boolean previousLoaded;
    private boolean currentSet;
    private boolean forceAccess;

    /**
     * Initializes a volatile field with an associated object.
     * @param field - the field.
     * @param container - the object this field belongs to.
     * @param forceAccess - whether or not to override any scope restrictions.
     */
    public VolatileField(Field field, Object container, boolean forceAccess) {
        this.accessor = Accessors.getFieldAccessor(field);
        this.container = container;
        this.forceAccess = forceAccess;
    }

    public VolatileField(FieldAccessor accessor, Object container) {
        this.accessor = accessor;
        this.container = container;
    }

    /**
     * Retrieves the current field.
     * @return The stored field.
     */
    public Field getField() {
        return accessor.getField();
    }

    /**
     * Retrieves the current field value.
     * @return The current field value.
     */
    public Object getValue() {
        // Retrieve the correct value
        if (!currentSet) {
            ensureLoaded();
            return previous;
        } else {
            return current;
        }
    }

    /**
     * Sets the current value. This will be reverted unless saveValue() is called.
     * @param newValue - new field value.
     */
    public void setValue(Object newValue) {
        // Remember to safe the previous value
        ensureLoaded();

        writeFieldValue(newValue);
        current = newValue;
        currentSet = true;
    }

    /**
     * Revert to the previously set value.
     */
    public void revertValue() {
        // Reset value if it hasn't been changed by anyone else
        if (currentSet) {
            if (getValue() == current) {
                setValue(previous);
                currentSet = false;
            } else {
                // This can be a bad sign
                ProtocolLogger.log("Unable to switch {0} to {1}. Expected {2}, but got {3}.", getField().toGenericString(), previous, current, getValue());
            }
        }
    }

    /**
     * Retrieve a synchronized version of the current field.
     * @return A synchronized volatile field.
     */
    public VolatileField toSynchronized() {
        return new VolatileField(new SynchronizedFieldAccessor(accessor), container);
    }

    private void ensureLoaded() {
        // Load the value if we haven't already
        if (!previousLoaded) {
            previous = readFieldValue();
            previousLoaded = true;
        }
    }

    /**
     * Read the content of the underlying field.
     * @return The field value.
     */
    private Object readFieldValue() {
        return accessor.get(container);
    }

    /**
     * Write the given value to the underlying field.
     * @param newValue - the new value.
     */
    private void writeFieldValue(Object newValue) {
        accessor.set(container, newValue);
    }

    @Override
    protected void finalize() throws Throwable {
        revertValue();
    }

    @Override
    public String toString() {
        return "VolatileField [accessor=" + accessor + ", container=" + container + ", previous="
                + previous + ", current=" + current + ", previousLoaded=" + previousLoaded
                + ", currentSet=" + currentSet + ", forceAccess=" + forceAccess + "]";
    }
}

class SynchronizedFieldAccessor implements FieldAccessor {
    private final FieldAccessor accessor;
    SynchronizedFieldAccessor(FieldAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public void set(Object instance, Object value) {
        Object lock = accessor.get(instance);

        if (lock != null) {
            synchronized (lock) {
                accessor.set(instance, value);
            }
        } else {
            accessor.set(instance, value);
        }
    }

    @Override
    public Object get(Object instance) {
        return accessor.get(instance);
    }

    @Override
    public Field getField() {
        return accessor.getField();
    }
}
