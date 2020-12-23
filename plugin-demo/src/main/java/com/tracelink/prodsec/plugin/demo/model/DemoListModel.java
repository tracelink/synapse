package com.tracelink.prodsec.plugin.demo.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This ListModel is a helper to order the {@link DemoItemModel} instances to
 * help the UI order items correctly (sort by product lines, then by projects
 * with vulns, then by name)
 *
 * @author csmith
 */
public class DemoListModel {

	private final Map<String, List<DemoItemModel>> uiModel = new TreeMap<>();
	private final DemoItemComparator comparator = new DemoItemComparator();

	/**
	 * adds the item to the ordering list. Orders the list immediately.
	 *
	 * @param product the product line to order this item list against
	 * @param item    the item for this product line
	 */
	public void addToModel(String product, DemoItemModel item) {
		List<DemoItemModel> list = uiModel.getOrDefault(product, new ArrayList<>());
		list.add(item);
		list.sort(comparator);
		uiModel.put(product, list);
	}

	public Map<String, List<DemoItemModel>> getMapping() {
		return uiModel;
	}

	/**
	 * A comparator to order items by most vulns (when configured), then project
	 * name
	 *
	 * @author csmith
	 */
	static class DemoItemComparator implements Comparator<DemoItemModel> {

		@Override
		public int compare(DemoItemModel o1, DemoItemModel o2) {
			if (o1.isConfigured() && o2.isConfigured()) {
				return Integer.compare(o2.getVulns(), o1.getVulns());
			} else if (o1.isConfigured()) {
				return -1;
			} else if (o2.isConfigured()) {
				return 1;
			}
			return o1.getProjectName().compareTo(o2.getProjectName());
		}

	}
}
