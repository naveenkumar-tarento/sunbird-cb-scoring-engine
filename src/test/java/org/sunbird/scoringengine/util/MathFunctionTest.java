package org.sunbird.scoringengine.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MathFunctionTest {

    private final List<Double> values = Arrays.asList(1.0, 2.0, 3.0);

    @Test
    public void sumReturnsTotalOfValues() {
        assertEquals(6.0, MathFunction.sum(values), 0.0001);
    }

    @Test
    public void minReturnsSmallestValue() {
        assertEquals(1.0, MathFunction.min(values).getAsDouble(), 0.0001);
    }

    @Test
    public void maxReturnsLargestValue() {
        assertEquals(3.0, MathFunction.max(values).getAsDouble(), 0.0001);
    }

    @Test
    public void meanReturnsAverageOfValues() {
        assertEquals(2.0, MathFunction.mean(values).getAsDouble(), 0.0001);
    }

    @Test
    public void weightedAvgMultipliesSumByWeightage() {
        assertEquals(3.0, MathFunction.weightedAvg(values, 0.5), 0.0001);
    }

    @Test
    public void maxWeightedAvgWithTotalMaxValueMultipliesByWeightage() {
        assertEquals(5.0, MathFunction.maxWeightedAvg(10.0, 0.5), 0.0001);
    }

    @Test
    public void minWeightedAvgWithTotalMinValueMultipliesByWeightage() {
        assertEquals(2.0, MathFunction.minWeightedAvg(4.0, 0.5), 0.0001);
    }

    @Test
    public void maxWeightedAvgWithListUsesMaxTimesCountTimesWeightage() {
        assertEquals(4.5, MathFunction.maxWeightedAvg(values, 0.5), 0.0001);
    }

    @Test
    public void minWeightedAvgWithListUsesMaxTimesCountTimesWeightage() {
        assertEquals(4.5, MathFunction.minWeightedAvg(values, 0.5), 0.0001);
    }

    @Test
    public void weightedScoreComputesPercentageOfMax() {
        assertEquals(25.0, MathFunction.weightedScore(5.0, 10.0, 0.5), 0.0001);
    }

    @Test
    public void weightedScoreReturnsZeroWhenMaxValueIsZero() {
        assertEquals(0.0, MathFunction.weightedScore(5.0, 0.0, 0.5), 0.0001);
    }
}
