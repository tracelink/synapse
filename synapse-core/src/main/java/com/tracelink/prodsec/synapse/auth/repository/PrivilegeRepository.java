package com.tracelink.prodsec.synapse.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.synapse.auth.model.PrivilegeModel;

/**
 * Repository to manage operations for Privileges in the database
 * 
 * @author csmith
 *
 */
@Repository
public interface PrivilegeRepository extends JpaRepository<PrivilegeModel, Long> {

	/**
	 * Get a Privilege by its name, or null if not found
	 * 
	 * @param name the name of the privilege
	 * @return a privilege with the given name, or null if not found
	 */
	PrivilegeModel findByName(String name);

}
