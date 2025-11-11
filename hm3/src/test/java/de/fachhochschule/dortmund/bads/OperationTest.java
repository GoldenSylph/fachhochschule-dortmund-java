package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.resources.BeveragesBox;
import de.fachhochschule.dortmund.bads.resources.Resource;
import de.fachhochschule.dortmund.bads.systems.Operation;

class OperationTest {
	
	private Operation operation;
	private Resource resource1;
	private Resource resource2;
	
	@BeforeEach
	void setUp() {
		operation = new Operation();
		resource1 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		resource2 = new BeveragesBox(BeveragesBox.Type.REFRIGERATED, "Milk", 15, 15, 15, 12);
	}
	
	@Test
	void testOperationCreation() {
		assertNotNull(operation);
		assertEquals(0, operation.getResourcesCount());
	}
	
	@Test
	void testAddResource() {
		operation.addResource(resource1);
		
		assertEquals(1, operation.getResourcesCount());
		assertEquals(resource1, operation.getResource(0));
	}
	
	@Test
	void testAddMultipleResources() {
		operation.addResource(resource1);
		operation.addResource(resource2);
		
		assertEquals(2, operation.getResourcesCount());
		assertEquals(resource1, operation.getResource(0));
		assertEquals(resource2, operation.getResource(1));
	}
	
	@Test
	void testGetResource() {
		operation.addResource(resource1);
		operation.addResource(resource2);
		
		assertEquals(resource1, operation.getResource(0));
		assertEquals(resource2, operation.getResource(1));
	}
	
	@Test
	void testEmptyOperation() {
		assertEquals(0, operation.getResourcesCount());
	}
	
	@Test
	void testAddDifferentResourceTypes() {
		BeveragesBox box = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		
		operation.addResource(box);
		assertEquals(1, operation.getResourcesCount());
		assertEquals(box, operation.getResource(0));
	}
	
	@Test
	void testMultipleOperations() {
		Operation op1 = new Operation();
		Operation op2 = new Operation();
		
		op1.addResource(resource1);
		op2.addResource(resource2);
		
		assertEquals(1, op1.getResourcesCount());
		assertEquals(1, op2.getResourcesCount());
		assertEquals(resource1, op1.getResource(0));
		assertEquals(resource2, op2.getResource(0));
	}
	
	@Test
	void testAddSameResourceMultipleTimes() {
		operation.addResource(resource1);
		operation.addResource(resource1);
		
		assertEquals(2, operation.getResourcesCount());
		assertEquals(resource1, operation.getResource(0));
		assertEquals(resource1, operation.getResource(1));
	}
	
	@Test
	void testOperationWithManyResources() {
		for (int i = 0; i < 10; i++) {
			BeveragesBox box = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Box" + i, 10, 10, 10, 24);
			operation.addResource(box);
		}
		
		assertEquals(10, operation.getResourcesCount());
	}
	
	@Test
	void testGetCreationTime() {
		int creationTime = operation.getCreationTime();
		assertTrue(creationTime >= 0);
	}
	
	@Test
	void testAddNullResourceThrowsException() {
		assertThrows(Exception.class, () -> {
			operation.addResource(null);
		});
	}
}