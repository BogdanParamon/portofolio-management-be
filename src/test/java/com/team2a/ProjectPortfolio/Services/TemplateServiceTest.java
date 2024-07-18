package com.team2a.ProjectPortfolio.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team2a.ProjectPortfolio.Commons.Template;
import com.team2a.ProjectPortfolio.Commons.TemplateAddition;
import com.team2a.ProjectPortfolio.Repositories.TemplateAdditionRepository;
import com.team2a.ProjectPortfolio.Repositories.TemplateRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class TemplateServiceTest {

  @Mock
  private TemplateRepository templateRepository;

  @Mock
  private TemplateAdditionRepository templateAdditionRepository;

  private TemplateService templateService;

  @BeforeEach
  void setUp() {
    templateRepository = Mockito.mock(TemplateRepository.class);
    templateService = new TemplateService(templateRepository, templateAdditionRepository);
  }

  @Test
  void testCreateTemplateForbiddenName() {
    Template template = new Template();
    template.setTemplateName("templateName");
    when(templateRepository.existsById("templateName")).thenReturn(true);
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> templateService.createTemplate(template));
    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
  }

  @Test
  void testCreateTemplateSuccess() {
    Template template = new Template();
    template.setTemplateName("templateName");
    when(templateRepository.existsById("templateName")).thenReturn(false);
    when(templateRepository.save(template)).thenReturn(template);
    assertEquals(template, templateService.createTemplate(template));
  }

  @Test
  void testDeleteTemplateNotFound() {
    when(templateRepository.existsById("templateName")).thenReturn(false);
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> templateService.deleteTemplate("templateName"));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void testDeleteTemplateSuccess() {
    when(templateRepository.existsById("templateName")).thenReturn(true);
    doNothing().when(templateRepository).deleteById("templateName");
    templateService.deleteTemplate("templateName");
    verify(templateRepository, times(1)).deleteById("templateName");
  }

  @Test
  void testGetTemplateByNameNotFound() {
    when(templateRepository.findById("templateName")).thenReturn(Optional.empty());
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> templateService.getTemplateByName("templateName"));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void testGetTemplateByNameSuccess() {
    Template template = new Template();
    when(templateRepository.findById("templateName")).thenReturn(Optional.of(template));
    assertEquals(template, templateService.getTemplateByName("templateName"));
  }

  @Test
  void testGetAllTemplates() {
    Template t1 = new Template();
    Template t2 = new Template();
    when(templateRepository.findAll()).thenReturn(List.of(t1, t2));
    assertEquals(List.of(t1, t2), templateService.getAllTemplates());
  }

  @Test
  void testAddTemplateAdditionNotFound() {
    when(templateRepository.findById("templateName")).thenReturn(Optional.empty());
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> templateService.addTemplateAddition("templateName", new TemplateAddition()));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void testAddTemplateAdditionSuccess() {
    Template temp = new Template();
    TemplateAddition ta = new TemplateAddition();
    when(templateRepository.findById("templateName")).thenReturn(Optional.of(temp));
    when(templateAdditionRepository.save(ta)).thenReturn(ta);
    TemplateAddition re = templateService.addTemplateAddition("templateName", ta);
    assertEquals(ta, re);
  }

  @Test
  void testDeleteTemplateAdditionNotFound() {
    UUID id = UUID.randomUUID();
    when(templateAdditionRepository.existsById(id)).thenReturn(false);
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> templateService.deleteTemplateAddition(id));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void testDeleteTemplateAdditionSuccess() {
    UUID id = UUID.randomUUID();
    when(templateAdditionRepository.existsById(id)).thenReturn(true);
    doNothing().when(templateAdditionRepository).deleteById(id);
    templateService.deleteTemplateAddition(id);
    verify(templateAdditionRepository, times(1)).deleteById(id);
  }

  @Test
  void getAllTemplateAdditionsTemplateNotFound() {
    when(templateRepository.existsById("templateName")).thenReturn(false);
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> templateService.getAllTemplateAdditions("templateName"));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void getAllTemplateAdditionsSuccess() {
    TemplateAddition ta1 = new TemplateAddition();
    TemplateAddition ta2 = new TemplateAddition();
    when(templateRepository.existsById("templateName")).thenReturn(true);
    when(templateAdditionRepository.findAllByTemplateTemplateName("templateName")).thenReturn(List.of(ta1, ta2));
    assertEquals(List.of(ta1, ta2), templateService.getAllTemplateAdditions("templateName"));
  }
}
