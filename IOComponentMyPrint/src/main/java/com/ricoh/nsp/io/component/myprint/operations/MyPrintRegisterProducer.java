/*
 * Copyright(c) 2017 Ricoh Co., Ltd. All Rights Reserved.
 */

package com.ricoh.nsp.io.component.myprint.operations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Strings;
import com.ricoh.nsp.io.component.ComponentException;
import com.ricoh.nsp.io.component.ComponentJsonResult;
import com.ricoh.nsp.io.component.IOBodyElement;
import com.ricoh.nsp.io.component.framework.AbstractBaseEndpoint;
import com.ricoh.nsp.io.component.framework.DefaultErrorCode;
import com.ricoh.nsp.io.component.framework.OperationInput;
import com.ricoh.nsp.io.component.framework.OperationOutput;
import com.ricoh.nsp.io.component.myprint.api.Credential;
import com.ricoh.nsp.io.component.myprint.api.services.aut.Authentication;
import com.ricoh.nsp.io.component.myprint.api.services.aut.Authentication.CreateTemporaryCodeSpace;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.FileResource;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.TemporaryCode;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.TemporaryCodeSpace;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.User;
import com.ricoh.nsp.io.component.myprint.api.services.tef.TemporaryFile;
import com.ricoh.nsp.io.component.myprint.api.services.tef.TemporaryFile.UploadFileToUserArea;
import com.ricoh.nsp.io.component.myprint.utils.RegisterOutput;
import com.ricoh.nsp.io.util.exception.ExternalSystemErrorException;
import com.ricoh.nsp.io.util.exception.HttpExecFailureException;
import com.ricoh.nsp.io.util.json.JsonConverter;

import static com.ricoh.nsp.io.component.myprint.operations.MyPrintConstants.*;

public class MyPrintRegisterProducer extends MyPrintAbstractBaseProducer {
	private final String DEFAULT_TMP_CODESPACE_CHARACTERS = "0123456789";
	private final int DEFAULT_TMP_CODESPACE_LENGTH = 6;

	//
	//
	public MyPrintRegisterProducer(final AbstractBaseEndpoint endpoint) {
		super(endpoint);
	}

	//
	//
	@Override
	protected OperationOutput operate(final OperationInput input) {
		checkInput(input);

		int codeSpaceLength = getCodeSpaceLength();
		String codeSpaceChars = getCodeSpaceChars();

		Authentication oauthAut = createOauthAuthentication();
		Authentication basicAut = createBasicAuthentication();

		final User user = oauthAut.fetchMe().fetch();
		final String userId = user.getUserId();
		final String tenantId = user.getTenantId();
		
		// Create temporary code space (if this not exists). We use the tenant-ID as codespace-ID.
		String codeSpaceId = tenantId;
		createTemporaryCodeSpace(basicAut, codeSpaceId, codeSpaceChars, codeSpaceLength);

		// Initialize temporary file storage for user area.
		final Credential credential = createOauthCredential();
		final TemporaryFile tef = createTemporaryFile(credential, tenantId);
		UploadFileToUserArea userAreaFileUploader = tef.new UploadFileToUserArea(userId, TEF_FOLDER_ID);

		// Get a temporary access key (temporary usage code) for user area. 
		final String accessUrl = userAreaFileUploader.getFolderUrl();
		final String tempAccessKey = tef.fetchUserAreaAccessKey(userId).fetchAccessKey(accessUrl);

		// Issue a temporary use code (PIN) within the specified temporary use code space. 
		String pinCode = issueTempUsageCode(codeSpaceId, tenantId, userId, tempAccessKey, basicAut).getCode();

		// Save files in user area of temporary file storage and create a list with information of each file.
		final List<FileResource> files = new ArrayList<FileResource>();
		for (int i = 0; i < input.getBodyElementSize(); i++) {
			IOBodyElement element = input.getBodyElement(i);
			String name = element.getFilename();
			String fileId = String.valueOf(i);
			String tempName = String.format("%s-%d", pinCode, i);

			userAreaFileUploader.uploadFile(tempName, element.getFile());

			files.add(new FileResource(fileId, name, tempName));
		}

		// Save information of the received files in a json-file in same temporary file storage.
		String json = JsonConverter.createJson(files);
		String jsonFilename = String.format("%s.json", pinCode);
		userAreaFileUploader.uploadJson(jsonFilename, json);

		String outputJson = JsonConverter.createJson(new RegisterOutput(pinCode, codeSpaceId, files));
		return new OperationOutput(new ComponentJsonResult(outputJson));
	}

