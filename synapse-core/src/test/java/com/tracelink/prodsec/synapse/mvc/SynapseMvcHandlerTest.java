package com.tracelink.prodsec.synapse.mvc;

import com.tracelink.prodsec.synapse.sidebar.model.SidebarDropdown;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.service.SidebarService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.view.RedirectView;

@RunWith(SpringRunner.class)
public class SynapseMvcHandlerTest {

	@MockBean
	private SidebarService mockSidebarService;

	@Test
	public void handleSidebarTest() throws Exception {
		Map<SidebarDropdown, List<SidebarLink>> sidebar = new HashMap<>();
		BDDMockito.when(mockSidebarService.getSidebar()).thenReturn(sidebar);

		HttpServletRequest request = BDDMockito.mock(HttpServletRequest.class);
		HttpServletResponse response = BDDMockito.mock(HttpServletResponse.class);
		SynapseModelAndView mav = new SynapseModelAndView("foo");

		SynapseMvcHandler handler = new SynapseMvcHandler(mockSidebarService);
		handler.postHandle(request, response, null, mav);

		Assert.assertEquals(sidebar, mav.getModel().get("sidebar"));
		BDDMockito.verify(mockSidebarService).getSidebar();
	}

	@Test
	public void handleSidebarTestNull() throws Exception {
		HttpServletRequest request = BDDMockito.mock(HttpServletRequest.class);
		HttpServletResponse response = BDDMockito.mock(HttpServletResponse.class);
		SynapseModelAndView mav = null;

		SynapseMvcHandler handler = new SynapseMvcHandler(mockSidebarService);
		handler.postHandle(request, response, null, mav);

		BDDMockito.verify(mockSidebarService, BDDMockito.never()).getSidebar();
	}

	@Test
	public void handleSidebarTestRedirect() throws Exception {
		Map<SidebarDropdown, List<SidebarLink>> sidebar = new HashMap<>();
		BDDMockito.when(mockSidebarService.getSidebar()).thenReturn(sidebar);

		HttpServletRequest request = BDDMockito.mock(HttpServletRequest.class);
		HttpServletResponse response = BDDMockito.mock(HttpServletResponse.class);
		SynapseModelAndView mav = new SynapseModelAndView("foo");
		mav.setView(new RedirectView());

		SynapseMvcHandler handler = new SynapseMvcHandler(mockSidebarService);
		handler.postHandle(request, response, null, mav);

		Assert.assertNull(mav.getModel().get("sidebar"));
		BDDMockito.verify(mockSidebarService, BDDMockito.never()).getSidebar();
	}
}
