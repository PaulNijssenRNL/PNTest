package com.ricoh.nsp.io.component.myprint.operations;

import static com.ricoh.nsp.io.component.myprint.operations.MyPrintConstants.PARAMS_ACCESS_TOKEN;
import static com.ricoh.nsp.io.component.myprint.operations.MyPrintConstants.PARAMS_CLIENT_ID;
import static com.ricoh.nsp.io.component.myprint.operations.MyPrintConstants.PARAMS_CLIENT_SECRET;
import static com.ricoh.nsp.io.component.myprint.operations.MyPrintConstants.TEF_FOLDER_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Strings;
import com.ricoh.nsp.io.component.ComponentException;
import com.ricoh.nsp.io.component.framework.AbstractBaseEndpoint;
import com.ricoh.nsp.io.component.framework.AbstractBaseProducer;
import com.ricoh.nsp.io.component.framework.DefaultErrorCode;
import com.ricoh.nsp.io.component.myprint.api.Credential;
import com.ricoh.nsp.io.component.myprint.api.services.aut.Authentication;
import com.ricoh.nsp.io.component.myprint.api.services.aut.Authentication.VerifyTemporaryCodeResult;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.FileResource;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.TemporaryCode;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.TemporaryCodeExtension;
import com.ricoh.nsp.io.component.myprint.api.services.tef.TemporaryFile;
import com.ricoh.nsp.io.component.myprint.utils.Utils;
import com.ricoh.nsp.io.util.http.HttpConnectionClient;
import com.ricoh.nsp.io.util.http.HttpConnectionClientBuilder;

public abstract class MyPrintAbstractBaseProducer extends AbstractBaseProducer {
	protected final HttpConnectionClient client;

	//
	//
	public MyPrintAbstractBaseProducer(AbstractBaseEndpoint endpoint) {
		super(endpoint);

		this.client = HttpConnectionClientBuilder.create();
	}

	//
	//
	protected Authentication createOauthAuthentication() {
		String accessToken = getNonEmptyParameter(PARAMS_ACCESS_TOKEN);
		Authentication auth = new Authentication(Credential.builder().accessToken(accessToken).build(), this.client);
		return auth;
	}

	//
	//
	protected Authentication createBasicAuthentication() {
		String clientId = getNonEmptyParameter(PARAMS_CLIENT_ID);
		String clientSecret = getNonEmptyParameter(PARAMS_CLIENT_SECRET);
		Authentication auth = new Authentication(Credential.builder().clientId(clientId).clientSecret(clientSecret).build(), this.client);
		return auth;
	}

	//
	//
	protected Credential createOauthCredential() {
		String accessToken = getNonEmptyParameter(PARAMS_ACCESS_TOKEN);
		Credential credential = Credential.builder().accessToken(accessToken).build();
		return credential;
	}

	//
	//
	protected Credential createTempAuthCredential(String tempAuthCode) {
		String clientId = getNonEmptyParameter(PARAMS_CLIENT_ID);
		String clientSecret = getNonEmptyParameter(PARAMS_CLIENT_SECRET);
		Credential credential = Credential.builder().tempAuthCode(tempAuthCode).clientId(clientId).clientSecret(clientSecret).build();
		return credential;
	}

	//
	//
	protected TemporaryFile createTemporaryFile(Credential credential, String tenantId) {
		TemporaryFile tef = new TemporaryFile(credential, this.client, tenantId);
		return tef;
	}

	//
	//
	protected TemporaryFile createTemporaryFile(String tenantId, String tempAuthCode) {
		String clientId = getNonEmptyParameter(PARAMS_CLIENT_ID);
		String clientSecret = getNonEmptyParameter(PARAMS_CLIENT_SECRET);
		Credential credential = Credential.builder().tempAuthCode(tempAuthCode).clientId(clientId).clientSecret(clientSecret).build();
		TemporaryFile temporaryFile = new TemporaryFile(credential, this.client, tenantId);
		return temporaryFile;
	}

	//
	//
	protected TemporaryCodeExtension getTemporaryCodeExtension(String spaceId, String code) {
		Authentication basicAut = createBasicAuthentication();
		VerifyTemporaryCodeResult verifyResult = basicAut.verifyTemporaryCode(spaceId, code).verify();
		if (verifyResult.isInvalid()) {
			throw createInternalError(String.format("The supplied code ('%s') is invalid", code));
		}
		TemporaryCodeExtension tce = getTemporaryCodeExtension(verifyResult);
		return tce;
	}

	//
	//
	protected TemporaryCodeExtension getTemporaryCodeExtension(VerifyTemporaryCodeResult verifyResult) {
		TemporaryCode temporaryCode = verifyResult.getTemporaryCode();
		TemporaryCodeExtension tce = TemporaryCodeExtension.fromMap(temporaryCode.getExtension());
		return tce;
	}

	//
	//
	protected List<FileResource> getFileResources(TemporaryFile temporaryFile, String jsonFilename, String userId) {
		String json = temporaryFile.downloadFileFromUserArea(userId, TEF_FOLDER_ID).downloadJson(jsonFilename);
		if (Strings.isNullOrEmpty(json)) {
			return new ArrayList<FileResource>();
		} else {
			List<FileResource> fileResources = Utils.jsonToFileResourceList(json);
			return fileResources;
		}
	}

	//
	//
	protected ComponentException createInternalError(String message) {
		return createError(DefaultErrorCode.EC_INTERNAL_ERROR, message);
	}

	//
	//
	private ComponentException createError(DefaultErrorCode errorCode, String message) {
		Map<String, Object> details = Collections.singletonMap("detail1", message);
		ComponentException ex = this.createException(errorCode, details, null);
		return ex;
	}
}
