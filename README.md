# DevoxxUK 2023: Cloud-Native Java + Quarkus <3

Here are step by step notes for the live coding session I did during my Tools-In-Action session @ DevoxxUK 2023.

## 1. Quarkus 3 project setup

* scaffold sample project either at https://code.quarkus.io or use the Quarkus CLI to do the same thing by running the following command:

`quarkus create app com.redhat.developers:devoxx-quarkus-love:1.0.1`

* got to project folder and start the Quarkus `dev mode` in either of the following ways

    - CLI: `quarkus dev`
    - Maven: `mvn quarkus:dev`
    - Maven Wrapper: `./mvnw quarkus:dev`

* after the app has been started in dev mode, in the terminal session press `w` to reach the default index page or `d` to visit the DEV UI and inspect what's offered there (Extension Overview, Configuration, Continous Testing, Dev Services, ...)

## 2. Add Persistence for MongoDB with dev services

- add movie class

```java
import java.util.List;

public class Movie {
    public String title;
    public List<String> genre;
    public Integer duration;
    public Boolean released;
    public Integer year;
}
```

- add mongodb panache extension

`quarkus ext add mongodb-panache`

- add database name to `application.properties` 

```
[SUCCESS] ✅  Extension io.quarkus:quarkus-mongodb-panache has been installed
```

```properties
quarkus.mongodb.database=devoxxuk
```

- **active record pattern** make data class a mongodb panache entity and annotate it accordingly

```java
import java.util.List;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "movies")
public class Movie extends PanacheMongoEntity {
    public String title;
    public List<String> genre;
    public Integer duration;
    public Boolean released;
    public Integer year;
}
```

- OPTIONAL: use the repository pattern as alternative to active record

```java
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MovieRepository implements PanacheMongoRepository<Movie> {
    
}
```

## 3. Add REST endpoints

- for json (de)serialization purposes let's add an extension

`quarkus ext add resteasy-reactive-jackson`

```
 [SUCCESS] ✅  Extension io.quarkus:quarkus-resteasy-reactive-jackson has been installed
```

- `MovieResource.java` with the active record pattern approach:

```java
import java.net.URI;
import java.util.List;

import org.bson.types.ObjectId;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/movie")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MovieResource {

    @GET
    public List<Movie> allMovies() {
        return Movie.listAll();
    }

    @GET
    @Path("{id}")
    public Response getMovie(@PathParam("id") String id) {
        var movie = Movie.findById(new ObjectId(id));
        return movie != null  
            ? Response.ok(movie).build() 
            : Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    public Response addMovie(Movie movie) {
        movie.persist();
        return Response.created(URI.create("/api/movies"+movie.id)).entity(movie).build();
    }
    
}

```

- optional: `MovieResource.java` with the repository pattern approach:

```java
import java.net.URI;
import java.util.List;

import org.bson.types.ObjectId;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/movie")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MovieResource {

    private MovieRepository repository;

    public MovieResource(MovieRepository repository) {
        this.repository = repository;
    }

    @GET
    public List<Movie> getAllMovies() {
        return repository.listAll();
    }

    @GET
    @Path("{id}")
    public Response getMovie(@PathParam("id") String id) {
        var movie = repository.findById(new ObjectId(id));
        return movie != null  
            ? Response.ok(movie).build() 
            : Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    public Response addMovie(Movie movie) {
        repository.persist(movie);
        return Response.created(URI.create(("/api/movie/"+movie.id)))
            .entity(movie).build();
    }

}
```

- add sample data during application bootstrap with liquibase mongodb extension

`quarkus ext add liquibase-mongodb`

```
[SUCCESS] ✅  Extension io.quarkus:quarkus-liquibase-mongodb has been installed
```

- add `src/main/resources/mongo/import.xml`

```xml
<?xml version="1.1" encoding="UTF-8" standalone="no"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
        http://www.liquibase.org/xml/ns/dbchangelog-ext https://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd">

    <changeSet id="1" author="hanspeter">
        <ext:createCollection collectionName="movies"/>
        <ext:insertMany collectionName="movies">
            <ext:documents>
            [
               {
    "title": "Grown Ups",
    "genre": [
      "Comedies"
    ],
    "duration": 103,
    "released": true,
    "year": 2010
  },
  {
    "title": "Dark Skies",
    "genre": [
      "Horror Movies",
      "Sci-Fi &amp; Fantasy"
    ],
    "duration": 97,
    "released": true,
    "year": 2013
  },
  {
    "title": "Paranoia",
    "genre": [
      "Thrillers"
    ],
    "duration": 106,
    "released": true,
    "year": 2013
  },
  {
    "title": "Ankahi Kahaniya",
    "genre": [
      "Dramas",
      "Independent Movies",
      "International Movies"
    ],
    "duration": 111,
    "released": true,
    "year": 2021
  },
  {
    "title": "The Father Who Moves Mountains",
    "genre": [
      "Dramas",
      "International Movies",
      "Thrillers"
    ],
    "duration": 110,
    "released": true,
    "year": 2021
  }
            ]
            </ext:documents>
        </ext:insertMany>
    </changeSet>

</databaseChangeLog>
```

