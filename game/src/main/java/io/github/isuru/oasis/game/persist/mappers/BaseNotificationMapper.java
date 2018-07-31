package io.github.isuru.oasis.game.persist.mappers;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author iweerarathna
 */
abstract class BaseNotificationMapper<E> implements MapFunction<E, String> {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

}
