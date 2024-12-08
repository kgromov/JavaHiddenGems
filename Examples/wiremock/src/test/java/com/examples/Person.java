package com.examples;

import java.util.UUID;

public record Person(String id, String firstName, String lastName) {

    public Person( String firstName, String lastName) {
        this(UUID.randomUUID().toString(), firstName, lastName);
    }
}
