package com.tracelink.prodsec.synapse.util.bucketer;

import java.time.LocalDateTime;
import java.util.function.Supplier;

/**
 * Abstract implementation of the {@link AbstractBucketer} that implements {@link
 * AbstractBucketer#getBucketIntervals()} and constructs a {@link BucketIntervals} from a set of
 * standard options.
 * <p>
 * This class does not implement {@link AbstractBucketer#itemBelongsInBucket(Object,
 * LocalDateTime, LocalDateTime)}, which is left for concrete classes to implement.
 *
 * @param <T> type of the items to be put into buckets
 * @author mcool
 */
public abstract class StandardIntervalBucketer<T> extends AbstractBucketer<T> {

	private final BucketIntervals bucketIntervals;

	/**
	 * Constructs an instance of this bucketer using the given string representing a period of
	 * time. Also instantiates a {@link BucketIntervals} to represent days for the past week, weeks
	 * for the past four weeks, months for the past six months, or months since the earliest {@link
	 * LocalDateTime}, which is supplied by the given {@link Supplier}. The supplier will only be
	 * executed if the given period is 'all-time'.
	 *
	 * @param timePeriod               string representing the time period for the {@link
	 *                                 BucketIntervals}
	 * @param earliestDateTimeSupplier supplier function to return the earliest date for the {@link
	 *                                 BucketIntervals} in the case of 'all-time'
	 * @throws IllegalArgumentException if the given time period does not match one of the standard
	 *                                  options
	 */
	public StandardIntervalBucketer(String timePeriod,
		Supplier<LocalDateTime> earliestDateTimeSupplier) throws IllegalArgumentException {
		switch (timePeriod) {
			case "last-week":
				bucketIntervals = new DayIntervals(LocalDateTime.now().minusDays(6),
					LocalDateTime.now().plusDays(1));
				break;
			case "last-four-weeks":
				bucketIntervals = new WeekIntervals(LocalDateTime.now().minusDays(27),
					LocalDateTime.now().plusDays(1));
				break;
			case "last-six-months":
				bucketIntervals = new MonthIntervals(
					LocalDateTime.now().withDayOfMonth(1).minusMonths(5),
					LocalDateTime.now().plusDays(1));
				break;
			case "all-time":
				bucketIntervals = new MonthIntervals(
					earliestDateTimeSupplier.get().withDayOfMonth(1),
					LocalDateTime.now().plusDays(1));
				break;
			default:
				throw new IllegalArgumentException("Unknown time period.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public BucketIntervals getBucketIntervals() {
		return bucketIntervals;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract boolean itemBelongsInBucket(T item, LocalDateTime bucketStart,
		LocalDateTime bucketEnd);
}
