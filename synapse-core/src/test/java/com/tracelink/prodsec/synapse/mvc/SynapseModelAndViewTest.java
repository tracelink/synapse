package com.tracelink.prodsec.synapse.mvc;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.thymeleaf.spring5.view.ThymeleafView;

public class SynapseModelAndViewTest {

	@SuppressWarnings("unchecked")
	@Test
	public void basicConfig() {
		String viewName = "viewName";
		SynapseModelAndView mav = new SynapseModelAndView(viewName);
		Assert.assertEquals(SynapseModelAndView.DEFAULT_VIEW_TEMPLATE, mav.getViewName());
		Assert.assertEquals(viewName, mav.getModel().get(SynapseModelAndView.CONTENT_VIEW_NAME));
		Assert.assertTrue(((List<String>) mav.getModel().get("styles")).isEmpty());
		Assert.assertTrue(((List<String>) mav.getModel().get("scripts")).isEmpty());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void basicStyleScript() {
		String viewName = "viewName";
		String style = "styleName";
		String script = "scriptName";

		SynapseModelAndView mav = new SynapseModelAndView(viewName);
		mav.addScriptReference(script);
		mav.addStyleReference(style);

		Assert.assertEquals(SynapseModelAndView.DEFAULT_VIEW_TEMPLATE, mav.getViewName());
		Assert.assertEquals(viewName, mav.getModel().get(SynapseModelAndView.CONTENT_VIEW_NAME));
		Assert.assertEquals(1, ((List<String>) mav.getModel().get("styles")).size());
		Assert.assertEquals(style, ((List<String>) mav.getModel().get("styles")).get(0));

		Assert.assertEquals(1, ((List<String>) mav.getModel().get("scripts")).size());
		Assert.assertEquals(script, ((List<String>) mav.getModel().get("scripts")).get(0));
	}

	@Test
	public void basicOverrideName() {
		String viewName = "viewName";

		SynapseModelAndView mav = new SynapseModelAndView(viewName);
		mav.setViewName(viewName);

		Assert.assertEquals(viewName, mav.getViewName());
		Assert.assertNull(mav.getModel().get(SynapseModelAndView.CONTENT_VIEW_NAME));
		Assert.assertNull(mav.getModel().get("styles"));
		Assert.assertNull(mav.getModel().get("scripts"));
	}

	@Test
	public void basicOverrideView() {
		String viewName = "viewName";

		SynapseModelAndView mav = new SynapseModelAndView(viewName);
		mav.setView(new ThymeleafView(viewName));

		Assert.assertNull(mav.getViewName());
		Assert.assertNull(mav.getModel().get(SynapseModelAndView.CONTENT_VIEW_NAME));
		Assert.assertNull(mav.getModel().get("styles"));
		Assert.assertNull(mav.getModel().get("scripts"));
	}

}
