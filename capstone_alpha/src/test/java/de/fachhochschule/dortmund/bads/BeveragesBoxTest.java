package de.fachhochschule.dortmund.bads;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.fachhochschule.dortmund.bads.resources.BeveragesBox;

class BeveragesBoxTest {
	
	private BeveragesBox ambientBox;
	private BeveragesBox refrigeratedBox;
	private BeveragesBox bulkBox;
	
	@BeforeEach
	void setUp() {
		ambientBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 24);
		refrigeratedBox = new BeveragesBox(BeveragesBox.Type.REFRIGERATED, "Milk", 15, 15, 15, 12);
		bulkBox = new BeveragesBox(BeveragesBox.Type.BULK, "Beer Keg", 25, 25, 25, 4);
	}
	
	@Test
	void testBeveragesBoxCreation() {
		assertNotNull(ambientBox);
		assertEquals(BeveragesBox.Type.AMBIENT, ambientBox.getType());
		assertEquals("Water", ambientBox.getBeverageName());
		assertEquals(10, ambientBox.getWidth());
		assertEquals(10, ambientBox.getHeight());
		assertEquals(10, ambientBox.getLength());
		assertEquals(24, ambientBox.getQuantity());
	}
	
	@Test
	void testAmbientType() {
		assertEquals(BeveragesBox.Type.AMBIENT, ambientBox.getType());
		assertEquals("Water", ambientBox.getBeverageName());
		assertEquals(24, ambientBox.getQuantity());
	}
	
	@Test
	void testRefrigeratedType() {
		assertEquals(BeveragesBox.Type.REFRIGERATED, refrigeratedBox.getType());
		assertEquals("Milk", refrigeratedBox.getBeverageName());
		assertEquals(15, refrigeratedBox.getWidth());
		assertEquals(15, refrigeratedBox.getHeight());
		assertEquals(15, refrigeratedBox.getLength());
		assertEquals(12, refrigeratedBox.getQuantity());
	}
	
	@Test
	void testBulkType() {
		assertEquals(BeveragesBox.Type.BULK, bulkBox.getType());
		assertEquals("Beer Keg", bulkBox.getBeverageName());
		assertEquals(25, bulkBox.getWidth());
		assertEquals(25, bulkBox.getHeight());
		assertEquals(25, bulkBox.getLength());
		assertEquals(4, bulkBox.getQuantity());
	}
	
	@Test
	void testDimensions() {
		assertEquals(10, ambientBox.getWidth());
		assertEquals(10, ambientBox.getHeight());
		assertEquals(10, ambientBox.getLength());
		
		assertEquals(15, refrigeratedBox.getWidth());
		assertEquals(15, refrigeratedBox.getHeight());
		assertEquals(15, refrigeratedBox.getLength());
		
		assertEquals(25, bulkBox.getWidth());
		assertEquals(25, bulkBox.getHeight());
		assertEquals(25, bulkBox.getLength());
	}
	
	@Test
	void testDifferentCapacities() {
		assertEquals(24, ambientBox.getQuantity());
		assertEquals(12, refrigeratedBox.getQuantity());
		assertEquals(4, bulkBox.getQuantity());
	}
	
	@Test
	void testCustomDimensions() {
		BeveragesBox customBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Soda", 11, 12, 13, 20);
		assertEquals(11, customBox.getWidth());
		assertEquals(12, customBox.getHeight());
		assertEquals(13, customBox.getLength());
		assertEquals(20, customBox.getQuantity());
	}
	
	@Test
	void testToString() {
		String result = ambientBox.toString();
		assertNotNull(result);
		assertTrue(result.contains("Water") || result.contains("AMBIENT"));
	}
	
	@Test
	void testDifferentNames() {
		BeveragesBox box1 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Orange Juice", 12, 12, 12, 12);
		BeveragesBox box2 = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Apple Juice", 12, 12, 12, 12);
		
		assertEquals("Orange Juice", box1.getBeverageName());
		assertEquals("Apple Juice", box2.getBeverageName());
		assertNotEquals(box1.getBeverageName(), box2.getBeverageName());
	}
	
	@Test
	void testLargeCapacity() {
		BeveragesBox largeBox = new BeveragesBox(BeveragesBox.Type.AMBIENT, "Water", 10, 10, 10, 100);
		assertEquals(100, largeBox.getQuantity());
	}
	
	@Test
	void testSmallCapacity() {
		BeveragesBox smallBox = new BeveragesBox(BeveragesBox.Type.REFRIGERATED, "Fresh Juice", 13, 13, 13, 6);
		assertEquals(6, smallBox.getQuantity());
	}
}