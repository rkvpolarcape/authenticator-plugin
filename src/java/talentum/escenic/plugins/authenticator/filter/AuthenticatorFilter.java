package talentum.escenic.plugins.authenticator.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import talentum.escenic.plugins.authenticator.AuthenticatorManager;
import talentum.escenic.plugins.authenticator.authenticators.AuthenticatedUser;
import talentum.escenic.plugins.authenticator.taglib.CookieUtil;

/**
 * J2EE Filter that checks if request iss missing user data cookie but has an
 * auto login cookie. If so an automatic login is performed.
 * 
 * @author stefan.norman
 * 
 */
public class AuthenticatorFilter implements Filter {

	public void destroy() {
	}

	public void doFilter(ServletRequest servletRequest,
			ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		Cookie userDataCookie = null;
		Cookie autologinCookie = null;
		Cookie[] allCookies = request.getCookies();
		for (int i = 0; allCookies != null && i < allCookies.length; i++) {
			if (allCookies[i].getName().equals(
					AuthenticatorManager.getInstance().getCookieName())) {
				userDataCookie = allCookies[i];
			} else if (allCookies[i].getName().equals(
					AuthenticatorManager.AUTOLOGIN_COOKIE)) {
				autologinCookie = allCookies[i];
			}
		}
		AuthenticatedUser user = null;
		// check the session cookie
		if (userDataCookie != null) {
			user = AuthenticatorManager.getInstance().getUser(
					userDataCookie.getValue());
		}
		// (user cookie is missing OR token is invalid) AND there is an
		// autologin cookie, then perform auto login
		if (user == null && autologinCookie != null) {
			user = AuthenticatorManager.getInstance().authenticateAuto(
					autologinCookie.getValue());
			if (user != null) {
				response
						.addCookie(CookieUtil.getSessionCookie(user.getToken()));
			}
		}
		// if a user is found add it to the request for later use
		if (user != null) {
			request.setAttribute("authenticatedUser", user);
		}

		chain.doFilter(servletRequest, servletResponse);
	}

	public void init(FilterConfig arg0) throws ServletException {
	}

}