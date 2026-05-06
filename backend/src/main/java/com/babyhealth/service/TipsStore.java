package com.babyhealth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class TipsStore {

    private static final Logger log = LoggerFactory.getLogger(TipsStore.class);
    private static final int MAX_TIPS = 50;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Map<String, String>> tips = Collections.synchronizedList(new ArrayList<>());
    private File dataFile;

    @Value("${app.tips.file:}")
    private String tipsFilePath;

    @PostConstruct
    public void init() {
        try {
            if (tipsFilePath != null && !tipsFilePath.isBlank()) {
                dataFile = new File(tipsFilePath);
            } else {
                Path projectRoot = Path.of(System.getProperty("user.dir"));
                Path resourceTips = projectRoot
                        .resolve("src").resolve("main").resolve("resources").resolve("tips.json");
                if (Files.exists(resourceTips)) {
                    dataFile = resourceTips.toFile();
                    log.info("Using project resource tips.json: {}", dataFile.getAbsolutePath());
                } else {
                    Path dataDir = projectRoot.resolve("data");
                    Files.createDirectories(dataDir);
                    dataFile = dataDir.resolve("tips.json").toFile();
                    log.info("Using data dir tips.json: {}", dataFile.getAbsolutePath());
                }
            }

            if (dataFile.exists() && dataFile.length() > 0) {
                loadFromFile(dataFile);
                log.info("Loaded {} tips from {}", tips.size(), dataFile.getAbsolutePath());
            } else {
                loadFromClasspath();
                saveToFile();
                log.info("Initialized tips from classpath, {} tips saved to {}", tips.size(), dataFile.getAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("Failed to init tips store from file, falling back to classpath: {}", e.getMessage());
            loadFromClasspath();
        }
    }

    @SuppressWarnings("unchecked")
    private void parseTips(List<?> raw) {
        tips.clear();
        for (Object item : raw) {
            if (item instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) item;
                String fake = objToStr(m.get("fake"));
                String reveal = objToStr(m.get("reveal"));
                if (!fake.isBlank() && !reveal.isBlank()) {
                    tips.add(Map.of("fake", fake, "reveal", reveal));
                }
            }
        }
    }

    private String objToStr(Object val) {
        return val != null ? val.toString().trim() : "";
    }

    private void loadFromFile(File file) {
        try {
            List<?> raw = objectMapper.readValue(file, List.class);
            parseTips(raw);
        } catch (IOException e) {
            log.warn("Failed to read tips file: {}", e.getMessage());
        }
    }

    private void loadFromClasspath() {
        try {
            ClassPathResource res = new ClassPathResource("tips.json");
            try (InputStream is = res.getInputStream()) {
                List<?> raw = objectMapper.readValue(is, List.class);
                parseTips(raw);
            }
        } catch (IOException e) {
            log.warn("Failed to load tips from classpath: {}", e.getMessage());
        }
    }

    public synchronized void saveToFile() {
        try {
            File parent = dataFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, new ArrayList<>(tips));
            log.info("Saved {} tips to {}", tips.size(), dataFile.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save tips file: {}", e.getMessage(), e);
        }
    }

    public List<Map<String, String>> getAll() {
        return new ArrayList<>(tips);
    }

    public synchronized void merge(List<Map<String, String>> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            log.warn("merge() called with empty incoming list");
            return;
        }

        Set<String> existingFakes = new HashSet<>();
        for (Map<String, String> t : tips) {
            existingFakes.add(t.get("fake"));
        }

        List<Map<String, String>> unique = new ArrayList<>();
        for (Map<String, String> t : incoming) {
            String fake = t.get("fake");
            if (fake != null && !existingFakes.contains(fake)) {
                unique.add(t);
                existingFakes.add(fake);
            }
        }

        if (unique.isEmpty()) {
            log.info("No new unique tips to merge");
            return;
        }

        tips.addAll(0, unique);

        while (tips.size() > MAX_TIPS) {
            tips.remove(tips.size() - 1);
        }

        saveToFile();
        log.info("Merged {} new tips, total {}", unique.size(), tips.size());
    }
}
