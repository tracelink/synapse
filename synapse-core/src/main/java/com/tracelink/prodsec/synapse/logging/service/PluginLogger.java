package com.tracelink.prodsec.synapse.logging.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.read.CyclicBufferAppender;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * A Logger configuration for each plugin.
 * <p>
 * This class allows custom configuration of each plugin's logging as well as
 * the overall synapse app. Each instance has its own Log Level and buffer of
 * messages.
 *
 * @author csmith
 */
public class PluginLogger {

	private final String pluginName;
	private final int logsSize;

	private final Logger logger;
	private final CyclicBufferAppender<ILoggingEvent> cyclicAppender;
	private final LayoutWrappingEncoder<ILoggingEvent> encoder;

	private static final Logger LOG = (Logger) LoggerFactory.getLogger(PluginLogger.class);
	/**
	 * A reasonable default number of logs
	 */
	public static final int DEFAULT_LOGS_NUMBER = 256;

	/**
	 * The default pattern we use for logging
	 */
	public static final String DEFAULT_PATTERN_LAYOUT = "%d{yyyy-MM-dd HH:mm:ss} %logger{36} %-5level - %msg%n";

	public PluginLogger(String pluginName, String packageName) {
		this(pluginName, packageName, DEFAULT_PATTERN_LAYOUT, DEFAULT_LOGS_NUMBER, Level.INFO);
	}

	public PluginLogger(String pluginName, String packageName, String pattern, int logsSize,
			Level level) {
		this.pluginName = pluginName;
		String appenderName = "ROLLING-" + pluginName;
		this.logsSize = logsSize;

		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		this.logger = context.getLogger(packageName);

		this.cyclicAppender = new CyclicBufferAppender<>();
		this.cyclicAppender.setMaxSize(logsSize);
		this.cyclicAppender.setName(appenderName);
		if (!this.cyclicAppender.isStarted()) {
			this.cyclicAppender.start();
		}
		this.logger.addAppender(cyclicAppender);

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(this.logger.getLoggerContext());
		encoder.setPattern(pattern);
		if (!encoder.isStarted()) {
			encoder.start();
		}
		this.encoder = encoder;
		this.logger.setLevel(level);
	}

	public String getName() {
		return this.pluginName;
	}

	/**
	 * Get the most recent number of logs, up to the length of the appender as
	 * defined by its max size {@link PluginLogger#DEFAULT_LOGS_NUMBER}
	 *
	 * @param number the number of logs requested
	 * @return a list of logs of either number size or the most the queue has
	 */
	public List<String> getLogs(int number) {
		int length = cyclicAppender.getLength();
		int returnSize = Math.min(number, length);
		List<String> logs = new ArrayList<>(returnSize);
		for (int i = 1; i <= returnSize; i++) {
			logs.add(new String(encoder.encode(cyclicAppender.get(length - i))));
		}
		return logs;
	}

	/**
	 * Sets the logger level for the Synapse logger
	 *
	 * @param level the level to set to
	 */
	public void setLogsLevel(Level level) {
		if (!LoggingService.ALLOWED_LEVELS.contains(level)) {
			level = Level.INFO;
		}
		logger.setLevel(level);
		LOG.warn("Logger \"" + this.getName() + "\" updated to: " + level.levelStr);
	}

	/**
	 * Return up to {@linkplain PluginLogger#DEFAULT_LOGS_NUMBER}
	 *
	 * @return all of the most recent logs
	 */
	public List<String> getLogs() {
		return this.getLogs(this.logsSize);
	}

	/**
	 * Get the logger level for the Synapse logger
	 *
	 * @return the Synapse logger level
	 */
	public Level getLogLevel() {
		return logger.getLevel();
	}

}
