package com.kgoel;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.sun.istack.internal.Nullable;
import org.bson.Document;

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
        collectionModel = collecModel;
        collectionConfig = collecConfig;
    }

    void insert(String collection, @Nullable Model modelObject, @Nullable Configuration configObject){
//        System.out.println("Database: entering insert");
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

    void update(String collection, BasicDBObject basicDBObject, @Nullable Model modelObject, @Nullable Configuration configObject){
        if(collection.compareTo(model)==0){
            FindIterable<Document> dbCursor = collectionModel.find(basicDBObject);
            for(Document doc:dbCursor){
                Gson gson = new Gson();
//                System.out.println("Updating doc:----" + doc.toJson());
                Model readModel = gson.fromJson(doc.toJson(), Model.class);
                if(readModel.getMacAddress().compareTo(modelObject.getMacAddress())==0 || readModel.getDeviceName().compareTo(modelObject.getDeviceName())==0){
//                    collectionModel.updateOne(eq("macAddress",readModel.getMacAddress()), (Bson) modelObject, new UpdateOptions().upsert(true));
                    collectionModel.replaceOne(new Document().append("macAddress",readModel.getMacAddress()), Document.parse(gson.toJson(modelObject)));
                    break;
                }
            }
        }
        else if(collection.compareTo(config)==0){
            FindIterable<Document> dbCursor = collectionConfig.find(basicDBObject);
            for(Document doc:dbCursor){
                Gson gson = new Gson();
                Configuration readConfig = gson.fromJson(doc.toJson(), Configuration.class);
                if(readConfig.getMacAddress().compareTo(configObject.getMacAddress())==0 || readConfig.getDeviceName().compareTo(configObject.getDeviceName())==0){
//                    configObject.setId(readConfig.getId());
                    collectionConfig.replaceOne(new Document().append("macAddress",readConfig.getMacAddress()), Document.parse(gson.toJson(configObject)));
                    break;
                }
            }
        }
    }

    Object query(String collection, BasicDBObject basicDBObject, @Nullable Model modelObject, @Nullable Configuration configObject){
        if(collection.compareTo(model)==0){
            Model object = new Model();
            FindIterable<Document> dbCursor = collectionModel.find(basicDBObject);
            for(Document doc:dbCursor){
                Gson gson = new Gson();
//                System.out.println("Reading doc: -----" + doc.toJson());
                Model readModel = gson.fromJson(doc.toJson(), Model.class);
                if(readModel.getMacAddress().compareTo(modelObject.getMacAddress())==0 || readModel.getDeviceName().compareTo(modelObject.getDeviceName())==0){
                    object = readModel;
                    break;
                }
            }
            return object;
        }
        else if(collection.compareTo(config)==0){
            Configuration object = new Configuration();
            FindIterable<Document> dbCursor = collectionConfig.find(basicDBObject);
            for(Document doc:dbCursor){
                Gson gson = new Gson();
                Configuration readConfig = gson.fromJson(doc.toJson(), Configuration.class);
                if(readConfig.getMacAddress().compareTo(configObject.getMacAddress())==0 || readConfig.getDeviceName().compareTo(configObject.getDeviceName())==0){
                    object = readConfig;
                    break;
                }
            }
            return object;
        }
        return null;
    }

    void delete(String collection, BasicDBObject basicDBObject, @Nullable Model modelObject, @Nullable Configuration configObject){
        if(collection.compareTo(model)==0){
            FindIterable<Document> dbCursor = collectionModel.find(basicDBObject);
            for(Document doc:dbCursor){
                Gson gson = new Gson();
//                System.out.println("Updating doc:----" + doc.toJson());
                Model readModel = gson.fromJson(doc.toJson(), Model.class);
                if(readModel.getMacAddress().compareTo(modelObject.getMacAddress())==0 || readModel.getDeviceName().compareTo(modelObject.getDeviceName())==0){
                    collectionModel.deleteOne(new Document().append("macAddress", readModel.getMacAddress()));
                    break;
                }
            }
        }
        else if(collection.compareTo(config)==0){
            FindIterable<Document> dbCursor = collectionConfig.find(basicDBObject);
            for(Document doc:dbCursor){
                Gson gson = new Gson();
//                System.out.println("Updating doc:----" + doc.toJson());
                Configuration readConfig = gson.fromJson(doc.toJson(), Configuration.class);
                if(readConfig.getMacAddress().compareTo(configObject.getMacAddress())==0 || readConfig.getDeviceName().compareTo(configObject.getDeviceName())==0){
                    collectionConfig.deleteOne(new Document().append("macAddress", readConfig.getMacAddress()));
                    break;
                }
            }
        }
    }
}
