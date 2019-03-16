package edu.kpi.iasa.ai.gui;

import edu.kpi.iasa.ai.configuration.Configured;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.function.Consumer;

public interface ConfiguredSlider<T extends Object> extends Configured<T> {

    ConfiguredSlider<T>  init(final String labelName, final T minimum, final T maximum, final T value);

    ConfiguredSlider<T>  init(final String labelName, final T minimum, final T maximum, final T value,
                              final int majorTicksSpacing, final int minorTicksSpacing,
                              final boolean snapToTicks, final Hashtable<T, JLabel> ticks);

    ConfiguredSlider<T>  init(final String labelName, final T minimum, final T maximum, final T value,
                              final ChangeListener sliderChangeListener,
                              final PropertyChangeListener textValueChangeListener);

    ConfiguredSlider<T>  init(final String labelName, final T minimum, final T maximum, final T value,
                              final int majorTicksSpacing, final int minorTicksSpacing,
                              final boolean snapToTicks, final Hashtable<T, JLabel> ticks,
                              final ChangeListener sliderChangeListener,
                              final PropertyChangeListener textValueChangeListener);

    ConfiguredSlider<T> finish();

    void setChangeValueListener(Consumer<T> changeValueListener);

    void setTicksLabels(Hashtable<T, JLabel> positions);

    Consumer<T> getChangeValueListener();

    JSlider getSlider();

    JTextField getTextField();

    JComponent toJComponent();
}
