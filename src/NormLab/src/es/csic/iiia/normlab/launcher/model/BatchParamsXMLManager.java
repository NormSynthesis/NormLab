package es.csic.iiia.normlab.launcher.model;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.csic.iiia.normlab.launcher.ui.NormLabFrame.NormLabSimulator;

/**
 * 
 * @author Javier Morales (jmorales@iiia.csic.es)
 *
 */
public class BatchParamsXMLManager {

	private DocumentBuilderFactory dbFactory;
	private DocumentBuilder dBuilder;
	private Document paramsXML;

	private File xmlFile, xmlFileBackup;

	/**
	 * 
	 */
	public BatchParamsXMLManager(String fileDir, String fileName) {
		this.xmlFile = new File(fileDir + File.separator + fileName);
		this.xmlFileBackup = new File(fileDir + File.separator + fileName + ".backup");
	}

	/**
	 * 
	 */
	public void backupParameters() {
		try {
			FileUtils.copyFile(xmlFile, xmlFileBackup);
		}
		catch (IOException e) {
			e.printStackTrace();
		}	
	}

	/**
	 * 
	 */
	public void restoreParameters(NormLabSimulator sim) {
		try {
			FileUtils.copyFile(xmlFileBackup, xmlFile);
		}
		catch (IOException e) {
			e.printStackTrace();
		}	
	}

	/**
	 * 
	 * @param file
	 * @param attr
	 * @return
	 * @throws Exception
	 */
	public String getAttribute(String attr)
			throws Exception {
		
		dbFactory =	DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();
		paramsXML = dBuilder.parse(xmlFile);	
		String ret = "";
		
		NodeList nodeList = paramsXML.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {

			//We have encountered an <employee> tag.
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Node nameItem = node.getAttributes().getNamedItem("name");
				String name = nameItem.getNodeValue();

				if(name.equals(attr)) {
					Node valueItem = node.getAttributes().getNamedItem("defaultValue");
					ret = valueItem.getTextContent();
				}
			}
		}
		return ret;
	}
	
	/**
	 * 
	 */
	public void setAttribute(String attr, String value) 
			throws Exception {
		
		dbFactory =	DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();
		paramsXML = dBuilder.parse(xmlFile);
		
		NodeList nodeList = paramsXML.getDocumentElement().getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {

			//We have encountered an <employee> tag.
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Node nameItem = node.getAttributes().getNamedItem("name");
				String name = nameItem.getNodeValue();

				if(name.equals(attr)) {
					Node valueItem = node.getAttributes().getNamedItem("defaultValue");
					valueItem.setTextContent(value);
				}
			}
		}

		/* Save into XML file */
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(paramsXML);
		StreamResult result = new StreamResult(xmlFile);
		transformer.transform(source, result);
	} 
	
	/**
	 * 
	 */
	public void addAttribute(String attr, String value) 
			throws Exception {
		
		dbFactory =	DocumentBuilderFactory.newInstance();
		dBuilder = dbFactory.newDocumentBuilder();
		Element	root = null;
		
		try {
			paramsXML = dBuilder.parse(xmlFile);
			root = paramsXML.getDocumentElement();
		}
		catch (IOException e) {
			paramsXML = dBuilder.newDocument();
			root = paramsXML.createElement("configuration");
			paramsXML.appendChild(root);
		}
		
		/* (Just in case) If the attribute exists, change it */
		if(this.existsAttribute(attr)) {
			this.setAttribute(attr, value);
		}
		
		/* If it does not exist, create it */
		else {
			Element elem = paramsXML.createElement("parameter");
			paramsXML.createAttribute(attr);
			elem.setAttribute("name", attr);
			elem.setAttribute("defaultValue", value);
			root.appendChild(elem);

			/* Save into XML file */
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			
			// format the XML nicely
			transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			DOMSource source = new DOMSource(paramsXML);
			StreamResult result = new StreamResult(xmlFile);
			transformer.transform(source, result);
		}
	}

	/**
	 * 
	 * @param attr
	 * @return
	 */
	private boolean existsAttribute(String attr) {
	 	boolean exists = false;
	 	
	 	NodeList nodeList = paramsXML.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {

			//We have encountered an <employee> tag.
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Node nameItem = node.getAttributes().getNamedItem("name");
				String name = nameItem.getNodeValue();

				if(name.equals(attr)) {
					exists = true;
				}
			}
		}
	  return exists;
  } 
}
