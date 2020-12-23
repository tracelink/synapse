package com.tracelink.prodsec.synapse.encryption.repository;

import com.tracelink.prodsec.synapse.encryption.model.EncryptionMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link EncryptionMetadata}.
 *
 * @author mcool
 */
@Repository
public interface EncryptionMetadataRepository extends JpaRepository<EncryptionMetadata, Long> {

}
