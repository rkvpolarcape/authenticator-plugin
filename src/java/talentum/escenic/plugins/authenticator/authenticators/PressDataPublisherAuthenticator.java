package talentum.escenic.plugins.authenticator.authenticators;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import net.kundservice.www.prenstatusws.login.LoginServiceLocator;
import net.kundservice.www.prenstatusws.login.LoginServiceSoapStub;
import net.kundservice.www.prenstatusws.login.UserStatusDto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implements authentication through Pressdata LoginService web service.
 * 
 * @author stefan.norman
 */
public class PressDataPublisherAuthenticator extends WSAuthenticator {

	private static Log log = LogFactory
			.getLog(PressDataPublisherAuthenticator.class);

	private String publisher;

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public AuthenticatedUser performLogin(String username, String password, String ipaddress)
			throws ServiceException, RemoteException {

		AuthenticatedUser user = null;

		// call web service top authenticate
		LoginServiceSoapStub binding = (LoginServiceSoapStub) new LoginServiceLocator()
				.getLoginServiceSoap();

		binding.setTimeout(getTimeout());

		UserStatusDto userSDto = binding.loginPublisher(publisher, username,
				password);

		if (!userSDto.isIsLoginOk()) {
			log.error("Authentication failed for user " + username);
		} else {
			// populate user object
			user = new PressDataUser(userSDto);
		}

		return user;
	}

}
