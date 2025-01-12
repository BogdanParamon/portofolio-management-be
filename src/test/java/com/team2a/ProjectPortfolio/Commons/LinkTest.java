package com.team2a.ProjectPortfolio.Commons;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LinkTest {

    @Test
    void testConstructor() {
        UUID id1 = UUID.randomUUID();
        Link l = new Link("name", "url");
        assertEquals(l.getName(), "name");
        assertEquals(l.getUrl(), "url");
        assertEquals(l.getRequestLinkProjects(), new ArrayList<>());
    }

}