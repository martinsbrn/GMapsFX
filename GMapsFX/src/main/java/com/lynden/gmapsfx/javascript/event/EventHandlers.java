/*
 * Copyright 2014 Geoff Capper.
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
package com.lynden.gmapsfx.javascript.event;

import com.lynden.gmapsfx.javascript.object.LatLong;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import netscape.javascript.JSObject;

/**
 * This object forms the bridge between Javascript events and the Java events.
 * <p>
 * An instance of this class is assigned to the document model so that
 * Javascript can access it to make callbacks when events occur.
 * <p>
 * An application calls on one of the two addXXEventHandler methods in the
 * {@link com.lynden.gmapsfx.javascript.object.GoogleMap} class to register an
 * instance of the {@link GFXEventHandler} interface against a certain event
 * type in the Google Maps event model. See addUIEventHandler and
 * addStateEventHandler.
 * <p>
 * This class uses a key based on a UUID to map the Javascript event handlers
 * registered against the Google Maps model back to our Java event handlers
 * stored here. The addXXEventHandler methods in GoogleMap create functions that
 * call back into this class using the supplied key.
 * <p>
 * Currently an instance of this class is registered as:
 * <code>document.jsHandlers</code>
 * <p>
 * The event listeners are established using the following Javascript calls:
 * <p>
 * {@link com.lynden.gmapsfx.javascript.object.GoogleMap#addUIEventHandler}
 * <p>
 * <blockquote><pre><code>
 * google.maps.event.addListener(map, 'event_type', function(event) {
 *      document.jsHandlers.handleUIEvent('key', event.latLng);
 * });
 * </code></pre></blockquote>
 * <p>
 * {@link com.lynden.gmapsfx.javascript.object.GoogleMap#addStateEventHandler}
 * <p>
 * <blockquote><pre><code>
 *  google.maps.event.addListener(map, 'event_type', function() {
 *      document.jsHandlers.handleStateEvent('key');
 *  });
 * </code></pre></blockquote>
 *
 * @author Geoff Capper
 *
 */
public class EventHandlers {

    private final Map<String, GFXEventHandler> handlers = new HashMap<>();

    public EventHandlers() {
    }

    /**
     * Registers a handler and returns the callback key to be passed to
     * Javascript.
     *
     * @param handler Handler to be registered.
     * @return A String random UUID that can be used as the callback key.
     */
    public String registerHandler(GFXEventHandler handler) {
        String uuid = UUID.randomUUID().toString();
        handlers.put(uuid, handler);
        return uuid;
    }

    /**
     * This method is called from Javascript, passing in the previously created
     * callback key, and the event object. The current implementation is
     * receiving the LatLng object that the Javascript MouseEvent contains.
     * <p>
     * It may be more useful to return the MouseEvent and let clients go from
     * there, but there is only the stop() method on the MouseEvent?
     *
     * @param callbackKey Key generated by the call to registerHandler.
     * @param result Currently the event object from the Google Maps event.
     */
    public void handleUIEvent(String callbackKey, JSObject result) {
        if (handlers.containsKey(callbackKey) && handlers.get(callbackKey) instanceof UIEventHandler) {
            ((UIEventHandler) handlers.get(callbackKey)).handle(result);
        } else if (handlers.containsKey(callbackKey) && handlers.get(callbackKey) instanceof MouseEventHandler) {
            ((MouseEventHandler) handlers.get(callbackKey)).handle(buildMouseEvent(result));
        } else {
            System.err.println("Error in handle: " + callbackKey + " for result: " + result);
        }
    }

    /**
     * This method is called from Javascript, passing in the previously created
     * callback key. It uses that to find the correct handler and then passes on
     * the call. State events in the Google Maps API don't pass any parameters.
     *
     * @param callbackKey Key generated by the call to registerHandler.
     */
    public void handleStateEvent(String callbackKey) {
        if (handlers.containsKey(callbackKey) && handlers.get(callbackKey) instanceof StateEventHandler) {
            ((StateEventHandler) handlers.get(callbackKey)).handle();
        } else {
            System.err.println("Error in handle: " + callbackKey + " for state handler ");
        }
    }

    
    
    protected GMapMouseEvent buildMouseEvent(JSObject jsObject) {
        LatLong latLong = new LatLong((JSObject) jsObject.getMember("latLng"));
        return new GMapMouseEvent(latLong);
    }
}
