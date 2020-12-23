package com.tracelink.prodsec.synapse.products.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.synapse.products.model.ProductLineModel;

/**
 * Repository to manage operations for ProductLines in the database
 * 
 * @author csmith
 *
 */
@Repository
public interface ProductLineRepo extends JpaRepository<ProductLineModel, Long> {
	/**
	 * Get a ProductLine by its name, or null if not found
	 * 
	 * @param name the name of the ProductLine
	 * @return a ProductLine with the given name, or null if not found
	 */
	ProductLineModel findByName(String name);

	/**
	 * Get all ProductLines and order them by their name
	 * 
	 * @return a List of ProductLines ordered by name, or empty if there are no
	 *         ProductLines
	 */
	List<ProductLineModel> findAllByOrderByNameAsc();

}
