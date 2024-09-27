package br.com.exceptions;

import java.io.Serializable;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

public class StandardError implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private String message;
	private String details;
	@JsonFormat(shape = Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
	private String timestamp;
	
	public StandardError() {
	}

	public StandardError(String message, String details, Instant timestamp) {
		super();
		this.message = message;
		this.details = details;
		this.timestamp = DateTimeFormatter.ISO_INSTANT.format(timestamp);
	}

	public String getMessage() {
		return message;
	}

	public String getDetails() {
		return details;
	}

	public String getTimestamp() {
		return timestamp;
	}
}
