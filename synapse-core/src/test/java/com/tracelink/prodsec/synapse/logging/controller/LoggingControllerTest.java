package com.tracelink.prodsec.synapse.logging.controller;

import ch.qos.logback.classic.Level;
import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.logging.service.LoggingService;
import com.tracelink.prodsec.synapse.logging.service.PluginLogger;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplicationCore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestSynapseBootApplicationCore.class)
@AutoConfigureMockMvc
public class LoggingControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private LoggingService mockLogsService;

	@Mock
	private PluginLogger mockLogger;

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testLoggerViewNoParam() throws Exception {
		List<String> logs = new ArrayList<>();
		Level level = Level.INFO;
		BDDMockito.when(mockLogsService.getLogger(BDDMockito.anyString())).thenReturn(mockLogger);
		BDDMockito.when(mockLogger.getLogLevel()).thenReturn(level);

		mockMvc.perform(MockMvcRequestBuilders.get("/logging"))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("currentLogLevel", Matchers.is(level)))
				.andExpect(MockMvcResultMatchers.model().attribute("logs", Matchers.is(logs)));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testLoggerViewWithParam() throws Exception {
		List<String> logs = new ArrayList<>();
		Level level = Level.INFO;
		String loggerName = "foo";
		BDDMockito.when(mockLogsService.getLogger(loggerName)).thenReturn(mockLogger);
		BDDMockito.when(mockLogger.getLogLevel()).thenReturn(level);

		mockMvc.perform(MockMvcRequestBuilders.get("/logging").param("logger", loggerName))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("currentLogLevel", Matchers.is(level)))
				.andExpect(MockMvcResultMatchers.model().attribute("logs", Matchers.is(logs)));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testLoggerViewWithBadParam() throws Exception {
		List<String> logs = new ArrayList<>();
		Level level = Level.INFO;
		String loggerName = "foo";
		BDDMockito.when(mockLogsService.getLogger(loggerName)).thenReturn(null);
		BDDMockito.when(mockLogsService.getLogger(LoggingService.SYNAPSE_LOGGER_NAME))
				.thenReturn(mockLogger);
		BDDMockito.when(mockLogger.getLogLevel()).thenReturn(level);

		mockMvc.perform(MockMvcRequestBuilders.get("/logging").param("logger", loggerName))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("currentLogLevel", Matchers.is(level)))
				.andExpect(MockMvcResultMatchers.model().attribute("logs", Matchers.is(logs)));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testLogSetSuccess() throws Exception {
		Level level = Level.DEBUG;
		BDDMockito.when(mockLogsService.getLogger(BDDMockito.anyString())).thenReturn(mockLogger);

		mockMvc.perform(
				MockMvcRequestBuilders.post("/logging/set").param("loglevel", level.levelStr)
						.with(SecurityMockMvcRequestPostProcessors.csrf()));

		ArgumentCaptor<Level> levelCaptor = ArgumentCaptor.forClass(Level.class);
		BDDMockito.verify(mockLogger).setLogsLevel(levelCaptor.capture());

		MatcherAssert.assertThat(levelCaptor.getValue(), Matchers.is(level));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testLogSetBadLogger() throws Exception {
		Level level = Level.DEBUG;
		String loggerName = "foo";
		BDDMockito.when(mockLogsService.getLogger(loggerName)).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.post("/logging/set").param("logger", loggerName)
				.param("loglevel", level.levelStr)
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.redirectedUrl("/logging"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								Matchers.is("Unknown logger")));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDownloadLogsSuccess() throws Exception {
		byte[] content = new byte[512];
		new Random().nextBytes(content);
		Path temp = Files.createTempFile(null, null);
		Files.write(temp, content);

		BDDMockito.when(mockLogsService.generateLogsZip()).thenReturn(temp);

		mockMvc.perform(MockMvcRequestBuilders.get("/logging/download"))
				.andExpect(MockMvcResultMatchers.content().bytes(content));
	}

	@Test
	@WithMockUser(authorities = SynapseAdminAuthDictionary.ADMIN_PRIV)
	public void testDownloadLogsException() throws Exception {
		BDDMockito.doThrow(IOException.class).when(mockLogsService).generateLogsZip();

		mockMvc.perform(MockMvcRequestBuilders.get("/logging/download"))
				.andExpect(MockMvcResultMatchers.status().is4xxClientError())
				.andExpect(MockMvcResultMatchers.content()
						.string(Matchers.containsString("Exception")));
	}
}
