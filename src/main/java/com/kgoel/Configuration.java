package com.kgoel;

public class Configuration {
    private String deviceName;
    private String macAddress;
    private Inputs inputs;
    private Outputs outputs;

    Configuration(){

    }

    public String getDeviceName() { return deviceName; }

    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getMacAddress() { return macAddress; }

    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public Inputs getInputs() { return inputs; }

    public void setInputs(Inputs inputs) { this.inputs = inputs; }

    public Outputs getOutputs() { return outputs; }

    public void setOutputs(Outputs outputs) { this.outputs = outputs; }
}
