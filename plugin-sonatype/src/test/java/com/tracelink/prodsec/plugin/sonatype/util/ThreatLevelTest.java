package com.tracelink.prodsec.plugin.sonatype.util;

import org.junit.Assert;
import org.junit.Test;

public class ThreatLevelTest {

	@Test
	public void testForLevelNull() {
		Assert.assertNull(ThreatLevel.forLevel(-1));
	}
}
