package com.tracelink.prodsec.synapse.spi.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.ComponentScan;

import com.tracelink.prodsec.synapse.spi.Plugin;

/**
 * The plugin annotation, used in conjunction with the {@link Plugin}.
 * 
 * This annotation enables a Spring component scan
 * 
 * @author csmith
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ComponentScan
public @interface SynapsePlugin {

}
