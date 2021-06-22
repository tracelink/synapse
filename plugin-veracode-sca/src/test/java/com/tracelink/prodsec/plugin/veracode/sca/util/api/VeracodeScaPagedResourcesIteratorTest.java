package com.tracelink.prodsec.plugin.veracode.sca.util.api;

import com.tracelink.prodsec.plugin.veracode.sca.util.model.PagedResourcesWorkspace;
import java.util.NoSuchElementException;
import org.junit.Assert;
import org.junit.Test;

public class VeracodeScaPagedResourcesIteratorTest {

	@Test
	public void testNullCurrentPage() {
		VeracodeScaPagedResourcesIterator<PagedResourcesWorkspace> iterator = new VeracodeScaPagedResourcesIterator<>(
				(page) -> new PagedResourcesWorkspace());
		try {
			iterator.next();
			iterator.next(); // Triggers exception
			Assert.fail();
		} catch (NoSuchElementException e) {
			// Correct
		}
	}
}
