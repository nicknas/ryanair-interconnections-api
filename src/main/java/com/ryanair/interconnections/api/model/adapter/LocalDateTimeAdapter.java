package com.ryanair.interconnections.api.model.adapter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(LocalDateTime.class)
public class LocalDateTimeAdapter {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @ProtoFactory
    LocalDateTime create(String text) {
        return LocalDateTime.parse(text, formatter);
    }

    @ProtoField(number = 1, required = true, defaultValue = "0")
    String text(LocalDateTime localDateTime) {
        return localDateTime.format(formatter);
    }
}
