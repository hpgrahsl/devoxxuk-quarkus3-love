package com.redhat.developers;

import java.util.List;

import org.bson.Document;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "movies")
public class Movie extends PanacheMongoEntity {
    
    public String title;
    public List<String> genre;
    public Integer duration;
    public Boolean released;
    public Integer year;

    public static Movie getRandomMovie() {
        return mongoCollection().aggregate(
            List.of(new Document("$sample",new Document("size",1L))),
            Movie.class
        ).first();
    }

}
