package com.devsuperior.dsmovie.services;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.devsuperior.dsmovie.entities.UserEntity;
import com.devsuperior.dsmovie.projections.UserDetailsProjection;
import com.devsuperior.dsmovie.repositories.UserRepository;
import com.devsuperior.dsmovie.tests.UserDetailsFactory;
import com.devsuperior.dsmovie.tests.UserFactory;
import com.devsuperior.dsmovie.utils.CustomUserUtil;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class UserServiceTests {

	@InjectMocks
	private UserService service;

	@Mock
	UserRepository userRepository;

	UserEntity user;

	@Mock
	private CustomUserUtil userUtil;

	List<UserDetailsProjection> userProjection;

	private String existinUsarname, nonExistingUsername;

	@BeforeEach
	void setUp() throws Exception {
		existinUsarname = "maria@gmail.com";
		nonExistingUsername = "Thiago";
		user = UserFactory.createUserEntity();
		userProjection = UserDetailsFactory.createCustomAdminClientUser(existinUsarname);

		Mockito.when(userRepository.findByUsername(existinUsarname)).thenReturn(Optional.of(user));
		Mockito.when(userRepository.findByUsername(nonExistingUsername)).thenThrow(UsernameNotFoundException.class);

		Mockito.when(userRepository.searchUserAndRolesByUsername(existinUsarname)).thenReturn(userProjection);
		Mockito.when(userRepository.searchUserAndRolesByUsername(nonExistingUsername)).thenReturn(List.of());

	}

	@Test
	public void authenticatedShouldReturnUserEntityWhenUserExists() {
		Mockito.when(userUtil.getLoggedUsername()).thenReturn(existinUsarname);
		UserEntity user = service.authenticated();
		Assertions.assertNotNull(user);
		Assertions.assertEquals(user.getUsername(), existinUsarname);
	}

	@Test
	public void authenticatedShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
		Mockito.when(userUtil.getLoggedUsername()).thenReturn(nonExistingUsername);
		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			service.authenticated();
		});

	}

	@Test
	public void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
		Mockito.when(userUtil.getLoggedUsername()).thenReturn(existinUsarname);

		UserService userService = Mockito.spy(service);
		Mockito.when(userService.authenticated()).thenReturn(user);

		UserDetails userEntity = service.loadUserByUsername(existinUsarname);

		Assertions.assertNotNull(userEntity);

	}

	@Test
	public void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExists() {
		Mockito.when(userUtil.getLoggedUsername()).thenReturn(nonExistingUsername);
		Mockito.doThrow(ClassCastException.class).when(userUtil).getLoggedUsername();

		Assertions.assertThrows(UsernameNotFoundException.class, () -> {
			service.loadUserByUsername(nonExistingUsername);

		});

	}
}
