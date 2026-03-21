package it.unibs.ingsoft.v3.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class Fruitore extends Persona
{
    @JsonCreator
    public Fruitore(@JsonProperty("username") String username)
    {
        super(username);
    }
}
