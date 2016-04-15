package es.csic.iiia.normlab.launcher.onlinecomm;


/**
 * Upload profile of the agent
 * 
 * @author davidsanchezpinsach
 * 
 */
public class UploadProfile {

	// Type of different upload content percentage
	private double correct = 0;
	private double insult = 0;
	private double spam = 0;
	private double violent = 0;
	private double porn = 0;

	private double uploadProbability = 0; // probability to upload contents

	/**
	 * Constructor of the uploadProfile
	 * 
	 * @param correct
	 *            Correct content frequency
	 * @param insult
	 *            Insult content frequency
	 * @param spam
	 *            Spam content frequency
	 * @param violent
	 *            Ciolent content frequency
	 * @param porn
	 *            Porn content frequency
	 * @param uploadProbability
	 *            UpLoadContent content frequency
	 */
	public UploadProfile(double uploadProb, double correct, double spam, double porn, double violent, double insult) {
		this.uploadProbability = uploadProb;
		this.correct = correct;
		this.spam = spam;
		this.porn = porn;
		this.violent = violent;
		this.insult = insult;
	}



	/**
	 * 
	 * 
	 * Getters and Setters
	 * 
	 * 
	 */
	public double getCorrect() {
		return correct;
	}

	public void setCorrect(double correct) {
		this.correct = correct;
	}

	public double getInsult() {
		return insult;
	}

	public void setInsult(double insult) {
		this.insult = insult;
	}

	public double getSpam() {
		return spam;
	}

	public void setSpam(double spam) {
		this.spam = spam;
	}

	public double getViolent() {
		return violent;
	}

	public void setViolent(double violent) {
		this.violent = violent;
	}

	public double getPorn() {
		return porn;
	}

	public void setPorn(double porn) {
		this.porn = porn;
	}


	public double getUploadProbability() {
		return uploadProbability;
	}

	public void setUploadProbability(double upLoadContent) {
		this.uploadProbability = upLoadContent;
	}
}
