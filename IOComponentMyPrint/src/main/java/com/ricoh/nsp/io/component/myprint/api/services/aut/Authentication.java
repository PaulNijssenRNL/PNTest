package com.ricoh.nsp.io.component.myprint.api.services.aut;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.ricoh.nsp.io.component.myprint.api.Credential;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.TemporaryCode;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.TemporaryCodeSpace;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.User;
import com.ricoh.nsp.io.component.myprint.utils.ApiError;
import com.ricoh.nsp.io.component.myprint.utils.Utils;
import com.ricoh.nsp.io.util.CharSetConstants;
import com.ricoh.nsp.io.util.exception.ExternalSystemErrorException;
import com.ricoh.nsp.io.util.exception.HttpExecFailureException;
import com.ricoh.nsp.io.util.http.HttpConnectionClient;
import com.ricoh.nsp.io.util.http.HttpConnectionClient.Method;
import com.ricoh.nsp.io.util.http.entity.StringDataEntity;
import com.ricoh.nsp.io.util.http.request.RequestContent;
import com.ricoh.nsp.io.util.http.response.ResponseContent;
import com.ricoh.nsp.io.util.http.util.ContentTypeUtil;
import com.ricoh.nsp.io.util.json.JsonConverter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;

@lombok.Generated
@Value
public class Authentication {
	Credential credential;
	HttpConnectionClient client;

	private static String baseUrl = Utils.getEndpoint();

	public FetchMe fetchMe() {
		return new FetchMe();
	}

	public FetchTemporaryCodeSpace fetchTemporaryCodeSpace(final String spaceId) {
		return new FetchTemporaryCodeSpace(spaceId);
	}

	public CreateTemporaryCodeSpace createTemporaryCodeSpace(final String spaceId) {
		return new CreateTemporaryCodeSpace(spaceId);
	}
	
	public DeleteTemporaryCodeSpace deleteTemporaryCodeSpace(final String spaceId) {
		return new DeleteTemporaryCodeSpace(spaceId);
	}

	public IssueTemporaryCode issueTemporaryCode(final String spaceId) {
		return new IssueTemporaryCode(spaceId);
	}

	public VerifyTemporaryCode verifyTemporaryCode(final String spaceId, final String code) {
		return new VerifyTemporaryCode(spaceId, code);
	}

	@lombok.Generated
	@Value
	public class FetchMe {
		public User fetch() {
			final String url = String.format("%s/v1/aut/me", baseUrl);
			final RequestContent request = new RequestContent(url, Method.GET);
			credential.toHeaders().forEach(kv -> request.addHeader(kv.getKey(), kv.getValue()));
			final ResponseContent response = client.sendRequest(request);
			return JsonConverter.readJson(response.getBody(), User.class);
		}
	}

	@lombok.Generated
	@Value
	public class FetchTemporaryCodeSpace {
		String spaceId;

		public TemporaryCodeSpace fetch() {
			final String url = String.format("%s/v1/aut/temporaryCode/spaces/%s", baseUrl, spaceId);
			final RequestContent request = new RequestContent(url, Method.GET);
			credential.toHeaders().forEach(kv -> request.addHeader(kv.getKey(), kv.getValue()));
			final ResponseContent response = client.sendRequest(request);
			return JsonConverter.readJson(response.getBody(), TemporaryCodeSpace.class);
		}
	}

	@lombok.Generated
	@Getter
	@Accessors(fluent = true)
	@RequiredArgsConstructor
	public class CreateTemporaryCodeSpace {
		final String spaceId;

		@Setter
		String characters = "";
		@Setter
		int length = 0;
		@Setter
		int expiresIn = 0;
		@Setter
		double collisionProbabilityLimit;
		@Setter
		boolean oneTime = false;

