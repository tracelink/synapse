package com.tracelink.prodsec.plugin.demo.model;

import com.tracelink.prodsec.plugin.demo.model.DemoListModel.DemoItemComparator;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class DemoListModelTest {

	@Test
	public void testDAO() {
		String product = "product";
		DemoItemModel dim = new DemoItemModel("", false, 1);
		DemoListModel dlm = new DemoListModel();
		dlm.addToModel(product, dim);
		Map<String, List<DemoItemModel>> map = dlm.getMapping();

		Assert.assertEquals(1, map.keySet().size());
		Assert.assertTrue(map.containsKey(product));
		Assert.assertEquals(1, map.get(product).size());
		Assert.assertTrue(map.get(product).contains(dim));
	}

	@Test
	public void testComparatorConfigured() {
		DemoItemComparator comparator = new DemoItemComparator();
		DemoItemModel d1 = new DemoItemModel("A", true, 2);
		DemoItemModel d2 = new DemoItemModel("B", true, 1);
		List<DemoItemModel> dimList = Arrays.asList(d2, d1);
		dimList.sort(comparator);
		Assert.assertEquals(d1, dimList.get(0));
		Assert.assertEquals(d2, dimList.get(1));
	}

	@Test
	public void testComparatorFirstConfigured() {
		DemoItemComparator comparator = new DemoItemComparator();
		DemoItemModel d1 = new DemoItemModel("A", true, 2);
		DemoItemModel d2 = new DemoItemModel("B", false, 1);
		List<DemoItemModel> dimList = Arrays.asList(d1, d2);
		dimList.sort(comparator);
		Assert.assertEquals(d1, dimList.get(0));
		Assert.assertEquals(d2, dimList.get(1));
	}

	@Test
	public void testComparatorSecondConfigured() {
		DemoItemComparator comparator = new DemoItemComparator();
		DemoItemModel d1 = new DemoItemModel("A", false, 2);
		DemoItemModel d2 = new DemoItemModel("B", true, 1);
		List<DemoItemModel> dimList = Arrays.asList(d1, d2);
		dimList.sort(comparator);
		Assert.assertEquals(d2, dimList.get(0));
		Assert.assertEquals(d1, dimList.get(1));
	}

	@Test
	public void testComparatorNeitherConfigured() {
		DemoItemComparator comparator = new DemoItemComparator();
		DemoItemModel d1 = new DemoItemModel("A", false, 2);
		DemoItemModel d2 = new DemoItemModel("B", false, 1);
		List<DemoItemModel> dimList = Arrays.asList(d2, d1);
		dimList.sort(comparator);
		Assert.assertEquals(d1, dimList.get(0));
		Assert.assertEquals(d2, dimList.get(1));
	}
}
