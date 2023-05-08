package com.redhat.developers;

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

    final static List<String> USERNAMES = List.of("Natale","Alex","Ian","Hans-Peter","Cedric","Evan","Kevin","Ryan","Josh","Elder");

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
