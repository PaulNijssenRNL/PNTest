package com.ricoh.nsp.io.component.myprint.utils;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiError {
	private String id;
	private String message;

	public static ApiError fromJson(String json) {
		ApiError apiError = new ApiError();

		try {
			ObjectMapper mapper = new ObjectMapper();
			Errors errors = mapper.readValue(json, Errors.class);
			Error err = errors.getFirstError();
			if (err != null) {
				apiError.id = err.getId();
				apiError.message = err.getMessage();
			}
			return apiError;
		} catch (Exception e) {
			throw new RuntimeException("Error parsing json or API error message", e);
		}
	}

	public String getId() {
		return id;
	}

	public String getMessage() {
		return message;
	}

	private ApiError() {
		this.id = "";
		this.message = "";
	}

	@JsonRootName(value = "Errors")
	static class Errors {
		private final List<Error> errors;

		private Errors() {
			this.errors = new ArrayList<Error>();
		}

		public List<Error> getErrors() {
			return errors;
		}

		@JsonIgnore
		public Error getFirstError() {
			if (errors.size() > 0) {
				return errors.get(0);
			} else {
				return null;
			}
		}
	}

	static class Error {
		@JsonProperty("message_id")
		private String id = "";
		private String message = "";

		public String getId() {
			return id;
		}

		public String getMessage() {
			return message;
		}
	}
}
