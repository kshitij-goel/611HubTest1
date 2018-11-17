package com.kgoel;

import com.sun.istack.internal.Nullable;
import org.bson.Document;

public class Configuration extends Document {
    private String deviceName;
    private String macAddress;
    private Inputs inputs;
    private Outputs outputs;

    private int ultraSensorDistance;

    public Configuration(String deviceName, String macAddress, @Nullable String redLed, @Nullable String yellowLed, @Nullable String greenLed, @Nullable float ultraSensorDistance, @Nullable float minValue, @Nullable float maxValue) {
        this.deviceName = deviceName;
        this.macAddress = macAddress;
        this.outputs.setRedLed(redLed);
        this.outputs.setYellowLed(yellowLed);
        this.outputs.setGreenLed(greenLed);
        this.inputs.setUltraSensorDistance(ultraSensorDistance);
        this.inputs.setMinValue(minValue);
        this.inputs.setMaxValue(maxValue);
    }

    public String getDeviceName() { return deviceName; }

    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getMacAddress() { return macAddress; }

    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public Inputs getInputs() { return inputs; }

    public void setInputs(Inputs inputs) { this.inputs = inputs; }

    public Outputs getOutputs() { return outputs; }

    public void setOutputs(Outputs outputs) { this.outputs = outputs; }

    public int getUltraSensorDistance() { return ultraSensorDistance; }

    public void setUltraSensorDistance(int ultraSensorDistance) { this.ultraSensorDistance = ultraSensorDistance; }
}
