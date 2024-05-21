package com.team2a.ProjectPortfolio.Commons;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MediaTest {

    @Test
    void testConstructor() {
        Project p = new Project("title", "description", "bibtex", false);
        Media m = new Media(p, "name", "path");
        assertEquals(m.getProject(), p);
        assertEquals(m.getName(), "name");
        assertEquals(m.getPath(), "path");
    }

}