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
    private static PubNub pubNub_global;

    private static Database database_global;

    private static boolean exit = false;

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

//            new Thread(new Runnable() {
//                public void run() {
//                }
//            });
        } catch (IOException e) {
            e.printStackTrace();
        }

//        Database database = new Database();
//        database_global=database;

//        new ClientTask().doInBackground("request", PI1_ADD,CLIENT_PORT);


//        new ClientTask().doInBackground("override#red#0#yellow#0#green#0", PI1_ADD,CLIENT_PORT);


        System.out.println("after calling servertask before exit check");

        PubNub pubNub = pubnubInitialisation();
        pubNub_global = pubNub;
        pubNubSubscribe(pubNub_global);
        String send = "initial";
        new ClientTask().doInBackground(send,PI1_ADD,CLIENT_PORT);
        while(!exit){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("after calling servertask after exit check");
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
//                System.out.println(message.getMessage().getAsJsonObject().toString());
//                System.out.println(message.getMessage().getAsJsonObject().get("nameValuePairs").getAsJsonObject().get("message"));

                TransmitObject transmitObject = new TransmitObject();
//                Log.d("kshitij","Listener received message: "+ transmitObject.deviceType);
                transmitObject.message = String.valueOf(message.getMessage().getAsJsonObject().get("nameValuePairs").getAsJsonObject().get("message").toString().replace("\"", ""));
                System.out.println("Listener received message: "+ transmitObject.message);
//                Log.d("kshitij","Listener received message: "+ transmitObject.message);
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
            PubNub pubNub = passClassAndroid.pubNub;
//            PubNub pubNub = pubNub_global;
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

        protected Void doInBackground(PassClassAndroid passClassAndroid) {
            String msgToSend = passClassAndroid.transmitObject.message;
            System.out.println("ClientTask msg to send: " + msgToSend);
            PubNub pubNub = passClassAndroid.pubNub;
            TransmitObject transmitObject = new TransmitObject();
            transmitObject.deviceType=passClassAndroid.transmitObject.deviceType;
            transmitObject.message = passClassAndroid.transmitObject.message;
            pubNubPublish(pubNub, transmitObject);

            return null;
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
                    if(split[0].contains("hub")){
                        System.out.println("Received message from Pubnub Server Task");
                    }
                    else if(split[0].contains("pi1")){
                        System.out.println("Received message from pi1");
                        if(split[1].compareTo("initial")==0){

                        }
                        else if(split[1].compareTo("update")==0){
                            System.out.println(recMsg);
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

        protected Void doInBackground(String strings, byte[] add, int clientPort) {
            String msgToSend = strings.trim();
            System.out.println("ClientTask msg to send: " + msgToSend);
//            Log.d("kshitij","ClientTask msg to send: " + msgToSend);
            try {
                Socket socket = new Socket(InetAddress.getByAddress(add), clientPort);
                ObjectOutputStream outstream = new ObjectOutputStream(socket.getOutputStream());
//                ObjectInputStream instream = new ObjectInputStream(socket.getInputStream());

                outstream.writeObject(msgToSend);
//                String recMsg = (String) instream.readObject();
//                System.out.println("Received message in ClientTask: "+recMsg);
                outstream.flush();
                outstream.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
            }
            return null;
        }
    }

    private static class PassClassAndroid {
        PubNub pubNub;
        TransmitObject transmitObject;
    }


}