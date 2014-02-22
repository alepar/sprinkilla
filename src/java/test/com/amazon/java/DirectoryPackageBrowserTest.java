package com.amazon.java;

import java.io.File;

import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class DirectoryPackageBrowserTest {

    private final PackageBrowser explorer = new DirectoryPackageBrowser(new File("resources/test/src"));

    @Test
    public void findsFileThatIsNotInsideZip() throws Exception {
        assertThat(explorer.find("java.lang.Number"), containsString("Number"));
        assertThat(explorer.find("java.lang.Integer"), containsString("Integer"));
    }

    @Test
    public void doesNotFindFileWhichIsNotInZip() throws Exception {
        assertThat(explorer.find("java.lang.NoSuchFileForSure"), nullValue());
    }
}
