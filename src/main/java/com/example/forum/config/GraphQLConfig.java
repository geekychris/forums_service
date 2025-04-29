package com.example.forum.config;

import graphql.language.StringValue;
import graphql.schema.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Configuration
public class GraphQLConfig {
    
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
            .scalar(localDateTimeScalar())
            .scalar(GraphQLLong());
    }
    
    @Bean
    public GraphQLScalarType localDateTimeScalar() {
        return GraphQLScalarType.newScalar()
            .name("DateTime")
            .description("Java LocalDateTime scalar")
            .coercing(new Coercing<LocalDateTime, String>() {
                @Override
                public String serialize(Object dataFetcherResult) throws CoercingSerializeException {
                    if (dataFetcherResult instanceof LocalDateTime) {
                        return ((LocalDateTime) dataFetcherResult).format(DateTimeFormatter.ISO_DATE_TIME);
                    } else {
                        throw new CoercingSerializeException(
                            "Expected a LocalDateTime object but was " + 
                            (dataFetcherResult == null ? "null" : dataFetcherResult.getClass().getName())
                        );
                    }
                }

                @Override
                public LocalDateTime parseValue(Object input) throws CoercingParseValueException {
                    try {
                        if (input instanceof String) {
                            return LocalDateTime.parse((String) input, DateTimeFormatter.ISO_DATE_TIME);
                        }
                        throw new CoercingParseValueException(
                            "Expected a String but was " + 
                            (input == null ? "null" : input.getClass().getName())
                        );
                    } catch (DateTimeParseException e) {
                        throw new CoercingParseValueException(
                            "Error parsing date-time: " + e.getMessage(), e
                        );
                    }
                }

                @Override
                public LocalDateTime parseLiteral(Object input) throws CoercingParseLiteralException {
                    if (input instanceof StringValue) {
                        try {
                            return LocalDateTime.parse(((StringValue) input).getValue(), DateTimeFormatter.ISO_DATE_TIME);
                        } catch (DateTimeParseException e) {
                            throw new CoercingParseLiteralException(
                                "Error parsing date-time: " + e.getMessage(), e
                            );
                        }
                    }
                    throw new CoercingParseLiteralException(
                        "Expected a StringValue but was " + 
                        (input == null ? "null" : input.getClass().getName())
                    );
                }
            })
            .build();
    }
    
    @Bean
    public GraphQLScalarType GraphQLLong() {
        return GraphQLScalarType.newScalar()
            .name("Long")
            .description("Java Long scalar")
            .coercing(new Coercing<Long, Long>() {
                @Override
                public Long serialize(Object dataFetcherResult) throws CoercingSerializeException {
                    if (dataFetcherResult instanceof Long) {
                        return (Long) dataFetcherResult;
                    } else if (dataFetcherResult instanceof Number) {
                        return ((Number) dataFetcherResult).longValue();
                    } else if (dataFetcherResult instanceof String) {
                        try {
                            return Long.parseLong((String) dataFetcherResult);
                        } catch (NumberFormatException e) {
                            throw new CoercingSerializeException(
                                "Invalid Long value: " + dataFetcherResult, e
                            );
                        }
                    }
                    throw new CoercingSerializeException(
                        "Expected type 'Long' but was '" + 
                        (dataFetcherResult == null ? "null" : dataFetcherResult.getClass().getName()) + "'"
                    );
                }

                @Override
                public Long parseValue(Object input) throws CoercingParseValueException {
                    if (input instanceof Long) {
                        return (Long) input;
                    } else if (input instanceof Number) {
                        return ((Number) input).longValue();
                    } else if (input instanceof String) {
                        try {
                            return Long.parseLong((String) input);
                        } catch (NumberFormatException e) {
                            throw new CoercingParseValueException(
                                "Invalid Long value: " + input, e
                            );
                        }
                    }
                    throw new CoercingParseValueException(
                        "Expected type 'Long' but was '" + 
                        (input == null ? "null" : input.getClass().getName()) + "'"
                    );
                }

                @Override
                public Long parseLiteral(Object input) throws CoercingParseLiteralException {
                    if (input instanceof StringValue) {
                        try {
                            return Long.parseLong(((StringValue) input).getValue());
                        } catch (NumberFormatException e) {
                            throw new CoercingParseLiteralException(
                                "Invalid Long value: " + ((StringValue) input).getValue(), e
                            );
                        }
                    }
                    throw new CoercingParseLiteralException(
                        "Expected a StringValue but was " + 
                        (input == null ? "null" : input.getClass().getName())
                    );
                }
            })
            .build();
    }
}

