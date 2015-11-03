package fi.iki.joker.accountmanager.persistence;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.SecureRandom;

import org.jvnet.hk2.annotations.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

@Service
public class AccountStorageMongoDBImpl implements AccountStorage {

	private static final String FACTCOLLECTION = "accounts";
	private static final String DBNAME_DEFAULT = "accountmgr";
	
	private static MongoClient mongoClient = null;
	private static DB db = null;
	private static DBCollection coll = null;
	private static String currentDbName = null;
	
	private SecureRandom random = new SecureRandom();

		
	public AccountStorageMongoDBImpl(String dbName) {
		currentDbName = dbName;
		if (mongoClient == null) {
			try {
				mongoClient = new MongoClient( "localhost:27017" );
			} catch (UnknownHostException e) {
				System.exit(1);
			}
			if(currentDbName == null) {
				db = mongoClient.getDB(DBNAME_DEFAULT);
			} else {
				db = mongoClient.getDB(currentDbName);
			}
			coll = db.getCollection(FACTCOLLECTION); 
		}
	}

	/**
	 * generate next account id
	 * (should verify uniqueness from db but this is not yet implemented)
	 * @return
	 */
	
	private String nextAccountId() {
		return new BigInteger(130, random).toString(32);
	}
	
	
	@Override
	public String createAccount() throws AccountStorageException {
		String errorMessage = "Failed to create account.";
		DBObject newAccount = new BasicDBObject();
		newAccount.put("balance", new Double(0));
		newAccount.put("active", true);
		newAccount.put("accountId", nextAccountId());
		coll.insert(newAccount);
		String id = newAccount.get( "accountId" ).toString();
		if(id != null) {
			// our account numbers are mongodb row ids for simplicity and uniqueness
			// this might create guessable account IDs which is bad for security though!
			return id.toString();
		}
		throw new AccountStorageException(errorMessage);
	}
	
	private DBObject getAccount(String accountId) throws AccountStorageException {
		DBObject accountQuery = new BasicDBObject();
		accountQuery.put("accountId", accountId);
		DBCursor found = coll.find(accountQuery);
		if(found.count() != 1) {
			throw new AccountStorageException("No such account: "+accountId);
		}
		DBObject account = found.one();
		if((Boolean)account.get("active") == false) {
			throw new AccountStorageException("Account is disabled!");
		}
		return found.one();
	}


	@Override
	public boolean suspendAccount(String accountId) throws AccountStorageException {
		DBObject account = getAccount(accountId);
		account.put("active", false);
		WriteResult res = coll.update(new BasicDBObject("accountId",accountId), account);
		if(res.isUpdateOfExisting()) {
			return true;
		}
		throw new AccountStorageException("Failed to susped account with accountId "+accountId);
	}

	@Override
	public double depositTo(String accountId, double amount) throws AccountStorageException {
		if(amount < 0) {
			throw new AccountStorageException("Error: Can only deposit positive sums!");
		}
		DBObject account = getAccount(accountId);
		double currentBalance = (Double)account.get("balance");
		double newBalance = currentBalance + amount;
		account.put("balance", newBalance);
		WriteResult res = coll.update(new BasicDBObject("accountId",accountId), account);
		if(res.isUpdateOfExisting()) {
			return newBalance;
		}
		throw new AccountStorageException("Failed to deposit to account with accountId "+accountId);

	}

	@Override
	public double withdrawFrom(String accountId, double amount) throws AccountStorageException {
		if(amount >= 0) {
			throw new AccountStorageException("Error: Can only withdraw negative sums!");
		}
		DBObject account = getAccount(accountId);
		double currentBalance = (Double)account.get("balance");
		double newBalance = currentBalance + amount;
		if(newBalance < 0) {
			throw new AccountStorageException("Withdrawal amount exceeds amount of funds on account!");
		}
		account.put("balance", newBalance);
		WriteResult res = coll.update(new BasicDBObject("accountId",accountId), account);
		if(res.isUpdateOfExisting()) {
			return newBalance;
		}
		throw new AccountStorageException("Failed to deposit to account with accountId "+accountId);
	}

	@Override
	public Double getBalance(String accountId) throws AccountStorageException {
		DBObject res = getAccount(accountId);
		return (Double)res.get("balance");
	}
	
	
	
}
