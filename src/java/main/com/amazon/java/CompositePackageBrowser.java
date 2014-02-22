package com.amazon.java;

import java.util.List;

public class CompositePackageBrowser implements PackageBrowser {

    private final List<PackageBrowser> packageBrowsers;

    public CompositePackageBrowser(List<PackageBrowser> packageBrowsers) {
        this.packageBrowsers = packageBrowsers;
    }

    @Override
    public String find(String fqcn) {
        for (PackageBrowser browser : packageBrowsers) {
            final String content = browser.find(fqcn);
            if (content != null) {
                return content;
            }
        }

        return null;
    }
}
