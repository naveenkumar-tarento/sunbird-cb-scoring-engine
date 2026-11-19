package org.sunbird.scoringengine.exception;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ApplicationServiceErrorTest {

    @Test
    public void messagePassedToConstructorIsReturnedByGetMessage() {
        ApplicationServiceError error = new ApplicationServiceError("service error");

        assertEquals("service error", error.getMessage());
    }

    @Test
    public void setMessageOverridesConstructorMessage() {
        ApplicationServiceError error = new ApplicationServiceError("service error");

        error.setMessage("updated message");

        assertEquals("updated message", error.getMessage());
    }
}
