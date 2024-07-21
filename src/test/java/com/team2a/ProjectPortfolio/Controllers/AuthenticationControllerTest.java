package com.team2a.ProjectPortfolio.Controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team2a.ProjectPortfolio.Services.AuthenticationService;
import com.team2a.ProjectPortfolio.WebSocket.AccountWebSocketHandler;
import com.team2a.ProjectPortfolio.dto.LoginUserRequest;
import com.team2a.ProjectPortfolio.dto.RegisterUserRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

  @Mock
  private AuthenticationService authenticationService;

  @Mock
  private AccountWebSocketHandler accountWebSocketHandler;
  @Mock
  private HttpServletRequest httpServletRequest;
  @Mock
  private HttpServletResponse httpServletResponse;
  @Captor
  private ArgumentCaptor<Cookie> captor;
  private AuthenticationController authenticationController;

  @BeforeEach
  void setUp() {
    authenticationService = Mockito.mock(AuthenticationService.class);
    accountWebSocketHandler = Mockito.mock(AccountWebSocketHandler.class);
    httpServletRequest = Mockito.mock(HttpServletRequest.class);
    httpServletResponse = Mockito.mock(HttpServletResponse.class);
    authenticationController = new AuthenticationController(authenticationService, accountWebSocketHandler);
  }

  @Test
  void createAccountConflictTest() {
    RegisterUserRequest request = new RegisterUserRequest("username", "password", "name");
    doThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists.")).when(authenticationService).registerUser(request);
    ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> authenticationController.createAccount(request));
    verify(accountWebSocketHandler, times(0)).broadcast(any());
    assertEquals(HttpStatus.CONFLICT, e.getStatusCode());
    assertEquals("Username already exists.", e.getReason());
  }

  @Test
  void createAccountSuccessTest() {
    RegisterUserRequest request = new RegisterUserRequest("username", "password", "name");
    doNothing().when(authenticationService).registerUser(request);
    assertEquals(HttpStatus.CREATED, authenticationController.createAccount(request).getStatusCode());
    verify(accountWebSocketHandler, times(1)).broadcast("add " + request.getUsername());
  }

  @Test
  void loginAlreadyLoggedInTest() {
    LoginUserRequest loginUserRequest = new LoginUserRequest("username", "password");
    when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{new Cookie("auth-cookie", null)});
    ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> authenticationController
        .login(loginUserRequest, httpServletRequest, httpServletResponse));
    assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
    assertEquals("Already logged in.", e.getReason());
  }

  @Test
  void loginUsernamePasswordIncorrectTest() {
    LoginUserRequest loginUserRequest = new LoginUserRequest("username", "password");
    when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{});
    when(authenticationService.authenticate(loginUserRequest)).thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username or password incorrect."));
    ResponseStatusException e = assertThrows(ResponseStatusException.class, () -> authenticationController
        .login(loginUserRequest, httpServletRequest, httpServletResponse));
    assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
    assertEquals("Username or password incorrect.", e.getReason());
  }

  @Test
  void loginSuccessTest() {
    LoginUserRequest loginUserRequest = new LoginUserRequest("username", "password");
    when(httpServletRequest.getCookies()).thenReturn(new Cookie[]{});
    when(authenticationService.authenticate(loginUserRequest)).thenReturn(null);
    ResponseEntity<String> e = authenticationController.login(loginUserRequest, httpServletRequest, httpServletResponse);
    verify(httpServletResponse, times(1)).addCookie(captor.capture());
    assertEquals("auth-cookie", captor.getValue().getName());
    assertEquals(HttpStatus.OK, e.getStatusCode());
  }

  @Test
  void logoutSuccessTest() {
    ResponseEntity<String> e = authenticationController.logout(httpServletResponse);
    verify(httpServletResponse, times(1)).addCookie(captor.capture());
    assertEquals("auth-cookie", captor.getValue().getName());
    assertEquals(HttpStatus.OK, e.getStatusCode());
    assertEquals("Logged out successfully", e.getBody());
  }

}