- add liquibase config props 

```properties
quarkus.liquibase-mongodb.change-log=mongo/import.xml
quarkus.liquibase-mongodb.migrate-at-start=true
```

## 4. Create Container Image & Generate Deployment Manifests

- you can setup your free managed mongodb instance on atlas here https://www.mongodb.com/cloud/atlas/register

- make sure that you set the database connection string for the prod profile because when running the application in a container in prod mode there are obviously no dev services available

```properties
# NOTE KEEP THE %prod. prefix so that you can continue to use the local containerized database when running quarkus in dev mode
%prod.quarkus.mongodb.connection-string=<YOUR_MONGODB_CONNECTION_STRING_HERE>

# if you want to load the sample data to the production database you have to keep the setting for liquibase like so
quarkus.liquibase-mongodb.migrate-at-start=true

# otherwise append a %dev prefix to only run the sample data initialization when running quarkus in dev mode
%dev.quarkus.liquibase-mongodb.migrate-at-start=true
```

- add JIB extension

`quarkus ext add container-image-jib`

```
[SUCCESS] ✅  Extension io.quarkus:quarkus-container-image-jib has been installed
```

- set `application.properties` config for fully qualified image name e.g.

```properties
#replace the fully-qualified image name above with your own specific one 
quarkus.container-image.image=quay.io/hgrahsl/devoxx-quarkus-love:1.0.1
```

- build the container image for the application

`quarkus image build jib`

- optional: push this image in case you plan to deploy it to a kubernetes that's not running of your local machine

- you can push in various different ways using `docker push ...` or `podman push ...` commands or by using the quarkus CLI like so

`quarkus image push`

- add the quarkus minikube extension and build the project to generate yml manifests

`quarkus ext add quarkus-minikube`

```
[SUCCESS] ✅  Extension io.quarkus:quarkus-minikube has been installed
```

`quarkus build --no-tests` 

- explore the `target/kubernetes` folder where you should see auto-generated YAML manifests and briefly inspect the `minikube.yml`

- this file contains a service + deployment for the containerized Quarkus application

- once you pointed your kubernetes context to the targeted cluster run 

`quarkus deploy minikube`

- the auto-generated yaml manifests will be applied to your kubernetes cluster and the app is deployed and running in a few moments

### **optional: native image compilation**

-> rename config in `application.properties` e.g. by changing the image tag to end with `-native`, otherwise if you keep the same name this would overwrite your original image that was previously created

```
quarkus.container-image.image=quay.io/hgrahsl/devoxx-quarkus-love:1.0.1-native
```

```
quarkus image build --native --no-tests -Dquarkus.native.container-build=true
``` 

- this process takes a while (usually between 2-3 min) and once it's successfully finished you can deploy the resulting container image with the native executable like before 

`quarkus deploy minikube` 

## 6. BONUS: Reactive Messaging / Event Streaming

- add smallrye reactive messaging kafka

`quarkus ext add smallrye-reactive-messaging-kafka`

```
[SUCCESS] ✅  Extension io.quarkus:quarkus-smallrye-reactive-messaging-kafka has been installed
```

- active record pattern: modifiy the `Movie.java` class by adding the following static method to the class which randomly selects a movie from the database

```java
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
```

- optional for the repository pattern: modify the `MovieRepository.java` class by adding the following method to randomly select a movie from the database



```java
import org.bson.Document;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MovieRepository implements PanacheMongoRepository<Movie> {
    
    public Movie getRandomMovie() {
        return mongoCollection().aggregate(
            List.of(new Document("$sample",new Document("size",1L))),
            Movie.class
        ).first();
    }

}
```

- add user activity simulator that generates random events that represent users watching movies:

- with the active record pattern:

`WatchersSimulator.java`

