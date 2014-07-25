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
				double lat = Double.parseDouble(elemtrkpt.getAttributeValue(new QName("lat")));
				double lon = Double.parseDouble(elemtrkpt.getAttributeValue(new QName("lon")));
				//System.out.println(lat+" , "+lon);			
				hm.put("lat", lat);
				hm.put("lon", lon);
				
				Iterator trkptItr = elemtrkpt.getChildElements();
				//System.out.println(trkptItr.next());
				while(trkptItr.hasNext()){
					OMElement trkptChild = (OMElement) trkptItr.next();
					//System.out.println(trkptChild.getLocalName());
					if(trkptChild.getLocalName().equals("speed")){
						hm.put("speed", Double.parseDouble(trkptChild.getText()));
					}
					else if(trkptChild.getLocalName().equals("hdop")){
						hm.put("hdop", Double.parseDouble(trkptChild.getText()));
					}
					else if(trkptChild.getLocalName().equals("vdop")){
						hm.put("vdop", Double.parseDouble(trkptChild.getText()));
					}
					else if(trkptChild.getLocalName().equals("pdop")){
						hm.put("pdop", Double.parseDouble(trkptChild.getText()));
					}
					else if(trkptChild.getLocalName().equals("course")){
						hm.put("course", Double.parseDouble(trkptChild.getText()));
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
	private static boolean kalmanInitialized = false;
	
	public static void printElement(HashMap hm){
		
		double lat =  (Double)hm.get("lat");
		double lon = (Double)hm.get("lon");
		double course = (Double)hm.get("course");
		double velocity = (Double)hm.get("speed");
		double Vx =(Double)hm.get("vdop"); 
		double Vy = (Double)hm.get("hdop");
		
		KalmanF.setgpshm(lat, lon, course, velocity, Vx, Vy);
		
		if(kalmanInitialized == false){
			System.out.println("kalmanInitialized "+kalmanInitialized);
			KalmanF.doKalman(lat, lon, course, velocity);
			kalmanInitialized = true;
		}
		
		KalmanF.doCorrect();		
		double[] tempArray = KalmanF.getEstimation();
		System.out.println(tempArray[0]+" , "+tempArray[1]);
		
	}
}
