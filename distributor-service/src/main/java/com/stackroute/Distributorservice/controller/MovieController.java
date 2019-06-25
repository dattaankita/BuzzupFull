package com.stackroute.Distributorservice.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stackroute.Distributorservice.model.Movie;
import com.stackroute.Distributorservice.service.Services;

@CrossOrigin(origins = "*")
// This is controller
@RestController
// Class level request mapping

@RequestMapping("/api/v1/")
public class MovieController {

	// Creating an instance of service
	private Services movieServices;

	// logger
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public MovieController(Services movieServices) {
		this.movieServices = movieServices;
	}

	// saves the movie accepted from User
	@RequestMapping(value = "/movie", method = RequestMethod.POST)
	public ResponseEntity<?> saveMovie(@RequestBody Movie movie) {
		Movie savedMovie;
		savedMovie = movieServices.saveMovie(movie);
		logger.info("movie is saved into database");
		return new ResponseEntity<Movie>(savedMovie, HttpStatus.OK);

	}

	// Get all the movies the data from database
	@RequestMapping(value = "/movies", method = RequestMethod.GET)

	public ResponseEntity<List<Movie>> getAllMovies() {
		List<Movie> movies = movieServices.getAllMovies();
		logger.info("retrived all movies from database");
		return new ResponseEntity<List<Movie>>(movies, HttpStatus.OK);
	}

	// To get the movie from the database using title
	@RequestMapping(value = "/getbytitle/movie", method = RequestMethod.GET)
	public ResponseEntity<?> getMovieByTitleFromDB(@RequestParam String movieTitle) {
		List<Movie> movie = movieServices.getByMovieTitle(movieTitle);
		if (!movie.isEmpty()) {
			logger.info("retrived a movies of given titile");
			return new ResponseEntity<List<Movie>>(movie, HttpStatus.FOUND);
		} else {
			logger.warn("No movies of given titile in database");
			return new ResponseEntity<String>("{ \"message\": \"" + "no movies with this name" + "\"}", HttpStatus.OK);
		}
	}
	@GetMapping("/api/v1/movie/{distributorCity}")
    public ResponseEntity<?> getByDistributorCity(@PathVariable String distributorCity){
        Movie movie = movieServices.getByCity(distributorCity);
        if (movie!=null) {
            return new ResponseEntity<Movie>(movie, HttpStatus.OK);
        } else {
            
            return new ResponseEntity<String>("not found", HttpStatus.OK);
        }
    }

}
