package hello;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
//import org.w3c.dom.Document;
//import org.xml.sax.InputSource;

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
			
			log.info("Reading config file started...");
			Map<String, String> configDetail = PropertyReader.getConfigDetail();
			
			if(null == configDetail || configDetail.size() < 1) {
				throw new Exception("Config detail not found!!!!!!!!!");
			}
			
			log.info("Reading config file completed.");
			
			//log.info("*********Config details***************");
			/*configDetail.forEach((key, value) -> {
			    log.info("Key : " + key + "  || Value : " + value);
			});*/
			
			List<String> requestList = PropertyReader.getRequestList(configDetail.get(Constants.REQUEST_LIST));
			
			if(null == requestList || requestList.size() < 1) {
				throw new Exception("Request list not valid!!!");
			}
			
			//log.info("*********Tally request list***************");
			//log.info(Arrays.toString(requestList.toArray()));
			
			for(String tallyRequest : requestList) {
				
				//get data from tally
				log.info("Retreiving data from Tally for " + tallyRequest  + ".................");
				//log.info("Tally request : " + configDetail.get(tallyRequest));
				
			    HttpHeaders headers = new HttpHeaders();
			    headers.setContentType(MediaType.APPLICATION_XML);
			    HttpEntity<String> request = new HttpEntity<String>(configDetail.get(tallyRequest), headers);
			    
			    ResponseEntity<String> response = restTemplate.postForEntity(configDetail.get(Constants.TALLY_URL), request, String.class);
			    
			    //log.info(response.getBody().toString());
			    
			    if(null != response && null != response.getStatusCode() ) {
			    	log.info("Response from Tally : " + response.getStatusCode().toString());
			    }
			    
			    //parse tally response, split
			    String tallyResponse = response.getBody().toString();
			    
			    if(null == tallyResponse || tallyResponse.length() < 1) {
			    	log.info("No valid response from tally...");
			    	return;
			    }
			    
			    log.info("Data retrived from tally successfully!!!");
			    
			    log.info("Data split and AWS transmission started...");
			    
			    XMLInputFactory xif = XMLInputFactory.newInstance();
			    Reader reader = new StringReader(tallyResponse);
		        //XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader("/Users/test_file_1.xml"));
		        XMLStreamReader xsr = xif.createXMLStreamReader(reader);
		        
		        xsr.nextTag(); // Advance to next element
		        
		        TransformerFactory tf = TransformerFactory.newInstance();
		        Transformer t = tf.newTransformer();
		        
		        int count =1;
		        
		        while(xsr.hasNext()) {
		        	
		        	if(xsr.isStartElement() && xsr.getLocalName().equals("TALLYMESSAGE")) {
		        		
		        		StringWriter sw = new StringWriter();
		                t.transform(new StAXSource(xsr), new StreamResult(sw));
		                
		                
		                
		                //post data received from tally to aws server
					    
					    request = new HttpEntity<String>(sw.toString(), headers);
				
					    response = restTemplate.postForEntity(configDetail.get(Constants.AWS_URL), request, String.class);
					    
					    //log.info(response.getBody().toString());
					    
					    if(null != response && response.getStatusCode().is2xxSuccessful()) {
					    	log.info("Data sent to server successfully!!!!!!! - " + count++);
					    } else {
					    	log.info("Data transmission failed!!!!!!!! - " + count++);
					    	//log.info(response.getBody().toString());
					    }
		                
			            
		        	}
		        	
		        	xsr.next();
		        	
		        }

		        log.info("Data split and AWS transmission completed...");
			    
			    //post data received from tally to aws server
			    /*log.info("Sending data to Report Server for " + tallyRequest + ".................");
			    
			    request = new HttpEntity<String>(response.getBody().toString(), headers);
		
			    response = restTemplate.postForEntity(configDetail.get(Constants.AWS_URL), request, String.class);
			    
			    //log.info(response.getBody().toString());
			    
			    if(null != response && response.getStatusCode().is2xxSuccessful()) {
			    	log.info("Data sent to server successfully!!!!!!!");
			    } else {
			    	log.info("Data transmission failed!!!!!!!!");
			    	log.info(response.getBody().toString());
			    }*/
		    
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