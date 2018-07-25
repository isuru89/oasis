package io.github.isuru.oasis.unittest.utils;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.JsonEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * @author iweerarathna
 */
public class CsvReaderPT implements SourceFunction<Event> {

    private String csvFile;
    private volatile boolean cancel;

    public CsvReaderPT(String csvFile) {
        this.csvFile = csvFile;
    }

    @Override
    public void run(SourceContext<Event> ctx) throws Exception {
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

            s.put("ts", System.currentTimeMillis());
            long delay = (long) s.get("delay");
            System.out.println("Emitting event #" + s.get("id") + " at " + s.get("ts"));
            ctx.collect(s);
            Thread.sleep(delay);
        }
    }

    @Override
    public void cancel() {
        System.out.println("Cancel called");
        cancel = true;
    }
}
