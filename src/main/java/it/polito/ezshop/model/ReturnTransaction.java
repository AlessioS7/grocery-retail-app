package it.polito.ezshop.model;

import java.util.HashMap;

public class ReturnTransaction {

	Integer returnId;
	Integer saleId;
	HashMap<String, Integer> products;
	double moneyToReturn;
	String state;

	public ReturnTransaction(Integer returnId, Integer saleId) {
		super();
		this.returnId = returnId;
		this.saleId = saleId;
		this.products = new HashMap<String, Integer>();
		this.moneyToReturn = 0.0;
		this.state = "active";
	}

	public Integer getReturnId() {
		return returnId;
	}

	public void setReturnId(Integer returnId) {
		this.returnId = returnId;
	}

	public Integer getSaleId() {
		return saleId;
	}

	public void setSaleId(Integer saleId) {
		this.saleId = saleId;
	}

	public HashMap<String, Integer> getProducts() {
		return products;
	}

	public void setProducts(HashMap<String, Integer> products) {
		this.products = products;
	}
	
	public double getMoneyToReturn() {
		return moneyToReturn;
	}

	public void setMoneyToReturn(double moneyToReturn) {
		this.moneyToReturn = moneyToReturn;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

}
