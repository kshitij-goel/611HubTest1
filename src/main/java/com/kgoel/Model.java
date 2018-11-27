package com.kgoel;

public class Model {
    private String deviceName;
    private String macAddress;
    private String ipAddress;
    private String hardwareID;
    private String outputs;
    private String inputs;
    private String desc;

    Model(){

    }

    public String getDeviceName() { return deviceName; }

    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getMacAddress() { return macAddress; }

    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public String getIpAddress() { return ipAddress; }

    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getHardwareID() { return hardwareID; }

    public void setHardwareID(String hardwareID) { this.hardwareID = hardwareID; }

    public String getOutputs() { return outputs; }

    public void setOutputs(String outputs) { this.outputs = outputs; }

    public String getInputs() { return inputs; }

    public void setInputs(String inputs) { this.inputs = inputs; }

    public String getDesc() { return desc; }

    public void setDesc(String desc) { this.desc = desc; }
}
