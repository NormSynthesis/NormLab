package es.csic.iiia.normlab.onlinecomm.agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import es.csic.iiia.normlab.onlinecomm.XMLParser.PopulationXMLManager;
import es.csic.iiia.normlab.onlinecomm.agents.profile.ComplaintProfile;
import es.csic.iiia.normlab.onlinecomm.agents.profile.UploadProfile;
import es.csic.iiia.normlab.onlinecomm.agents.profile.ViewProfile;

/**
 * 
 * @author Javi
 *
 */
public class OnlineCommunityPopulation {

	/**
	 * 
	 * @author Javier Morales
	 *
	 */
	public enum UserType {
		Moderate, Spammer, Pornographic, Violent, Rude;
	}

	private PopulationXMLManager populationLoader;
	private List<OnlineCommunityUser> userProfiles;
	private List<UserType> userTypes;
	private String name;
	private int size;

	/**
	 * 
	 * @param populationName
	 */
	public OnlineCommunityPopulation(String populationDir, 
			String populationName) {
		
		this.loadFromXML(populationDir, populationName);
	}

	/**
	 * @throws TransformerException 
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * 
	 */
	public void saveToXML(String fileDir, String filename)
			throws ParserConfigurationException, IOException, TransformerException {
		
		this.populationLoader.saveAs(userProfiles, fileDir, filename);
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	public int getNumUsers(UserType type) {
		OnlineCommunityUser user = this.getUserProfile(type);
		if(user != null) {
			return user.getQuantity();
		}
		return 0;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<UserType> getUserTypes() {
		return this.userTypes; 
	}

	/**
	 * 
	 * @param type
	 * @return
	 * @throws Exception 
	 */
	public UploadProfile getUploadProfile(UserType type) throws Exception {
		OnlineCommunityUser user = this.getUserProfile(type);
		if(user == null) {
			throw new Exception("Not found");
		}
		return user.getUploadProfile();
	}

	/**
	 * 
	 * @param type
	 * @return
	 * @throws Exception 
	 */
	public ViewProfile getViewProfile(UserType type) throws Exception {
		OnlineCommunityUser user = this.getUserProfile(type);
		if(user == null) {
			throw new Exception("Not found");
		}
		return user.getViewProfile();
	}

	/**
	 * 
	 * @param type
	 * @return
	 * @throws Exception 
	 */
	public ComplaintProfile getComplainProfile(UserType type) throws Exception {
		OnlineCommunityUser user = this.getUserProfile(type);
		if(user == null) {
			throw new Exception("Not found");
		}
		return user.getComplaintProfile();
	}

	/**
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 
	 * @return
	 */
	public List<OnlineCommunityUser> getUsers() {
		return this.userProfiles;
	}

	/**
	 * 
	 * @return
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * 
	 * @param userTypes
	 */
	public void setUserTypes(List<UserType> userTypes) {
		
	}
	
	/**
	 * 
	 */
	public void setName(String name) {
		this.name = name;
		
		for(UserType type : this.userTypes) {
			this.getUserProfile(type).setPopulationName(name);
		}
	}
	
	/**
	 * 
	 * @param type
	 * @param num
	 * @throws Exception 
	 */
	public void setNumUsers(UserType type, int num) throws Exception {
		OnlineCommunityUser user = this.getUserProfile(type);
		if(user == null) {
			throw new Exception("Not found");
		}
		user.setQuantity(num);
	}
	
	/**
	 * 
	 */
	private void loadFromXML(String populationDir, String populationName) {
		populationLoader = new PopulationXMLManager(populationDir, populationName);

		name = populationLoader.getPopulationName();
		userProfiles = populationLoader.getUserProfiles();
		userTypes = new ArrayList<UserType>();
		size = 0;

		for(OnlineCommunityUser user : userProfiles) {
			size += user.getQuantity();
			userTypes.add(this.getUserTypeAsEnum(user.getType()));
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private int getUserTypeAsInt(UserType type) {
		switch(type) {
		case Moderate:			return 1;
		case Spammer: 			return 2;
		case Pornographic: 	return 3;
		case Violent: 			return 4;
		case Rude: 					return 5;
		default:						return 0;
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private UserType getUserTypeAsEnum(int type) {
		switch(type) {
		case 1:		return UserType.Moderate;
		case 2: 	return UserType.Spammer;
		case 3:		return UserType.Pornographic;
		case 4: 	return UserType.Violent;
		case 5: 	return UserType.Rude;
		default:	return null;
		}
	}

	/**
	 * 
	 * @param type
	 * @return
	 */
	private OnlineCommunityUser getUserProfile(UserType type) {
		OnlineCommunityUser userRet = null;
		for(OnlineCommunityUser user : userProfiles) {
			if(user.getType() == this.getUserTypeAsInt(type)) {
				userRet = user;
				break;
			}
		}
		return userRet;
	}
}
