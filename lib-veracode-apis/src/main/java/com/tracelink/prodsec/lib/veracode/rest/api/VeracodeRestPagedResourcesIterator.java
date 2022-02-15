package com.tracelink.prodsec.lib.veracode.rest.api;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import com.tracelink.prodsec.lib.veracode.rest.api.model.AbstractPagedResources;
import com.tracelink.prodsec.lib.veracode.rest.api.model.PageMetadata;

/**
 * Iterator to fetch all pages of results for an {@link AbstractPagedResources} from the Veracode
 * SCA server.
 *
 * @author mcool
 * @param <T> type of the paged resources to fetch from Veracode
 */
public class VeracodeRestPagedResourcesIterator<T extends AbstractPagedResources> implements
		Iterator<T> {

	private final Function<Long, T> pagedResourcesFunction;
	private PageMetadata currentPage;

	/**
	 * Constructs an instance of this iterator with a function that returns a single page of results
	 * from the Veracode server, given a page number.
	 *
	 * @param pagedResourcesFunction function that fetches a page of results given a page number
	 */
	public VeracodeRestPagedResourcesIterator(Function<Long, T> pagedResourcesFunction) {
		this.pagedResourcesFunction = pagedResourcesFunction;
		PageMetadata page = new PageMetadata();
		page.setNumber(-1L);
		this.currentPage = page;
	}

	@Override
	public boolean hasNext() {
		return currentPage != null && (currentPage.getNumber() == -1
				|| currentPage.getNumber() + 1 < currentPage.getTotalPages());
	}

	@Override
	public T next() {
		if (currentPage == null) {
			throw new NoSuchElementException();
		}
		long nextPage = currentPage.getNumber() + 1;
		// Apply pagedResourcesFunction to get the next page of results
		T pagedResources = pagedResourcesFunction.apply(nextPage);
		currentPage = pagedResources.getPage();
		return pagedResources;
	}
}
