package edu.kpi.iasa.ai.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {

    //store values in double buffer
    //top buffer (changedValues) - for "uncommited" changes
    //bottom (values) - for "rollback" purposes
    //configuredItems - storing elements for rollback

    private HashMap<String, ConfigurationItem> configuredItems = new HashMap<>();

    private HashMap<String,Object> values = new HashMap<>();
    private HashMap<String,Object> changedValues = new HashMap<>();

    public void register(String param, Configured item) {
        values.put(param, item.getValue());
        changedValues.put(param, item.getValue());
        configuredItems.put(param, new ConfigurationItem<>(param, item, item.getValue().getClass()));
    }

    public <T> void register(String param, Configured item, T value) {
        item.setValue(value);
        values.put(param, value);
        changedValues.put(param, value);
        configuredItems.put(param, new ConfigurationItem(param, item, value.getClass()));
    }

    public void unregister(String param) {
        configuredItems.remove(param);
        values.remove(param);
        changedValues.remove(param);
    }

    public void unregister(Configured value) {
        List<Object> keys = new ArrayList<>();
        for (Map.Entry entry : configuredItems.entrySet())
            if(entry.getValue().equals(value)) {
                keys.add(entry.getKey());
        }
        for(Object key : keys) {
            unregister((String) key);
        }
    }

    public void setValue(String param, Object value) {
        changedValues.put(param, value);
    }

    public <T extends Object> T getActualValue(String param) {
        return convertInstanceOfObject(values.get(param));
    }

    public <T extends Object> T getValue(String param) {
        Object value = changedValues.get(param);
        if(value == null) {
            value = values.get(param);
        }
        return convertInstanceOfObject(value);
    }

    public ConfigurationItem getConfigurationItem(String param) {
        return configuredItems.get(param);
    }

    public ConfigurationItem getConfigurationItem(Configured item) {
        for (Map.Entry entry : configuredItems.entrySet()) {
            ConfigurationItem conf = ((ConfigurationItem) (entry.getValue()));
            if (conf.getItem().equals(item)) {
                return conf;
            }
        }
        return null;
    }

    private <T extends Object> T convertInstanceOfObject(Object o, Class<T> clazz) {
        if(clazz == null) return null;
        return clazz.isInstance(o) ? clazz.cast(o) : null;
    }

    private <T extends Object> T convertInstanceOfObject(Object o) {
        try {
            return (T) o;
        } catch (ClassCastException e) {
            return null;
        }
    }

    //all top overwrites bottom
    public void save() {
        values.putAll(changedValues);
        changedValues.clear();
        refresh();
    }

    //clear top
    public void cancel() {
        changedValues.clear();
        refresh();
    }

    public void refresh() {
        refreshBuffer(values);
        refreshBuffer(changedValues);
    }

    private void refreshBuffer(HashMap<String,Object> buffer) {
        for(String key : buffer.keySet()) {
            ConfigurationItem item = configuredItems.get(key);
            if(item != null) {
                Object value = buffer.get(key);
                if(value != null) {
                    item.setValue(value);
                }
            }
        }
    }

    class ConfigurationItem<T extends Object> {
        private String param;
        private Configured<T> item;
        private Class<T> type;

        public ConfigurationItem(String param, Configured<T> item, Class<T> type) {
            this.param = param;
            this.item = item;
            this.type = type;
        }

        public void setValue(T value) {
            item.setValue(value);
        }

        public String getParam() {
            return param;
        }

        public Configured<T> getItem() {
            return item;
        }

        public Class<T> getType() {
            return type;
        }
    }
}
