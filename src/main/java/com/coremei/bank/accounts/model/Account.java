package com.coremei.bank.accounts.model;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@ToString
public class Account {

	@Column(name = "account_number")
	private int accountNumber;

	@Id
	@Column(name = "customer_id")
	private int customerId;
	
	@Column(name = "account_type")
	private String type;

	@Column(name = "branch_address")
	private String branchAddress;

	@Column(name = "create_dt")
	private LocalDate createDt;

}
