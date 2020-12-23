package com.tracelink.prodsec.synapse.util.bucketer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.junit.Assert;
import org.junit.Test;

public class SimpleBucketerTest {

	@Test
	public void testGetLabelsDays() {
		SimpleBucketer<LocalDateTime> bucketer = new SimpleBucketer<>("last-week",
			LocalDateTime::now, Function.identity());
		BucketIntervals intervals = bucketer.getBucketIntervals();
		Assert.assertTrue(intervals instanceof DayIntervals);
	}

	@Test
	public void testGetLabelsWeeks() {
		SimpleBucketer<LocalDateTime> bucketer = new SimpleBucketer<>("last-four-weeks",
			LocalDateTime::now, Function.identity());
		BucketIntervals intervals = bucketer.getBucketIntervals();
		Assert.assertTrue(intervals instanceof WeekIntervals);
	}

	@Test
	public void testGetLabelsMonths() {
		SimpleBucketer<LocalDateTime> bucketer = new SimpleBucketer<>("last-six-months",
			LocalDateTime::now, Function.identity());
		BucketIntervals intervals = bucketer.getBucketIntervals();
		Assert.assertTrue(intervals instanceof MonthIntervals);
	}

	@Test
	public void testGetLabelsAllTime() {
		SimpleBucketer<LocalDateTime> bucketer = new SimpleBucketer<>("all-time",
			() -> LocalDateTime.now().minusMonths(2), Function.identity());
		BucketIntervals intervals = bucketer.getBucketIntervals();
		Assert.assertTrue(intervals instanceof MonthIntervals);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSimpleBucketerInvalidPeriod() {
		new SimpleBucketer<>("foo", LocalDateTime::now, Function.identity());
	}

	@Test
	public void testPutItemsInBuckets() {
		SimpleBucketer<LocalDateTime> bucketer = new SimpleBucketer<>("last-week",
			LocalDateTime::now, Function
			.identity());
		List<List<LocalDateTime>> bucketedItems = bucketer
			.putItemsInBuckets(Collections.singletonList(LocalDateTime.now().minusDays(3)));
		Assert.assertEquals(7, bucketedItems.size());
		Assert.assertFalse(bucketedItems.get(3).isEmpty());
		Assert
			.assertEquals(bucketedItems.get(3).get(0).toLocalDate(), LocalDate.now().minusDays(3));

	}

}
