package com.tracelink.prodsec.plugin.veracode.sast.service;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastClientConfigModel;
import com.tracelink.prodsec.plugin.veracode.sast.repository.VeracodeSastClientConfigRepository;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class VeracodeSastClientConfigServiceTest {

	@MockBean
	private VeracodeSastClientConfigRepository mockClientConfigRepo;

	private VeracodeSastClientConfigService configService;

	@Before
	public void setup() {
		this.configService = new VeracodeSastClientConfigService(mockClientConfigRepo);
	}

	@Test
	public void testGetClientConfig() {
		VeracodeSastClientConfigModel config = new VeracodeSastClientConfigModel();
		BDDMockito.when(mockClientConfigRepo.findAll()).thenReturn(Arrays.asList(config));
		Assert.assertEquals(config, configService.getClientConfig());
	}

	@Test
	public void testGetClientConfigNull() {
		BDDMockito.when(mockClientConfigRepo.findAll())
				.thenReturn(new ArrayList<>());
		Assert.assertNull(configService.getClientConfig());
	}

	@Test
	public void testGetApiClient() {
		VeracodeSastClientConfigModel config = new VeracodeSastClientConfigModel();
		config.setApiId("1234");
		config.setApiKey("5678");
		BDDMockito.when(mockClientConfigRepo.findAll()).thenReturn(Arrays.asList(config));
		Assert.assertNotNull(configService.getApiClient());
	}

	@Test
	public void testGetApiClientNull() {
		BDDMockito.when(mockClientConfigRepo.findAll())
				.thenReturn(new ArrayList<>());
		Assert.assertNull(configService.getApiClient());
	}

	@Test
	public void testSetClientConfigNew() {
		BDDMockito.when(mockClientConfigRepo.findAll())
				.thenReturn(new ArrayList<>());
		String apiId = "1234";
		String apiKey = "5678";
		BDDMockito.given(mockClientConfigRepo.saveAndFlush(BDDMockito.any()))
				.willAnswer(e -> e.getArgument(0));
		VeracodeSastClientConfigModel config = configService.setClientConfig(apiId, apiKey);
		Assert.assertEquals(apiId, config.getApiId());
		Assert.assertEquals(apiKey, config.getApiKey());
	}

	@Test
	public void testSetClientConfigReplace() {
		VeracodeSastClientConfigModel config = new VeracodeSastClientConfigModel();
		String apiId = "1234";
		String apiKey = "5678";
		config.setApiId(apiId);
		config.setApiKey(apiKey);

		String newApiId = "ABCD";
		String newApiKey = "EFGH";
		BDDMockito.when(mockClientConfigRepo.findAll()).thenReturn(Arrays.asList(config));
		BDDMockito.given(mockClientConfigRepo.saveAndFlush(BDDMockito.any()))
				.willAnswer(e -> e.getArgument(0));

		VeracodeSastClientConfigModel newConfig = configService
				.setClientConfig(newApiId, newApiKey);
		Assert.assertEquals(newApiId, newConfig.getApiId());
		Assert.assertEquals(newApiKey, newConfig.getApiKey());
	}
}
