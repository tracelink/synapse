package com.tracelink.prodsec.plugin.bsimm.repo;

import com.tracelink.prodsec.plugin.bsimm.model.response.MeasureResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository to store BSIMM measure responses.
 *
 * @author csmith
 */
public interface BsimmMeasureResponseRepo extends JpaRepository<MeasureResponseEntity, Long> {

}
