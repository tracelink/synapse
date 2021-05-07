package com.tracelink.prodsec.synapse.auth.service;

import com.tracelink.prodsec.synapse.auth.SynapseAdminAuthDictionary;
import com.tracelink.prodsec.synapse.auth.model.PrivilegeModel;
import com.tracelink.prodsec.synapse.auth.model.RoleModel;
import com.tracelink.prodsec.synapse.auth.model.UserModel;
import com.tracelink.prodsec.synapse.auth.repository.PrivilegeRepository;
import com.tracelink.prodsec.synapse.auth.repository.RoleRepository;
import com.tracelink.prodsec.synapse.auth.repository.UserRepository;
import java.security.Principal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service to manage business logic around authentication and
 * user/role/privilege management
 * <p>
 * Ensures that there is a default admin with full authorization when first
 * started up. On every subsequent startup, ensures that the default admin role
 * has all permissions in the system.
 *
 * @author csmith
 */
@Service
public class AuthService implements UserDetailsService {

	private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

	private final PasswordEncoder passwordEncoder;

	private final UserRepository userRepo;

	private final RoleRepository roleRepo;

	private final PrivilegeRepository privRepo;

	public AuthService(@Autowired PasswordEncoder passwordEncoder, @Autowired UserRepository userRepo, @Autowired RoleRepository roleRepo, @Autowired PrivilegeRepository privRepo) {
		this.passwordEncoder = passwordEncoder;
		this.userRepo = userRepo;
		this.roleRepo = roleRepo;
		this.privRepo = privRepo;
		setupDefaultAuth();
	}

	/**
	 * Create the default admin user if it doesn't exist
	 * <p>
	 * Create the default admin role if it doesn't exist
	 * <p>
	 * Create the default admin privilege if it doesn't exist
	 * <p>
	 * Set the admin role to have all privileges in the system, including from all
	 * plugins
	 */
	private void setupDefaultAuth() {
		// get priv or create it
		PrivilegeModel adminPriv = privRepo.findByName(SynapseAdminAuthDictionary.ADMIN_PRIV);
		if (adminPriv == null) {
			adminPriv = new PrivilegeModel();
			adminPriv.setName(SynapseAdminAuthDictionary.ADMIN_PRIV);
			adminPriv = privRepo.saveAndFlush(adminPriv);
		}

		// get role or create it
		RoleModel adminRole = roleRepo.findByRoleName(SynapseAdminAuthDictionary.ADMIN_ROLE);
		if (adminRole == null) {
			adminRole = new RoleModel();
			adminRole.setRoleName(SynapseAdminAuthDictionary.ADMIN_ROLE);
			adminRole = roleRepo.saveAndFlush(adminRole);
		}

		// the admin role must contain all privs
		adminRole.setPrivileges(privRepo.findAll());
		adminRole = roleRepo.saveAndFlush(adminRole);

		// get the admin user or create it with a new random pw
		UserModel adminUser = userRepo.findByUsername(SynapseAdminAuthDictionary.DEFAULT_ADMIN_USERNAME);
		if (adminUser == null) {
			byte[] pwByte = new byte[16];
			new SecureRandom().nextBytes(pwByte);
			String pw = new String(Hex.encode(pwByte));
			adminUser = createNewUser(SynapseAdminAuthDictionary.DEFAULT_ADMIN_USERNAME, pw);
			LOG.info("New default password set for default admin: " + pw);
		}

		// the admin user must contain the admin role
		if (adminUser.getRoles() == null || !adminUser.getRoles().contains(adminRole)) {
			adminUser.setRoles(Arrays.asList(adminRole));
			userRepo.saveAndFlush(adminUser);
		}

	}

	//////////////
	// Users
	//////////////

	/**
	 * Gets a list of all users in the {@link UserRepository}.
	 *
	 * @return list of all users
	 */
	public List<UserModel> findAllUsers() {
		return userRepo.findAll();
	}

	/**
	 * Gets the user with the given username, or null if no such user exists.
	 *
	 * @param username username of the user to retrieve
	 * @return user with the given username, or null
	 */
	public UserModel findByUsername(String username) {
		return userRepo.findByUsername(username);
	}

	/**
	 * Populates a {@link UserDetails} object for the user with the given username.
	 *
	 * @param username the username of the user to load from the database
	 * @return {@link UserDetails} object containing user information
	 * @throws UsernameNotFoundException if no user exists with the given username
	 */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserModel user = userRepo.findByUsername(username);
		if (user == null) {
			throw new UsernameNotFoundException("Unknown username");
		}

