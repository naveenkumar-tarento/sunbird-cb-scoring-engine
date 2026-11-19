package org.sunbird.scoringengine.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BadRequestExceptionTest {

    @Test
    public void messagePassedToConstructorIsReturnedByGetMessage() {
        BadRequestException exception = new BadRequestException("bad request");

        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void setMessageOverridesConstructorMessage() {
        BadRequestException exception = new BadRequestException("bad request");

        exception.setMessage("updated message");

        assertEquals("updated message", exception.getMessage());
    }
}
