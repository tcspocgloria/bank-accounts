package com.coremei.bank.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.coremei.bank.accounts.model.Account;

@Repository
public interface AccountRepository extends CrudRepository<Account, Integer> {

}