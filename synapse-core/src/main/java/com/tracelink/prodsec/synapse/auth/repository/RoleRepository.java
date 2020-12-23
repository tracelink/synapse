package com.tracelink.prodsec.synapse.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.synapse.auth.model.RoleModel;

/**
 * Repository to manage operations for Roles in the database
 * 
 * @author csmith
 *
 */
@Repository
public interface RoleRepository extends JpaRepository<RoleModel, Long> {
	/**
	 * Get a Role by its name, or null if not found
	 * 
	 * @param name the name of the role
	 * @return a role with the given name, or null if not found
	 */
	RoleModel findByRoleName(String name);

}
