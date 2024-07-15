package com.example.movie.repositories;

import com.example.movie.entities.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;

public interface MovieRepository extends JpaRepository<Movie,Integer> {

}
