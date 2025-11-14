package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GUIConfigurationTest {

	@BeforeEach
	void setUp() {
		// GUIConfiguration requires CoreConfiguration to be autowired first
		if (!CoreConfiguration.INSTANCE.getAutowiredStatus()) {
			CoreConfiguration.INSTANCE.autowire();
		}
	}
	
	@AfterEach
	void tearDown() {
		// Clean up after each test
		CoreConfiguration.INSTANCE.shutdown();
	}

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