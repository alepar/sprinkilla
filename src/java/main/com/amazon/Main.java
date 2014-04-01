package com.amazon;

import com.amazon.java.CompositePackageBrowser;
import com.amazon.java.DirectoryPackageBrowser;
import com.amazon.java.PackageBrowser;
import com.amazon.java.ZipFilePackageBrowser;
import com.amazon.spring.BeanDefinition;
import com.amazon.spring.parser.FrameworkSpringBeanParser;
import com.amazon.spring.parser.SpringBeanParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        final String[] packageDirs = new String[] {
            "/workplace/parfenov/Fenix/src/FenixBusinessLogic",
            "/workplace/parfenov/Fenix/src/FenixCTREstimator",
            "/workplace/parfenov/Fenix/src/FenixCacheClient",
            "/workplace/parfenov/Fenix/src/FenixClientModel",
            "/workplace/parfenov/Fenix/src/FenixDecorationStore",
            "/workplace/parfenov/Fenix/src/FenixDecorators",
            "/workplace/parfenov/Fenix/src/FenixFramework",
            "/workplace/parfenov/Fenix/src/FenixMetrics",
            "/workplace/parfenov/Fenix/src/FenixModel",
            "/workplace/parfenov/Fenix/src/FenixProductAds",
            "/workplace/parfenov/Fenix/src/FenixRequestDecorators",
            "/workplace/parfenov/Fenix/src/FenixService",
        };
        final String jdkSource = "/usr/lib/jvm/java-7-openjdk-i386/src.zip";
        final String xmlFolder = "/workplace/parfenov/Fenix/src/FenixService/spring-configuration/application";

        final PackageBrowser packageBrowser = createPackageBrowser(packageDirs, jdkSource);

        final Map<String, String> springFiles = readSpringFiles(xmlFolder);

        final Map<String, BeanDefinition> springBeans = new HashMap<>();
        final SpringBeanParser springParser = new FrameworkSpringBeanParser();
        BeanDefinition parse;
        for (Map.Entry<String, String> entry : springFiles.entrySet()) {
            try {
                while((parse = springParser.parse(entry.getValue())) != null) {
                    System.out.println(parse);
                }
            } catch (Exception e) {
                throw new RuntimeException("failed to parse " + entry.getKey(), e);
            }
        }
    }

    private static Map<String, String> readSpringFiles(String xmlFolder) {
        final Map<String, String> springFiles = new HashMap<>();
        final File xmlDir = new File(xmlFolder);
        final String[] list = xmlDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".xml");
            }
        });
        for (String name : list) {
            try {
                try (InputStream is = new FileInputStream(new File(xmlDir, name))) {
                    final String xmlContent = FileUtil.readToString(is);
                    springFiles.put(name, xmlContent);
                }
            } catch (Exception e) {
                throw new RuntimeException("failed to read xml file "+ name, e);
            }
        }
        return springFiles;
    }

    private static PackageBrowser createPackageBrowser(String[] packageDirs, String jdkSource) {
        final List<PackageBrowser> packageBrowsers = new ArrayList<>(packageDirs.length+1);
        for (String packageDir : packageDirs) {
            packageBrowsers.add(new DirectoryPackageBrowser(new File(packageDir, "src")));
        }
        packageBrowsers.add(new ZipFilePackageBrowser(new File(jdkSource)));

        return new CompositePackageBrowser(packageBrowsers);
    }


}
