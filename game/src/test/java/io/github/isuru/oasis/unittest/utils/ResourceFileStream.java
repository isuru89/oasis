package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.Event;
import io.github.isuru.oasis.EventSource;
import io.github.isuru.oasis.JsonEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ResourceFileStream implements EventSource<Event>, Serializable {

    private List<String> csvFiles;
    private boolean cancel = false;
    private boolean orderByTs = true;

    public ResourceFileStream(Collection<String> csvFile) {
        this.csvFiles = new LinkedList<>(csvFile);
    }

    public ResourceFileStream(String csvFile, boolean orderByTS) {
        this.csvFiles = Collections.singletonList(csvFile);
        this.orderByTs = orderByTS;
    }

    @Override
    public void run(SourceContext<Event> ctx) throws Exception {
        for (String csvFile : csvFiles) {
            LineIterator lineIterator = IOUtils.lineIterator(TestUtils.loadResource(csvFile), StandardCharsets.UTF_8);
            String header = lineIterator.next();
            String[] headerParts = header.split("[,]");

            List<JsonEvent> events = new ArrayList<>();
            while (!cancel && lineIterator.hasNext()) {

                String line = lineIterator.next();
                if (line.trim().length() <= 0) continue;
                if (line.startsWith("#")) continue;
                String[] parts = line.split("[,]");

                JsonEvent s = new JsonEvent();
                for (int i = 0; i < headerParts.length; i++) {
                    String title = headerParts[i].substring(0, headerParts[i].lastIndexOf('-'));
                    String suffix = headerParts[i].substring(headerParts[i].lastIndexOf('-') + 1);
                    String value = parts[i];

                    if (suffix.equals("s")) {
                        s.put(title, value);
                    } else if (suffix.equals("i")) {
                        s.put(title, value.isEmpty() ? -1 : Integer.parseInt(value));
                    } else if (suffix.equals("l")) {
                        s.put(title, value.isEmpty() ? -1L : Long.parseLong(value));
                    } else if (suffix.equals("b")) {
                        s.put(title, Boolean.parseBoolean(value));
                    } else if (suffix.equals("t")) {
                        s.put(title, Instant.parse(value).toEpochMilli());
                    } else {
                        throw new Exception("Unknown value type!");
                    }
                }

                //System.out.println("Pumping record " + s.getRefId());
                if (!orderByTs) {
                    ctx.collect(s);
                } else {
                    events.add(s);
                }
            }

            if (orderByTs) {
                events.sort(Comparator.comparingLong(JsonEvent::getTimestamp));
                for (JsonEvent e : events) {
                    ctx.collect(e);
                }
            }
        }
        System.out.println("All records pumped!");
    }

    @Override
    public void cancel() {

    }
}
