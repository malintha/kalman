package org.geo.cep.extension;

/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

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

    String variable = "";
    int variablePosition = 0;
    int noValue = 0;
    Map<Object, InEvent> uniqueWindow = null;

    @Override
    /**
     *This method called when processing an event
     */
    protected void processEvent(InEvent inEvent) {
        acquireLock();
        System.out.println("DEBUG:***Variable = "+variable+" noValue = "+noValue+" uniqueWindow = "+uniqueWindow.toString()+" variablePosition = "+variablePosition);
        System.out.println("DEBUG:******* inEvent.getData1().toString() = "+inEvent.getData1().toString());
        System.out.println("DEBUG:******* inEvent.getData0().toString() = "+inEvent.getData0().toString());
        try {
//            doProcessing(inEvent);
        } finally

        {
            releaseLock();
        }

    }

    @Override
    /**
     *This method called when processing an event list
     */
    protected void processEvent(InListEvent inListEvent) {

        for (int i = 0; i < inListEvent.getActiveEvents(); i++) {
            InEvent inEvent = (InEvent) inListEvent.getEvent(i);
            processEvent(inEvent);
        }
    }

    @Override
    /**
     * This method iterate through the events which are in window
     */
    public Iterator<StreamEvent> iterator() {
        return null;
    }

    @Override
    /**
     * This method iterate through the events which are in window but used in distributed processing
     */
    public Iterator<StreamEvent> iterator(String s) {
        return null;
    }

    @Override
    /**
     * This method used to return the current state of the window, Used for persistence of data
     */
    protected Object[] currentState() {
        return new Object[]{uniqueWindow};
    }

    @Override
    /**
     * This method is used to restore from the persisted state
     */
    protected void restoreState(Object[] objects) {
    }

    @Override
    /**
     * Method called when initialising the extension
     */
    protected void init(Expression[] expressions,
                        QueryPostProcessingElement queryPostProcessingElement,
                        AbstractDefinition abstractDefinition, String s, boolean b,
                        SiddhiContext siddhiContext) {

        if (expressions.length != 2) {
            log.error("Parameters count is not matching, There should be two parameters ");
        }
        variable = ((Variable) expressions[0]).getAttributeName();
        noValue = ((IntConstant) expressions[1]).getValue();
        uniqueWindow = new LinkedHashMap<Object, InEvent>();
        variablePosition = abstractDefinition.getAttributePosition(variable);
        System.out.println("DEBUG:***Initializing custom window*********************************************************");
        System.out.println("DEBUG:***Variable = "+variable+"noValue = "+noValue+"uniqueWindow = "+uniqueWindow.toString());
    }

    private void doProcessing(InEvent event) {
        event.getData(variablePosition);

        log.info("###Event###"+event);
        Object eventKey = event.getData(variablePosition);
        if (uniqueWindow.containsKey(eventKey)) {
            InEvent firstEvent = uniqueWindow.remove(eventKey);
            uniqueWindow.put(eventKey, event);
            if (uniqueWindow.size() == noValue) {
                nextProcessor.process(firstEvent);
            }
        } else {
            if (uniqueWindow.size() < noValue) {
                uniqueWindow.put(eventKey, event);
            } else if (uniqueWindow.size() == noValue) {
                Object firstKey = uniqueWindow.keySet().toArray()[0];
                InEvent firstEvent = uniqueWindow.remove(firstKey);
                uniqueWindow.put(eventKey, event);
                nextProcessor.process(firstEvent);
            }
        }

    }

    @Override
    public void destroy() {
    }
}
