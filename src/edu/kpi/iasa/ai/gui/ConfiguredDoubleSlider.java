package edu.kpi.iasa.ai.gui;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.function.Consumer;

public class ConfiguredDoubleSlider extends AbstractConfiguredSlider<Double> {

    private static final int SLIDER_MIN = 0;
    private static final int SLIDER_MAX = 100000000; //just enough for accuracy e^-4 on double range of 10^5

    public ConfiguredDoubleSlider(Consumer<Double> changeValueListener) {
        this.changeValueListener = changeValueListener;
        this.textFieldColumnCount = 0;
        this.textValue = new JFormattedTextField(NumberFormat.getNumberInstance());
    }

    public ConfiguredDoubleSlider(Consumer<Double> changeValueListener, int textFieldColumnCount) {
        this.changeValueListener = changeValueListener;
        this.textFieldColumnCount = textFieldColumnCount;
        this.textValue = new JFormattedTextField(NumberFormat.getNumberInstance());
    }

    @Override
    public ConfiguredDoubleSlider init(final String labelName, final Double minimum, final Double maximum, final Double value) {

        return init(labelName, minimum, maximum, value, 0, 0, false, null,
                null, null);
    }

    @Override
    public ConfiguredDoubleSlider  init(final String labelName, final Double minimum, final Double maximum, final Double value,
                                       final int majorTicksSpacing, final int minorTicksSpacing,
                                       final boolean snapToTicks, final Hashtable<Double, JLabel> ticks) {

        return init(labelName, minimum, maximum, value, majorTicksSpacing, minorTicksSpacing, snapToTicks, ticks,
                null, null);
    }

    @Override
    public ConfiguredDoubleSlider init(final String labelName, final Double minimum, final Double maximum, final Double value,
                                      final ChangeListener sliderChangeListener, final PropertyChangeListener textValueChangeListener) {
        return init(labelName, minimum, maximum, value, 0, 0, false, null,
                sliderChangeListener, textValueChangeListener);
    }

    @Override
    public ConfiguredDoubleSlider init(final String labelName, final Double minimum, final Double maximum, final Double value,
                                      final int majorTicksSpacing, final int minorTicksSpacing,
                                      final boolean snapToTicks, final Hashtable<Double, JLabel> ticks,
                                      final ChangeListener sliderChangeListener, final PropertyChangeListener textValueChangeListener) {

        valueMinimum = minimum;
        valueMaximum = maximum;

        label = new JLabel(labelName, JLabel.CENTER);
        if(textFieldColumnCount <= 0) {
            textValue.setColumns(Math.max(("" + maximum).length(), 5));
        }
        else {
            textValue.setColumns(textFieldColumnCount);
        }
        textValue.addPropertyChangeListener(textValueChangeListener);

        slider.setMinimum(SLIDER_MIN);
        slider.setMaximum(SLIDER_MAX);

        slider.setValue(realDoubleToSliderInt(value));
        textValue.setValue(value);

        if (majorTicksSpacing > 0) {
            slider.setMajorTickSpacing(majorTicksSpacing);
        }

        if (minorTicksSpacing > 0) {
            slider.setMinorTickSpacing(minorTicksSpacing);
        }

        slider.setPaintTicks((minorTicksSpacing > 0 || majorTicksSpacing > 0));

        slider.setSnapToTicks(snapToTicks);

        if(ticks != null) {
            setTicksLabels(ticks);
        }

        // Add change listener to the slider

        slider.addChangeListener(
                sliderChangeListener == null ? e -> {
                    double newValue = sliderIntToRealDouble(((JSlider) e.getSource()).getValue());
                    textValue.setValue(newValue);
                    changeValueListener.accept(newValue);
                }
                        : sliderChangeListener);

        textValue.addPropertyChangeListener(
                textValueChangeListener == null ? evt -> {
                    Object eventValue = ((JFormattedTextField) evt.getSource()).getValue();
                    Double newValue;
                    if(eventValue instanceof Long) {
                        newValue = ((Long) eventValue).doubleValue();
                    }
                    else if(eventValue instanceof Integer) {
                        newValue = ((Integer) eventValue).doubleValue();
                    }
                    else if(eventValue instanceof Double) {
                        newValue = (Double) eventValue;
                    }
                    else if(eventValue instanceof Float) {
                        newValue = ((Float) eventValue).doubleValue();;
                    }
                    else if(eventValue instanceof Number) {
                        newValue = ((Number) eventValue).doubleValue();
                    }
                    else {
                        newValue = minimum;
                    }
                    if(newValue < minimum) {
                        newValue = minimum;
                        textValue.setValue(minimum);
                    }
                    else if(newValue > maximum) {
                        newValue = maximum;
                        textValue.setValue(maximum);
                    }
                    slider.setValue(realDoubleToSliderInt(newValue));
                    changeValueListener.accept(newValue);
                }
                        : textValueChangeListener
        );
        return this;
    }

    private double sliderIntToRealDouble(int value) {
        return (value/(double)SLIDER_MAX)*(valueMaximum - valueMinimum) + valueMinimum;
    }

    private int realDoubleToSliderInt(double value) {
        return (int)Math.round((value - valueMinimum)/(valueMaximum - valueMinimum)*SLIDER_MAX);
    }

    @Override
    public void setValue(Double value) {
        if(value < valueMinimum) {
            value = valueMinimum;
        }
        else if(value > valueMaximum) {
            value = valueMaximum;
        }
        textValue.setValue(value);
        slider.setValue(realDoubleToSliderInt(value));
    }

    @Override
    public Double getValue() {
        return sliderIntToRealDouble(slider.getValue());
    }

}
