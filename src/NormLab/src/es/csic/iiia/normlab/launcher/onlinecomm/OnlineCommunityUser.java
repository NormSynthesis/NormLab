package es.csic.iiia.normlab.launcher.onlinecomm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.scenario.data.ContextData;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;

/**
 * Agents action class.
 * 
 * @author Iosu Mendizabal
 * 
 */
public class OnlineCommunityUser {

	private long id;
	private int section;

	// Profiles of the agent
	protected UploadProfile upLoadProfile;
	protected ViewProfile viewProfile;
	protected ComplaintProfile complaintProfile;
	protected Context<Object> context;

		// Agents characteristics
	private int type;
	private int quantity;
	private String name, population;

	/**
	 * Constructor to generate populations of the agents with a same type
	 * 
	 * @param type
	 *        Type of the agent
	 * @param quantity
	 *        Quantity of the agent
	 * @param name
	 *        Name of the agent
	 * @param up
	 *        UploadProfile
	 * @param vp
	 *        ViewProfile
	 * @param cp
	 *        ComplaintProfile
	 * @param population
	 *        Name of the population to use
	 */
	public OnlineCommunityUser(int type, int quantity, String name, UploadProfile up,
			ViewProfile vp, ComplaintProfile cp, String population) {

		this.type = type;
		this.quantity = quantity;
		this.name = name;
		this.upLoadProfile = up;
		this.viewProfile = vp;
		this.complaintProfile = cp;
		this.population = population;
	}

	/**
	 * Getters and Setters
	 * 
	 * 
	 */
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UploadProfile getUploadProfile() {
		return upLoadProfile;
	}

	public void setUploadProfile(UploadProfile upLoadProfile) {
		this.upLoadProfile = upLoadProfile;
	}

	public ViewProfile getViewProfile() {
		return viewProfile;
	}

	public void setViewProfile(ViewProfile viewProfile) {
		this.viewProfile = viewProfile;
	}

	public ComplaintProfile getComplaintProfile() {
		return complaintProfile;
	}

	public void setComplaintProfile(ComplaintProfile complaintProfile) {
		this.complaintProfile = complaintProfile;
	}

	public String getPopulation(){
		return population;
	}
	
	public void setPopulationName(String populationName) {
		this.population = populationName;
	}

	public long getId() {
		return this.id;
	}

	public int getSection() {
		return this.section;
	}
}
