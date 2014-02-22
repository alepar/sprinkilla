package com.amazon;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtil {
    public static String readToString(InputStream is) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final InputStreamReader reader = new InputStreamReader(is);

        final char[] buf = new char[102400];
        int read;
        while((read = reader.read(buf)) != -1) {
            sb.append(buf, 0, read);
        }

        return sb.toString();
    }
}
