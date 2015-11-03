package fi.iki.joker.accountmanager;
import org.springframework.context.annotation.*;

import fi.iki.joker.accountmanager.persistence.AccountStorage;
import fi.iki.joker.accountmanager.persistence.AccountStorageMongoDBImpl;

@Configuration

public class AccountServerConfig {
	
	@Bean
	public AccountStorage getStorage() {
		return new AccountStorageMongoDBImpl("accountmanager");
	}
	
}
