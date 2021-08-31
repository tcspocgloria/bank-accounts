package com.coremei.bank.service.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.coremei.bank.accounts.model.Card;
import com.coremei.bank.accounts.model.Customer;

@FeignClient("cards")
public interface CardsFeignClient {

	@PostMapping(value = "myCards", consumes = "application/json")
	List<Card> getCardsDetails(@RequestHeader("bank-correlation-id") String correlationid,
			@RequestBody Customer customer);
}
