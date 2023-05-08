package com.redhat.developers;

import java.net.URI;
import java.util.List;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/movie")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovieResource {
    
    @GET
    public List<Movie> allMovies() {
        return Movie.listAll();
    }

    @POST
    public Response addMovie(Movie movie) {
        movie.persist();
        return Response.created(URI.create("api/movie"+movie.id)).entity(movie).build();
    }

}
