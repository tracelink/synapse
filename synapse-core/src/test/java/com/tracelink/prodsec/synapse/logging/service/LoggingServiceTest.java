package com.tracelink.prodsec.synapse.logging.service;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import ch.qos.logback.classic.LoggerContext;

@RunWith(SpringRunner.class)
public class LoggingServiceTest {

	@Test
	public void testRegisterGetLifecycle() {
		String loggerName = "logName";
		PluginLogger mockLogger = BDDMockito.mock(PluginLogger.class);
		BDDMockito.when(mockLogger.getName()).thenReturn(loggerName);

		LoggingService logService = new LoggingService();
		logService.registerLogger(mockLogger);
		MatcherAssert.assertThat(logService.getLogger(loggerName), Matchers.is(mockLogger));
		MatcherAssert.assertThat(logService.getLoggers(), Matchers.hasItem(loggerName));
	}

	@Test
	public void generateLogsTest() throws Exception {
		String logName = "log.txt";
		Path logDir = Files.createTempDirectory(null);
		String logMessage = "logMessage";
		Files.write(logDir.resolve(logName), logMessage.getBytes());

		try {
			((LoggerContext) LoggerFactory.getILoggerFactory()).putProperty("LOGS_DIR", logDir.toString());
			LoggingService logService = new LoggingService();
			Path logFileZipped = logService.generateLogsZip();
			ZipInputStream zis = new ZipInputStream(new FileInputStream(logFileZipped.toFile()));
			ZipEntry entry = zis.getNextEntry();

			MatcherAssert.assertThat(zis.getNextEntry(), Matchers.nullValue());
			MatcherAssert.assertThat(entry.getName(), Matchers.is(logName));
			zis.close();
		} finally {
			FileUtils.deleteDirectory(logDir.toFile());
		}
	}
}
