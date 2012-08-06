package edu.berkeley.eecs.ruzenafit.sample;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.berkeley.eecs.ruzenafit.shared.model.WorkoutTick;

/**
 * Sample code for "Hello, world".  We're using Jersey as a REST middleman API.
 * @author gibssa
 */
@Path("/hello")
public class HelloWorldSample {

	// This method is called if XML is request
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public WorkoutTick sayXMLHello() {
		return new WorkoutTick("test imei", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "l");
	}
}