```java
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WatchersSimulator {

    final static List<String> USERNAMES = List.of("Alex","Natale","Cedric","Evan","Kevin","Ryan","Josh","Ian","Hans-Peter");

    public static record WatcherData(
        String movieId,
        String username,
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime timestamp
    ) {};
    
    @Outgoing("movie-watchers")
    public Multi<KafkaRecord<String, WatcherData>> simulateRandomWatcherActivity() {
        Log.info("starting to simulate random watchers activity every ... ms");
        return Multi.createFrom().ticks().every(Duration.ofMillis(1000))
                .map(t -> {
                    var movie = Movie.getRandomMovie();
                    var user = USERNAMES.get(new Random().nextInt(USERNAMES.size()));
                    Log.infov("randomly selected user ''{0}'' and movie ''{1}'' (id={2})",user,movie.title,movie.id);
                    return new WatcherData(movie.id.toHexString(),user,LocalDateTime.now());
                })
                .invoke(wd -> Log.infov("producing watcher data to kafka topic -> {0}", wd))
                .map(wd -> KafkaRecord.of(wd.movieId,wd));
    }

}
```

- optional with the repository pattern:

`WatchersSimulator.java`

```java
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.eclipse.microprofile.reactive.messaging.Outgoing;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WatchersSimulator {

    @Inject
    MovieRepository repository;

    final static List<String> USERNAMES = List.of("Alex","Natale","Cedric","Evan","Kevin","Ryan","Josh","Ian","Hans-Peter");

    public static record WatcherData(
        String movieId,
        String username,
        @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS")
        LocalDateTime timestamp
    ) {};
    
    @Outgoing("movie-watchers")
    public Multi<KafkaRecord<String, WatcherData>> simulateRandomWatcherActivity() {
        Log.info("starting to simulate random watchers activity every ... ms");
        return Multi.createFrom().ticks().every(Duration.ofMillis(1000))
                .map(t -> {
                    var movie = repository.getRandomMovie();
                    var user = USERNAMES.get(new Random().nextInt(USERNAMES.size()));
                    Log.infov("randomly selected user ''{0}'' and movie ''{1}'' (id={2})",user,movie.title,movie.id);
                    return new WatcherData(movie.id.toHexString(),user,LocalDateTime.now());
                })
                .invoke(wd -> Log.infov("producing watcher data to kafka topic -> {0}", wd))
                .map(wd -> KafkaRecord.of(wd.movieId,wd));
    }

}
```

- finally start quarkus in dev mode using `quarkus dev` which should now launch 2 dev service backed containers in the background:
    - the mongodb database just like before 
    - redpanda as event streaming platform that is  kafka API compatible

- if you follow the logs you see the randomly generated user activity written to a kafka topic

```
...
2023-05-12 15:12:53,251 INFO  [com.red.dev.WatchersSimulator] (executor-thread-1) randomly selected user 'Josh' and movie 'System Crasher' (id=645e3b54c25410612e120423)
2023-05-12 15:12:53,253 INFO  [com.red.dev.WatchersSimulator] (executor-thread-1) producing watcher data to kafka topic -> WatcherData[movieId=645e3b54c25410612e120423, username=Josh, timestamp=2023-05-12T15:12:53.253174]
2023-05-12 15:12:54,253 INFO  [com.red.dev.WatchersSimulator] (executor-thread-1) randomly selected user 'Alex' and movie 'Breaking Free' (id=645e3b54c25410612e120d2a)
2023-05-12 15:12:54,255 INFO  [com.red.dev.WatchersSimulator] (executor-thread-1) producing watcher data to kafka topic -> WatcherData[movieId=645e3b54c25410612e120d2a, username=Alex, timestamp=2023-05-12T15:12:54.255846]
2023-05-12 15:12:55,283 INFO  [com.red.dev.WatchersSimulator] (executor-thread-1) randomly selected user 'Elder' and movie 'How to Change the World' (id=645e3b54c25410612e120f1b)
2023-05-12 15:12:55,284 INFO  [com.red.dev.WatchersSimulator] (executor-thread-1) producing watcher data to kafka topic -> WatcherData[movieId=645e3b54c25410612e120f1b, username=Elder, timestamp=2023-05-12T15:12:55.284188]
2023-05-12 15:12:56,275 INFO  [com.red.dev.WatchersSimulator] (executor-thread-1) randomly selected user 'Josh' and movie 'Avicii: True Stories' (id=645e3b54c25410612e120ca0)
2023-05-12 15:12:56,275 INFO  [com.red.dev.WatchersSimulator] (executor-thread-1) producing watcher data to kafka topic -> WatcherData[movieId=645e3b54c25410612e120ca0, username=Josh, timestamp=2023-05-12T15:12:56.275932]
2023-05-12 15:12:57,280 INFO  [com.red.dev.WatchersSimulator] (executor-thread-1) randomly selected user 'Ian' and movie 'Chris D'Elia: Man on Fire' (id=645e3b54c25410612e120a4a)
2023-05-12 15:12:57,282 INFO  [com.red.dev.WatchersSimulator] (executor-thread-1) producing watcher data to kafka topic -> WatcherData[movieId=645e3b54c25410612e120a4a, username=Ian, timestamp=2023-05-12T15:12:57.281942]
...
```
