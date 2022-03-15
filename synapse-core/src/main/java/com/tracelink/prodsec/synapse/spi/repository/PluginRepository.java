package com.tracelink.prodsec.synapse.spi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.synapse.spi.model.PluginModel;

/**
 * Repository to manage Plugins in the database
 * 
 * @author csmith
 *
 */
@Repository
public interface PluginRepository extends JpaRepository<PluginModel, Long> {

	PluginModel getByPluginName(String pluginName);

}
