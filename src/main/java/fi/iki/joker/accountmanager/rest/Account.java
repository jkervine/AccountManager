package fi.iki.joker.accountmanager.rest;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.owlike.genson.Genson;

import fi.iki.joker.accountmanager.AccountServerConfig;
import fi.iki.joker.accountmanager.persistence.AccountStorage;
import fi.iki.joker.accountmanager.persistence.AccountStorageException;

@Component
@Path("/account")
public class Account {

	@Context
	private ServletContext application;

	@Autowired
	@Qualifier("getStorage")
	public AccountStorage store;

	/**
	 * This is messed up. There was some problem using Spring IoC with the
	 * Jersey servlet so I need to manually pull the storage service from
	 * spring container
	 * @return
	 */
	
	public AccountStorage getStore() {
		@SuppressWarnings("resource")
		ApplicationContext springContext = new AnnotationConfigApplicationContext(AccountServerConfig.class);
        return (AccountStorage) springContext.getBean("getStorage");
	}
	
	/**
	 * Register account
	 * @param req
	 * @return
	 */

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public String createAccount() {
		try {
			return new Genson().serialize(getStore().createAccount());
		} catch (AccountStorageException e) {
			return new Genson().serialize(e.getMessage());
		}
	}

	/**
	 * Inactivate account
	 * @param req
	 * @return
	 */

	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	public String revokeAccount(@DefaultValue("") @QueryParam("accountId") String accountId) {
		try {
			if(getStore().suspendAccount(accountId)) {
				return new Genson().serialize("Account suspended");
			}
		} catch (AccountStorageException e) {
			return new Genson().serialize("Failed to suspend account: "+e.getMessage());
		}
		return new Genson().serialize("Failed to suspend account");
	}


	/**
	 * Get account balance	
	 * @return
	 */

	@Path("/balance")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getBalance(@DefaultValue("") @QueryParam("accountId") String accountId) {
		double balance;
		try {
			balance = getStore().getBalance(accountId);
		} catch (AccountStorageException e) {
			return new Genson().serialize("Failed to query balance: "+e.getMessage());
		}
		return new Genson().serialize(new Double(balance));
	}

	@Path("/balance")
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public String deposit(@DefaultValue("") @QueryParam("accountId") String accountId, 
			@DefaultValue("0") @QueryParam("amount") String amount) {
		double amountDouble;
		double newBalance;
		try {
			amountDouble = Double.parseDouble(amount);	  
		} catch (NumberFormatException e) {
			return new Genson().serialize("Invalid amount given to deposit.");
		}
		if(amountDouble == 0) {
			return new Genson().serialize("No action taken because of zero amount of money.");
		} else if(amountDouble > 0) {
			try {
				newBalance = getStore().depositTo(accountId, amountDouble);
			} catch ( AccountStorageException e) {
				return new Genson().serialize("Failed to make a deposit: "+e.getMessage());
			}
		} else {
			try {
				newBalance = getStore().withdrawFrom(accountId, amountDouble);
			} catch ( AccountStorageException e) {
				return new Genson().serialize("Failed to make a withdrawal: "+e.getMessage());
			}
		}
		return new Genson().serialize("New balance: "+newBalance);
	}

} 