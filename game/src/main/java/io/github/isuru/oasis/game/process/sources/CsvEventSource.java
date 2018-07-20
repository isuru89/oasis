package io.github.isuru.oasis.game.process.sources;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.JsonEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * @author iweerarathna
 */
public class CsvEventSource implements SourceFunction<Event> {

    private File sourceFile;

    public CsvEventSource(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    @Override
    public void run(SourceContext<Event> ctx) throws Exception {
        LineIterator lineIterator = FileUtils.lineIterator(sourceFile, StandardCharsets.UTF_8.name());
        String header = lineIterator.nextLine();
        String[] headerParts = header.split("[,]");

        while (lineIterator.hasNext()) {
            String line = lineIterator.nextLine();
            if (line.trim().length() <= 0) continue;
            if (line.startsWith("#")) continue;
            String[] parts = line.split("[,]");

            JsonEvent s = new CsvRowEvent();
            for (int j = 0; j < headerParts.length; j++) {
                String title = headerParts[j].substring(0, headerParts[j].lastIndexOf('-'));
                String suffix = headerParts[j].substring(headerParts[j].lastIndexOf('-') + 1);
                String value = parts[j];

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

            System.out.println("Pumping record " + s.getExternalId());
            ctx.collect(s);
        }

    }

    @Override
    public void cancel() {

    }

    public static class CsvRowEvent extends JsonEvent {
        @Override
        public String getEventType() {
            return "submission";
        }
    }
}
