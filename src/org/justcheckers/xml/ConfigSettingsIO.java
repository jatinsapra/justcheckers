package org.justcheckers.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import android.util.Log;

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
 
/**
* Concrete child class of the XML_IO class. Use its load() and save() methods
* to write XML into/write out XML from, the associated ConfigSettings object.
* @author Brinick Simmons (brinick@users.sourceforge.net)
*/
public class ConfigSettingsIO extends XML_IO{

	//---------------------------//
	//      Class Methods        //
	//---------------------------//

	//---------------------------------------------------------------------
	// -- Interface -------------------------------------------------------
	//---------------------------------------------------------------------

	/** 
	* Causes the XML data held within the File object referenced by the member 
	* variable "file" (inherited from XML_IO) to be loaded into the ConfigSettings
	* object associated with this class. The reading in of XML data and subsequent 
	* transformation is dealt with by the inner reader class of this class.
	*/
	public void load(){
		try{		
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(getFile());
			Element rootElement = doc.getRootElement();
			ConfigSettingsIOReader csior = new ConfigSettingsIOReader();
			csior.visit(rootElement);
		}
		catch(IOException e){
			String msg = "Problem : " + e.getMessage();
			Log.e("ConfigSettingsIO", msg);
		}
		catch(JDOMException e){
			String msg = "Problem : " + getFile().toString() 
				+ " is not a well formed XML document";
			Log.e("ConfigSettingsIO", msg);
		}
	}

	/** 
	* Causes the data held within the ConfigSettings object associated with 
	* this class to be saved as XML format into the File object referenced by 
	* the member variable "file". Transformation of ConfigSettings data into XML 
	* format and subsequent writing to file is handled by the inner writer 
	* class of this class.
	*/		
	public void save(){
		try{			
			ConfigSettingsIOWriter csiow = new ConfigSettingsIOWriter();
			Document doc = csiow.createXMLDocument();
			XMLOutputter outputter = new XMLOutputter("",true);
			PrintWriter pw = new PrintWriter(
										new BufferedWriter(
											new FileWriter(getFile())));
			outputter.output(doc,pw);
		}
		catch(IOException e){
			String msg = "Problem : couldn't output to the given file : " 
				+ getFile().toString();
			Log.e("ConfigSettingsIO", msg);
		}
	}	

	//---------------------------------------------------------------------
	// -- Contructors -----------------------------------------------------
	//---------------------------------------------------------------------

	/** 
	* The constructor used to initiate the ConfigSettingsIO 
	* object with a given file object
	* @param fileObject The file object with which 
	* to initiate this ConfigSettingsIO.
	*/
 	public ConfigSettingsIO(File fileObject){
		super(fileObject,ConfigSettings.getInstance());
	}

 	public ConfigSettingsIO(File fileObject, ConfigSettings cs){
		super(fileObject,cs);
	}

 	public ConfigSettingsIO(URL urlObject){
		super(urlObject, ConfigSettings.getInstance());
	}

 	public ConfigSettingsIO(URL urlObject, ConfigSettings cs){
		super(urlObject,cs);
	}

	///////////////////////////////////
	//   INNER CLASS : READER        //
	///////////////////////////////////
	
	private class ConfigSettingsIOReader extends XML_IO.XMLFileReader{

	  /*
		* Loads text information held in an Element object 
		* into the ConfigSettings object
		*/
		protected void loadElementIntoSettings(Element e){
			String text = e.getTextTrim();
			if(text!=null && text.length()!=0){
				ConfigSettings.getInstance().setElementText(toFullSettingName(e),text);
			}
		}

	  /*
		* Loads Attribute information held in an Element object 
		* into the ConfigSettings object
		*/		
		protected void loadAttributesIntoSettings(Element e){
			List attributes = e.getAttributes();
			ConfigSettings cs = ConfigSettings.getInstance();
			for(int k=0;k<attributes.size();k++){
				Attribute current = (Attribute)attributes.get(k);

				String total = toFullSettingName(e) + "." + current.getName();
				cs.setElementAttribute(total,current.getValue());
			}	
		}	  
		
