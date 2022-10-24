package com.example.itis5280_project8;



import android.util.Log;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

public class MongoDB {
    MongoClient mongo = MongoClients.create("mongodb+srv://group35280:uncc2022@cluster0.rts9eht.mongodb.net/?retryWrites=true&w=majority");
    MongoDatabase database = mongo.getDatabase("grocerystore");

    public MongoDB() {
    }

    public String getGrocery(int region) {
        String names = "";
        for (String name : database.listCollectionNames()) {
            names += names;
        }
        return names;
    }
}
