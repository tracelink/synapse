package com.tracelink.prodsec.synapse.sidebar.service;

import com.tracelink.prodsec.synapse.sidebar.model.SidebarDropdown;
import com.tracelink.prodsec.synapse.sidebar.model.SidebarLink;
import com.tracelink.prodsec.synapse.spi.PluginDisplayGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.springframework.stereotype.Service;

/**
 * Handles the Sidebar business logic to add plugin items to the sidebar and get
 * the sidebar in order
 *
 * @author csmith
 */
@Service
public class SidebarService {

	private final SortedMap<SidebarDropdown, List<SidebarLink>> sidebar;
	private final Map<PluginDisplayGroup, SidebarDropdown> dropdownLookup;

	public SidebarService() {
		this.sidebar = new TreeMap<>(new SidebarGroupSorter());
		this.dropdownLookup = new HashMap<>();
	}

	/**
	 * adds a link to the sidebar that is keyed by a synthetic
	 * {@link SidebarDropdown}. This {@link SidebarDropdown} is created based on the
	 * {@link PluginDisplayGroup}
	 *
	 * @param sidebarGroup the plugin information to generate a
	 *                     {@link SidebarDropdown} from
	 * @param link         the {@link SidebarLink} object to add in a
	 *                     {@link SidebarDropdown}
	 */
	public void addLink(PluginDisplayGroup sidebarGroup, SidebarLink link) {
		SidebarDropdown dropdown = this.dropdownLookup.get(sidebarGroup);
		if (dropdown == null) {
			dropdown = new SidebarDropdown(sidebarGroup);
			this.dropdownLookup.put(sidebarGroup, dropdown);
		}
		dropdown.addAuthorization(link.getAuthorizePrivileges());
		List<SidebarLink> links = this.sidebar.getOrDefault(dropdown, new ArrayList<>());
		links.add(link);
		this.sidebar.put(dropdown, links);
	}

	public Map<SidebarDropdown, List<SidebarLink>> getSidebar() {
		return Collections.unmodifiableMap(this.sidebar);
	}

	/**
	 * Sort the Sidebar Dropdowns by display name. This is sorted for the values
	 * between the Scorecard link and the administrative functions
	 *
	 * @author csmith
	 */
	static class SidebarGroupSorter implements Comparator<SidebarDropdown> {

		@Override
		public int compare(SidebarDropdown o1, SidebarDropdown o2) {
			return o1.getDisplayName().compareTo(o2.getDisplayName());
		}
	}
}
