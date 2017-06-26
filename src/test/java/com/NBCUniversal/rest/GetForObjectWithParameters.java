package com.NBCUniversal.rest;

import java.util.HashMap;

import junitparams.FileParameters;
import junitparams.JUnitParamsRunner;
import junitparams.mappers.CsvWithHeaderMapper;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.web.client.RestTemplate;


@RunWith(JUnitParamsRunner.class)
public class GetForObjectWithParameters {
	private final String DEFAULT_ACCESS_KEY = "DEMO_KEY";
	
	private RestTemplate restTemplate;
	private String baseUrl = "https://api.nasa.gov";
	private String templateUrl = baseUrl + "/planetary/sounds?api_key={api_key}&limit={limit}";
	
	@Before
	public void setUp(){
		restTemplate = new RestTemplate();
	}
	
	@Test
	@FileParameters(value = "validLimits.csv", mapper = CsvWithHeaderMapper.class)
	public void validLimitValuesTest(String limit, String expected){
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("limit", limit);
		vars.put("api_key", DEFAULT_ACCESS_KEY);
				
		String response = restTemplate.getForObject(templateUrl, String.class, vars);
	    JSONParser parser = new JSONParser();
	    long count = -1;
	    
	    try {
	        JSONObject json = (JSONObject) parser.parse(response);
	
	        count = (Long) json.get("count");

	    } catch (Exception ex) {
	         ex.printStackTrace();
	    }

        Assert.assertEquals(expected, count);
	}
	
	@Test
	@FileParameters(value = "invalidLimits.csv", mapper = CsvWithHeaderMapper.class)
	public void invalidLimitValuesTest(String limit, String expected) throws Exception {
		HashMap<String, String> vars = new HashMap<String, String>();
		vars.put("limit", limit);
		vars.put("api_key", DEFAULT_ACCESS_KEY);
		
		String response = restTemplate.getForObject(templateUrl, String.class, vars);
	    JSONParser parser = new JSONParser();
	    long count = -1;

	    try {
	        JSONObject json = (JSONObject) parser.parse(response);
	        count = (Long) json.get("count");

	    } catch (Exception ex) {
	         ex.printStackTrace();
	    }
	    Assert.assertEquals(expected, count);
	}

}
