package com.coremei.bank.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaAsync;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.coremei.bank.accounts.model.Account;
import com.coremei.bank.accounts.model.Card;
import com.coremei.bank.accounts.model.Customer;
import com.coremei.bank.accounts.model.CustomerDetails;
import com.coremei.bank.accounts.model.Loan;
import com.coremei.bank.accounts.model.Properties;
import com.coremei.bank.config.AccountServiceConfig;
import com.coremei.bank.repository.AccountRepository;
import com.coremei.bank.service.client.CardsFeignClient;
import com.coremei.bank.service.client.LoansFeignClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;

@RestController
public class AccountController {

	@Autowired
	private AccountRepository repository;

	@Autowired
	private AccountServiceConfig config;

	@Autowired
	private LoansFeignClient loansFeignClient;

	@Autowired
	private CardsFeignClient cardsFeignClient;

	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

	@PostMapping("/myAccount")
	@Timed(value = "getAccountsDetails.time", description = "Time taken to return account details")
	public Account getAccountDetails(@RequestBody Customer customer) {
		return repository.findById(customer.getCustomerId()).orElseGet(null);
	}

	@GetMapping("/accounts/properties")
	public String getPropertyDetails() throws JsonProcessingException {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		Properties properties = new Properties(config.getMsg(), config.getBuildVersion(), config.getMailDetails(),
				config.getActiveBranches());
		return ow.writeValueAsString(properties);
	}

	@PostMapping("/myCustomerDetails")
// "detailsForCustomerSupportApp tiene configurado valores en application.properties
//	@CircuitBreaker(name = "detailsForCustomerSupportApp", fallbackMethod = "myCustomerDetailsFallBack")
	@Retry(name = "retryFormCustomerDetails", fallbackMethod = "myCustomerDetailsFallBack")
	public CustomerDetails myCustomerDetails(@RequestHeader("bank-correlation-id") String correlationId,
			@RequestBody Customer customer) {
		logger.info("myCustomerDetails() method started");

		Account accounts = repository.findById(customer.getCustomerId()).orElse(null);
		List<Loan> loans = loansFeignClient.getLoansDetail(correlationId, customer);
		List<Card> cards = cardsFeignClient.getCardsDetails(correlationId, customer);

		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accounts);
		customerDetails.setLoans(loans);
		customerDetails.setCards(cards);

		logger.info("myCustomerDetails() method finished");
		return customerDetails;
	}

	private CustomerDetails myCustomerDetailsFallBack(@RequestHeader("bank-correlation-id") String correlationId,
			Customer customer, Throwable t) {
		Account accounts = repository.findById(customer.getCustomerId()).orElse(null);
		List<Loan> loans = loansFeignClient.getLoansDetail(correlationId, customer);

		CustomerDetails customerDetails = new CustomerDetails();
		customerDetails.setAccounts(accounts);
		customerDetails.setLoans(loans);
		return customerDetails;
	}

	@GetMapping("/sayHello")
	@RateLimiter(name = "sayHello", fallbackMethod = "sayHelloFallBack")
	public String sayHello() {
		return "Hello, Welcome to the bank with Kubernetes Cluster V2";
	}

	@GetMapping("/lambda")
	public void callLambdaSes() {
		AWSLambdaAsyncClientBuilder lambdaBuilder = AWSLambdaAsyncClientBuilder.standard().withRegion(Regions.US_EAST_1);
		AWSLambdaAsync lambdaAsynClient = lambdaBuilder.build();
		
		JSONObject payloadObject = new JSONObject();
		try {
			payloadObject.put("emailFrom", "mke.nolasco@gmail.com");
			payloadObject.put("emailTo", "mke.nolasco@gmail.com");
		} catch (JSONException ex) {
			logger.error("Error al construir json", ex);
		}
		
		InvokeRequest request = new InvokeRequest();
		request.withFunctionName("common-email-fnc").withPayload(payloadObject.toString());
		
		try {
			InvokeResult result = lambdaAsynClient.invokeAsync(request).get();
			String resultJSON = new String(result.getPayload().array(), StandardCharsets.UTF_8);
			logger.info(resultJSON);
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Error al invocar al lambda de SES", e);
		}
	}

	private String sayHelloFallBack(Throwable t) {
		return "Hi, Welcome to the bank";
	}

}
