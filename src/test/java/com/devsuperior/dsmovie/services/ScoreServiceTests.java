package com.devsuperior.dsmovie.services;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dsmovie.dto.MovieDTO;
import com.devsuperior.dsmovie.dto.ScoreDTO;
import com.devsuperior.dsmovie.entities.MovieEntity;
import com.devsuperior.dsmovie.entities.ScoreEntity;
import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.repositories.MovieRepository;
import com.devsuperior.dsmovie.repositories.ScoreRepository;
import com.devsuperior.dsmovie.services.exceptions.ResourceNotFoundException;
import com.devsuperior.dsmovie.tests.MovieFactory;
import com.devsuperior.dsmovie.tests.ScoreFactory;

@ExtendWith(SpringExtension.class)
public class ScoreServiceTests {
	
	@InjectMocks
	private ScoreService service;
	
	@Mock
	ScoreRepository scoreRepository;
	
	@Mock
	UserService userService;
	
	@Mock
	MovieRepository movieRepository;
	
	private ScoreEntity scoreEntity;
	private ScoreDTO scoreDTO;
	private UserEntity userEntity;
	private Long existingMovieID, nonExistingMovieId;
	private MovieEntity movieEntity;
	
	
	@BeforeEach
	void setUp() throws Exception { 
		existingMovieID = 1L;
		nonExistingMovieId = 2L;
		movieEntity = MovieFactory.createMovieEntity();
		scoreEntity = ScoreFactory.createScoreEntity();
		scoreDTO = ScoreFactory.createScoreDTO();
		
		Mockito.when(userService.authenticated()).thenReturn(userEntity);
		
		Mockito.when(movieRepository.save(movieEntity)).thenReturn(movieEntity);	
		Mockito.when(movieRepository.findById(existingMovieID)).thenReturn(Optional.of(movieEntity));
		Mockito.when(movieRepository.findById(nonExistingMovieId)).thenReturn(Optional.empty());
		
		Mockito.when(scoreRepository.saveAndFlush(scoreEntity)).thenReturn(scoreEntity);

	}
	
	@Test
	public void saveScoreShouldReturnMovieDTO() {
		MovieDTO result = service.saveScore(scoreDTO);
		Assertions.assertNotNull(result);
	
	}
	
	
	@Test
	public void saveScoreShouldThrowResourceNotFoundExceptionWhenNonExistingMovieId() {

		ScoreDTO newScoreDTO = new ScoreDTO(nonExistingMovieId, 10.0);
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.saveScore(newScoreDTO);
		});
	}

}
