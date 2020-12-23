package com.tracelink.prodsec.plugin.bsimm.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tracelink.prodsec.plugin.bsimm.model.survey.MeasureEntity;

public interface BsimmMeasureRepo extends JpaRepository<MeasureEntity, Long> {

}
