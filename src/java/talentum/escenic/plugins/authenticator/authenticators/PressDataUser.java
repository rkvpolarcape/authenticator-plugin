package talentum.escenic.plugins.authenticator.authenticators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.kundservice.www.WS.Authorization.UserStruct;
import net.kundservice.www.prenstatusws.loginservice.NTUserStatusDto;
import net.kundservice.www.prenstatusws.loginservice.ProductDto;
import net.kundservice.www.prenstatusws.loginservice.UserStatusDto;

import org.apache.commons.collections.ListUtils;


public class PressDataUser implements AuthenticatedUser {

	private int userId;
	private int ntUserId = 0;
	private String token;
	private String userName;
	private String name;
	private String companyName;
	private String email;
	private String productId;
	private int customerNumber = 0;
	private Date expirationDate;
	private Date loggedInTime = new Date();

	private ArrayList products = new ArrayList();

	/**
	 * Constructs a user from the LoginService web service
	 * @param userSDto
	 */
	public PressDataUser(UserStatusDto userSDto, int ntUserId) {
		this.userId = userSDto.getUserId();
		if(ntUserId > 0) {
			this.ntUserId = ntUserId + 10000000; //add 10 million to distinguish from normal user ids.
		}
		this.userName = userSDto.getUsername();
		this.name = userSDto.getFirstname() + " " + userSDto.getLastname();
		this.companyName = userSDto.getCompanyName();
		this.email = userSDto.getEmail();
		// set smno as cutomer number
		this.customerNumber = userSDto.getSmno();
		
		
		ProductDto[] prDto = userSDto.getProducts();
		for (int i = 0; i < prDto.length; i++) {
			addProduct(prDto[i].getProductId(), prDto[i].getStatus(), prDto[i].getRoles());
			// token of last product used as token of user
			this.token = prDto[i].getToken();
			// productId of last product used as productId of user
			this.productId = prDto[i].getProductId();
		}
	}

	/**
	 * Constructs a user from the NTLoginService web service
	 * @param userSDto
	 * @param ntuserValidDays how many days to add to createdtime to set expirationDate
	 */
	public PressDataUser(NTUserStatusDto userSDto, int ntuserValidDays) {
		this.userId = userSDto.getUserId() + 10000000; //add 10 million to distinguish from normal user ids.
		this.ntUserId = this.userId;
		this.userName = userSDto.getUserName();
		this.name = userSDto.getUserName();
		this.email = userSDto.getEmail();
		this.token = userSDto.getToken();
		// add configured number of days to createdTime set the expirationDate
		Calendar expiration = userSDto.getCreatedTime();
		expiration.add(Calendar.DATE, ntuserValidDays);
		this.expirationDate = userSDto.getCreatedTime().getTime();
		
		ProductDto prDto = userSDto.getProducts();
		addProduct(prDto.getProductId(), prDto.getStatus(), prDto.getRoles());
		// productId of last product used as productId of user
		this.productId = prDto.getProductId();
	}
	
	/**
	 * Constructs a user from the Authorization web service
	 * @param userStruct
	 * @param product
	 */
	public PressDataUser(UserStruct userStruct, String product) {
		this.userId = Integer.parseInt(userStruct.getUserId());
		this.userName = userStruct.getUserName();
		this.name = userStruct.getFirstName() + " " + userStruct.getLastName();
		this.companyName = userStruct.getCompanyName();
		this.email = userStruct.getEMail();
		this.token = userStruct.getToken();
		this.productId = product;
		// the basic ws has only one product
		addProduct(product, userStruct.getStatus(), userStruct.getRoles());
	}
	
	public int getUserId() {
		return userId;
	}

	public int getSourceUserId() {
		return ntUserId;
	}

	public String getToken() {
		return token;
	}

	public String getUserName() {
		return userName;
	}

	public String getName() {
		return name;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getEmail() {
		return email;
	}

	public String getProductId() {
		return productId;
	}

	public int getCustomerNumber() {
		return customerNumber;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public Date getLoggedInTime() {
		return loggedInTime;
	}

	public void addProduct(String productId, String status, String[] roles) {
		products.add(new Product(productId, status, roles));
	}

	public String[] getRoles() {
		ArrayList roles = new ArrayList();
		for (Iterator iter = products.iterator(); iter.hasNext();) {
			Product product = (Product) iter.next();
			roles.addAll(Arrays.asList(product.getRoles()));
		}
		return (String[]) roles.toArray(new String[roles.size()]);
	}

	public String[] matchingRoles(String[] roles) {
		// intersecion() returns a new list containing all elements that are contained in both given lists
		List diff = ListUtils.intersection(Arrays.asList(getRoles()), Arrays.asList(roles));
		return (String[]) diff.toArray(new String[diff.size()]);
	}


	/**
	 * Check if user has passive role(s)
	 * 
	 * @return true If user has no active roles and at least one matching passive role
	 */
	public boolean hasPassiveStatusForRole(String[] roles) {
		int passiveRoleMatches = 0;
		int activeRoleMatches = 0;
		for (Iterator iter = products.iterator(); iter.hasNext();) {
			Product product = (Product) iter.next();
			// intersecion() returns a new list containing all elements that are contained in both given lists
			List diff = ListUtils.intersection(Arrays.asList(product.getRoles()), Arrays.asList(roles));
			if(!diff.isEmpty()) {
				if(product.isPassive()) {
					passiveRoleMatches++;
				} else {
					activeRoleMatches++;
				}
			}
		}
		return (activeRoleMatches==0 && passiveRoleMatches > 0);
	}

	public String getAdminPage() {
		return null;
	}

	private class Product implements Serializable {
		
		private static final String STATUS_NORMAL = "NORMAL";
		private static final String STATUS_PASSIVE = "PASSIVE";
		private static final String STATUS_PENDING = "PENDING";

		String productId;
		String status;
		String[] roles;
		
		public Product(String productId, String status, String[] roles) {
			this.productId = productId;
			this.status = status;
			this.roles = new String[roles.length];
			for (int i = 0; i < roles.length; i++) {
				this.roles[i] = roles[i].trim();
			}
		}

		public String[] getRoles() {
			return roles;
		}

		public boolean isPassive() {
			return status.equals(STATUS_PASSIVE);
		}
		
	}

	public String[] getProductIds() {
		return null;
	}

	public List getProducts() {
		return products;
	}

	
}
