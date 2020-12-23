package com.tracelink.prodsec.synapse.logging.service;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

@RunWith(SpringRunner.class)
public class PluginLoggerTest {

	private static class PluginLoggerTestTarget {
		private static final Logger LOG = (Logger) LoggerFactory.getLogger(PluginLoggerTestTarget.class);

		void logInfoMessage(String s) {
			LOG.info(s);
		}
	}

	@Test
	public void getNameTest() {
		String loggerName = "customLogger";
		PluginLogger logger = new PluginLogger(loggerName, PluginLoggerTestTarget.class.getPackage().getName());
		MatcherAssert.assertThat(logger.getName(), Matchers.is(loggerName));
	}

	@Test
	public void logAndResetLevelTest() {
		String loggerName = "resetLogger";
		PluginLogger logger = new PluginLogger(loggerName, PluginLoggerTestTarget.class.getName());
		MatcherAssert.assertThat(logger.getLogLevel(), Matchers.is(Level.INFO));
		PluginLoggerTestTarget target = new PluginLoggerTestTarget();
		String message1 = "firstMessage";
		target.logInfoMessage(message1);
		MatcherAssert.assertThat(logger.getLogs(), Matchers.hasItem(Matchers.containsString(message1)));
		logger.setLogsLevel(Level.ERROR);
		MatcherAssert.assertThat(logger.getLogLevel(), Matchers.is(Level.ERROR));
		String message2 = "secondMessage";
		target.logInfoMessage(message2);
		MatcherAssert.assertThat(logger.getLogs(), Matchers.hasItem(Matchers.containsString(message1)));
		MatcherAssert.assertThat(logger.getLogs(), Matchers.not(Matchers.hasItem(Matchers.containsString(message2))));
	}

}
