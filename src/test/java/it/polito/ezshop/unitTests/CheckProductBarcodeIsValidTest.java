package it.polito.ezshop.unitTests;

import static org.junit.Assert.*;
import org.junit.Test;
import it.polito.ezshop.data.EZShop;

public class CheckProductBarcodeIsValidTest {

	@Test
	public void test01_nullBarCode() {
		assertFalse("checkProductBarcodeIsValid(null) should be false", EZShop.checkProductBarcodeIsValid(null));
	}

	@Test
	public void test02_emptyBarCode() {
		assertFalse("checkProductBarcodeIsValid(\"\") should be false", EZShop.checkProductBarcodeIsValid(""));
	}

	@Test
	public void test03_7CharLongBarCode() {
		assertFalse("checkProductBarcodeIsValid(\"7888387\") should be false",
				EZShop.checkProductBarcodeIsValid("7888387"));
	}

	@Test
	public void test03_11CharLongBarCode() {
		assertFalse("checkProductBarcodeIsValid(\"97888386688\") should be false",
				EZShop.checkProductBarcodeIsValid("97888386688"));
	}

	@Test
	public void test04_15CharLongBarBarCode() {
		assertFalse("checkProductBarcodeIsValid(\"978883866882111\") should be false",
				EZShop.checkProductBarcodeIsValid("978883866882111"));
	}

	@Test
	public void test04_18CharLongBarBarCode() {
		assertFalse("checkProductBarcodeIsValid(\"123456789412301243\") should be false",
				EZShop.checkProductBarcodeIsValid("123456789412301243"));
	}

	@Test
	public void test05_BarCodeWithLetters() {
		assertFalse("checkProductBarcodeIsValid(\"978883866882A\") should be false",
				EZShop.checkProductBarcodeIsValid("978883866882A"));
	}

	@Test
	public void test06_1_wrongCheckSum12BarCode() {
		assertFalse("checkProductBarcodeIsValid(\"123456789011\") should be false",
				EZShop.checkProductBarcodeIsValid("123456789011"));
	}

	@Test
	public void test06_2_wrongCheckSum13BarCode() {
		assertFalse("checkProductBarcodeIsValid(\"1234567890123\") should be false",
				EZShop.checkProductBarcodeIsValid("1234567890123"));
	}

	@Test
	public void test06_3_wrongCheckSum14BarCode() {
		assertFalse("checkProductBarcodeIsValid(\"12345678901234\") should be false",
				EZShop.checkProductBarcodeIsValid("12345678901234"));
	}

	@Test
	public void test07_1_correct12BarCode() {
		assertTrue("checkProductBarcodeIsValid(\"012345678905\") should be true",
				EZShop.checkProductBarcodeIsValid("012345678905"));
	}

	@Test
	public void test07_2_correct13BarCode() {
		assertTrue("checkProductBarcodeIsValid(\"9788838668821\") should be true",
				EZShop.checkProductBarcodeIsValid("9788838668821"));
	}

	@Test
	public void test07_3_correct14BarCode() {
		assertTrue("checkProductBarcodeIsValid(\"00012345678905\") should be true",
				EZShop.checkProductBarcodeIsValid("00012345678905"));
	}

	@Test
	public void test07_4_correct12BarCode() {
		assertTrue("checkProductBarcodeIsValid(\"123456789012\") should be true",
				EZShop.checkProductBarcodeIsValid("123456789012"));
	}

	@Test
	public void test07_5_correct12BarCode() {
		assertTrue("checkProductBarcodeIsValid(\"233254321519\") should be true",
				EZShop.checkProductBarcodeIsValid("233254321519"));
	}

	@Test
	public void test07_6_correct13BarCode() {
		assertTrue("checkProductBarcodeIsValid(\"6291041500213\") should be true",
				EZShop.checkProductBarcodeIsValid("6291041500213"));
	}
	

	@Test
	public void test07_7_correct14BarCode() {
		assertTrue("checkProductBarcodeIsValid(\"54326476412231\") should be true",
				EZShop.checkProductBarcodeIsValid("54326476412231"));
	}
}
