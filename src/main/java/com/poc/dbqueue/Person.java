package com.poc.dbqueue;

import org.springframework.data.annotation.Id;

public class Person {

	public enum Status {
		CREATED,
		PROCESSING,
		APPROVED
	}

	@Id
	private String id;

	private String firstName;
	private String lastName;
	private Status status = Status.CREATED;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

}