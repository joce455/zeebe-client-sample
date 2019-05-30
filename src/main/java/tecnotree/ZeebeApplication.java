package tecnotree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.events.DeploymentEvent;
import io.zeebe.client.api.events.WorkflowInstanceEvent;
import io.zeebe.client.api.subscription.JobWorker;
import io.zeebe.model.bpmn.instance.Message;

@SpringBootApplication
public class ZeebeApplication {

	public static void main(String[] args) {
		final ZeebeClient client = ZeebeClient.newClientBuilder()
				// change the contact point if needed
				.brokerContactPoint("127.0.0.1:26500").build();
		
			
		final Map<String, Object> data = new HashMap<>();
        data.put("orderId", 1234);
        data.put("orderValue", 101);
        
		final WorkflowInstanceEvent wfInstance = client.newCreateInstanceCommand().bpmnProcessId("Store_process")
				.latestVersion().variables(data).send().join();

		final long workflowInstanceKey = wfInstance.getWorkflowInstanceKey();
		System.out.println("Workflow instance created. Key: " + workflowInstanceKey);
		
	
		final JobWorker jobWorker = client.newWorker()
	            .jobType("initiate-payment")
	            .handler((jobClient, job) ->
	            {
	                final Map<String, Object> variables = job.getVariablesAsMap();

	                jobClient.newCompleteCommand(job.getKey())
	                    .send()
	                    .join();
	            })
	            .open();

		 client.newPublishMessageCommand().messageName("payment-received").correlationKey("1234").variables(data).send();
		
		final JobWorker jobWorker3 = client.newWorker()
	            .jobType("ship-without-insurance")
	            .handler((jobClient, job) ->
	            {
	                final Map<String, Object> variables = job.getVariablesAsMap();

	                jobClient.newCompleteCommand(job.getKey())
	                    .send()
	                    .join();
	            })
	            .fetchVariables("orderId")
	            .open();
		
		final JobWorker jobWorker2 = client.newWorker()
	            .jobType("ship-with-insurance")
	            .handler((jobClient, job) ->
	            {
	                final Map<String, Object> variables = job.getVariablesAsMap();

	                jobClient.newCompleteCommand(job.getKey())
	                    .send()
	                    .join();
	            })
	            .fetchVariables("orderId")
	            .open();
		
		/*jobWorker3.close();
		jobWorker.close();*/
		
		
		
		
	    }
	
	

}
