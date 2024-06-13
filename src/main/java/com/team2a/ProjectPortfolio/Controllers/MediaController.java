package com.team2a.ProjectPortfolio.Controllers;

import com.team2a.ProjectPortfolio.Commons.Media;
import com.team2a.ProjectPortfolio.Commons.RequestMediaProject;
import com.team2a.ProjectPortfolio.CustomExceptions.MediaNotFoundException;
import com.team2a.ProjectPortfolio.CustomExceptions.NotFoundException;
import com.team2a.ProjectPortfolio.Routes;
import com.team2a.ProjectPortfolio.Services.MediaService;
import com.team2a.ProjectPortfolio.WebSocket.MediaProjectWebSocketHandler;
import jakarta.validation.Valid;
import org.antlr.v4.runtime.misc.Pair;
import org.antlr.v4.runtime.misc.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

import static com.team2a.ProjectPortfolio.security.Permissions.*;

@RestController
@RequestMapping(Routes.MEDIA)
@CrossOrigin("http://localhost:4200")
public class MediaController {

    private final MediaService mediaService;

    private final MediaProjectWebSocketHandler mediaProjectWebSocketHandler;

    /**
     * Constructor for the media controller
     * @param mediaService the media service instance
     * @param mediaProjectWebSocketHandler the web socket handler for media to project
     */
    @Autowired
    public MediaController (MediaService mediaService,
                            MediaProjectWebSocketHandler mediaProjectWebSocketHandler) {
        this.mediaService = mediaService;
        this.mediaProjectWebSocketHandler = mediaProjectWebSocketHandler;
    }

    /**
     * Gets all Medias under a certain Project
     * @param projectId the id of the Project whose Media to be retrieved
     * @return the List of all Medias corresponding to the project
     */
    @GetMapping("/public/images/{projectId}")
    public ResponseEntity<List<Triple<String,String,String>>> getImagesContentByProjectId (@PathVariable("projectId")
                                                                                               UUID projectId) {
        return ResponseEntity.ok(mediaService.getImagesContentByProjectId(projectId));
    }

    /**
     * Returns the content of a document based on its mediaId
     * @param mediaId the mediaId of the document we need to retrieve
     * @return the media content
     */
    @GetMapping("/public/file/content/{mediaId}")
    public ResponseEntity<Pair<String,String>> getDocumentContentByMediaId (@PathVariable("mediaId") UUID mediaId) {
        return ResponseEntity.ok(mediaService.getDocumentByMediaId(mediaId));
    }

    /**
     * Returns the list of medias of a specific projectId
     * @param projectId the projectID
     * @return the list of medias
     */
    @GetMapping("/public/file/{projectId}")
    public ResponseEntity<List<Media>> getDocumentsByProjectId (@PathVariable("projectId") UUID projectId) {
        return ResponseEntity.ok(mediaService.getDocumentsByProjectId(projectId));
    }

    /**
     * Adds a Media associated with an already existing project
     * @param projectId the id of the Project that gets the Media
     * @param file the Media to be added
     * @param name the name of the media
     * @return the Media instance generated and saved
     */
    @PostMapping("/{projectId}")
    @PreAuthorize(EDITOR_IN_PROJECT)
    public ResponseEntity<Media> addMediaToProject (@PathVariable("projectId") UUID projectId,
                                                    @RequestParam("file") MultipartFile file, @RequestParam String name) {
        Media body = mediaService.addMediaToProject(projectId, file,name);
        mediaProjectWebSocketHandler.broadcast(projectId.toString());
        return ResponseEntity.ok(body);
    }

    /**
     * Deletes a media from the database
     * @param mediaId the id of the Media under deletion
     * @param projectId the id of the Project that the Media belongs to
     * @return the status of the operation
     */
    @DeleteMapping("/{projectId}/{mediaId}")
    @PreAuthorize(EDITOR_IN_PROJECT)
    public ResponseEntity<String> deleteMedia (@PathVariable("projectId") UUID projectId,
                                               @PathVariable("mediaId") UUID mediaId) {
        try {
            Media m = mediaService.deleteMedia(mediaId);
            mediaProjectWebSocketHandler.broadcast(m.getProject().getProjectId().toString());
            return ResponseEntity.status(HttpStatus.OK).body("Media deleted successfully.");
        }
        catch (MediaNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Edit Media provided it exists already by id
     * @param media - the Media with the new fields
     * @return - the edited Media
     */
    @PutMapping("")
    public ResponseEntity<Media> editMedia (@Valid @RequestBody Media media) {
        Media body = mediaService.editMedia(media);
        mediaProjectWebSocketHandler.broadcast(media.getProject().getProjectId().toString());
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    @GetMapping("/request/{requestId}")
    @PreAuthorize(PM_IN_PROJECT)
    public ResponseEntity<List<RequestMediaProject>> getMediaForRequest (@PathVariable("requestId") UUID requestId) {
        try {
            List<RequestMediaProject> body = mediaService.getMediaForRequest(requestId);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/request/remove/{requestId}/{mediaID}")
    @PreAuthorize(USER_IN_PROJECT)
    public ResponseEntity<Media> addRemovedMediaToRequest (@PathVariable("requestId") UUID requestId,
                                                           @PathVariable("mediaId") UUID mediaId) {
        try{
            Media body = mediaService.addRemovedMediaToRequest(requestId, mediaId);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("request/add/{requestId}")
    @PreAuthorize(USER_IN_PROJECT)
    public ResponseEntity<Media> addAddedMediaToRequest (@PathVariable("requestId") UUID requestId,
                                                         @RequestParam("file") MultipartFile file,
                                                         @RequestParam String name) {
        try {
            Media body = mediaService.addAddedMediaToRequest(requestId, file, name);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (NotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
