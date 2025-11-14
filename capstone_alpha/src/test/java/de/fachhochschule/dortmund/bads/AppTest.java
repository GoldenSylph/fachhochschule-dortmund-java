package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AppTest {
	
	private App app;
	
	@BeforeEach
	public void setUp() {
		app = new App();
	}
	
	@AfterEach
	public void tearDown() {
		// Shutdown the configuration to clean up resources
		CoreConfiguration.INSTANCE.shutdown();
	}
	
	@Test
	public void testConfiguration() {
		// Initialize the system without running the infinite loop
		CoreConfiguration.INSTANCE.autowire();
		
		// Verify autowiring was successful
		assertTrue(CoreConfiguration.INSTANCE.getAutowiredStatus(), 
			"CoreConfiguration should be autowired successfully");
	}
	
	@Test
	public void testWarehouseInitialization() {
		// Test that we can create the basic components without starting the app
		assertNotNull(app, "App instance should be created");
		
		// Verify configuration can be initialized
		CoreConfiguration.INSTANCE.autowire();
		assertTrue(CoreConfiguration.INSTANCE.getAutowiredStatus());
	}
}