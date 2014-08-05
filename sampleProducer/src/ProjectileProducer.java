import com.google.gson.Gson;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Produces data for a projectile of a particle with angle 60 degrees from X axis with
 * speed 60m/s
 *
 * Created by malintha on 8/5/14.
 */
public class ProjectileProducer {
    private static final String HTTP_EVENT_INPUT = "http://localhost:9763/endpoints/http_test_input/gps";
    /**
     * 0 : X
     * 1 : dX
     * 2 : Y
     * 3 : dY
     */
    static double[][] dataArr = {{0, 30, 0, 51.96},{30, 30, 47.1, 41.96},{60, 30, 84.3, 31.96},{90, 30, 111.8, 21.96},
            {120,30,129.4, 11.6},{150, 30, 137.3, 1.6},{180, 30, 135.4, 8.04},{210,30,123.6,18.04},{240, 30, 102.1, 28.04},
            {270, 30, 70.8, 38.04},{300, 30, 29.62, 48.04}};

    static RandomGenerator rand = new JDKRandomGenerator();
    static HashMap<String,Double> hm = new HashMap<String, Double>();

    public static void main(String[] args) throws Exception{
        for(int i =0;i<dataArr.length;i++){
            double n_X = dataArr[i][0] + rand.nextGaussian()/1;
            double n_dX = dataArr[i][1] + rand.nextGaussian()/10;
            double n_Y = dataArr[i][2] + rand.nextGaussian()/1;
            double n_dY = dataArr[i][3] + rand.nextGaussian()/10;

            hm.put("X",dataArr[i][0]);
            hm.put("dX",dataArr[i][1]);
            hm.put("Y",dataArr[i][2]);
            hm.put("dY",dataArr[i][3]);

            hm.put("n_X",n_X);
            hm.put("n_dX",n_dX);
            hm.put("n_Y",n_Y);
            hm.put("n_dY",n_dY);

            String jsonString = new Gson().toJson(hm);
            System.out.println(jsonString);
            sendDataOverHTTP(jsonString);
            hm.clear();
            Thread.sleep(1000);
        }
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


}
