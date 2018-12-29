package io.github.isuru.oasis.game.factory.badges;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.util.function.Function;

class TimeConverterFunction implements Function<Long, String>, Serializable {

    @Override
    public String apply(Long aLong) {
        return Instant.ofEpochMilli(aLong).atZone(ZoneId.of("UTC")).toLocalDate().toString();
    }
}
