package fi.iki.joker.accountmanager;

import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.persistence.jaxb.rs.MOXyJsonProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import fi.iki.joker.accountmanager.persistence.AccountStorage;
import fi.iki.joker.accountmanager.rest.CORSFilter;

public class RunAccountServer {
	
	public static void main(String[] args) throws Exception {
		String dbName = "accountmanager";
		Integer port = 8080;
		System.out.println("Starting Jersey and Jetty server...");
		ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.SESSIONS);
		ApplicationContext springContext = new AnnotationConfigApplicationContext(AccountServerConfig.class);
        servletContext.setContextPath("/");
        FilterHolder holder = new FilterHolder();
        holder.setFilter(new CORSFilter());
        servletContext.addFilter(holder, "/*", EnumSet.of(DispatcherType.REQUEST));
        
        Server jettyServer = new Server(port);
        jettyServer.setHandler(servletContext);
        AccountStorage storageService = (AccountStorage) springContext.getBean("getStorage");
        if(storageService != null) {
        	servletContext.addBean(storageService);
        } else {
        	System.out.println("Cannot initialize storage backend");
        	System.exit(1);
        }
        ServletHolder jerseyServlet = servletContext.addServlet(
             org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);
 
        jerseyServlet.setInitParameter(
           "jersey.config.server.provider.classnames",
           fi.iki.joker.accountmanager.rest.Account.class.getCanonicalName());
        // log verbose
        jerseyServlet.setInitParameter(
        	"jersey.config.server.tracing","ALL");
        jerseyServlet.setInitParameter(
        		"jersey.config.server.tracing.threshold", "VERBOSE");
        
        MOXyJsonProvider moxyJsonProvider = new MOXyJsonProvider();
        moxyJsonProvider.setWrapperAsArrayName(true);
   
        System.out.println("Starting...");
        jettyServer.start();
        System.out.println("Started at port "+port+". Joining...");
        jettyServer.join();
       
    }

}


