package com.bac.asi_http_task_2;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class RestResponse extends Application {

	/**
	 * Creates a root Restlet that will receive all incoming calls.
	 */
	@Override
	public Restlet createInboundRoot() {

		// Create a router Restlet that routes each call to a
		// new instance of RestResource.
		Router router = new Router(getContext());

		// Defines only one route
		router.attachDefault(RestResource.class);

		return router;
	}
}
