package io.github.foiovituh.libys.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.foiovituh.libys.event.Event;
import java.io.File;

public class EventStore {
    private static final String SLASH = "/";
    private static final String JSON_EXTENSION = ".json";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String DIR = "data/events";

    public static void save(Event event) throws Exception {
        new File(DIR).mkdirs();
        final File file = newFile(event.id, JSON_EXTENSION);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, event);
        System.out.println("Event created: " + file.getPath());
    }

    public static Event load(String id) throws Exception {
        return MAPPER.readValue(newFile(id, JSON_EXTENSION), Event.class);
    }

    public static File newFile(String id, String extension) {
        new File(DIR).mkdirs();
        return new File(DIR + SLASH + id + extension);
    }
}