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
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;
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
        if (expressions.length != 2) {
            log.error("Parameters count is not matching, There should be two parameters ");
        }
        controlInput = ((Variable) expressions[0]).getAttributeName();
//        noValue = ((IntConstant) expressions[1]).getValue();
//        uniqueWindow = new LinkedHashMap<Object, InEvent>();
        variablePosition = abstractDefinition.getAttributePosition(controlInput);
    }
    private void doProcessing(InEvent event) {
        String data = (String) event.getData(variablePosition);
        System.out.println("DEBUG:****ControlInput_data = "+data);
        Double latitude = (Double) event.getData0();
        Double longitude = (Double) event.getData1();
        System.out.println("DEBUG:****Latitude = "+latitude+ "Longitude = "+longitude+"\n\nevent.getStreamId() = "+event.getStreamId());
        Object[] new_data = new Object[]{
                new Double(123.456),
                new Double(654.321),
                new Double(654.321),
                new Double(654.321),
                new Double(654.321),
                new String("Ohhhh"),
                new Double(654.321),
        };
        InEvent newIn = new InEvent(event.getStreamId(),System.currentTimeMillis(),new_data);
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
    @Override
    public void destroy() {
    }
}