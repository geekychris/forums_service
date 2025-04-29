package com.example.forum.config;

import graphql.scalars.ExtendedScalars;
import graphql.schema.GraphQLScalarType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.execution.RuntimeWiringConfigurer;

/**
 * Configuration for GraphQL scalar types and other GraphQL-related settings.
 */
@Configuration
public class GraphQlConfig {

    /**
     * Register the DateTime scalar type.
     * This is used for date and time fields in the GraphQL schema.
     *
     * @return the DateTime scalar type
     */
    @Bean
    public GraphQLScalarType dateTimeScalar() {
        return ExtendedScalars.DateTime;
    }

    /**
     * Register the Long scalar type.
     * This is used for Long IDs and other numeric fields in the GraphQL schema.
     *
     * @return the Long scalar type
     */
    @Bean
    public GraphQLScalarType longScalar() {
        return ExtendedScalars.GraphQLLong;
    }

    /**
     * Configure the RuntimeWiring to register the scalar types.
     * This makes the scalar types available to the GraphQL schema.
     *
     * @return RuntimeWiringConfigurer for configuring the GraphQL schema
     */
    @Bean
    public RuntimeWiringConfigurer runtimeWiringConfigurer() {
        return wiringBuilder -> wiringBuilder
                .scalar(dateTimeScalar())
                .scalar(longScalar());
    }
}

