package org.sunbird.scoringengine.schema.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RangeTest {

    @Test
    public void getterSettersRoundTripAllFields() {
        Range range = new Range();
        range.setName("10-20");
        range.setMin(10);
        range.setMax(20);
        range.setAssignedValue(5);

        assertEquals("10-20", range.getName());
        assertEquals(Integer.valueOf(10), range.getMin());
        assertEquals(Integer.valueOf(20), range.getMax());
        assertEquals(Integer.valueOf(5), range.getAssignedValue());
    }
}
