package hello;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String args[]) {
		SpringApplication.run(Application.class);
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		
		return args -> {
			
			/*String xmlString = "<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Data</TYPE><ID>Trial Balance</ID></HEADER><BODY><DESC><STATICVARIABLES>"
					+ "<EXPLODEFLAG>Yes</EXPLODEFLAG><SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT></STATICVARIABLES></DESC></BODY></ENVELOPE>";*/
			
			//String xmlString = "<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Export</TALLYREQUEST><TYPE>Data</TYPE><ID>Balance Sheet</ID></HEADER><BODY><DESC><STATICVARIABLES>"
			//		+ "<EXPLODEFLAG>Yes</EXPLODEFLAG><SVEXPORTFORMAT>$$SysName:XML</SVEXPORTFORMAT></STATICVARIABLES></DESC></BODY></ENVELOPE>";
			
			log.info("Reading config file............");
			Map<String, String> configDetail = PropertyReader.getConfigDetail();
			
			if(null == configDetail || configDetail.size() < 1) {
				throw new Exception("Config detail not found!!!!!!!!!");
			}
			
			log.info("Reading config file completed.");
			
			//log.info("*********Config details***************");
			configDetail.forEach((key, value) -> {
			    //log.info("Key : " + key + "  || Value : " + value);
			});
			
			List<String> requestList = PropertyReader.getRequestList(configDetail.get(Constants.REQUEST_LIST));
			
			if(null == requestList || requestList.size() < 1) {
				throw new Exception("Request list not valid!!!!!!!!!");
			}
			
			//log.info("*********Tally request list***************");
			//log.info(Arrays.toString(requestList.toArray()));
			
			for(String tallyRequest : requestList) {
				
				//get data from tally
				log.info("Retreiving data from Tally for " + tallyRequest  + ".................");
				log.info("Tally request : " + configDetail.get(tallyRequest));
				
			    HttpHeaders headers = new HttpHeaders();
			    headers.setContentType(MediaType.APPLICATION_XML);
			    HttpEntity<String> request = new HttpEntity<String>(configDetail.get(tallyRequest), headers);
			    
			    //HttpEntity<String> request = new HttpEntity<String>(xmlString, headers);
			    //ResponseEntity<String> response = restTemplate.postForEntity("http://192.168.0.9:9000", request, String.class);
			    //ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:9000", request, String.class);
			    ResponseEntity<String> response = restTemplate.postForEntity(configDetail.get(Constants.TALLY_URL), request, String.class);
			    
			    //log.info(response.getBody().toString());
			    
			  
			    //post data received from tally to aws server
			    log.info("Sending data to Report Server for " + tallyRequest + ".................");
			    
			    request = new HttpEntity<String>(response.getBody().toString(), headers);
		
			    //response = restTemplate.postForEntity("http://localhost:8080/restws/services/tallyservice/tally", request, String.class);
			    response = restTemplate.postForEntity(configDetail.get(Constants.AWS_URL), request, String.class);
			    
			    //log.info(response.getBody().toString());
			    
			    if(null != response && response.getStatusCode().is2xxSuccessful()) {
			    	log.info("Data sent to server successfully!!!!!!!");
			    } else {
			    	log.info("Data transmission failed!!!!!!!!");
			    	log.info(response.getBody().toString());
			    }
		    
			}
			
			/*//testing - load file xml string 
			String fileData = FileUtil.getFileDataAsString();
			//String fileData = "Hai";
			HttpHeaders headers = new HttpHeaders();
		    headers.setContentType(MediaType.APPLICATION_XML);
		    HttpEntity<String> request = new HttpEntity<String>(fileData, headers);
		    ResponseEntity<String> response = restTemplate.postForEntity("http://localhost:8080/restws/services/tallyservice/tally", request, String.class);*/
		    
		};
	}
}