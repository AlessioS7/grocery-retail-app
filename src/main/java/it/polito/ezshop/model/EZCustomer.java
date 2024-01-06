package it.polito.ezshop.model;

import it.polito.ezshop.data.Customer;

public class EZCustomer implements Customer {

    private String customerName;
    private String customerCard;
    private Integer id;
    private Integer points;

    
    public EZCustomer(Integer id, String customerName) {
		super();
		this.customerName = customerName;
		this.id = id;
		this.points = 0;
	}
    
    @Override
    public String getCustomerName() {
        return this.customerName;
    }

	@Override
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Override
    public String getCustomerCard() {
        return this.customerCard;
    }

    @Override
    public void setCustomerCard(String customerCard) {
        this.customerCard = customerCard;
    }

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getPoints() {
        return this.points;
    }

    @Override
    public void setPoints(Integer points) {
    	if(this.customerCard != null)
    		this.points = points;
    }
    
    
    public boolean modifyPointsOnCard(int pointsToBeAdded) {
    	// Checking if the card has enough points
        if (points + pointsToBeAdded < 0)
            return false;

        points += pointsToBeAdded;
    	return true;
    }
}
