package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GUIConfigurationTest {

	@Test
	void testSingletonInstance() {
		assertNotNull(GUIConfiguration.INSTANCE);
		assertSame(GUIConfiguration.INSTANCE, GUIConfiguration.INSTANCE);
	}

	@Test
	void testAutowire() {
		IConfiguration config = GUIConfiguration.INSTANCE.autowire();
		assertNotNull(config);
		assertSame(GUIConfiguration.INSTANCE, config);
	}

	@Test
	void testImplementsIConfiguration() {
		assertTrue(GUIConfiguration.INSTANCE instanceof IConfiguration);
	}
}
