package com.tracelink.prodsec.plugin.bsimm.repo;

import com.tracelink.prodsec.plugin.bsimm.model.survey.MeasureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository to store BSIMM measures.
 *
 * @author csmith
 */
public interface BsimmMeasureRepo extends JpaRepository<MeasureEntity, Long> {

}
