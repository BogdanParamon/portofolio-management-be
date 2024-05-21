package com.team2a.ProjectPortfolio.Controllers;

import com.team2a.ProjectPortfolio.Commons.Request;
import com.team2a.ProjectPortfolio.CustomExceptions.NotFoundException;
import com.team2a.ProjectPortfolio.Services.RequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class RequestControllerTest {

    @Mock
    private RequestService requestService;

    private RequestController sut;

    @BeforeEach
    void setup() {
        requestService = Mockito.mock(RequestService.class);
        sut = new RequestController(requestService);
    }
    @Test
    void testGetRequestsForUserOk() {

        Request r = new Request(UUID.randomUUID(), "title", "desc",
                "bib", true);
        when(requestService.getRequestsForUser("aa")).thenReturn(List.of(r));

        ResponseEntity<List<Request>> resp = sut.getRequestsForUser("aa");

        assertEquals(resp.getStatusCode(), HttpStatus.OK);
        assertEquals(resp.getBody(), List.of(r));
    }

    @Test
    void testGetRequestsForUserNotFound() {
        when(requestService.getRequestsForUser("aa")).thenThrow(NotFoundException.class);

        ResponseEntity<List<Request>> resp = sut.getRequestsForUser("aa");

        assertEquals(resp.getStatusCode(), HttpStatus.NOT_FOUND);

    }

    @Test
    void testGetRequests () {

        Request r = new Request(UUID.randomUUID(), "title", "description", "bibtex", false);
        when(requestService.getRequests()).thenReturn(List.of(r));

        ResponseEntity<List<Request>> res = sut.getRequests();

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(res.getBody(), List.of(r));
    }

    @Test
    void testGetRequestsForProjectNotFound () {
        UUID id1 = UUID.randomUUID();
        when(requestService.getRequestsForProject(id1)).thenThrow(new NotFoundException());
        ResponseEntity<List<Request>> res = sut.getRequestsForProject(id1);

        assertEquals(res.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetRequestsForProjectFound () {
        UUID id1 = UUID.randomUUID();
        Request r = new Request(UUID.randomUUID(), "title", "desc",
                "bib", true);
        when(requestService.getRequestsForProject(id1)).thenReturn(List.of(r));
        ResponseEntity<List<Request>> res = sut.getRequestsForProject(id1);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(res.getBody(), List.of(r));
    }

    @Test
    void testAddRequestOk() {

        Request r = new Request(UUID.randomUUID(), "title", "description", "bibtex", false);
        UUID id1 = UUID.randomUUID();
        when(requestService.addRequest(r, id1)).thenReturn(r);
        ResponseEntity<Request> res = sut.addRequest(id1, r);
        assertEquals(res.getStatusCode(), HttpStatus.CREATED);
        assertEquals(res.getBody(), r);
    }

    @Test
    void testAddRequestNotFound() {

        Request r = new Request(UUID.randomUUID(), "title", "description", "bibtex", false);
        UUID id1 = UUID.randomUUID();
        when(requestService.addRequest(r, id1)).thenThrow(NotFoundException.class);
        ResponseEntity<Request> response = sut.addRequest(id1, r);
        assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND);

    }

    @Test
    void testDeleteRequestNotFound () {
        UUID id1 = UUID.randomUUID();
        doThrow(NotFoundException.class).when(requestService).deleteRequest(id1);
        ResponseEntity<Void> res = sut.deleteRequest(id1);
        assertEquals(res.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteRequestOk () {
        UUID id1 = UUID.randomUUID();

        doNothing().when(requestService).deleteRequest(id1);

        ResponseEntity<Void> res = sut.deleteRequest(id1);
        assertEquals(res.getStatusCode(), HttpStatus.NO_CONTENT);
    }

}