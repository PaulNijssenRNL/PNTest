package com.ricoh.nsp.io.component.myprint.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import com.google.common.base.Strings;
import com.ricoh.nsp.io.component.framework.AbstractBaseEndpoint;
import com.ricoh.nsp.io.component.framework.AbstractGetEntryListProducer;
import com.ricoh.nsp.io.component.framework.resource.EntryResource;
import com.ricoh.nsp.io.component.framework.resource.FolderResource;
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

import static com.ricoh.nsp.io.component.myprint.operations.MyPrintConstants.*;

public class MyPrintGetEntryListProducer extends AbstractGetEntryListProducer {
	private final String FOLDER_RESOURCE_ID = "myPrint";
	private final String FOLDER_RESOURCE_NAME = "myPrint";

	//
	//
	public MyPrintGetEntryListProducer(AbstractBaseEndpoint endpoint) {
		super(endpoint);
	}

	//
	//
	@Override
	protected FolderResource getFolderInfo(String notUsed1, String notUsed2, Map<String, Object> option) {
		String spaceId = getNonEmptyParameter(PARAMS_TENANT_ID, option);
		String clientId = getNonEmptyParameter(PARAMS_CLIENT_ID, option);
		String clientSecret = getNonEmptyParameter(PARAMS_CLIENT_SECRET, option);
		String code = getParameter(PARAMS_CODE, option); // PIN
		String invalidCodeMessage = getParameter(PARAMS_INVALID_CODE_MSG, option);
		String noMoreFilesMessage = getParameter(PARAMS_NO_MORE_FILES_MSG, option);

		List<EntryResource> entries = new ArrayList<EntryResource>();
		String folderName = FOLDER_RESOURCE_NAME;

		HttpConnectionClient client = HttpConnectionClientBuilder.create();

		// Get the data stored as extension in the temporary use code space.
		Credential credential = Credential.builder().clientId(clientId).clientSecret(clientSecret).build();
		Authentication aut = new Authentication(credential, client);
		VerifyTemporaryCodeResult verifyResult = aut.verifyTemporaryCode(spaceId, code).verify();
		if (verifyResult.isInvalid()) {
			String errorMessage = verifyResult.getErrorMessage();
			if(Strings.isNullOrEmpty(errorMessage)) {
				if (Strings.isNullOrEmpty(invalidCodeMessage))
					folderName = "The supplied code (PIN) is not valid.";
				else
					folderName = invalidCodeMessage;
			} else {
				folderName = errorMessage;
			}
		} else {
			TemporaryCode temporaryCode = verifyResult.getTemporaryCode();
			TemporaryCodeExtension tce = TemporaryCodeExtension.fromMap(temporaryCode.getExtension());
			String tempAuthCode = tce.getTempAuthCode();
			String tenantId = tce.getTenantId();
			String userId = tce.getUserId();
			System.out.println("Temp aut code: " + tempAuthCode + ", user ID: " + userId);

			// Get the list with FileResources from the json file in the user area. 
			credential = Credential.builder().tempAuthCode(tempAuthCode).clientId(clientId).clientSecret(clientSecret).build();
			TemporaryFile temporaryFile = new TemporaryFile( credential, client, tenantId);
			String jsonFilename = String.format("%s.json", code);
			String json = temporaryFile.downloadFileFromUserArea(userId, TEF_FOLDER_ID).downloadJson(jsonFilename);
			if(Strings.isNullOrEmpty(json)) {
				if (Strings.isNullOrEmpty(noMoreFilesMessage))
					folderName = "No more files available.";
				else 
					folderName = noMoreFilesMessage;
			} else {
				final List<FileResource> files = Utils.jsonToFileResourceList(json);
	
				// Construct the output for the getEntryList.
				files.forEach(file -> {
					String id = file.getFileId();
					String name = file.getName();
					String ext = FilenameUtils.getExtension(name);
	
					EntryResource entry = new EntryResource();
					entry.setEntryId(id);
					entry.setEntryName(name);
					entry.setExtension(ext);
					entry.setType("file");
					entry.setEditable(true);
					entries.add(entry);
				});
			}
		}

		FolderResource resource = new FolderResource();
		resource.setFolderId(FOLDER_RESOURCE_ID);
		resource.setFolderName(folderName);
		resource.setParentId("");
		resource.setParentName("");
		resource.setEntries(entries);

		return resource;
	}

	//
	//
	private String getParameter(String paramName, Map<String, Object> option) {
		String value = (String) option.get(paramName);
		if (value == null || value.isEmpty())
			return "";
		else
			return value;
	}

	//
	//
	private String getNonEmptyParameter(String paramName, Map<String, Object> option) {
		String value = (String) option.get(paramName);
		if (value == null || value.isEmpty())
			throw new RuntimeException(String.format("Parameter '%s' is missing.", paramName));
		return value;
	}
}
