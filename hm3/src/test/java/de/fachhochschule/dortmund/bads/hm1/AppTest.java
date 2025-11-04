package de.fachhochschule.dortmund.bads.hm1;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.App;
import de.fachhochschule.dortmund.bads.CoreConfiguration;

public class AppTest {
	@Test
	public void testConfiguration() {
		App app = new App();
		app.run();
		assertEquals(CoreConfiguration.INSTANCE.getAutowiredStatus(), true);
	}
}