	//
	//
	private void checkInput(OperationInput input) {
		if (input.getBodyElementSize() < 1) {
			throw createException(DefaultErrorCode.EC_NO_INPUT_FILES);
		}
	}

	//
	//
	private int getCodeSpaceLength() {
		String lenString = getParameter(PARAMS_CODE_SPACE_LENGTH);
		System.out.println("====> code space len: " + lenString);
		if (Strings.isNullOrEmpty(lenString))
			return DEFAULT_TMP_CODESPACE_LENGTH;
		else {
			try {
				int len = Integer.parseInt(lenString);
				return len;
			} catch (NumberFormatException e) {
				Map<String, Object> options = Collections.singletonMap("Info", "Invalid value for parameter " + PARAMS_CODE_SPACE_LENGTH);
				ComponentException ex = createException(DefaultErrorCode.EC_INTERNAL_ERROR, options, e);
				throw ex;
			}
		}
	}
	
	//
	//
	private String getCodeSpaceChars() {
		String codeSpaceChars = getParameter(PARAMS_CODE_SPACE_CHARS);
		if (Strings.isNullOrEmpty(codeSpaceChars))
			return DEFAULT_TMP_CODESPACE_CHARACTERS;
		else
			return codeSpaceChars;
	}

	//
	//
	private void createTemporaryCodeSpace(Authentication basicAut, String spaceId, String characters, int length) {
		boolean createTempCodeSpace = true;
		boolean removeTempCodeSpace = false;
		
		try {
			TemporaryCodeSpace codeSpace = basicAut.fetchTemporaryCodeSpace(spaceId).fetch();
			// If fetch does not fail the code space is available so creation is not needed. 
			createTempCodeSpace = false;
			
			// Check if the length of the code space or the character set has changed. If so, 
			// the code space have to be removed and recreated.
			int codeSpaceLength = codeSpace.getLength();
			String codeSpaceChars = codeSpace.getCharacters();
			removeTempCodeSpace = (codeSpaceLength != length) || (!codeSpaceChars.equals(characters)); 
		} catch (ExternalSystemErrorException e) {
			if (!(e.getCause() instanceof HttpExecFailureException)) {
				throw e;
			}
			HttpExecFailureException ex = (HttpExecFailureException) e.getCause();
			if (!Objects.equals(ex.getStatusCode(), Integer.valueOf(404))) {
				throw e;
			}
		}
		
		if(removeTempCodeSpace) {
			basicAut.deleteTemporaryCodeSpace(spaceId).delete();
			createTempCodeSpace = true;
		}

		if (createTempCodeSpace) {
			// The "Temporary use code" (temp auth for user area) is valid for 72 hours 
			// so the temporary coded space (PIN) should be valid for the same period. 
			final int expiresIn = 72 * 3600;

			// The collisionProbabilityLimit is set to a fixed value of 0.01 here. 
			CreateTemporaryCodeSpace tempCodeSpace = basicAut.createTemporaryCodeSpace(spaceId);
			tempCodeSpace.characters(characters).length(length).expiresIn(expiresIn).collisionProbabilityLimit(0.01).create();
		}
	}

	//
	//
	private TemporaryCode issueTempUsageCode(String spaceId, String tenantId, String userId, String tempAccessKey, Authentication basicAut) {
		final Map<String, Object> extension = new HashMap<>();
		extension.put("tenantId", tenantId);
		extension.put("userId", userId);
		extension.put("tempAuthCode", tempAccessKey);
		return basicAut.issueTemporaryCode(spaceId).extension(extension).issue();
	}
}
