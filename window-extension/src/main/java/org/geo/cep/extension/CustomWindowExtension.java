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
    private static boolean isKalmanInitialized = false;
    Kalman k;

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

        }
        controlInput = ((Variable) expressions[0]).getAttributeName();
        variablePosition = abstractDefinition.getAttributePosition(controlInput);
        k = new Kalman();
    }

    private void doProcessing(InEvent event) {
        System.out.println("###Event "+event.toString());

        /**
         * lat
         * lon
         * speed
         * hdop
         * vdop
         * control input
         * course
         */

        /**
         * first initialize matrices. Then set the initial state.
         * Finally call doKalmanCorrect method and get double array or Estimation vector
         */
        double lat =  new Double((Double)event.getData0());
        double lon = new Double((Double)event.getData1());
        double velocity = new Double((Double) event.getData2());
        double Vy = new Double((Double)event.getData3());
        double Vx = new Double((Double)event.getData4());
        //double ctrlIp = Double.parseDouble((String)event.getData5());
        double course = new Double((Double)event.getData6());

        k.addCurrentMeasurement(lat, lon, course, velocity,Vx, Vy);
        System.out.println("###isKalmanInitialized "+isKalmanInitialized);
        if(!isKalmanInitialized) {
            k.initializeMatrices(lat, lon, course, velocity);
            isKalmanInitialized=true;
            System.out.println("###isKalmanInitialized "+isKalmanInitialized);
        }
        k.doKalmanCorrect();
        double[] processedData =  k.getEstimation();
        System.out.println("###processedData "+processedData[0]+","+processedData[1]);
        Object[] sendData = new Object[]{
                processedData[0],
                new Double(123.456),
                new Double(123.456),
                new Double(123.456),
                processedData[1],
                new String("0"),
                new Double(654.321)
        };
        InEvent newIn = new InEvent(event.getStreamId(),System.currentTimeMillis(),sendData);
        nextProcessor.process(newIn);
    }

    @Override
    public void destroy() {
    }
}