		/** Adds the correct number on the end of the settings key. 
		Thus a.b.c.d becomes a.b.c.d.x where x=1,2,3...Checks if the key
		a.b.c.d.1 exists. If so, looks for a.b.c.d.2, etc. in the settings class,
		and so on, until it has a new key. This method thus allows multi Elements 
		with the same name to be stored uniquely. Without this, for example, 
		game moves would all have the same key e.g. gamesettings.game.move 
		*/
		protected String correctKeyForNumber(String uncorrectedKey){
			int i = 1;
			String correctedKey = uncorrectedKey + "." + i;
			//is there a key with this name?
			boolean reachedEnd = (ConfigSettings.getInstance().getElementText(correctedKey) == null);
			while(!reachedEnd){
				i++;
				int lastDotIndex = correctedKey.lastIndexOf('.');
				correctedKey = correctedKey.substring(0,lastDotIndex+1) + i;
				reachedEnd = (ConfigSettings.getInstance().getElementText(correctedKey) == null);
			}
			
			return correctedKey;
		}
		
	} //end of inner class ConfigSettingsIOReader
	
	
	///////////////////////////////////
	//   INNER CLASS : WRITER        //
	///////////////////////////////////	
	private class ConfigSettingsIOWriter extends XML_IO.XMLFileWriter{		
		/*	Creates and returns the Document root Element */	
		public Element createRootElement(){
			ConfigSettings cs = ConfigSettings.getInstance();
			Iterator textEntries = cs.getTextEntries().iterator();
			Map.Entry me = (Map.Entry)textEntries.next();
			String settingKey = (String)me.getKey();
			String settingValue = (String)me.getValue();
			ArrayList settingKeyTokens = splitSettingKey(settingKey);
			
			return new Element((String)settingKeyTokens.get(0));
		}
		
		/* Adds org.jdom.Comment objects to the current Document */
		public void addComments(Document doc){
			String text = "This file is important. Do not delete it,"
						+ " do not displace it, do not rename it. Do any of these things"
						+ " and as the universe is our witness we'll fry your ass, your"
						+ " hard drive, sleep with your girlfriend (and she'll enjoy it," 
						+ " believe us!), have you fired from your job, and transfer your" 
						+ " bank accounts to our names. Yes; this program is that good.";
			doc.addContent(new Comment(text));
		}
		
		/* Create the JDOM tree from the ConfigSettings class */
		public void addElements(Element rootElement){
			ConfigSettings cs = ConfigSettings.getInstance();
			Iterator textEntries = cs.getTextEntries().iterator();
			//iterator returns Map.Entry objects
			while(textEntries.hasNext()){
				Map.Entry me = (Map.Entry)textEntries.next();
				String settingKey = (String)me.getKey();
				String settingValue = (String)me.getValue();
				ArrayList settingKeyTokens = splitSettingKey(settingKey);
				
				Element parent = rootElement;
				Element child = null;
				for(int k=1;k<settingKeyTokens.size();k++){
					String childName = (String)settingKeyTokens.get(k);
					child = getChildElement(parent,childName);
					if(child==null){
						child = new Element(childName);
						parent.addContent(child);
						if(isLastToken(k,settingKeyTokens.size())){
							child.addContent(settingValue);
						}					
					}
					parent = child;
				}
			}	
		}
		
		/* Adds org.jdom.Attribute objects to the DOM tree Elements */
		public void addAttributes(Element root){
			Iterator attributeEntries = 
				ConfigSettings.getInstance().getAttributeEntries().iterator();	
				
			while(attributeEntries.hasNext()){
				Map.Entry me = (Map.Entry)attributeEntries.next();
				String settingKey = (String)me.getKey();
				String settingValue = (String)me.getValue();
				ArrayList settingKeyTokens = splitSettingKey(settingKey);
				Element e = getElement(settingKeyTokens,root);
				e.setAttribute((String)settingKeyTokens.get(
					settingKeyTokens.size()-1),settingValue);
			}
		}
	
		//Checks whether the token from the StringTokenizer operation
		private boolean isLastToken(int k, int listSize){
			return (k==listSize-1);
		}
	
		//Navigates down the jdom tree until it reaches the element
		//to which we wish to attach an attribute. Returns that element.	
		private Element getElement(ArrayList tokens, Element root){
			Element e = root;
			for(int k=1;k<tokens.size()-1;k++){
				e = e.getChild((String)tokens.get(k));
			}
			
			return e;
		}	
		
		/* 
		 * Does this parent Element already have a child with this name attached? 
		 * If so, return it, else return null
		 */
		private Element getChildElement(Element parent, String childName){
			return parent.getChild(childName);
		}
		
		/*	Splits a Setting key of the form a.b.c.d into it's 
			respective parts, delimited by the "."	*/
		private ArrayList splitSettingKey(String key){	
			ArrayList l = new ArrayList();
			StringTokenizer st = new StringTokenizer(key,".",false);		
			while(st.hasMoreTokens()){
				l.add(st.nextToken());
			}
			
			return l;
		}
	}//end of inner class ConfigSettingsIOWriter
	
}//end of class ConfigSettingsIO
