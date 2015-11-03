package fi.iki.joker.accountmanager.persistence;

import fi.iki.joker.accountmanager.persistence.AccountStorageException;

public interface AccountStorage {

	String createAccount() throws AccountStorageException;;
	boolean suspendAccount(String accountId) throws AccountStorageException;;
	double depositTo(String accountId, double amount) throws AccountStorageException;;
	double withdrawFrom(String accountId, double amount) throws AccountStorageException;;
	Double getBalance(String accountId) throws AccountStorageException;
	
}
