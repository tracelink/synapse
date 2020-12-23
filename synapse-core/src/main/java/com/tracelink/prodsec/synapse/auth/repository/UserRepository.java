package com.tracelink.prodsec.synapse.auth.repository;

import com.tracelink.prodsec.synapse.auth.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository to manage operations for Users in the database
 *
 * @author csmith
 */
@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {

	/**
	 * Get a user by its SSO id, or null if not found.
	 *
	 * @param ssoId the id of the user from the configured SSO
	 * @return a user with the given SSO id, or null
	 */
	UserModel findBySsoId(String ssoId);

	/**
	 * Get a User by its username, or null if not found
	 *
	 * @param username the username of the user
	 * @return a user with the given username, or null if not found
	 */
	UserModel findByUsername(String username);
}
