package com.amazon.java;

import com.amazon.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DirectoryPackageBrowser implements PackageBrowser {

    private final File dir;

    public DirectoryPackageBrowser(File dir) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("should be a directory " + dir.getAbsolutePath());
        }

        this.dir = dir;
    }

    @Override
    public String find(String fqcn) {
        try {
            final String relativePath = fqcn.replaceAll("\\.", "" + File.separatorChar) + ".java";
            final File file = new File(dir, relativePath);
            if (!file.exists()) {
                return null;
            }
            try (InputStream is = new FileInputStream(file)) {
                return FileUtil.readToString(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("could not read file for " + fqcn, e);
        }
    }
}
