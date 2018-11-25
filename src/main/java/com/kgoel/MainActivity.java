package com.kgoel;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNReconnectionPolicy;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class MainActivity {

    private static final String subscriberKey = "sub-c-b3f1894c-b61b-11e8-9c8c-5aa277adf39c";
    private static final String publisherKey = "pub-c-a48eea9b-bec6-437f-a198-c629b1d05c4c";
    private static final String subscribeChannel = "Mobile Channel";
    private static final String publishChannel = "Hub Channel";
    private static PubNub pubNub_global;

    private static Database database_global;
    private static Model modelObject = new Model();
    private static Configuration config = new Configuration();

    private static final int SERVER_PORT = 50000;
    private static final byte[] SERVER_ADD = {(byte)192,(byte)168,(byte)1,(byte)187};
    private static final byte[] PI1_ADD = {(byte)192,(byte)168,(byte)1,(byte)202};
    private static final int CLIENT_PORT = 9999;

    public static void main(String[] args) {
        System.out.println("started");
        try {
            final ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            Runnable runnable = new Runnable() {
                public void run() {
                    new ServerTask().doInBackground(serverSocket);

                }
            };
            new Thread(runnable).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        database_global= new Database();

//        new ClientTask().doInBackground("request", PI1_ADD,CLIENT_PORT);


//        new ClientTask().doInBackground("override#red#0#yellow#0#green#0", PI1_ADD,CLIENT_PORT);


        System.out.println("after calling servertask before exit check");

//        pubNub_global = pubnubInitialisation();
//        pubNubSubscribe(pubNub_global);
        String send = "initial";
        new ClientTask().doInBackground(send,PI1_ADD,CLIENT_PORT);
        System.out.println("after calling servertask after exit check");
    }

    private static PubNub pubnubInitialisation(){
        PNConfiguration pn = new PNConfiguration();
        pn.setPublishKey(publisherKey);
        pn.setSubscribeKey(subscriberKey);
        pn.setReconnectionPolicy(PNReconnectionPolicy.LINEAR);
        return new PubNub(pn);
    }

    private static void pubNubPublish(PubNub pubNub, TransmitObject obj){
        JSONObject jsonObject = obj.toJSON();
        pubNub.publish().message(jsonObject).channel(publishChannel)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError() to see if error happened
                        if(!status.isError()) {
                            System.out.println("Publish success at time:" + result.getTimetoken());
                        }
                        else {
                            System.out.println("Publish fail with code:" + status.getStatusCode());
                        }
                    }
                });
    }

    private static void pubNubSubscribe(PubNub pubNub){

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {

            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                TransmitObject transmitObject = new TransmitObject();
                transmitObject.message = String.valueOf(message.getMessage().getAsJsonObject().get("nameValuePairs").getAsJsonObject().get("message").toString().replace("\"", ""));
                System.out.println("Listener received message: "+ transmitObject.message);
                transmitObject.deviceType = String.valueOf(message.getMessage().getAsJsonObject().get("nameValuePairs").getAsJsonObject().get("deviceType").toString().replace("\"", ""));
                System.out.println("Listener received message: "+ transmitObject.deviceType);
                PassClassAndroid passClassAndroid = new PassClassAndroid();
                passClassAndroid.transmitObject = transmitObject;
                passClassAndroid.pubNub = pubnub;
                new ServerTaskPubSub().doInBackground(passClassAndroid);
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {

            }
        });
        pubNub.subscribe().channels(Arrays.asList(subscribeChannel)).execute();
    }

    private static class ServerTaskPubSub {

        void doInBackground(PassClassAndroid passClassAndroid) {
            TransmitObject transmitObject = passClassAndroid.transmitObject;
            System.out.println("Inside ServerTaskPubSub-------");
            String msg = transmitObject.message;
            String deviceType = transmitObject.deviceType;
            System.out.println(deviceType);
            if(deviceType.compareTo("android")==0){
                new ClientTaskPubSub().doInBackground(passClassAndroid);
                String[] pre = msg.split("#");
                String send = "request#";
                for (int i = 2; i < pre.length; i++) {
                    if(i==pre.length-1)
                        send+=pre[i];
                    else
                        send+=pre[i]+"#";
                }
                System.out.println("//////////////////////////////////////////////ServerTaskPubSub message to pi1: "+send);
                new ClientTask().doInBackground(send,PI1_ADD,CLIENT_PORT);
            }
        }

    }

    private static class ClientTaskPubSub {

        private void doInBackground(PassClassAndroid passClassAndroid) {
            String msgToSend = passClassAndroid.transmitObject.message;
            System.out.println("ClientTaskPubSub msg to send: " + msgToSend);
            PubNub pubNub = passClassAndroid.pubNub;
            pubNubPublish(pubNub, passClassAndroid.transmitObject);
        }
    }


    private static class ServerTask {

        void doInBackground(ServerSocket... serverSocket) {
            ServerSocket socket = serverSocket[0];
            while(true){
                try {
                    Socket accept = socket.accept();
                    System.out.println("After Accept in ServerTask: ");
                    ObjectInputStream instream = new ObjectInputStream(accept.getInputStream());
                    System.out.println("After ObjectInputStream in ServerTask: ");
                    String recMsg = (String) instream.readObject();
                    System.out.println("Received message in ServerTask: "+recMsg);
                    String[] split= recMsg.split("#");
                    if(split[0].contains("pi1")){
                        System.out.println("Received message from pi1");
                        if(split[1].compareTo("initial")==0){
                            System.out.println(split[1]);
                            modelObject.setDeviceName("Raspberry Pi 1");
                            modelObject.setMacAddress(split[2]);

//                            modelObject.setIpAddress(ipStr(PI1_ADD));
                            modelObject.setIpAddress("192.168.1.202");
//                            modelObject.setIpAddress(String.valueOf(PI1_ADD[0])+String.valueOf(PI1_ADD[1])+String.valueOf(PI1_ADD[2])+String.valueOf(PI1_ADD[3]));
                            modelObject.setHardwareID(split[3]);
//                            String ip = String.valueOf(PI1_ADD[0])+String.valueOf(PI1_ADD[1])+String.valueOf(PI1_ADD[2])+String.valueOf(PI1_ADD[3]);
                            StringBuilder str = new StringBuilder();
                            StringBuilder str2 = new StringBuilder();
                            int flag = 0;
                            for (int i = 5; i < split.length; i++) {
                                if(flag==0) {
                                    if(split[i].compareTo("in")==0){
                                        flag=1;
                                        continue;
                                    }
                                    str.append(split[i]).append(" ");
                                }
                                else if(flag==1){
                                    str2.append(split[i]).append(" ");
                                }
                            }
                            modelObject.setOutputs(str.toString());
                            modelObject.setInputs(str2.toString());
                            modelObject.setDesc("Description of Pi 1");
//                            Model modelObject = new Model("Raspberry Pi 1",split[2],ip,split[3],str.toString(),str2.toString(),"Description of Pi 1");
                            System.out.println(modelObject);
                            BasicDBObject basicDBObject = new BasicDBObject();
                            basicDBObject.put("deviceName","Raspberry Pi 1");
                            Model model_test = (Model) database_global.query("model", basicDBObject, modelObject,null);
                            System.out.println("Model - test: " + model_test);
                            System.out.println("Model - test: " + new Gson().toJson(model_test));
                            if(model_test.getDeviceName() == null){
                                System.out.println("empty");
                                database_global.insert("model", modelObject,null);
                            }
                            else{
                                System.out.println("something");
                                database_global.update("model", basicDBObject, modelObject,null);
                            }
                        }
                        else if(split[1].compareTo("update")==0){
                            System.out.println(recMsg);
                            config.setDeviceName("Raspberry Pi 1");
                            config.setMacAddress(split[2]);
                            Inputs inp = new Inputs();
                            String[] inps= modelObject.getInputs().split(" ");
                            inp.setName(inps[0]+" "+inps[1]);
                            inp.setMinValue(Float.parseFloat(inps[2]));
                            inp.setMaxValue(Float.parseFloat(inps[3]));
                            inp.setUnit(inps[4]);
                            inp.setUltraSensorDistance(Float.parseFloat(split[12]));
                            config.setInputs(inp);
                            Outputs out = new Outputs();
                            out.setRedLed(split[6]);
                            out.setYellowLed(split[8]);
                            out.setGreenLed(split[10]);
                            config.setOutputs(out);

                            BasicDBObject basicDBObject = new BasicDBObject();
                            basicDBObject.put("macAddress",modelObject.getMacAddress());
                            Configuration config_test = (Configuration) database_global.query("collection", basicDBObject, null, config);
                            System.out.println("Configuration - test: " + config_test);
                            System.out.println("Configuration - test: " + new Gson().toJson(config_test));
                            if(config_test.getDeviceName() == null){
                                System.out.println("empty");
                                database_global.insert("collection", null,config);
                            }
                            else{
                                System.out.println("something");
                                database_global.update("model", basicDBObject, null,config);
                            }

                            String sendMobile = "";
                            StringBuilder str = new StringBuilder();
                            for (int i = 3; i < split.length; i++) {
                                if(i==split.length-1)
                                    str.append(split[i]);
                                else
                                    str.append(split[i]).append("#");
                            }
                            sendMobile = str.toString();
                            PassClassAndroid passClassAndroid = new PassClassAndroid();
                            passClassAndroid.transmitObject = new TransmitObject();
                            passClassAndroid.transmitObject.message = sendMobile;
                            passClassAndroid.transmitObject.deviceType = "hub";
                            passClassAndroid.pubNub = pubNub_global;
                            System.out.println("Sending to mobile: "+ sendMobile);
                            new ClientTaskPubSub().doInBackground(passClassAndroid);
                        }
                    }
                    else if(split[0].contains("pi2")){
                        System.out.println("Received message from pi2");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientTask {

        private void doInBackground(String strings, byte[] add, int clientPort) {
            String msgToSend = strings.trim();
            System.out.println("ClientTask msg to send: " + msgToSend);
            try {
                Socket socket = new Socket(InetAddress.getByAddress(add), clientPort);
                ObjectOutputStream outstream = new ObjectOutputStream(socket.getOutputStream());
                outstream.writeObject(msgToSend);
                outstream.flush();
                outstream.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    private static String ipStr(byte[] addr){
//        if (addr.length == 4) {
//            int address  = addr[3] & 0xFF;
//            address |= ((addr[2] << 8) & 0xFF00);
//            address |= ((addr[1] << 16) & 0xFF0000);
//            address |= ((addr[0] << 24) & 0xFF000000);
//            return Integer.toString(address);
//        }
//        return null;
//    }

    private static class PassClassAndroid {
        PubNub pubNub;
        TransmitObject transmitObject;
    }
}