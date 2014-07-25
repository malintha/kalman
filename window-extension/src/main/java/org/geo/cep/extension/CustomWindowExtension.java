package org.geo.cep.extension;

import org.apache.commons.math3.filter.KalmanFilter;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.query.processor.window.WindowProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@SiddhiExtension(namespace = "geo", function = "kalmanFilter")
public class CustomWindowExtension extends WindowProcessor {
    String controlInput = "";
    int variablePosition = 0;

    /**
     *This method called when processing an event
     */
    @Override
    protected void processEvent(InEvent inEvent) {
        acquireLock();
        System.out.println("ControlInput = "+controlInput+" VariablePosition = "+variablePosition);
        try {
            doProcessing(inEvent);
        } finally
        {
            releaseLock();
        }
    }

    /**
     *This method called when processing an event list
     */
    @Override
    protected void processEvent(InListEvent inListEvent) {
        for (int i = 0; i < inListEvent.getActiveEvents(); i++) {
            InEvent inEvent = (InEvent) inListEvent.getEvent(i);
            processEvent(inEvent);
        }
    }

    /**
     * This method iterate through the events which are in window
     */
    @Override
    public Iterator<StreamEvent> iterator() {
        return null;
    }

    /**
     * This method iterate through the events which are in window but used in distributed processing
     */
    @Override
    public Iterator<StreamEvent> iterator(String s) {
        return null;
    }

    /**
     * This method used to return the current state of the window, Used for persistence of data
     */
    @Override
    protected Object[] currentState() {
        return new Object[]{};
    }

    /**
     * This method is used to restore from the persisted state
     */
    @Override
    protected void restoreState(Object[] objects) {
    }

    /**
     * Method called when initialising the extension
     */
    @Override
    protected void init(Expression[] expressions,
                        QueryPostProcessingElement queryPostProcessingElement,
                        AbstractDefinition abstractDefinition, String s, boolean b,
                        SiddhiContext siddhiContext) {
        if (expressions.length != 1) {
//            log.in("Parameters count is not matching, There should be two parameters ");
        }
        controlInput = ((Variable) expressions[0]).getAttributeName();
//        noValue = ((IntConstant) expressions[1]).getValue();
//        uniqueWindow = new LinkedHashMap<Object, InEvent>();
        variablePosition = abstractDefinition.getAttributePosition(controlInput);
    }
    private void doProcessing(InEvent event) {
        System.out.println("###0### ");
//        String data = (String) event.getData(variablePosition);
//        System.out.println("DEBUG:****ControlInput_data = "+data);

        HashMap<String,Double> processDataHashMap = new HashMap<String, Double>();
        /**
         * lat
         * lon
         * speed
         * hdop
         * vdop
         * control input
         * course
         */

        processDataHashMap.put("lat", new Double((Double)event.getData0()));
        processDataHashMap.put("lon", new Double((Double)event.getData1()));
        processDataHashMap.put("velocity",new Double((Double) event.getData2()));
        processDataHashMap.put("hdop",new Double((Double)event.getData3()));
        processDataHashMap.put("vdop",new Double((Double)event.getData4()));
        //processDataHashMap.put("ctrlIp",new Double((Double)event.getData5()));
        processDataHashMap.put("course", new Double((Double)event.getData6()));

        System.out.println("#####: came"+processDataHashMap);

      // double[] processedLatLon = doKalmanProcess(processDataHashMap);

//        for (int i = 0; i < 6; i++) {
//            data = String.valueOf(event.getData(i));
//            System.out.println("#####: Data attribute number ("+i+") Data value = "+data);
//        }

        System.out.println("#### "+processDataHashMap.get("lat"));
        System.out.println("#### "+processDataHashMap.get("lon"));
        System.out.println("#### "+processDataHashMap.get("velocity"));
        System.out.println("#### "+processDataHashMap.get("hdop"));
        System.out.println("#### "+processDataHashMap.get("vdop"));
        //System.out.println("#### "+processDataHashMap.get("ctrlIp"));
        System.out.println("#### "+processDataHashMap.get("course"));

        double[] processedData = doKalmanProcess(processDataHashMap);
        System.out.println("#### Processed "+processedData[0]+" , "+processedData[1]);


        Object[] sendData = new Object[]{
                processedData[0],
                new Double(123.456),
                new Double(123.456),
                new Double(123.456),
                processedData[1],
                new String("ControlInputPass"),
                new Double(654.321),
        };

        InEvent newIn = new InEvent(event.getStreamId(),System.currentTimeMillis(),sendData);

        nextProcessor.process(newIn);
//        log.info(event);
//        Object eventKey = event.getData(variablePosition);
//        if (uniqueWindow.containsKey(eventKey)) {
//            InEvent firstEvent = uniqueWindow.remove(eventKey);
//            uniqueWindow.put(eventKey, event);
//            if (uniqueWindow.size() == noValue) {
//                nextProcessor.process(firstEvent);
//            }
//        } else {
//            if (uniqueWindow.size() < noValue) {
//                uniqueWindow.put(eventKey, event);
//            } else if (uniqueWindow.size() == noValue) {
//                Object firstKey = uniqueWindow.keySet().toArray()[0];
//                InEvent firstEvent = uniqueWindow.remove(firstKey);
//                uniqueWindow.put(eventKey, event);
//                nextProcessor.process(firstEvent);
//            }
//        }
    }

    private static boolean kalmanInitialized = false;

    public static double[] doKalmanProcess(HashMap<String,Double> hm){

        System.out.println("#### doKalmanProcess");

        double lat =  (Double)hm.get("lat");
        double lon = (Double)hm.get("lon");
        double course = (Double)hm.get("course");
        double velocity = (Double)hm.get("velocity");
        double Vx = (Double)hm.get("vdop");
        double Vy = (Double)hm.get("hdop");
        System.out.println("#### doKalmanProcess Vy");

        KalmanFIlter.setgpshm(lat, lon, course, velocity, Vx, Vy);
        System.out.println("#### doKalmanProcess setgpshm");

        if(kalmanInitialized == false){
            System.out.println("kalmanInitialized "+kalmanInitialized);
            KalmanFIlter.doKalman(lat, lon, course, velocity);
            kalmanInitialized = true;
        }

        KalmanFIlter.doCorrect();
        System.out.println("#### doCorrect");
        double[] tempArray = KalmanFIlter.getEstimation();

        return tempArray;
    }


    @Override
    public void destroy() {
    }
}