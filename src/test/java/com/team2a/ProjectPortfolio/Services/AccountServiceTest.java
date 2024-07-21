package com.team2a.ProjectPortfolio.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team2a.ProjectPortfolio.Commons.Account;
import com.team2a.ProjectPortfolio.Commons.Project;
import com.team2a.ProjectPortfolio.Commons.ProjectsToAccounts;
import com.team2a.ProjectPortfolio.Commons.Role;
import com.team2a.ProjectPortfolio.Commons.RoleInProject;
import com.team2a.ProjectPortfolio.CustomExceptions.AccountNotFoundException;
import com.team2a.ProjectPortfolio.CustomExceptions.DuplicatedUsernameException;
import com.team2a.ProjectPortfolio.CustomExceptions.NotFoundException;
import com.team2a.ProjectPortfolio.CustomExceptions.ProjectNotFoundException;
import com.team2a.ProjectPortfolio.Repositories.AccountRepository;
import com.team2a.ProjectPortfolio.Repositories.ProjectRepository;
import com.team2a.ProjectPortfolio.Repositories.ProjectsToAccountsRepository;
import com.team2a.ProjectPortfolio.dto.AccountDisplay;
import com.team2a.ProjectPortfolio.dto.AccountTransfer;
import com.team2a.ProjectPortfolio.dto.ProjectTransfer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private ProjectsToAccountsRepository projectsToAccountsRepository;

  private AccountService accountService;

  private ProjectsToAccounts pta;

  private Account a;

  private final UUID projectId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    accountRepository = Mockito.mock(AccountRepository.class);
    projectRepository = Mockito.mock(ProjectRepository.class);
    projectsToAccountsRepository = Mockito.mock(ProjectsToAccountsRepository.class);
    accountService = new AccountService(accountRepository, projectRepository, projectsToAccountsRepository);
    a = new Account("username", "name", "password", Role.ROLE_USER);
    Project project = new Project();
    project.setProjectId(projectId);
    project.setTitle("Title project");
    pta = new ProjectsToAccounts(RoleInProject.CONTENT_CREATOR, a, project);
    accountService = new AccountService(accountRepository, projectRepository, projectsToAccountsRepository);
  }
  @Test
  void testEditAccountAccountNotFoundException() {
    Account account = new Account("username", "name", "password", Role.ROLE_USER);
    when(accountRepository.findById("username")).thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class, () -> accountService.editAccount(account));
    verify(accountRepository, never()).save(any(Account.class));
  }

  @Test
  void testEditAccountSuccess() {
    Account account = new Account("username", "name", "password", Role.ROLE_USER);
    when(accountRepository.findById("username")).thenReturn(Optional.of(account));
    when(accountRepository.save(account)).thenReturn(account);
    Account retrieved_account = accountService.editAccount(account);
    assertEquals(retrieved_account, account);
    verify(accountRepository, times(1)).save(account);
  }

  @Test
  void testGetAccountByIdAccountNotFoundException() {
    when(accountRepository.findById("username")).thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class, () -> accountService.getAccountById("username"));
  }

  @Test
  void testGetAccountByIdSuccess() {
    Account account = new Account("username", "name", "password", Role.ROLE_USER);
    when(accountRepository.findById("username")).thenReturn(Optional.of(account));
    Account retrieved_account = accountService.getAccountById("username");
    assertEquals(retrieved_account, account);
    verify(accountRepository, times(1)).findById("username");
  }

  @Test
  void testDeleteAccountByIdAccountNotFoundException() {
    when(accountRepository.findById("username")).thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class, () -> accountService.deleteAccount("username"));
    verify(accountRepository, never()).deleteById("username");
  }

  @Test
  void testDeleteAccountSuccess() {
    Account account = new Account("username", "name", "password", Role.ROLE_USER);
    when(accountRepository.findById("username")).thenReturn(Optional.of(account));
    doNothing().when(accountRepository).deleteById("username");
    accountService.deleteAccount("username");
    verify(accountRepository, times(1)).deleteById("username");
  }

  @Test
  void testAddRoleAccountNotFound() {
    when(accountRepository.findById("username")).thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class, () -> accountService.addRole("username", UUID.randomUUID(), RoleInProject.CONTENT_CREATOR));
  }

  @Test
  void testAddRoleProjectNotFound() {
    UUID id = UUID.randomUUID();
    when(accountRepository.findById("username")).thenReturn(Optional.of(new Account()));
    when(projectRepository.findById(id)).thenReturn(Optional.empty());
    assertThrows(ResponseStatusException.class, () -> accountService.addRole("username", id, RoleInProject.CONTENT_CREATOR));
  }

  @Test
  void testAddRoleAlreadyHasRole() {
    UUID id = UUID.randomUUID();
    when(accountRepository.findById("username")).thenReturn(Optional.of(new Account()));
    when(projectRepository.findById(id)).thenReturn(Optional.of(new Project()));
    Project p = new Project();
    Account a = new Account();
    when(projectRepository.findById(id)).thenReturn(Optional.of(p));
    when(accountRepository.findById("username")).thenReturn(Optional.of(a));
    when(projectsToAccountsRepository.findAll()).thenReturn(List.of(new ProjectsToAccounts(RoleInProject.CONTENT_CREATOR, a, p)));
    assertThrows(ResponseStatusException.class, () -> accountService.addRole("username", id, RoleInProject.CONTENT_CREATOR));
  }

  @Test
  void testAddRoleSuccess() {
    UUID id = UUID.randomUUID();
    when(accountRepository.findById("username")).thenReturn(Optional.of(new Account()));
    when(projectRepository.findById(id)).thenReturn(Optional.of(new Project()));
    when(projectsToAccountsRepository.findAll()).thenReturn(List.of());
    accountService.addRole("username", id, RoleInProject.CONTENT_CREATOR);
    verify(projectsToAccountsRepository, times(1)).save(any());
  }

  @Test
  void testDeleteRoleNotFoundProject() {
    UUID id = UUID.randomUUID();
    Project p = new Project();
    p.setProjectId(id);
    Account a = new Account();
    a.setUsername("username");
    ProjectsToAccounts pta = new ProjectsToAccounts(RoleInProject.CONTENT_CREATOR, a, p);
    when(projectsToAccountsRepository.findAll()).thenReturn(List.of(pta));
    assertThrows(ResponseStatusException.class, () -> accountService.deleteRole("username1", id));
  }

  @Test
  void testDeleteRoleNotFoundAccount() {
    UUID id = UUID.randomUUID();
    Project p = new Project();
    p.setProjectId(id);
    UUID id2;
    do {
      id2 = UUID.randomUUID();
    } while (id2.equals(id));
    Account a = new Account();
    a.setUsername("username");
    ProjectsToAccounts pta = new ProjectsToAccounts(RoleInProject.CONTENT_CREATOR, a, p);
    when(projectsToAccountsRepository.findAll()).thenReturn(List.of(pta));
    UUID finalId = id2;
    assertThrows(ResponseStatusException.class, () -> accountService.deleteRole("username1", finalId));
  }

  @Test
  void testDeleteRoleSuccess() {
    UUID id = UUID.randomUUID();
    Project p = new Project();
    p.setProjectId(id);
    Account a = new Account();
    a.setUsername("username");
    ProjectsToAccounts pta = new ProjectsToAccounts(RoleInProject.CONTENT_CREATOR, a, p);
    when(projectsToAccountsRepository.findAll()).thenReturn(List.of(pta));
    accountService.deleteRole("username", id);
    verify(projectsToAccountsRepository, times(1)).deleteById(any());
  }

  @Test
  void testUpdateRoleSuccess() {
    when(projectsToAccountsRepository.findAll()).thenReturn(List.of(pta));

    accountService.updateRole(a.getUsername(), projectId, RoleInProject.PM);

    assertEquals(RoleInProject.PM, pta.getRole());
    verify(projectsToAccountsRepository, times(1)).save(any(ProjectsToAccounts.class));
  }

  @Test
  void testUpdateRoleNotFound() {
    when(projectsToAccountsRepository.findAll()).thenReturn(List.of());

    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
        accountService.updateRole(a.getUsername(), projectId, RoleInProject.PM));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    verify(projectsToAccountsRepository, never()).save(any(ProjectsToAccounts.class));
  }

  @Test
  void testGetRoleSuccess() {
    when(projectsToAccountsRepository.findAll()).thenReturn(List.of(pta));

    String role = accountService.getRole(a.getUsername(), projectId);

    assertEquals(RoleInProject.CONTENT_CREATOR.toString(), role);
  }

  @Test
  void testGetRoleNotFound() {
    when(projectsToAccountsRepository.findAll()).thenReturn(List.of());

    String role = accountService.getRole(a.getUsername(), projectId);

    assertEquals("VISITOR", role);
  }

  @Test
  void testGetProjects() {
    when(projectsToAccountsRepository.findAll()).thenReturn(List.of(pta));
    List<ProjectTransfer> projects = accountService.getProjects("username");
    assertEquals(1, projects.size());
    assertEquals(RoleInProject.CONTENT_CREATOR, projects.get(0).getRoleInProject());
    assertEquals(projectId, projects.get(0).getProjectId());
    assertEquals("Title project", projects.get(0).getName());
  }

  @Test
  void testGetAccounts() {
    when(accountRepository.findAll()).thenReturn(List.of(a));
    List<AccountTransfer> accounts = accountService.getAccounts();
    assertEquals(1, accounts.size());
    assertFalse(accounts.get(0).isAdmin());
    assertFalse(accounts.get(0).isPM());
    assertEquals(a.getUsername(), accounts.get(0).getUsername());
  }

  @Test
  void testGetAccountsByName() {
    when(accountRepository.findAll()).thenReturn(List.of(a));
    List<String> usernames = accountService.getAccountsByName("name");
    assertEquals(1, usernames.size());
    assertEquals("username", usernames.get(0));
  }

  @Test
  void testEditAccountTransferNotFound() {
    AccountTransfer accountTransfer = new AccountTransfer("username", false, false);
    when(accountRepository.findById(accountTransfer.getUsername())).thenReturn(Optional.empty());
    assertThrows(AccountNotFoundException.class, () -> accountService.editAccount(accountTransfer));
  }

  @Test
  void testEditAccountTransferSuccess() {
    AccountTransfer accountTransfer = new AccountTransfer("username", false, false);
    Account account = new Account("username", "name", "password", Role.ROLE_PM);
    when(accountRepository.findById(accountTransfer.getUsername())).thenReturn(Optional.of(account));
    ArgumentCaptor<Account> ac = ArgumentCaptor.forClass(Account.class);
    accountService.editAccount(accountTransfer);
    verify(accountRepository, times(1)).save(ac.capture());
    assertEquals("username", ac.getValue().getUsername());
    assertEquals(Role.ROLE_USER, ac.getValue().getRole());
  }

  @Test
  void testGetAccountsInProject(){
    when(projectsToAccountsRepository.findAll()).thenReturn(List.of(pta));
    List<AccountDisplay> accounts = accountService.getAccountsInProject(projectId);
    assertEquals(1, accounts.size());
    AccountDisplay account = accounts.get(0);
    assertEquals(a.getUsername(), account.getUsername());
    assertEquals(a.getName(), account.getName());
    assertEquals(RoleInProject.CONTENT_CREATOR.toString(), account.getRoleInProject());

  }

}
