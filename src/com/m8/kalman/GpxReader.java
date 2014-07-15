package com.m8.kalman;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

/**
 * 
 * @author malintha
 *get lat, long, elevation, speed, hdop, vdop, pdop
 */

public class GpxReader {
	
	public static void main(String args[]){
		getGpxElement();
	}

	public static void getGpxElement(){
		
		HashMap hm = new HashMap();
		
		try {
		InputStream is = GpxReader.class.getResourceAsStream("20140715.gpx");
		//System.out.println(is.available());
		
		StAXOMBuilder sb = new StAXOMBuilder(is);
		OMElement docElem = sb.getDocumentElement();
		Iterator itemItr = docElem.getChildrenWithName(new QName("trk"));
		
		while(itemItr.hasNext()) {
		Thread.sleep(1000);
			OMElement trk = (OMElement) itemItr.next();
			//System.out.println(trk);
			OMElement trkseg = trk.getFirstElement(); //trkseg
			//System.out.println(trkseg.getLocalName());
			Iterator itr = trkseg.getChildrenWithName(new QName("trkpt"));
			//System.out.println("came");
			while(itr.hasNext()){
				OMElement elemtrkpt = (OMElement) itr.next();
			//	System.out.println(elemtrkpt);
				String lat = elemtrkpt.getAttributeValue(new QName("lat"));
				String lon = elemtrkpt.getAttributeValue(new QName("lon"));
				//System.out.println(lat+" , "+lon);			
				hm.put("lat", lat);
				hm.put("lon", lon);
				
				Iterator trkptItr = elemtrkpt.getChildElements();
				//System.out.println(trkptItr.next());
				while(trkptItr.hasNext()){
					OMElement trkptChild = (OMElement) trkptItr.next();
					//System.out.println(trkptChild.getLocalName());
					if(trkptChild.getLocalName().equals("speed")){
						hm.put("speed", trkptChild.getText());
					}
					else if(trkptChild.getLocalName().equals("hdop")){
						hm.put("hdop", trkptChild.getText());
					}
					else if(trkptChild.getLocalName().equals("vdop")){
						hm.put("vdop", trkptChild.getText());
					}
					else if(trkptChild.getLocalName().equals("pdop")){
						hm.put("pdop", trkptChild.getText());
					}
				}
				Thread.sleep(1000);
				printElement(hm);
			}
			}	
		}
		catch (Exception e){
			System.out.println(e.getLocalizedMessage());
		}
		

	}
	public static void printElement(HashMap hm){
		System.out.println("lat : "+hm.get("lat"));
		System.out.println("lon : "+hm.get("lon"));
		System.out.println("speed : "+hm.get("speed"));
		System.out.println("vdop : "+hm.get("vdop"));
		System.out.println("pdop : "+hm.get("pdop"));
		System.out.println();
	}
}
