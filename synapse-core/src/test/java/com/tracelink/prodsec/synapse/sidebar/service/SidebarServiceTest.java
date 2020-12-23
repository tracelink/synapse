package com.tracelink.prodsec.synapse.sidebar.service;

import com.tracelink.prodsec.synapse.sidebar.model.SidebarDropdown;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.sidebar.model.SimpleSidebarLink;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class SidebarServiceTest {

	@Test
	public void createSidebarTest() {
		String pdgDisplayName1 = "pdgDisplayName1";
		String materialIcon1 = "materialIcon1";
		PluginDisplayGroup pdg1 = new PluginDisplayGroup(pdgDisplayName1, materialIcon1);

		String linkDisplayName1 = "linkDisplayName1";
		SidebarLink link1 = new SimpleSidebarLink(linkDisplayName1);

		String pdgDisplayName2 = "pdgDisplayName2";
		String materialIcon2 = "materialIcon2";
		PluginDisplayGroup pdg2 = new PluginDisplayGroup(pdgDisplayName2, materialIcon2);

		String linkDisplayName2 = "linkDisplayName2";
		SidebarLink link2 = new SimpleSidebarLink(linkDisplayName2);

		SidebarService sidebarService = new SidebarService();

		// out of order to check sorting
		sidebarService.addLink(pdg2, link2);
		sidebarService.addLink(pdg1, link1);

		Map<SidebarDropdown, List<SidebarLink>> sidebar = sidebarService.getSidebar();
		Iterator<SidebarDropdown> dropIter = sidebar.keySet().iterator();
		SidebarDropdown dropDown = dropIter.next();
		Assert.assertEquals(pdgDisplayName1, dropDown.getDisplayName());
		Assert.assertEquals(link1, sidebar.get(dropDown).get(0));

		dropDown = dropIter.next();
		Assert.assertEquals(pdgDisplayName2, dropDown.getDisplayName());
		Assert.assertEquals(link2, sidebar.get(dropDown).get(0));

	}
}
