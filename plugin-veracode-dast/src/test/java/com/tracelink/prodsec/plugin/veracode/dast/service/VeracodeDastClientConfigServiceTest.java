package com.tracelink.prodsec.plugin.veracode.dast.service;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastClientConfigModel;
import com.tracelink.prodsec.plugin.veracode.dast.repository.VeracodeDastClientConfigRepository;

@RunWith(SpringRunner.class)
public class VeracodeDastClientConfigServiceTest {

	@MockBean
	private VeracodeDastClientConfigRepository mockClientConfigRepo;

	private VeracodeDastClientConfigService configService;

	@Before
	public void setup() {
		this.configService = new VeracodeDastClientConfigService(mockClientConfigRepo);
	}

	@Test
	public void testGetClientConfig() {
		VeracodeDastClientConfigModel config = new VeracodeDastClientConfigModel();
		BDDMockito.when(mockClientConfigRepo.findAll()).thenReturn(Arrays.asList(config));
		Assert.assertEquals(config, configService.getClientConfig());
	}

	@Test
	public void testGetClientConfigNull() {
		BDDMockito.when(mockClientConfigRepo.findAll()).thenReturn(new ArrayList<>());
		Assert.assertNull(configService.getClientConfig());
	}

	@Test
	public void testGetApiClient() {
		VeracodeDastClientConfigModel config = new VeracodeDastClientConfigModel();
		config.setApiId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		config.setApiKey(
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		BDDMockito.when(mockClientConfigRepo.findAll()).thenReturn(Arrays.asList(config));
		Assert.assertNotNull(configService.getApiClient());
	}

	@Test
	public void testGetApiClientNull() {
		BDDMockito.when(mockClientConfigRepo.findAll()).thenReturn(new ArrayList<>());
		Assert.assertNull(configService.getApiClient());
	}

	@Test
	public void testSetClientConfigNew() {
		BDDMockito.when(mockClientConfigRepo.findAll()).thenReturn(new ArrayList<>());
		String apiId = "1234";
		String apiKey = "5678";
		BDDMockito.given(mockClientConfigRepo.saveAndFlush(BDDMockito.any())).willAnswer(e -> e.getArgument(0));
		VeracodeDastClientConfigModel config = configService.setClientConfig(apiId, apiKey);
		Assert.assertEquals(apiId, config.getApiId());
		Assert.assertEquals(apiKey, config.getApiKey());
	}

	@Test
	public void testSetClientConfigReplace() {
		VeracodeDastClientConfigModel config = new VeracodeDastClientConfigModel();
		String apiId = "1234";
		String apiKey = "5678";
		config.setApiId(apiId);
		config.setApiKey(apiKey);

		String newApiId = "ABCD";
		String newApiKey = "EFGH";
		BDDMockito.when(mockClientConfigRepo.findAll()).thenReturn(Arrays.asList(config));
		BDDMockito.given(mockClientConfigRepo.saveAndFlush(BDDMockito.any())).willAnswer(e -> e.getArgument(0));

		VeracodeDastClientConfigModel newConfig = configService.setClientConfig(newApiId, newApiKey);
		Assert.assertEquals(newApiId, newConfig.getApiId());
		Assert.assertEquals(newApiKey, newConfig.getApiKey());
	}

	@Test
	public void testTestAccess() {
		VeracodeDastClientConfigModel config = new VeracodeDastClientConfigModel();
		config.setApiId("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		config.setApiKey(
				"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		BDDMockito.when(mockClientConfigRepo.findAll()).thenReturn(Arrays.asList(config));
		
	}
}
