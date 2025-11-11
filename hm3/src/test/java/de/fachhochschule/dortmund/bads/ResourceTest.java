package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.resources.BeveragesBox;
import de.fachhochschule.dortmund.bads.resources.Resource;

class ResourceTest {
	
	@Test
	void testResourceAsBeveragesBox() {
		Resource resource = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		assertNotNull(resource);
	}
	
	@Test
	void testDifferentResourceTypes() {
		Resource ambient = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		Resource refrigerated = new BeveragesBox(BeveragesBox.Type.REFRIGERATED, "Milk", 15, 15, 15, 12);
		Resource bulk = new BeveragesBox(BeveragesBox.Type.BULK, "Beer", 25, 25, 25, 4);
		
		assertNotNull(ambient);
		assertNotNull(refrigerated);
		assertNotNull(bulk);
	}
	
	@Test
	void testResourcePolymorphism() {
		// Test that BeveragesBox can be treated as Resource
		Resource resource = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Juice", 12, 12, 12, 12);
		assertTrue(resource instanceof Resource);
		assertTrue(resource instanceof BeveragesBox);
	}
}
