package it.polito.ezshop.model;

import java.time.LocalDate;

import it.polito.ezshop.data.BalanceOperation;

public class EZBalanceOperation implements BalanceOperation{

    private int balanceId;
    private LocalDate date;
    private double money;
    private String type;
    
    
    public EZBalanceOperation(int balanceId, LocalDate date, double money, String type) {
		super();
		this.balanceId = balanceId;
		this.date = date;
		this.money = money;
		this.type = type;
	}

	@Override
    public int getBalanceId() {
        return this.balanceId;
    }

    @Override
    public void setBalanceId(int balanceId) {
        this.balanceId = balanceId;
    }

    @Override
    public LocalDate getDate() {
        return this.date;
    }

    @Override
    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public double getMoney() {
        return this.money;
    }

    @Override
    public void setMoney(double money) {
        this.money = money;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
    
}
