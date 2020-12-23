package com.tracelink.prodsec.plugin.veracode.dast.repository;

import com.tracelink.prodsec.plugin.veracode.dast.model.VeracodeDastAppModel;
import com.tracelink.prodsec.synapse.products.model.ProductLineModel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Handles DB operations for the {@linkplain VeracodeDastAppModel}
 *
 * @author csmith
 */
@Repository
public interface VeracodeDastAppRepository extends JpaRepository<VeracodeDastAppModel, Long> {

	/**
	 * Gets all {@link VeracodeDastAppModel} entities where the Synapse {@link ProductLineModel} is
	 * not null.
	 *
	 * @return list of mapped Veracode apps
	 */
	List<VeracodeDastAppModel> findAllBySynapseProductLineNotNull();

	/**
	 * Gets all {@link VeracodeDastAppModel} entities where the Synapse {@link ProductLineModel} is
	 * null.
	 *
	 * @return list of unmapped Veracode apps
	 */
	List<VeracodeDastAppModel> findAllBySynapseProductLineIsNull();

	/**
	 * Gets the {@link VeracodeDastAppModel} for the given Synapse {@link ProductLineModel}.
	 *
	 * @param synapseProductLine the Synapse Product Line to search by
	 * @return the Veracode app mapped to the Synapse Product Line, or null
	 */
	List<VeracodeDastAppModel> findBySynapseProductLine(ProductLineModel synapseProductLine);

	/**
	 * Gets the {@link VeracodeDastAppModel} with the given name.
	 *
	 * @param name the name to search by
	 * @return the Veracode app with the given name, or null
	 */
	VeracodeDastAppModel findByName(String name);
}
