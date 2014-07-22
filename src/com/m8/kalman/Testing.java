/**
 * Created by kbsoft on 7/14/14.
 */

import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.*;

public class Testing {
    private static final String SAMPLE_CSV_PATH = "/home/kbsoft/testing/sample.csv";
    private static final String HTTP_EVENT_INPUT = "http://localhost:9763/endpoints/http_test_input/gps";

    public static void main(String[] args) {
        System.out.println("Starting Http Agent");

        try {
            getGpxElement();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Successfully all data send over HTTP");


    }

    //Send sample data stored in CSV format
    private static void publishLogEvents() throws FileNotFoundException {
        Scanner scanner = new Scanner(new FileInputStream(SAMPLE_CSV_PATH));
        while (scanner.hasNextLine()) {
            String csvLine = scanner.nextLine();
            Map obj = new LinkedHashMap();
            String[] values = csvLine.split(",");

            obj.put("evntid",values[10].substring(0,5));
            obj.put("vid",values[10].substring(5,7));
            obj.put("lat",values[2]);
            obj.put("log",values[4]);
            obj.put("times",values[0]);

            String jsonString = new Gson().toJson(obj);
//            System.out.println(jsonString);
            try {
                sendDataOverHTTP(jsonString);
                Thread.sleep(10);
                System.out.println("Sending data...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        scanner.close();

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
        }
        finally {
            //Close the httpclient connection
        }
    }



    public static void getGpxElement(){

        HashMap hm = new HashMap();

        try {
            InputStream is = Testing.class.getResourceAsStream("20140715.gpx");
//            System.out.println(is.available());
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
                    String jsonString = new Gson().toJson(hm);
                    System.out.println(jsonString);
                    sendDataOverHTTP(jsonString);
                }
            }
        }
        catch (Exception e){
            System.out.println("Ohh noo Exceptionnnnn :( ");
//            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }


    }
}
