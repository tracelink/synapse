package com.tracelink.prodsec.synapse.util.bucketer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

public class BucketIntervalsTest {

	@Test
	public void testMonthIntervals() {
		LocalDateTime start = LocalDateTime.of(2020, 7, 12, 0, 0, 0).minusMonths(1);
		LocalDateTime end = start.plusWeeks(2);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM");
		MonthIntervals monthIntervals = new MonthIntervals(start, end);
		List<String> labels = monthIntervals.getLabels();
		Assert.assertEquals(1, labels.size());
		Assert.assertTrue(labels.get(0).contains(formatter.format(start)));

		monthIntervals = new MonthIntervals(LocalDateTime.of(2019, 12, 2, 0, 0, 0),
				LocalDateTime.of(2020, 1, 5, 0, 0, 0));
		labels = monthIntervals.getLabels();
		Assert.assertEquals(2, labels.size());
		Assert.assertEquals("Dec 2019", labels.get(0));
		Assert.assertEquals("Jan 2020", labels.get(1));
	}

	@Test
	public void testWeekIntervals() {
		LocalDateTime start = LocalDateTime.now().minusWeeks(1);
		LocalDateTime end = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");
		WeekIntervals weekIntervals = new WeekIntervals(start, end);
		List<String> labels = weekIntervals.getLabels();
		Assert.assertEquals(1, labels.size());
		Assert.assertTrue(labels.get(0).contains(formatter.format(start)));

		weekIntervals = new WeekIntervals(LocalDateTime.of(2019, 12, 29, 0, 0, 0),
				LocalDateTime.of(2020, 1, 5, 0, 0, 0));
		labels = weekIntervals.getLabels();
		Assert.assertEquals(1, labels.size());
		Assert.assertEquals("Dec 29 - Jan 04", labels.get(0));
	}

	@Test
	public void testDayIntervals() {
		LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
		LocalDateTime end = LocalDate.now().atStartOfDay();
		DayIntervals dayIntervals = new DayIntervals(start, end);
		List<String> labels = dayIntervals.getLabels();
		Assert.assertEquals(1, labels.size());
		Assert.assertEquals(start.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US),
				labels.get(0));

		dayIntervals = new DayIntervals(LocalDateTime.of(2019, 12, 29, 0, 0, 0),
				LocalDateTime.of(2020, 1, 1, 0, 0, 0));
		labels = dayIntervals.getLabels();
		Assert.assertEquals(3, labels.size());
		Assert.assertTrue(labels.contains("Sun"));
		Assert.assertTrue(labels.contains("Mon"));
		Assert.assertTrue(labels.contains("Tue"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void TestBucketIntervalsStartAfterEnd() {
		new MonthIntervals(LocalDateTime.now(), LocalDateTime.now().minusWeeks(1));
	}

	@Test
	public void testGetStart() {
		LocalDateTime start = LocalDateTime.now();
		MonthIntervals monthIntervals = new MonthIntervals(start, LocalDateTime.now().plusYears(1));
		Assert.assertEquals(start.toLocalDate().atStartOfDay(), monthIntervals.getStart());
	}

	@Test
	public void testGetEnd() {
		LocalDateTime end = LocalDateTime.now();
		WeekIntervals weekIntervals = new WeekIntervals(LocalDateTime.now().minusYears(1), end);
		Assert.assertEquals(end.toLocalDate().atStartOfDay(), weekIntervals.getEnd());
	}

	@Test
	public void testGetBuckets() {
		LocalDateTime start = LocalDateTime.now().minusDays(3);
		LocalDateTime end = LocalDateTime.now();
		DayIntervals dayIntervals = new DayIntervals(start, end);
		List<LocalDateTime> buckets = dayIntervals.getBuckets();
		Assert.assertEquals(4, buckets.size());
		Assert.assertTrue(buckets.get(0).isBefore(buckets.get(1)));
		Assert.assertTrue(buckets.get(1).isBefore(buckets.get(2)));
		Assert.assertTrue(buckets.get(2).isBefore(buckets.get(3)));
		Assert.assertEquals(0, buckets.get(0).compareTo(start.toLocalDate().atStartOfDay()));
		Assert.assertEquals(0, buckets.get(3).compareTo(end.toLocalDate().atStartOfDay()));
	}

}
