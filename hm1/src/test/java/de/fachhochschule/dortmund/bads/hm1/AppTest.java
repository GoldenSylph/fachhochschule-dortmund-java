package de.fachhochschule.dortmund.bads.hm1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AppTest {
	@Test
	public void testConfiguration() {
		App app = new App();
		app.run();
		assertEquals(CoreConfiguration.INSTANCE.getAutowiredStatus(), true);
	}
}
