package edu.berkeley.eecs.ruzenafit.sample;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Sample code for "Hello, world".  We're using Jersey as a REST middleman API.
 * @author gibssa
 */
@Path("/hello")
public class HelloWorldSample {

	// This method is called if XML is request
	@GET
	@Produces(MediaType.TEXT_XML)
	public String sayXMLHello() {
		return "<?xml version=\"1.0\"?>" + "<hello> Hello Jersey" + "</hello>";
	}
}
