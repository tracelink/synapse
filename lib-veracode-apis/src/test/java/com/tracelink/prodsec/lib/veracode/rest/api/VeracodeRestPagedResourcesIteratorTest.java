package com.tracelink.prodsec.lib.veracode.rest.api;

import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

import com.tracelink.prodsec.lib.veracode.rest.api.VeracodeRestPagedResourcesIterator;
import com.tracelink.prodsec.lib.veracode.rest.api.model.PagedResourcesWorkspace;

public class VeracodeRestPagedResourcesIteratorTest {

	@Test
	public void testNullCurrentPage() {
		VeracodeRestPagedResourcesIterator<PagedResourcesWorkspace> iterator = new VeracodeRestPagedResourcesIterator<>(
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
