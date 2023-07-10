package com.devsuperior.dsmovie.services;

import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.services.exceptions.DatabaseException;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
public class MovieServiceTests {

	@InjectMocks
	private MovieService service;

	@Mock
	private MovieRepository movieRepository;

	private MovieEntity movie;
	private MovieDTO movieDTO;
	private Long existingMovieID, nonExistingMovieID, dependentMovieId;

	private PageImpl<MovieEntity> page;

	@BeforeEach
	void setUp() throws Exception {
		movie = MovieFactory.createMovieEntity();
		movieDTO = new MovieDTO(movie);

		page = new PageImpl<>(List.of(movie));

		existingMovieID = 1L;
		nonExistingMovieID = 2L;
		dependentMovieId = 3L;

		Mockito.when(movieRepository.searchByTitle(any(), (Pageable) any())).thenReturn(page);
		Mockito.when(movieRepository.findById(existingMovieID)).thenReturn(Optional.of(movie));
		Mockito.when(movieRepository.findById(nonExistingMovieID)).thenReturn(Optional.empty());
		Mockito.when(movieRepository.save(any())).thenReturn(movie);
		Mockito.when(movieRepository.getReferenceById(existingMovieID)).thenReturn(movie);
		Mockito.when(movieRepository.getReferenceById(nonExistingMovieID)).thenThrow(EntityNotFoundException.class);
		Mockito.when(movieRepository.existsById(existingMovieID)).thenReturn(true);
		Mockito.when(movieRepository.existsById(nonExistingMovieID)).thenReturn(false);
		Mockito.when(movieRepository.existsById(dependentMovieId)).thenReturn(true);
		Mockito.doNothing().when(movieRepository).deleteById(existingMovieID);
		Mockito.doThrow(ResourceNotFoundException.class).when(movieRepository).deleteById(nonExistingMovieID);
		Mockito.doThrow(DataIntegrityViolationException.class).when(movieRepository).deleteById(dependentMovieId);
	}

	@Test
	public void findAllShouldReturnPagedMovieDTO() {
		String title = "t";
		Pageable page = PageRequest.of(0, 10);
		Page<MovieDTO> list = service.findAll(title, (Pageable) page);
		Assertions.assertNotNull(list);
		Mockito.verify(movieRepository, Mockito.times(1)).searchByTitle(title, page);
	}

	@Test
	public void findByIdShouldReturnMovieDTOWhenIdExists() {
		MovieDTO result = service.findById(existingMovieID);
		Assertions.assertNotNull(result);
		Assertions.assertEquals(result.getId(), movie.getId());
	}

	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingMovieID);
		});
	}

	@Test
	public void insertShouldReturnMovieDTO() {
		MovieDTO result = service.insert(movieDTO);
		Assertions.assertNotNull(result);
	}

	@Test
	public void updateShouldReturnMovieDTOWhenIdExists() {	
		MovieDTO result = service.update(existingMovieID, movieDTO);
		Assertions.assertNotNull(result);
	}

	@Test
	public void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {	
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			 service.update(nonExistingMovieID, movieDTO);
		});
	}

	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingMovieID);
		});
	}

	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			 service.delete(nonExistingMovieID);
		});
	}

	@Test
	public void deleteShouldThrowDatabaseExceptionWhenDependentId() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			 service.delete(dependentMovieId);
		});
		
	}
}
