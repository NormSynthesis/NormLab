package es.csic.iiia.normlab.launcher.onlinecomm;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to manipulate or create XML files.
 * 
 * @author Iosu Mendizabal
 *
 */
public class PopulationXMLManager {

	private String populationFileName;
	private String populationFileDir;

	private String populationName;
	private List<OnlineCommunityUser> profiles;

	/**
	 * First constructor of the XMLParser
	 * 
	 * @param populationFileName
	 * 			Name of the XML file to write.
	 * @param userProfiles
	 * 			ArrayList of the agents to save in the XML.
	 */
	public PopulationXMLManager(String populationFileDir,
			String populationFileName) {

		this.populationFileDir = populationFileDir;
		this.populationFileName = populationFileName;

		this.loadPopulation();
	}

	/**
	 * 
	 */
	private void loadPopulation() {
		profiles =	new ArrayList<OnlineCommunityUser>();

		try {
			File file = new File(populationFileDir + populationFileName);
			DocumentBuilderFactory dbFactory = 
					DocumentBuilderFactory.newInstance();

			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(file);

			doc.getDocumentElement().normalize();

			NodeList nList = doc.getElementsByTagName("agent");

			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;

					Double correct = Double.parseDouble(eElement.getElementsByTagName(
							"correct").item(0).getTextContent());
					Double spam = Double.parseDouble(eElement.getElementsByTagName(
							"spam").item(0).getTextContent());
					Double porn = Double.parseDouble(eElement.getElementsByTagName(
							"porn").item(0).getTextContent());
					Double violent = Double.parseDouble(eElement.getElementsByTagName(
							"violent").item(0).getTextContent());
					Double insult = Double.parseDouble(eElement.getElementsByTagName(
							"insult").item(0).getTextContent());
					Double uploadProb = Double.parseDouble(eElement.getElementsByTagName(
							"uploadProbability").item(0).getTextContent());

					//Upload profile settings
					UploadProfile uploadProfile = new UploadProfile(uploadProb, 
							correct, spam, porn, violent, insult);

					//View profile settings
					ViewProfile viewProfile = new ViewProfile(
							Double.parseDouble(eElement.getElementsByTagName("secForum").
									item(0).getTextContent()),
									Double.parseDouble(eElement.getElementsByTagName("secTheReporter").
											item(0).getTextContent()),
											Double.parseDouble(eElement.getElementsByTagName("secPhotoVideo").
													item(0).getTextContent()), 
													Integer.parseInt(eElement.getElementsByTagName("viewMode").
															item(0).getTextContent()));

					//Complaint Profile
					ComplaintProfile complaintProfile = new ComplaintProfile(
							Double.parseDouble(eElement.getElementsByTagName("complaintSpam").
									item(0).getTextContent()),
									Double.parseDouble(eElement.getElementsByTagName("complaintPorn").
											item(0).getTextContent()),
											Double.parseDouble(eElement.getElementsByTagName("complaintViolent").
													item(0).getTextContent()),
													Double.parseDouble(eElement.getElementsByTagName("complaintInsult").
															item(0).getTextContent()));

					int numAgent = 0;
					if(eElement.getElementsByTagName("name").
							item(0).getTextContent().equals("moderate")){
						numAgent = 1;
					}
					if(eElement.getElementsByTagName("name").
							item(0).getTextContent().equals("spammer")){
						numAgent = 2;
					}
					if(eElement.getElementsByTagName("name").
							item(0).getTextContent().equals("pornographic")){
						numAgent = 3;
					}
					if(eElement.getElementsByTagName("name").
							item(0).getTextContent().equals("violent")){
						numAgent = 4;
					}
					if(eElement.getElementsByTagName("name").
							item(0).getTextContent().equals("rude")){
						numAgent = 5;
					}
					if(eElement.getElementsByTagName("name").
							item(0).getTextContent().equals("another")){
						numAgent = 6;
					}

					this.populationName = eElement.getElementsByTagName("populationName").
							item(0).getTextContent();

					/* Create and add user */
					OnlineCommunityUser user = new OnlineCommunityUser(
							numAgent, Integer.parseInt(eElement.getElementsByTagName(
									"quantity").item(0).getTextContent()),
									eElement.getElementsByTagName("name").
									item(0).getTextContent(),
									uploadProfile, viewProfile, complaintProfile, populationName);

					profiles.add(user);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method to write an XML file with a given ArrayList.
	 * 
	 * @param users
	 * 			ArrayList of agents that we want to save in the XML.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws TransformerException 
	 */
	public void saveAs(List<OnlineCommunityUser> users, String popDir, String popFilename) 
			throws ParserConfigurationException, IOException, TransformerException {

		DocumentBuilderFactory dFact = DocumentBuilderFactory.newInstance();
		DocumentBuilder build = dFact.newDocumentBuilder();
		Document doc = build.newDocument();

		Element	root = doc.createElement("agents");
		doc.appendChild(root);

		for(int i = 0 ; i < users.size() ; i++ ) { 
			Element Details = doc.createElement("agent");
			root.appendChild(Details);

			Element quantity = doc.createElement("quantity");
			quantity.appendChild(doc.createTextNode(String.valueOf(users.get(i).getQuantity())));
			Details.appendChild(quantity);

			Element name = doc.createElement("name");
			name.appendChild(doc.createTextNode(String.valueOf(users.get(i).getName())));
			Details.appendChild(name);

			Element correct = doc.createElement("correct");
			correct.appendChild(doc.createTextNode(String.valueOf(users.get(i).getUploadProfile().getCorrect())));
			Details.appendChild(correct);

			Element insult = doc.createElement("insult");
			insult.appendChild(doc.createTextNode(String.valueOf(users.get(i).getUploadProfile().getInsult())));
			Details.appendChild(insult);

			Element spam = doc.createElement("spam");
			spam.appendChild(doc.createTextNode(String.valueOf(users.get(i).getUploadProfile().getSpam())));
			Details.appendChild(spam);

			Element violent = doc.createElement("violent");
			violent.appendChild(doc.createTextNode(String.valueOf(users.get(i).getUploadProfile().getViolent())));
			Details.appendChild(violent);

			Element porn = doc.createElement("porn");
			porn.appendChild(doc.createTextNode(String.valueOf(users.get(i).getUploadProfile().getPorn())));
			Details.appendChild(porn);

			Element uploadContent = doc.createElement("uploadProbability");
			uploadContent.appendChild(doc.createTextNode(String.valueOf(users.get(i).getUploadProfile().getUploadProbability())));
			Details.appendChild(uploadContent);

			Element forum = doc.createElement("secForum");
			forum.appendChild(doc.createTextNode(String.valueOf(users.get(i).getViewProfile().getForum())));
			Details.appendChild(forum);

			Element reporter = doc.createElement("secTheReporter");
			reporter.appendChild(doc.createTextNode(String.valueOf(users.get(i).getViewProfile().getTheReporter())));
			Details.appendChild(reporter);

			Element photoVideo = doc.createElement("secPhotoVideo");
			photoVideo.appendChild(doc.createTextNode(String.valueOf(users.get(i).getViewProfile().getPhotoVideo())));
			Details.appendChild(photoVideo);

			Element viewMode = doc.createElement("viewMode");
			viewMode.appendChild(doc.createTextNode(String.valueOf(users.get(i).getViewProfile().getViewMode())));
			Details.appendChild(viewMode);

			Element distributionMode = doc.createElement("distributionMode");
			distributionMode.appendChild(doc.createTextNode(String.valueOf(0)));
			Details.appendChild(distributionMode);

			Element cInsult = doc.createElement("complaintInsult");
			cInsult.appendChild(doc.createTextNode(String.valueOf(users.get(i).getComplaintProfile().getInsult())));
			Details.appendChild(cInsult);

			Element cSpam = doc.createElement("complaintSpam");
			cSpam.appendChild(doc.createTextNode(String.valueOf(users.get(i).getComplaintProfile().getSpam())));
			Details.appendChild(cSpam);

			Element cViolent = doc.createElement("complaintViolent");
			cViolent.appendChild(doc.createTextNode(String.valueOf(users.get(i).getComplaintProfile().getViolent())));
			Details.appendChild(cViolent);

			Element cPorn = doc.createElement("complaintPorn");
			cPorn.appendChild(doc.createTextNode(String.valueOf(users.get(i).getComplaintProfile().getPorn())));
			Details.appendChild(cPorn);

			Element popName = doc.createElement("populationName");
			popName.appendChild(doc.createTextNode(String.valueOf(users.get(i).getPopulation())));
			Details.appendChild(popName);
		}

		/* Save the document to the disk file */
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer aTransformer = tranFactory.newTransformer();

		/* Format the XML nicely */
		aTransformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
		aTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		aTransformer.setOutputProperty(OutputKeys.INDENT, "yes");

		DOMSource source = new DOMSource(doc);
		File file = new File(popDir + popFilename);
		if(!file.exists()) {
			file.createNewFile();
		}
		BufferedWriter fos = new BufferedWriter(new FileWriter(file));

		StreamResult result = new StreamResult(fos);
		aTransformer.transform(source, result);
	}

	/**
	 * 
	 * @return
	 */
	public String getPopulationName() {
		return this.populationName;
	}

	/**
	 * Loads a list of agents from a population XML file
	 * 
	 * @return a list of agents in a population XML file
	 */
	public List<OnlineCommunityUser> getUserProfiles() {
		return this.profiles;
	}
}
