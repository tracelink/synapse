package com.tracelink.prodsec.synapse.spi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.tracelink.prodsec.synapse.spi.PluginWithDatabase;

/**
 * The plugin annotation, used in conjunction with the
 * {@link PluginWithDatabase}.
 * 
 * This annotation enables a Spring component scan, JPA configuration, and
 * Entity Discovery
 * 
 * @author csmith
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ComponentScan
@EnableJpaRepositories
@EntityScan
public @interface SynapsePluginDatabaseEnabled {

}
