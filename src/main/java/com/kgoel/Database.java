package com.kgoel;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.client.*;
import com.sun.istack.internal.Nullable;
import javafx.scene.chart.PieChart;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class Database {

    private static MongoCollection<Document> collectionModel;
    private static MongoCollection<Document> collectionConfig;
    private static String model = "model";
    private static String config = "config";

    Database(){
//        MongoClient mongoClient = MongoClients.create();
//        MongoClientURI mongoClientURI = new MongoClientURI("mongodb://localhost:27017")
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase mongoDatabase = mongoClient.getDatabase("611");
        MongoCollection<Document> collecModel = mongoDatabase.getCollection("model");
        MongoCollection<Document> collecConfig = mongoDatabase.getCollection("config");

        Model modelObject = new Model("0","0","0","0","0","0","0");
        Gson gson = new Gson();
        Document doc = Document.parse(gson.toJson(modelObject));
        collecModel.insertOne(doc);

        Configuration configObject = new Configuration("0","0","0","0","0",0,0,0);
        Gson gson2 = new Gson();
        Document doc2 = Document.parse(gson2.toJson(configObject));
        collecConfig.insertOne(doc2);

        collectionModel = collecModel;
        collectionConfig = collecConfig;
    }

    public static void insert(String collection, @Nullable Model modelObject, @Nullable Configuration configObject){

        if(collection.compareTo(model)==0){
            Gson gson = new Gson();
            Document doc = Document.parse(gson.toJson(modelObject));
            collectionModel.insertOne(doc);
        }
        else if(collection.compareTo(config)==0){
            Gson gson = new Gson();
            Document doc = Document.parse(gson.toJson(configObject));
            collectionConfig.insertOne(doc);
        }
    }

    public  static void update(String collection, BasicDBObject basicDBObject, @Nullable Model modelObject, @Nullable Configuration configObject){
        if(collection.compareTo(model)==0){
            MongoCursor dbCursor = (MongoCursor) collectionModel.find(basicDBObject);
            while(dbCursor.hasNext()){
                DBObject dbObject = (DBObject) dbCursor.next();
                Gson gson = new Gson();
                Model readModel = gson.fromJson(dbObject.toString(),Model.class);
                if(readModel.getMacAddress().compareTo(modelObject.getMacAddress())==0 || readModel.getDeviceName().compareTo(modelObject.getDeviceName())==0){
//                    modelObject.setId(readModel.getId());
                    collectionModel.updateOne(eq("macAddress",readModel.getMacAddress()),modelObject);

//                    collectionModel.findOneAndUpdate()
                    break;
                }
            }
//            Document doc = collectionModel.find(eq("macAddress", modelObject.getMacAddress()));
        }
        else if(collection.compareTo(config)==0){
            MongoCursor dbCursor = (MongoCursor) collectionConfig.find(basicDBObject);
            while(dbCursor.hasNext()){
                DBObject dbObject = (DBObject) dbCursor.next();
                Gson gson = new Gson();
                Configuration readConfig = gson.fromJson(dbObject.toString(),Configuration.class);
                if(readConfig.getMacAddress().compareTo(configObject.getMacAddress())==0 || readConfig.getDeviceName().compareTo(configObject.getDeviceName())==0){
//                    configObject.setId(readConfig.getId());
                    collectionConfig.updateOne(eq("macAddress",readConfig.getMacAddress()),configObject);
                    break;
                }
            }
//            collectionConfig.find(eq("macAddress", configObject.getMacAddress()));
        }
    }

    public static Object query(String collection, BasicDBObject basicDBObject, @Nullable Model modelObject, @Nullable Configuration configObject){
        Object object = new Object();
        if(collection.compareTo(model)==0){
            MongoCursor dbCursor = (MongoCursor) collectionModel.find(basicDBObject);
            while(dbCursor.hasNext()){
                DBObject dbObject = (DBObject) dbCursor.next();
                Gson gson = new Gson();
                Model readModel = gson.fromJson(dbObject.toString(),Model.class);
                if(readModel.getMacAddress().compareTo(modelObject.getMacAddress())==0 || readModel.getDeviceName().compareTo(modelObject.getDeviceName())==0){
                    object = readModel;
                    break;
                }
            }
//            Document doc = collectionModel.find(eq("macAddress", modelObject.getMacAddress()));

        }
        else if(collection.compareTo(config)==0){
            MongoCursor dbCursor = (MongoCursor) collectionConfig.find(basicDBObject);
            while(dbCursor.hasNext()){
                DBObject dbObject = (DBObject) dbCursor.next();
                Gson gson = new Gson();
                Configuration readConfig = gson.fromJson(dbObject.toString(),Configuration.class);
                if(readConfig.getMacAddress().compareTo(configObject.getMacAddress())==0 || readConfig.getDeviceName().compareTo(configObject.getDeviceName())==0){
                    object = readConfig;
                    break;
                }
            }
//            collectionConfig.find(eq("macAddress", configObject.getMacAddress()));
        }
        return object;
    }

    public static void delete(){

    }
}
