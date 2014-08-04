package org.geo.cep.extension;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.query.processor.window.WindowProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import java.util.Iterator;

@SiddhiExtension(namespace = "math", function = "kalmanFilter")
public class MathKalmanWindowExtension extends WindowProcessor {
    int variablePosition = 0;
    private static boolean isKalmanInitialized = false;
    private MathKalman k;
    String controlInput;
    /**
     * This method called when processing an event
     */

    @Override
    protected void processEvent(InEvent inEvent) {
        acquireLock();
        try {
//            for (int i = 0; i < 7; i++) {
//                System.out.println("Index = "+i+" Data = "+inEvent.getData(i));
//            }
            doProcessing(inEvent);
        } finally {
            releaseLock();
        }
    }

    /**
     * This method called when processing an event list
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

    /**
     * define the attributes in order.......
     *
     *
     * @param expressions
     * @param queryPostProcessingElement
     * @param abstractDefinition
     * @param s
     * @param b
     * @param siddhiContext
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
        System.out.println("######protected void init#####");
        k = new MathKalman();

    }

    private void doProcessing(InEvent event) {
        System.out.println("###Event " + event.toString());

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
        /*
        For reference:
        Index = 0 Data = 6.87308728
        Index = 1 Data = 79.87337101
        Index = 2 Data = 4.25
        Index = 3 Data = 1.2
        Index = 4 Data = 0.9
        Index = 5 Data = 310.1
        Index = 6 Data = 123.456

        Producer data:
        {"course":310.1,"lon":79.87337101,"speed":4.25,"controlInput":123.456,"vdop":0.9,"hdop":1.2,"lat":6.87308728}
        */
        double X = new Double((Double) event.getData0());
        double dX = new Double((Double) event.getData1());
        double Y = new Double((Double) event.getData2());
        double dY = new Double((Double) event.getData3());
        double Vx = new Double((Double) event.getData4());
        double course = new Double((Double) event.getData5());
        double ctrlIp = new Double((Double) event.getData6());
        int eventId = new Integer((Integer) event.getData7());

//        System.out.println("lat = "+lat+"lon = "+lon+" course = "+course+" velo = "+velocity+" Vx = "+Vx+" Vy = "+Vy);
        k.addCurrentMeasurement(X, dX, Y, dY);
        System.out.println("###isKalmanInitialized " + isKalmanInitialized);
        if (!isKalmanInitialized) {
            k.initializeMatrices(X, dX, Y, dY);
            isKalmanInitialized = true;
            System.out.println("###isKalmanInitialized " + isKalmanInitialized);
        }
        k.doKalmanCorrect();
        double[] processedData = k.getEstimation();
        System.out.println("###processedData " + processedData[0] + "," + processedData[2]);
        Object[] sendData = new Object[]{
                processedData[0],
                dX,
                processedData[1],
                dY,
                Vx,
                course,
                ctrlIp,
                eventId
        };
        InEvent newIn = new InEvent(event.getStreamId(), System.currentTimeMillis(), sendData);
        nextProcessor.process(newIn);
    }

    @Override
    public void destroy() {
    }
}
