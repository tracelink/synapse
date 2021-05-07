package com.tracelink.prodsec.plugin.veracode.sca.util.api;

import com.tracelink.prodsec.plugin.veracode.sca.util.model.AbstractPagedResources;
import com.tracelink.prodsec.plugin.veracode.sca.util.model.PageMetadata;
import java.util.Iterator;
import java.util.function.Function;

/**
 * Iterator to fetch all pages of results for an {@link AbstractPagedResources} from the Veracode
 * SCA server.
 *
 * @author mcool
 * @param <T> type of the paged resources to fetch from Veracode
 */
public class VeracodeScaPagedResourcesIterator<T extends AbstractPagedResources> implements
	Iterator<T> {

	private final Function<Long, T> pagedResourcesFunction;
	private PageMetadata currentPage; // Null if we have not yet fetched the first page

	/**
	 * Constructs an instance of this iterator with a function that returns a single page of results
	 * from the Veracode server, given a page number.
	 *
	 * @param pagedResourcesFunction function that fetches a page of results given a page number
	 */
	public VeracodeScaPagedResourcesIterator(Function<Long, T> pagedResourcesFunction) {
		this.pagedResourcesFunction = pagedResourcesFunction;
	}

	@Override
	public boolean hasNext() {
		return currentPage == null || currentPage.getNumber() + 1 < currentPage.getTotalPages();
	}

	@Override
	public T next() {
		long nextPage = currentPage == null ? 0L : currentPage.getNumber() + 1;
		// Apply pagedResourcesFunction to get the next page of results
		T pagedResources = pagedResourcesFunction.apply(nextPage);
		currentPage = pagedResources.getPage();
		return pagedResources;
	}
}
