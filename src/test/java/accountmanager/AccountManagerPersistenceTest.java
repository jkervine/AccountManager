package accountmanager;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import fi.iki.joker.accountmanager.persistence.AccountStorage;
import fi.iki.joker.accountmanager.persistence.AccountStorageException;
import fi.iki.joker.accountmanager.persistence.AccountStorageMongoDBImpl;

/**
 * This test requires a running mongodb @ localhost:27017
 * @author juha
 *
 */

public class AccountManagerPersistenceTest {

	private static AccountStorage storage;
	
	@BeforeClass
	public static void setup() {
		storage = new AccountStorageMongoDBImpl("testaccounts");
	}
	
	@Test
	public void testCreateAccount() {
		try {
			String accountId = storage.createAccount();
			assertNotNull(accountId);
		} catch (AccountStorageException e) {
			fail("Basic account creation failed: "+e.getMessage());
		}
	}
	
	@Test
	public void testNewAccountHasZeroBalance() {
		try {
			String accountId = storage.createAccount();
			assertNotNull(accountId);
			double balance = storage.getBalance(accountId);
			assertEquals(balance,0,10); 
		} catch (AccountStorageException e) {
			fail("Basic account creation failed: "+e.getMessage());
		}

	}
	
	@Test(expected = AccountStorageException.class)
	public void testWhenAccountIsDisabledCannotCheckBalance() throws AccountStorageException {
		String accountId = null;
		try {
			accountId = this.storage.createAccount();
			assertNotNull(accountId);
			storage.suspendAccount(accountId);
		} catch (AccountStorageException e) {
			fail("Basic account creation/susped failed: "+e.getMessage());
		}
		this.storage.getBalance(accountId);

	}

	@Test(expected = AccountStorageException.class)
	public void testWhenAccountIsDisabledCannotDeposit() throws AccountStorageException {
		String accountId = null;
		try {
			accountId = storage.createAccount();
			assertNotNull(accountId);
			storage.suspendAccount(accountId);
		} catch (AccountStorageException e) {
			fail("Basic account creation/susped failed: "+e.getMessage());
		}
		this.storage.depositTo(accountId, 10);
	}

	@Test(expected = AccountStorageException.class)
	public void testWhenAccountIsDisabledCannotWithdraw() throws AccountStorageException {
		String accountId = null;
		try {
			accountId = storage.createAccount();
			assertNotNull(accountId);
			storage.suspendAccount(accountId);
		} catch (AccountStorageException e) {
			fail("Basic account creation/susped failed: "+e.getMessage());
		}
		this.storage.withdrawFrom(accountId, 10);
	}
	
	@Test
	public void testDepositsAndWithdrawals() {
		String accountId = null;
		try {
			accountId = this.storage.createAccount();
			assertNotNull(accountId);
			storage.depositTo(accountId, 10);
			double balance = this.storage.getBalance(accountId);
			assertEquals(10,balance,2);
			storage.withdrawFrom(accountId, -5);
			double balanceAfter = this.storage.getBalance(accountId);
			assertEquals(5,balanceAfter,2);
		} catch (AccountStorageException e) {
			fail("Basic account creation/susped failed: "+e.getMessage());
		}
	}

	
}
