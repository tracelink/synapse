package com.tracelink.prodsec.plugin.veracode.sast.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tracelink.prodsec.plugin.veracode.sast.model.VeracodeSastClientConfigModel;

/**
 * Handles DB operations for the {@linkplain VeracodeSastClientConfigModel}
 * 
 * @author csmith
 *
 */
@Repository
public interface VeracodeSastClientConfigRepository extends JpaRepository<VeracodeSastClientConfigModel, Long> {

}
