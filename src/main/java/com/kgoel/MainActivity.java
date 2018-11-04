package com.kgoel;

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
//    private static PubNub pubNub_global;

    private static final int SERVER_PORT = 10000;
    private static final byte[] SERVER_ADD = {10,0,1,1};
    private static final int CLIENT_PORT = 9999;

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().doInBackground(serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        PubNub pubNub = pubnubInitialisation();
//        pubNub_global = pubNub;
        pubNubSubscribe(pubNub);
    }

    private static PubNub pubnubInitialisation(){
        PNConfiguration pn = new PNConfiguration();
        pn.setPublishKey(publisherKey);
        pn.setSubscribeKey(subscriberKey);
        pn.setReconnectionPolicy(PNReconnectionPolicy.LINEAR);
        PubNub pubNub = new PubNub(pn);
        return pubNub;
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
//                            System.out.println("pub timetoken: " + result.getTimetoken());
                            System.out.println("Publish success at time:" + result.getTimetoken());
//                            Log.d("kshitij","Publish success at time:" + result.getTimetoken());
                        }
                        else {
                            System.out.println("Publish fail with code:" + status.getStatusCode());
//                            Log.d("kshitij", "Publish fail with code:" + status.getStatusCode());

//                        System.out.println("pub status code: " + status.getStatusCode());
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
                System.out.println("Listener received message: "+ transmitObject.deviceType);
//                Log.d("kshitij","Listener received message: "+ transmitObject.deviceType);
                transmitObject.deviceType = String.valueOf(message.getMessage().getAsJsonObject().get("deviceType"));
                System.out.println("Listener received message: "+ transmitObject.message);
//                Log.d("kshitij","Listener received message: "+ transmitObject.message);
                transmitObject.message = String.valueOf(message.getMessage().getAsJsonObject().get("message"));
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
            PubNub pubNub = passClassAndroid.pubNub;

        }

    }

    private static class ClientTaskPubSub {

        protected Void doInBackground(PassClassAndroid passClassAndroid) {
            String msgToSend = passClassAndroid.transmitObject.message;
            PubNub pubNub = passClassAndroid.pubNub;
            System.out.println("ClientTask msg to send: " + msgToSend);
//            Log.d("kshitij","ClientTask msg to send: " + msgToSend);
            for(int i=0;i<5;i++){
                String msg="Testing "+i;
                System.out.println("Publishing: "+ msg);
//                Log.d("kshitij","Publishing: "+ msg);
                TransmitObject transmitObject = new TransmitObject();
                transmitObject.deviceType=msg;
                pubNubPublish(pubNub, transmitObject);
                System.out.println("After pubnub publish iteration: " + i);
//                Log.d("kshitij","After pubnub publish iteration: " + i);
            }
            return null;
        }
    }

    private static class ServerTask {

        void doInBackground(ServerSocket... serverSocket) {
            ServerSocket socket = serverSocket[0];
            while(true){
                try {
                Socket accept = socket.accept();
                    ObjectInputStream instream = new ObjectInputStream(accept.getInputStream());
                    String recMsg = (String) instream.readObject();
                    String[] split= recMsg.split("#");
                    if(split[0].contains("hub")){
                        System.out.println("Received message from Pubnub Server Task");
                    }
                    else if(split[0].contains("pi1")){
                        System.out.println("Received message from pi1");
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

        protected Void doInBackground(String strings, byte[] add, int clientPort) {
            String msgToSend = strings.trim();
            System.out.println("ClientTask msg to send: " + msgToSend);
//            Log.d("kshitij","ClientTask msg to send: " + msgToSend);
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
            return null;
        }
    }

    private static class PassClassAndroid {
        PubNub pubNub;
        TransmitObject transmitObject;
    }


}