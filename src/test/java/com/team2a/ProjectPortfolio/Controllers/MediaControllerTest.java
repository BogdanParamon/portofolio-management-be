package com.team2a.ProjectPortfolio.Controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.team2a.ProjectPortfolio.Commons.Media;
import com.team2a.ProjectPortfolio.Commons.Project;
import com.team2a.ProjectPortfolio.CustomExceptions.IdIsNullException;
import com.team2a.ProjectPortfolio.CustomExceptions.MediaNotFoundException;
import com.team2a.ProjectPortfolio.CustomExceptions.ProjectNotFoundException;
import com.team2a.ProjectPortfolio.Services.MediaService;
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

@DataJpaTest
@ExtendWith(MockitoExtension.class)
public class MediaControllerTest {

  @Mock
  private MediaService mediaService;
  private MediaController mediaController;

  @BeforeEach
  void setup() {
    mediaService = Mockito.mock(MediaService.class);
    mediaController = new MediaController(mediaService);
  }

  @Test
  void testGetMediaByProjectIdNullIdException() {
    when(mediaService.getMediaByProjectId(any())).thenThrow(new IdIsNullException(""));
    ResponseEntity<List<Media>> entity = mediaController.getMediaByProjectId(null);
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    assertNull(entity.getBody());
  }

  @Test
  void testGetMediaByProjectIdProjectNotFoundException() {
    when(mediaService.getMediaByProjectId(any(UUID.class))).thenThrow(new ProjectNotFoundException(""));
    ResponseEntity<List<Media>> entity = mediaController.getMediaByProjectId(UUID.randomUUID());
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    assertNull(entity.getBody());
  }

  @Test
  void testGetMediaByProjectIdSuccess() {
    Project p = new Project("title", "description", "bibtex", false);
    Media m1 = new Media(p, "path1");
    Media m2 = new Media(p, "path2");
    Media m3 = new Media(p, "path3");
    when(mediaService.getMediaByProjectId(any(UUID.class))).thenReturn(List.of(m1, m2, m3));
    ResponseEntity<List<Media>> entity = mediaController.getMediaByProjectId(UUID.randomUUID());
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(List.of(m1, m2, m3), entity.getBody());
  }

  @Test
  void testAddMediaToProjectNullIdException() {
    when(mediaService.addMediaToProject(any(UUID.class), any(String.class))).thenThrow(new IdIsNullException(""));
    ResponseEntity<Media> entity = mediaController.addMediaToProject(UUID.randomUUID(), "path");
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    assertNull(entity.getBody());
  }

  @Test
  void testAddMediaToProjectProjectNotFoundException() {
    when(mediaService.addMediaToProject(any(UUID.class), any(String.class))).thenThrow(new ProjectNotFoundException(""));
    ResponseEntity<Media> entity = mediaController.addMediaToProject(UUID.randomUUID(), "path");
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    assertNull(entity.getBody());
  }

  @Test
  void testAddMediaToProjectSuccess() {
    Project p = new Project("title", "description", "bibtex", false);
    Media m1 = new Media(p, "path1");
    when(mediaService.addMediaToProject(any(UUID.class), any(String.class))).thenReturn(m1);
    ResponseEntity<Media> entity = mediaController.addMediaToProject(UUID.randomUUID(), "path1");
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals(m1, entity.getBody());
  }

  @Test
  void testDeleteMediaFromProjectNullId() {
    doThrow(new IdIsNullException("Null id not accepted.")).when(mediaService).deleteMedia(any(UUID.class));
    ResponseEntity<String> entity = mediaController.deleteMedia(UUID.randomUUID());
    assertEquals(HttpStatus.BAD_REQUEST, entity.getStatusCode());
    assertEquals("Null id not accepted.", entity.getBody());
  }

  @Test
  void testDeleteMediaFromProjectMediaNotFound() {
    UUID i1 = UUID.randomUUID();
    doThrow(new MediaNotFoundException("No media with the id " + i1 + " could be found."))
        .when(mediaService).deleteMedia(i1);
    ResponseEntity<String> entity = mediaController.deleteMedia(i1);
    assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    assertEquals("No media with the id " + i1 + " could be found.", entity.getBody());
  }

  @Test
  void testDeleteMediaFromProjectSuccess() {
    doNothing().when(mediaService).deleteMedia(any());
    ResponseEntity<String> entity = mediaController.deleteMedia(UUID.randomUUID());
    assertEquals(HttpStatus.OK, entity.getStatusCode());
    assertEquals("Media deleted successfully.", entity.getBody());
  }
}
