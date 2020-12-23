package com.tracelink.prodsec.plugin.bsimm.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelink.prodsec.plugin.bsimm.model.response.MeasureResponseEntity;

public interface BsimmMeasureResponseRepo extends JpaRepository<MeasureResponseEntity, Long> {

}
