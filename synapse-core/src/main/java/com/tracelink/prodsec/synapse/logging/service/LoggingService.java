package com.tracelink.prodsec.synapse.logging.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Handles all logic around Logging and loggers
 *
 * @author csmith
 */
@Service
public class LoggingService {

	private static final Logger LOG = LoggerFactory.getLogger(LoggingService.class);

	/**
	 * The display name of the core logger for Synapse
	 */
	public static final String SYNAPSE_LOGGER_NAME = "core";

	/**
	 * The name of the Synapse logger
	 */
	public static final String LOGGER_NAME = "com.tracelink.prodsec.synapse";

	public static final List<Level> ALLOWED_LEVELS = Collections
			.unmodifiableList(Arrays.asList(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR));

	private final Map<String, PluginLogger> loggers = new HashMap<>();

	public LoggingService() {
		PluginLogger coreLogger = new PluginLogger(SYNAPSE_LOGGER_NAME, LOGGER_NAME);
		this.registerLogger(coreLogger);
	}

	/**
	 * Registers the given plugin logger with this service. The logger name is used
	 * as a key.
	 *
	 * @param logger the logger to register
	 */
	public void registerLogger(PluginLogger logger) {
		this.loggers.put(logger.getName(), logger);
	}

	/**
	 * removes this logger from the cached loggers
	 * 
	 * @param loggerName the logger's name
	 */
	public void unregisterLogger(String loggerName) {
		this.loggers.remove(loggerName);
	}

	public PluginLogger getLogger(String pluginName) {
		return this.loggers.get(pluginName);
	}

	public Collection<String> getLoggers() {
		return this.loggers.keySet();
	}

	/**
	 * Zip all contents in the logs directory and create a zip file. Return the path
	 * to the zip
	 *
	 * @return the path to the zip of all logs
	 * @throws IOException if a filesystem error occurs
	 */
	public Path generateLogsZip() throws IOException {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		Path logDir = Paths.get(context.getProperty("LOGS_DIR"));
		Path target = Files.createTempFile(null, ".zip");
		zipLogs(logDir, target);
		return target;
	}

	private void zipLogs(Path logDir, Path target) throws IOException {
		try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(target))) {
			Files.walk(logDir).filter(path -> !Files.isDirectory(path)).forEach(path -> {
				ZipEntry zipEntry = new ZipEntry(logDir.relativize(path).toString());
				try {
					zs.putNextEntry(zipEntry);
					Files.copy(path, zs);
					zs.closeEntry();
				} catch (IOException e) {
					LOG.error("Error while zipping", e);
				}
			});
		}
	}

}
