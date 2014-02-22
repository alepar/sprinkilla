package com.amazon.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.amazon.FileUtil;

public class ZipFilePackageBrowser implements PackageBrowser {

    private final ZipFile zipFile;
    private final Map<String,ZipEntry> zipEntries;

    public ZipFilePackageBrowser(File file) {
        if(!file.canRead()) {
            throw new IllegalArgumentException("unreadable zip file " + file.getAbsolutePath());
        }
        try {
            zipFile = new ZipFile(file);
            this.zipEntries = readEntries(zipFile);
        } catch (IOException e) {
            throw new RuntimeException("could not read zip entries from " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public String find(String fqcn) {
        final ZipEntry zipEntry = zipEntries.get(fqcn.replaceAll("\\.", "" + File.separatorChar) + ".java");
        if (zipEntry == null) {
            return null;
        }
        try {
            try (InputStream is = zipFile.getInputStream(zipEntry)) {
                return FileUtil.readToString(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("could not read zip entry " + zipEntry.getName(), e);
        }
    }

    private static Map<String,ZipEntry> readEntries(ZipFile zipFile) {
        final Map<String,ZipEntry> zipEntries = new HashMap<>();
        final Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            final ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().endsWith(".java")) {
                zipEntries.put(zipEntry.getName(), zipEntry);
            }
        }
        return zipEntries;
    }
}
