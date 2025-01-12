package com.team2a.ProjectPortfolio.Services;

import com.team2a.ProjectPortfolio.Commons.*;
import com.team2a.ProjectPortfolio.Repositories.ProjectRepository;
import com.team2a.ProjectPortfolio.Repositories.ProjectsToAccountsRepository;
import com.team2a.ProjectPortfolio.security.SecurityUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjectServiceTest {
    private ProjectRepository projectRepository;

    private ProjectsToAccountsRepository projectsToAccountsRepository;
    private SecurityUtils securityUtils;
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectRepository = mock(ProjectRepository.class);
        projectsToAccountsRepository = mock(ProjectsToAccountsRepository.class);
        securityUtils = mock(SecurityUtils.class);
        projectService = new ProjectService(projectRepository, securityUtils, projectsToAccountsRepository);
    }

    @Test
    void getProjectsEmpty() {
        List<Project> expected = new ArrayList<>();
        when(projectRepository.findAll()).thenReturn(List.of());
        List<Project> response = projectService.getProjects();
        assertEquals(expected, response);
    }

    @Test
    void getProjectsNotEmpty() {
        Project project1 = new Project("Title1", "Description1",  false);
        Project project2 = new Project("Title2", "Description2", false);
        Project project3 = new Project("Title3", "Description3", false);
        List<Project> projects = List.of(project1, project2, project3);

        when(projectRepository.findAll()).thenReturn(projects);

        List<Project> response = projectService.getProjects();
        assertEquals(projects, response);
    }

    @Test
    void deleteProjectSuccessful() {
        UUID projectId = UUID.randomUUID();
        Project project1 = new Project("Title1", "Description1", false);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project1));
        projectService.deleteProject(projectId);
        verify(projectRepository,times(1)).delete(project1);
    }
    @Test
    void updateProjectSuccess() {
        UUID projectId = UUID.randomUUID();
        Project project1 = new Project("Title1", "Description1",  false);
        Project project2 = new Project("Title2", "Description2", false);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project1));
        when(projectRepository.save(project1)).thenReturn(project2);
        Project response = projectService.updateProject(projectId, project2);
        assertEquals(project2, response);
    }
    @Test
    void createProjectSuccess() {
        String title = "title1";
        String desc = "desc1";
        Project project = new Project(title, desc, false);
        when(projectRepository.findFirstByTitleAndDescription(title, desc))
            .thenReturn(Optional.empty());
        when(projectRepository.save(any())).thenReturn(new Project(title, desc, false));
        when(projectsToAccountsRepository.save(any())).thenReturn(null);
        when(securityUtils.getCurrentUser()).thenReturn(new Account());
        Project response = projectService.createProject(project);
        assertEquals(project.getTitle(), response.getTitle());
        assertEquals(project.getDescription(), response.getDescription());
    }

    @Test
    void createProjectExistsAlready() {
        String title = "title1";
        String desc = "desc1";
        Project project = new Project(title, desc, false);
        when(projectRepository.findFirstByTitleAndDescription(title, desc))
                .thenReturn(Optional.of(new Project(title, desc, false)));
        assertThrows(ResponseStatusException.class, () -> projectService.createProject(project));
    }

    @Test
    void getProjectByIdSuccess() {
        UUID projectId = UUID.randomUUID();
        Project project1 = new Project("Title1", "Description1", false);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project1));
        Project response = projectService.getProjectById(projectId);
        assertEquals(project1, response);
    }

    @Test
    void getProjectByIdNotFound() {
        UUID projectId = UUID.randomUUID();
        assertThrows(ResponseStatusException.class, () ->
                projectService.getProjectById(projectId));
    }

    @Test
    void testUserBelongsToProjectProjectNotFound() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () ->
                projectService.userBelongsToProject("username",projectId));
    }

    @Test
    void testUserBelongsToProjectUserNotInProject() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("Title1", "Description1", false);
        project.setProjectsToAccounts(new ArrayList<>());
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        assertThrows(ResponseStatusException.class, () ->
                projectService.userBelongsToProject("username",projectId));
    }

    @Test
    void testUserBelongsToProjectUserInProject() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project("Title1", "Description1", false);
        Account account = new Account();
        account.setUsername("username");
        project.setProjectsToAccounts(List.of(
                new ProjectsToAccounts(RoleInProject.PM, account, project)));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        RoleInProject response =
                projectService.userBelongsToProject("username",projectId);
        assertEquals(RoleInProject.PM, response);
    }

    @Test
    void updateProjectTemplateSuccess() {
        UUID projectId = UUID.randomUUID();
        Template template = new Template("TempTitle",
                "StandardDescription", 6);
        Project project1 = new Project("Title1", "Description1", false, null);
        Project project2 = new Project("Title1", "Description1", false, template);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project1));
        when(projectRepository.save(project1)).thenReturn(project2);
        Project response = projectService.updateProjectTemplate(projectId, template);
        assertEquals(project2, response);
    }

    @Test
    void updateProjectTemplateNotFound() {
        UUID projectId = UUID.randomUUID();
        Template template = new Template("TempTitle",
                "StandardDescription", 6);
        Project project1 = new Project("Title1", "Description1", false, null);
        Project project2 = new Project("Title1", "Description1", false, template);
        when(projectRepository.findById(projectId)).thenThrow(ResponseStatusException.class);
        when(projectRepository.save(project1)).thenReturn(project2);
        assertThrows(ResponseStatusException.class, () ->
                projectService.updateProjectTemplate(projectId, template));
        verify(projectRepository, times(0)).save(project1);
    }

    @Test
    void removeTemplateFromProjectSuccess() {
        UUID projectId = UUID.randomUUID();
        Template template = new Template("TempTitle",
                "StandardDescription", 6);
        Project project1 = new Project("Title1", "Description1", false, template);
        Project project2 = new Project("Title1", "Description1", false, null);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project1));
        when(projectRepository.save(project1)).thenReturn(project2);
        Project response = projectService.removeTemplateFromProject(projectId);
        assertEquals(project2, response);
    }

    @Test
    void removeTemplateFromProjectNotFound() {
        UUID projectId = UUID.randomUUID();
        Template template = new Template("TempTitle",
                "StandardDescription", 6);
        Project project1 = new Project("Title1", "Description1", false, template);
        Project project2 = new Project("Title1", "Description1", false, null);
        when(projectRepository.findById(projectId)).thenThrow(ResponseStatusException.class);
        when(projectRepository.save(project1)).thenReturn(project2);
        assertThrows(ResponseStatusException.class, () ->
                projectService.removeTemplateFromProject(projectId));
        verify(projectRepository, times(0)).save(project1);
    }

    @Test
    void getTemplateByProjectIdSuccess() {
        UUID projectId1 = UUID.randomUUID();
        UUID projectId2 = UUID.randomUUID();

        Template template = new Template("TempTitle",
                "StandardDescription", 6);
        Project project1 = new Project("Title1", "Description1", false, null);
        Project project2 = new Project("Title1", "Description1", false, template);
        when(projectRepository.findById(projectId1)).thenReturn(Optional.of(project1));
        when(projectRepository.findById(projectId2)).thenReturn(Optional.of(project2));
        Template response1 = projectService.getTemplateByProjectId(projectId1);
        Template response2 = projectService.getTemplateByProjectId(projectId2);
        assertEquals(template, response2);
        assertNull(response1);
    }

    @Test
    void getTemplateByProjectIdNotFound() {
        UUID projectId = UUID.randomUUID();
        Template template = new Template("TempTitle",
                "StandardDescription", 6);
        Project project = new Project("Title1", "Description1", false, template);
        when(projectRepository.findById(projectId)).thenThrow(ResponseStatusException.class);
        assertThrows(ResponseStatusException.class, () ->
                projectService.getTemplateByProjectId(projectId));
    }

}