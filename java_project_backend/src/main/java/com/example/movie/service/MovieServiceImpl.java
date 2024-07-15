package com.example.movie.service;

import com.example.movie.dto.MovieDto;
import com.example.movie.dto.MoviePageResponse;
import com.example.movie.entities.Movie;
import com.example.movie.exceptions.FileExistsException;
import com.example.movie.exceptions.MovieNotFoundException;
import com.example.movie.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


//import java.awt.print.Pageable;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
@Service
public class MovieServiceImpl implements MovieService{

    private final MovieRepository movieRepository;
    private final FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService){
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {
        // 1. upload the file
        if(Files.exists(Paths.get(path + File.separator + file.getOriginalFilename()))){
            throw new FileExistsException("File already  exists! Please enter another file name!");
        }

        String uploadFileName = fileService.uploadFile(path, file);
        // 2. set the value of filed 'poster' as filename
        movieDto.setPoster(uploadFileName);
        // 3. map dto to Movie object
        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );
        // 4. save the movie object -> saved Movie object
        Movie saveMovie = movieRepository.save(movie);
        // 5. generate the posterUrl
        String posterUrl = baseUrl + "/file/" + uploadFileName;
        // 6. map Movie object to Dto object sand return it
        MovieDto response = new MovieDto(
                saveMovie.getMovieId(),
                saveMovie.getTitle(),
                saveMovie.getDirector(),
                saveMovie.getStudio(),
                saveMovie.getMovieCast(),
                saveMovie.getReleaseYear(),
                saveMovie.getPoster(),
                posterUrl

        );
        return response;
    }

    @Override
    public MovieDto getMovie(Integer movieId) {
        // 1. check the data in DB and if exists, fetch the data of given ID
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new RuntimeException("Movie not found!"));
        // 2. generate posterUrl
        String posterUrl = baseUrl + "/file/" + movie.getPoster();
        //3. map to MovieDto object and return it
        MovieDto response = new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl

        );
        return response;
    }

    @Override
    public List<MovieDto> getAllMovies() {
        // 1. fetch all data from DB
        List<Movie> movie = movieRepository.findAll();
        List<MovieDto> movieDtos = new ArrayList<>();
        // 2. interate through the list, generate posterUrl for each movie obj,
        //and map to MovieDto obj
        for(Movie mv : movie){
            String posterUrl = baseUrl + "/file/" + mv.getPoster();
            MovieDto respose = new MovieDto(
                    mv.getMovieId(),
                    mv.getTitle(),
                    mv.getDirector(),
                    mv.getStudio(),
                    mv.getMovieCast(),
                    mv.getReleaseYear(),
                    mv.getPoster(),
                    posterUrl

            );
            movieDtos.add(respose);
        }
        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException {

        // 1. check if movie object exists with given movieId
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie not found with id = " + movieId));

        // 2. if file is null, do nothing
        // if file not null, then delete existing file associated with the record,
        // and upload the new file

        String fileName = movie.getPoster();
        if(file != null){
            Files.deleteIfExists(Paths.get(path + File.separator + fileName));
            fileName = fileService.uploadFile(path, file);
        }
        // 3. set movieDto's poster value, according to step 2
        movieDto.setPoster(fileName);
        // 4. map it to Movie object
        Movie movie1 = new Movie(
                movie.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );
        // 5. save the movie object -> return saved movie object
        Movie save = movieRepository.save(movie1);
        // 6. generate posterUrl for it
        String posterUrl = baseUrl + "/file/" + fileName;
        // 7. map to MovieDto and return it
        MovieDto response = new MovieDto(
                movie1.getMovieId(),
                movie1.getTitle(),
                movie1.getDirector(),
                movie1.getStudio(),
                movie1.getMovieCast(),
                movie1.getReleaseYear(),
                movie1.getPoster(),
                posterUrl

        );
        return response;
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException{
        // 1. check if movie object exists  in DB
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new MovieNotFoundException("Movie not found with id = " + movieId));
        Integer id = movie.getMovieId();
        // 2. delete the file associated with this object
        Files.deleteIfExists(Paths.get(path + File.separator + movie.getPoster()));
        // 3. delete the movie
        movieRepository.delete(movie);
        return "Movie deleted with id = " + id;
    }

    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = (Pageable) PageRequest.of(pageNumber,pageSize);
        Page<Movie> moviePages = movieRepository.findAll(pageable);

        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();

        // 2. iterate through the list, generate posterUrl for each movie obj,
        // and map to MovieDto obj
        for(Movie movie : movies) {
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }


        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize, String sortBy, String dir) {
        Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = (Pageable) PageRequest.of(pageNumber, pageSize, sort);

        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();

        // 2. iterate through the list, generate posterUrl for each movie obj,
        // and map to MovieDto obj
        for(Movie movie : movies) {
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }


        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast());
    }
}
