package it.polito.ezshop.unitTests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.polito.ezshop.model.EZProductType;

public class EZProductTypeSettersTest {

	@Test
	public void testSetQuantity() {
		EZProductType p = new EZProductType(1, "Prod1", "012345678905", 1.2, "this is a test");
		
		p.setQuantity(3);
		assertTrue(p.getQuantity() == 3);
	}
	
	
	@Test
	public void testSetLocation() {
		EZProductType p = new EZProductType(1, "Prod1", "012345678905", 1.2, "this is a test");
		
		p.setLocation("11-aa-11");
		assertTrue(p.getLocation().equals("11-aa-11"));
	}
	
	@Test
	public void testSetNote() {
		EZProductType p = new EZProductType(1, "Prod1", "012345678905", 1.2, "this is a test");
		
		p.setNote("test");
		assertTrue(p.getNote().equals("test"));
	}
	
	
	@Test
	public void testSetProductDescription() {
		EZProductType p = new EZProductType(1, "Prod1", "012345678905", 1.2, "this is a test");
		
		p.setProductDescription("prod3");
		assertTrue(p.getProductDescription().equals("prod3"));
	}
	
	
	@Test
	public void testSetBarCode() {
		EZProductType p = new EZProductType(1, "Prod1", "012345678905", 1.2, "this is a test");
		
		p.setBarCode("00012345678905");
		assertTrue(p.getBarCode().equals("00012345678905"));
	}
	
	
	@Test
	public void testSetPricePerUnit() {
		EZProductType p = new EZProductType(1, "Prod1", "012345678905", 1.2, "this is a test");
		
		p.setPricePerUnit(3.7);
		assertTrue(p.getPricePerUnit() == 3.7);
	}
	
	
	@Test
	public void testSetId() {
		EZProductType p = new EZProductType(1, "Prod1", "012345678905", 1.2, "this is a test");
		
		p.setId(3);
		assertTrue(p.getId() == 3);
	}
	
}
