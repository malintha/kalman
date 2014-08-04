/**
 * Created by kbsoft on 7/14/14.
 */

import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;

public class SampleProducer {
    static RandomGenerator rand = new JDKRandomGenerator();
    private static final String HTTP_EVENT_INPUT = "http://localhost:9763/endpoints/http_test_input/gps";
    static Integer eventId = 100;
    public static void main(String[] args) {
        System.out.println("Starting Http Agent");
        try {
            getGpxElement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Successfully all data send over HTTP");

    }


    static void sendDataOverHTTP(String josnString) {
        HttpPost post = new HttpPost(HTTP_EVENT_INPUT);
        post.setHeader("Content-type", "application/json");
        HttpClient httpclient = HttpClientBuilder.create().build();
        try {
            post.setEntity(new StringEntity(josnString));
            HttpResponse response = httpclient.execute(post);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Close the httpclient connection
        }
    }


    public static void getGpxElement() {
        boolean addNoise = true;
        HashMap hm = new HashMap();
        int count=0;

        try {
            InputStream is = SampleProducer.class.getResourceAsStream("20140715.gpx");
            //System.out.println(is.available());

            StAXOMBuilder sb = new StAXOMBuilder(is);
            OMElement docElem = sb.getDocumentElement();
            Iterator itemItr = docElem.getChildrenWithName(new QName("trk"));

            while (itemItr.hasNext()) {
                Thread.sleep(1000);
                OMElement trk = (OMElement) itemItr.next();
                //System.out.println(trk);
                OMElement trkseg = trk.getFirstElement(); //trkseg
                //System.out.println(trkseg.getLocalName());
                Iterator itr = trkseg.getChildrenWithName(new QName("trkpt"));
                //System.out.println("came");
                while (itr.hasNext()) {
                    OMElement elemtrkpt = (OMElement) itr.next();
                    //	System.out.println(elemtrkpt);
                    double lat = Double.parseDouble(elemtrkpt.getAttributeValue(new QName("lat")));
                    double lon = Double.parseDouble(elemtrkpt.getAttributeValue(new QName("lon")));
                    //System.out.println(lat+" , "+lon);
                    if(addNoise == true) {
                        lat = lat + (rand.nextGaussian())/100;
                        lon = lon + (rand.nextGaussian())/100;
                    }

                    hm.put("lat", lat);
                    hm.put("lon", lon);

                    Iterator trkptItr = elemtrkpt.getChildElements();
                    //System.out.println(trkptItr.next());
                    while (trkptItr.hasNext()) {
                        OMElement trkptChild = (OMElement) trkptItr.next();
                        //System.out.println(trkptChild.getLocalName());
                        hm.put("eventId", ++eventId);
                        if (trkptChild.getLocalName().equals("speed")) {
                            hm.put("speed", Double.parseDouble(trkptChild.getText()));
                        } else if (trkptChild.getLocalName().equals("hdop")) {
                            hm.put("hdop", Double.parseDouble(trkptChild.getText()));
                        } else if (trkptChild.getLocalName().equals("vdop")) {
                            hm.put("vdop", Double.parseDouble(trkptChild.getText()));
                        } else if (trkptChild.getLocalName().equals("course")) {
                            hm.put("course", Double.parseDouble(trkptChild.getText()));
                        }
                        hm.put("controlInput", 0);

                    }
                    Thread.sleep(2000);

                    String jsonString = new Gson().toJson(hm);
                    System.out.println(jsonString);
                    sendDataOverHTTP(jsonString);
                    count++;
                    if (count == -1)
                        break;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

    }
}