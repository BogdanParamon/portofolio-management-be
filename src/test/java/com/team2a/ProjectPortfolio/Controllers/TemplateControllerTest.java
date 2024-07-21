package com.team2a.ProjectPortfolio.Controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.team2a.ProjectPortfolio.Commons.Template;
import com.team2a.ProjectPortfolio.Commons.TemplateAddition;
import com.team2a.ProjectPortfolio.Services.TemplateService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class TemplateControllerTest {

  @Mock
  private TemplateService templateService;
  private TemplateController templateController;

  @BeforeEach
  void setUp() {
    templateService = Mockito.mock(TemplateService.class);
    templateController = new TemplateController(templateService);
  }

  @Test
  void testCreateTemplateAlready() {
    when(templateService.createTemplate(any())).thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      templateController.createTemplate(new Template());
    });
    assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
  }

  @Test
  void testCreateTemplateSuccess() {
    Template temp = new Template();
    when(templateService.createTemplate(temp)).thenReturn(temp);
    ResponseEntity<Template> re = templateController.createTemplate(new Template());
    assertEquals(temp, re.getBody());
    assertEquals(HttpStatus.OK, re.getStatusCode());
  }

  @Test
  void testDeleteTemplateNotFound() {
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(templateService).deleteTemplate("templateName");
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      templateController.deleteTemplate("templateName");
    });
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void testDeleteTemplateSuccess() {
    doNothing().when(templateService).deleteTemplate("templateName");
    ResponseEntity<Void> re = templateController.deleteTemplate("templateName");
    assertEquals(HttpStatus.OK, re.getStatusCode());
    assertNull(re.getBody());
  }

  @Test
  void testGetTemplateByNameNotFound() {
    when(templateService.getTemplateByName("templateName")).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      templateController.getTemplateByName("templateName");
    });
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void testGetTemplateByNameSuccess() {
    Template temp = new Template();
    temp.setTemplateName("templateName");
    when(templateService.getTemplateByName("templateName")).thenReturn(temp);
    ResponseEntity<Template> re = templateController.getTemplateByName("templateName");
    assertEquals(HttpStatus.OK, re.getStatusCode());
    assertEquals(temp, re.getBody());
  }

  @Test
  void testGetAllTemplatesSuccess() {
    Template t1 = new Template();
    Template t2 = new Template();
    when(templateService.getAllTemplates()).thenReturn(List.of(t1, t2));
    ResponseEntity<List<Template>> re = templateController.getAllTemplates();
    assertEquals(HttpStatus.OK, re.getStatusCode());
    assertEquals(List.of(t1, t2), re.getBody());
  }

  @Test
  void testAddTemplateAdditionNotFound() {
    TemplateAddition ta = new TemplateAddition();
    when(templateService.addTemplateAddition("templateName", ta)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> templateController.addTemplateAddition("templateName", ta));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void testAddTemplateAdditionSuccess() {
    TemplateAddition ta = new TemplateAddition();
    when(templateService.addTemplateAddition("templateName", ta)).thenReturn(ta);
    ResponseEntity<TemplateAddition> re = templateController.addTemplateAddition("templateName", ta);
    assertEquals(HttpStatus.OK, re.getStatusCode());
    assertEquals(ta, re.getBody());
  }

  @Test
  void testDeleteTemplateAdditionNotFound() {
    UUID id = UUID.randomUUID();
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(templateService).deleteTemplateAddition(id);
    ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
      templateController.deleteTemplateAddition(id);
    });
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void testDeleteTemplateAdditionSuccess() {
    UUID id = UUID.randomUUID();
    doNothing().when(templateService).deleteTemplateAddition(id);
    ResponseEntity<Void> re = templateController.deleteTemplateAddition(id);
    assertEquals(HttpStatus.OK, re.getStatusCode());
    assertNull(re.getBody());
  }

  @Test
  void testGetAllTemplateAdditionsNotFound() {
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND)).when(templateService).getAllTemplateAdditions("templateName");
    ResponseStatusException exception = assertThrows(ResponseStatusException.class,
        () -> templateController.getAllTemplateAdditions("templateName"));
    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  @Test
  void testGetAllTemplateAdditionsSuccess() {
    List<TemplateAddition> list = List.of(new TemplateAddition(), new TemplateAddition());
    when(templateService.getAllTemplateAdditions("templateName")).thenReturn(list);
    ResponseEntity<List<TemplateAddition>> re = templateController.getAllTemplateAdditions("templateName");
    assertEquals(HttpStatus.OK, re.getStatusCode());
    assertEquals(list, re.getBody());
  }
}
