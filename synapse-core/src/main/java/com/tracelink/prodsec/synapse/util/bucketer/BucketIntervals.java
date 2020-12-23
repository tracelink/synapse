package com.tracelink.prodsec.synapse.util.bucketer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Represents the interval of time for "buckets" of data gathered from database.
 * Each instance has a start and an end date, and the time between those dates
 * will be subdivided into time periods with appropriate lengths. Additionally,
 * each instance has an interval function, to help divide the time period into
 * these buckets.
 *
 * @author mcool
 */
public abstract class BucketIntervals {

	private final LocalDateTime start;
	private final LocalDateTime end;
	private final Function<LocalDateTime, LocalDateTime> intervalFunction;

	BucketIntervals(LocalDateTime start, LocalDateTime end,
		Function<LocalDateTime, LocalDateTime> intervalFunction) {
		if (start.isAfter(end)) {
			throw new IllegalArgumentException("Start date cannot be after end date");
		}
		this.start = start;
		this.end = end;
		this.intervalFunction = intervalFunction;
	}

	protected LocalDateTime getStart() {
		return this.start;
	}

	protected LocalDateTime getEnd() {
		return this.end;
	}

	/**
	 * Gets a list of local date objects that subdivide the time period from the
	 * start this BucketIntervals object to the end of this BucketIntervals object
	 * into appropriate chunks.
	 *
	 * @return list of local date objects that divide a time period into smaller
	 * chunks
	 */
	public List<LocalDateTime> getBuckets() {
		List<LocalDateTime> buckets = new ArrayList<>();

		LocalDateTime bucketStart = start;
		while (bucketStart.compareTo(end) < 0) {
			// Add the start of this bucket to the list
			buckets.add(bucketStart);
			// Set up for next loop
			bucketStart = intervalFunction.apply(bucketStart);
		}
		// Add end to close off last bucket
		buckets.add(end);
		return buckets;
	}

	/**
	 * Gets a list of labels that represent the chunks of time that this
	 * BucketIntervals object is subdivided into. Labels span from the start this
	 * BucketIntervals object to the end of this BucketIntervals object.
	 *
	 * @return list of labels representing periods of time
	 */
	public abstract List<String> getLabels();
}
