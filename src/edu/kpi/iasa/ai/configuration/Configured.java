package edu.kpi.iasa.ai.configuration;

public interface Configured<T extends Object> {

    void setValue(T value);

    T getValue();

    void setModifiable(boolean state);

    boolean isModifiable();
}
