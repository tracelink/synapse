package com.tracelink.prodsec.synapse.encryption.controller;

import static org.mockito.Mockito.times;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.encryption.model.DataEncryptionKey;
import com.tracelink.prodsec.synapse.encryption.model.EncryptionMetadata;
import com.tracelink.prodsec.synapse.encryption.service.KeyRotationService;
import com.tracelink.prodsec.synapse.encryption.utils.EncryptionUtils;
import com.tracelink.prodsec.synapse.mvc.SynapseModelAndView;
import com.tracelink.prodsec.synapse.test.TestSynapseBootApplicationCore;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
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
public class EncryptionMgmtControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private KeyRotationService keyRotationService;

	private DataEncryptionKey dataEncryptionKey;
	private EncryptionMetadata encryptionMetadata;

	@Before
	public void init() {
		dataEncryptionKey = new DataEncryptionKey();
		dataEncryptionKey.setCurrentKey(EncryptionUtils.generateKey());
		dataEncryptionKey.setLastRotationDateTime(LocalDateTime.now().minusWeeks(2));

		encryptionMetadata = new EncryptionMetadata();
		encryptionMetadata.setRotationScheduleEnabled(true);
		encryptionMetadata.setRotationPeriod(90);
		encryptionMetadata.setLastRotationDateTime(LocalDateTime.now().minus(4, ChronoUnit.MONTHS));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testGetEncryptionMgmt() throws Exception {
		BDDMockito.when(keyRotationService.getKeys())
				.thenReturn(Collections.singletonList(dataEncryptionKey));
		BDDMockito.when(keyRotationService.getEncryptionMetadata())
				.thenReturn(encryptionMetadata);

		mockMvc.perform(MockMvcRequestBuilders.get("/encryption"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.model().attribute("contentViewName", "encryption"))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("scripts", Matchers.contains("/scripts/encryption.js")))
				.andExpect(MockMvcResultMatchers.model()
						.attribute("deks", Matchers.contains(dataEncryptionKey)))
				.andExpect(MockMvcResultMatchers.model().attribute("metadata", encryptionMetadata));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testRotateKeys() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/encryption/rotate")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/encryption"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, "Key rotation in progress"));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testRotateKeysSingleKey() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/encryption/rotate").param("keyId", "1")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/encryption"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH, "Key rotation in progress"));
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testEnableRotationScheduleSuccess() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/encryption/rotate/schedule")
				.param("enable", "false").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/encryption"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Successfully disabled rotation schedule"));

		BDDMockito.verify(keyRotationService, times(1)).enableRotationSchedule(false, null);
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testEnableRotationScheduleWithPeriod() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/encryption/rotate/schedule")
				.param("enable", "true").param("rotationPeriod", "120")
				.with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/encryption"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.SUCCESS_FLASH,
								"Successfully updated rotation schedule"));

		BDDMockito.verify(keyRotationService, times(1)).enableRotationSchedule(true, 120);
	}

	@Test
	@WithMockUser(authorities = {SynapseAdminAuthDictionary.ADMIN_PRIV})
	public void testEnableRotationScheduleFailure() throws Exception {
		BDDMockito.doThrow(new IllegalArgumentException("Invalid")).when(keyRotationService)
				.enableRotationSchedule(true, null);

		mockMvc.perform(MockMvcRequestBuilders.post("/encryption/rotate/schedule")
				.param("enable", "true").with(SecurityMockMvcRequestPostProcessors.csrf()))
				.andExpect(MockMvcResultMatchers.status().is3xxRedirection())
				.andExpect(MockMvcResultMatchers.redirectedUrl("/encryption"))
				.andExpect(MockMvcResultMatchers.flash()
						.attribute(SynapseModelAndView.FAILURE_FLASH,
								"Invalid"));

		BDDMockito.verify(keyRotationService, times(1)).enableRotationSchedule(true, null);
	}
}
