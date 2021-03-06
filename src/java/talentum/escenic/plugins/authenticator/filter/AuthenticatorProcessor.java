package talentum.escenic.plugins.authenticator.filter;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import talentum.escenic.plugins.authenticator.AuthenticatorManager;
import talentum.escenic.plugins.authenticator.authenticators.AuthenticatedUser;

import com.escenic.presentation.servlet.GenericProcessor;
import com.escenic.servlet.Constants;

public class AuthenticatorProcessor extends GenericProcessor implements
		Constants {

	private static Log log = LogFactory.getLog(AuthenticatorProcessor.class);

	public boolean doBefore(ServletContext pContext,
			ServletRequest pServletRequest, ServletResponse pServletResponse)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) pServletRequest;
		HttpServletResponse response = (HttpServletResponse) pServletResponse;

		// check if it's static content, then skip it
		String contextPath = (String) request.getAttribute("com.escenic.context.path");
		if(contextPath != null && (
				contextPath.startsWith("static") ||
				contextPath.endsWith(".jpg") ||
				contextPath.endsWith(".gif") ||
				contextPath.endsWith(".png") ||
				contextPath.endsWith(".js") ||
				contextPath.endsWith(".css") ||
				contextPath.endsWith(".swf") ||
				contextPath.endsWith(".html")
				)
		) {
			return true;
		}
		
		String publicationName = (String) request
				.getAttribute("com.escenic.publication.name");
		if (publicationName == null) {
			log
					.error("Could not find publication name. Make sure the AuthenticatorFilter is loaded AFTER EscenicStandardFilterChain.");
		}

		AuthenticatedUser user = AuthenticatorManager.getInstance()
				.getUserFromCookie(publicationName, request.getCookies());
		
		// backdoor to perform autologin (from Tiger)
		if(user == null && request.getParameter("artefact")!=null) {
			user = AuthenticatorManager.getInstance().authenticateUsingToken(publicationName, request.getParameter("artefact"));
		}
		
		// if a user is found add it to the request for later use
		if (user != null) {
			request.setAttribute("authenticatedUser", user);
		}

		return true;
	}
}
