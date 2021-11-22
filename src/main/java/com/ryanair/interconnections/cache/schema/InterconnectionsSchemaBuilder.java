package com.ryanair.interconnections.cache.schema;

import com.ryanair.interconnections.api.model.adapter.LocalDateTimeAdapter;
import com.ryanair.interconnections.api.model.response.FlightLegResponse;
import com.ryanair.interconnections.api.model.response.FlightResponse;
import com.ryanair.interconnections.api.model.response.InterconnectionResponse;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(schemaPackageName = "flight", 
schemaFileName = "flight.proto", 
schemaFilePath = "proto/", 
includeClasses = {LocalDateTimeAdapter.class, FlightLegResponse.class, FlightResponse.class, InterconnectionResponse.class})
public interface InterconnectionsSchemaBuilder extends GeneratedSchema {}