		return User.builder().username(user.getUsername()).password(user.getPassword()).disabled(!user.isEnabled()).accountExpired(false).accountLocked(false).credentialsExpired(false).authorities(getAuthorities(user.getRoles())).build();
	}

	private List<GrantedAuthority> getAuthorities(Collection<RoleModel> roles) {
		List<GrantedAuthority> authorities = new ArrayList<>();
		for (RoleModel role : roles) {
			for (PrivilegeModel priv : role.getPrivileges()) {
				authorities.add(new SimpleGrantedAuthority(priv.getName()));
			}
		}
		return authorities;
	}

	/**
	 * Gets the user with the given ID, or null if no such user exists.
	 *
	 * @param id ID of the user to retrieve
	 * @return user with the given ID, or null
	 */
	public UserModel findById(long id) {
		return userRepo.findById(id).orElse(null);
	}

	/**
	 * Determines whether the given user is an Admin of Synapse.
	 *
	 * @param user the user to check Admin privileges for
	 * @return true if the user has the Synapse Admin role, false otherwise
	 */
	public boolean isAdmin(UserModel user) {
		return user.getRoles().stream().anyMatch(r -> r.getRoleName().equals(SynapseAdminAuthDictionary.ADMIN_ROLE));
	}

	/**
	 * Creates a new, enabled user in the {@link UserRepository} with the given username and
	 * password.
	 *
	 * @param username the username of the new user
	 * @param password the password of the new user
	 * @return the {@link UserModel} stored in the database
	 */
	public UserModel createNewUser(String username, String password) {
		UserModel user = new UserModel();
		user.setUsername(username);
		user.setEnabled(true);
		user.setPassword(passwordEncoder.encode(password));
		return userRepo.saveAndFlush(user);
	}

	/**
	 * Saves the given user in the {@link UserRepository}.
	 *
	 * @param user the user to save
	 * @return the updated {@link UserModel} stored in the database
	 */
	public UserModel saveUser(UserModel user) {
		return userRepo.saveAndFlush(user);
	}

	/**
	 * Deletes the given user from the {@link UserRepository}.
	 *
	 * @param user the user to delete
	 */
	public void deleteUser(UserModel user) {
		userRepo.delete(user);
	}

	/**
	 * Determines whether the hash of the given password provided by the given user matches the
	 * currently stored password hash for that user.
	 *
	 * @param user            the user to check a password match for
	 * @param currentPassword the current password as provided by the user
	 * @return true if the password hashes match, false otherwise
	 */
	public boolean passwordMatches(UserModel user, String currentPassword) {
		return passwordEncoder.matches(currentPassword, user.getPassword());
	}

	/**
	 * Helper to determine if the given user is the same as the current
	 * authenticated user.
	 *
	 * @param user              to compare with authenticated user
	 * @param authenticatedUser the current user
	 * @return true if the two users are equal, false otherwise
	 */
	public boolean isCurrentUser(UserModel user, Principal authenticatedUser) {
		return user.getUsername().equals(authenticatedUser.getName());
	}

	///////////////
	// Roles
	///////////////

	/**
	 * Gets a list of all roles in the {@link RoleRepository}.
	 *
	 * @return list of all roles
	 */
	public List<RoleModel> findAllRoles() {
		return roleRepo.findAll();
	}

	/**
	 * Gets the role with the given ID, or null if no such role exists.
	 *
	 * @param id the ID of the role to retrieve
	 * @return the role with the given ID, or null
	 */
	public RoleModel findRoleById(long id) {
		return roleRepo.findById(id).orElse(null);
	}

	/**
	 * Gets the role with the given name, or null if no such role exists.
	 *
	 * @param roleName the name of the role to retrieve
	 * @return the role with the given name, or null
	 */
	public RoleModel findRoleByName(String roleName) {
		return roleRepo.findByRoleName(roleName);
	}

	/**
	 * Updates the given role in the {@link RoleRepository}.
	 *
	 * @param role the role to update
	 * @return the updated {@link RoleModel} stored in the database
	 */
	public RoleModel updateRole(RoleModel role) {
		return roleRepo.saveAndFlush(role);
	}

	/**
	 * Deletes the given role from the {@link RoleRepository}.
	 *
	 * @param role the user to delete
	 */
	public void deleteRole(RoleModel role) {
		roleRepo.delete(role);
	}

	/**
	 * Updates the default Synapse Admin role to include the given privilege.
	 *
	 * @param priv the privilege to add to the Admin role
	 */
	private void updateDefaultAdminRoleWithPrivilege(PrivilegeModel priv) {
		RoleModel adminRole = roleRepo.findByRoleName(SynapseAdminAuthDictionary.ADMIN_ROLE);
		Collection<PrivilegeModel> privs = adminRole.getPrivileges();
		privs.add(priv);
		adminRole.setPrivileges(privs);
		roleRepo.saveAndFlush(adminRole);
	}

	/**
	 * Helper to determine if the given role is built-in and should not be modified
	 *
	 * @param role to compare with built-in roles
	 * @return true if the role is built-in, false otherwise
	 */
	public boolean isBuiltInRole(RoleModel role) {
		return SynapseAdminAuthDictionary.ADMIN_ROLE.equals(role.getRoleName());
	}

	///////////////
	// Privileges
	///////////////

	/**
	 * Gets a list of all privileges in the {@link PrivilegeRepository}.
	 *
	 * @return list of all privileges
	 */
	public List<PrivilegeModel> findAllPrivileges() {
		return privRepo.findAll();
	}

	/**
	 * Gets the privilege with the given name, or creates the privilege if it does not already
	 * exist.
	 *
	 * @param priv the name of the privilege to retrieve or create
	 * @return the privilege with the given name
	 */
	public PrivilegeModel createOrGetPrivilege(String priv) {
		PrivilegeModel privilege = privRepo.findByName(priv);
		if (privilege == null) {
			privilege = new PrivilegeModel();
			privilege.setName(priv);
			privilege = privRepo.save(privilege);
			updateDefaultAdminRoleWithPrivilege(privilege);
		}
		return privilege;
	}

	/**
	 * Gets the privilege with the given name, or null if no such privilege exists.
	 *
	 * @param privilegeName the name of the privilege to retrieve
	 * @return the privilege with the given name, or null
	 */
	public PrivilegeModel findPrivilegeByName(String privilegeName) {
		return privRepo.findByName(privilegeName);
	}
}