		public TemporaryCodeSpace create() {
			final String url = String.format("%s/v1/aut/temporaryCode/spaces", baseUrl);
			final RequestContent request = new RequestContent(url, Method.POST);
			credential.toHeaders().forEach(kv -> request.addHeader(kv.getKey(), kv.getValue()));

			@SuppressWarnings("serial")
			final Map<String, Object> body = new HashMap<String, Object>() {
				{
					put("spaceId", spaceId);
					if (StringUtils.isNotEmpty(characters)) {
						put("characters", characters);
					}
					if (length != 0) {
						put("length", length);
					}
					if (expiresIn > 0) {
						put("expiresIn", expiresIn);
					}
					if (collisionProbabilityLimit > 0) {
						put("collisionProbabilityLimit", collisionProbabilityLimit);
					}
					put("oneTime", oneTime);
				}
			};
			final String json = JsonConverter.createJson(body);
			request.setEntity(new StringDataEntity(json, ContentTypeUtil.getJsonType(), CharSetConstants.UTF_8));
			final ResponseContent response = client.sendRequest(request);
			return JsonConverter.readJson(response.getBody(), TemporaryCodeSpace.class);
		}
	}

	@lombok.Generated
	@Getter
	@Accessors(fluent = true)
	@RequiredArgsConstructor
	public class DeleteTemporaryCodeSpace {
		final String spaceId;

		public void delete() {
			final String url = String.format("%s/v1/aut/temporaryCode/spaces/%s", baseUrl, spaceId);
			final RequestContent request = new RequestContent(url, Method.DELETE);
			credential.toHeaders().forEach(kv -> request.addHeader(kv.getKey(), kv.getValue()));			

			final ResponseContent response = client.sendRequest(request);
		}
	}

	@lombok.Generated
	@Getter
	@Accessors(fluent = true)
	@RequiredArgsConstructor
	public class IssueTemporaryCode {
		final String spaceId;
		@Setter
		Map<String, Object> extension = null;

		public TemporaryCode issue() {
			final String url = String.format("%s/v1/aut/temporaryCode/spaces/%s/issue", baseUrl, spaceId);
			final RequestContent request = new RequestContent(url, Method.POST);
			credential.toHeaders().forEach(kv -> request.addHeader(kv.getKey(), kv.getValue()));			

			@SuppressWarnings("serial")
			final Map<String, Object> body = new HashMap<String, Object>() {
				{
					if (Objects.nonNull(extension)) {
						put("extension", extension);
					}
				}
			};
			final String json = JsonConverter.createJson(body);
			request.setEntity(new StringDataEntity(json, ContentTypeUtil.getJsonType(), CharSetConstants.UTF_8));

			final ResponseContent response = client.sendRequest(request);
			return JsonConverter.readJson(response.getBody(), TemporaryCode.class);
		}
	}

	@lombok.Generated
	@Value
	public class VerifyTemporaryCodeResult {
		boolean invalid;
		TemporaryCode temporaryCode;
		String errorMessage;
	}

	@lombok.Generated
	@Value
	public class VerifyTemporaryCode {
		String spaceId;
		String code;

		public VerifyTemporaryCodeResult verify() {
			try {
				final String url = String.format("%s/v1/aut/temporaryCode/spaces/%s/verify", baseUrl, spaceId);
				final RequestContent request = new RequestContent(url, Method.POST);
				credential.toHeaders().forEach(kv -> request.addHeader(kv.getKey(), kv.getValue()));
				request.addHeader("X-Temporary-Authorization-Code", code);
				final ResponseContent response = client.sendRequest(request);
				TemporaryCode temporaryCode = JsonConverter.readJson(response.getBody(), TemporaryCode.class);
				return new VerifyTemporaryCodeResult(false, temporaryCode, "");
			} catch (ExternalSystemErrorException e) {
				// Handle a HTTP 400 (Bad request) with error- id "error.wrong_temporary_code". 
				// This error is caused by a wrong code (PIN) and must be shown at the user.
				if (e.getCause() instanceof HttpExecFailureException) {
					HttpExecFailureException ex = (HttpExecFailureException) e.getCause();
					if (Objects.equals(ex.getStatusCode(), Integer.valueOf(400))) {
						ApiError apiError = ApiError.fromJson(ex.getResponseBody());
						if (apiError.getId().equals("error.wrong_temporary_code"))
							return new VerifyTemporaryCodeResult(true, null, "");
					}
				}
				return new VerifyTemporaryCodeResult(true, null, "Code verification has failed.");
			}
		}
	}
}
