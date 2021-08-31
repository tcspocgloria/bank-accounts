package com.coremei.bank.service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.coremei.bank.accounts.model.Customer;
import com.coremei.bank.accounts.model.Loan;

@FeignClient("loans")
public interface LoansFeignClient {

	@PostMapping(value = "myLoans", consumes = "application/json")
	List<Loan> getLoansDetail(@RequestHeader("bank-correlation-id") String correlationId,
			@RequestBody Customer customer);
}
