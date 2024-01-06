package it.polito.ezshop.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import it.polito.ezshop.data.*;

public class AccountBook {

	private List<BalanceOperation> operations;
	private double currentBalance;

	public AccountBook() {
		super();
		this.currentBalance = 0;
		this.operations = new ArrayList<BalanceOperation>();
	}

	public double computeBalance() {
		return currentBalance;
	}

	public void setCurrentBalance(double currentBalance) {
		this.currentBalance = currentBalance;
	}

	public List<BalanceOperation> getOperations() {
		return operations;
	}
	
	public void setOperations(List<BalanceOperation> operations) {
		this.operations = operations;
	}

	public int getIdOfNextBalanceOperation() {
		int maxId = 0;
		for (BalanceOperation bo : operations) {
			if (maxId < bo.getBalanceId()) {
				maxId = bo.getBalanceId();
			}
		}
		return maxId + 1;
	}

	public boolean addBalanceOperation(BalanceOperation bo) {
		if (bo.getType().equals("CREDIT")) {
			operations.add(bo);
			currentBalance += bo.getMoney();
		} else if (bo.getType().equals("DEBIT")) {
			operations.add(bo);
			currentBalance -= bo.getMoney();
		} else {
			return false;
		}
		return true;
	}

	public List<BalanceOperation> getOperationBetween(LocalDate from, LocalDate to) {
		ArrayList<BalanceOperation> res = new ArrayList<BalanceOperation>();

		if (from == null) {
			if (to == null)
				return operations; // from and to are null and so we want all the balance operations
			else
				for (BalanceOperation bo : operations)
					// from is null and so we want all the balance operations before to
					if (bo.getDate().isEqual(to) || bo.getDate().isBefore(to))
						res.add(bo);

			return res;
		}

		if (to == null) {
			for (BalanceOperation bo : operations)
				// to is null and so we want all the balance operations after from
				if (bo.getDate().isEqual(from) || bo.getDate().isAfter(from))
					res.add(bo);

			return res;
		}

		// Checking if the user inverted the dates
		if (from.isAfter(to)) {
			LocalDate tmp = from;
			from = to;
			to = tmp;
		}

		// neither null nor are null so we want all the balance operations after from
		// and before to
		for (BalanceOperation bo : operations)
			if (bo.getDate().isEqual(from) || bo.getDate().isEqual(to)
					|| (bo.getDate().isAfter(from) && bo.getDate().isBefore(to)))
				res.add(bo);

		return res;
	}
}
