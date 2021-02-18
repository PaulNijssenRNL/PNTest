/*
 * Copyright(c) 2019 Ricoh Co., Ltd. All Rights Reserved.
 */

package com.ricoh.nsp.io.component.myprint.operations;

import java.io.File;
import java.util.List;
import com.ricoh.nsp.io.component.ComponentJsonResult;
import com.ricoh.nsp.io.component.IOBodyElement;
import com.ricoh.nsp.io.component.framework.AbstractBaseEndpoint;
import com.ricoh.nsp.io.component.framework.OperationInput;
import com.ricoh.nsp.io.component.framework.OperationOutput;
import com.ricoh.nsp.io.component.myprint.api.Credential;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.FileResource;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.TemporaryCodeExtension;
import com.ricoh.nsp.io.component.myprint.api.services.tef.TemporaryFile;
import static com.ricoh.nsp.io.component.myprint.operations.MyPrintConstants.*;

public class MyPrintReleaseProducer extends MyPrintAbstractBaseProducer {
	//
	//
	public MyPrintReleaseProducer(final AbstractBaseEndpoint endpoint) {
		super(endpoint);
	}

	//
	// If multiple files to print are selected (through getEntryList) this method is called for each
	// single file. So if, for example, three files are selected, this method is called three times. 
	//
	@Override
	protected OperationOutput operate(final OperationInput input) {
		final String spaceId = getNonEmptyParameter(PARAMS_TENANT_ID);
		final String code = getNonEmptyParameter(PARAMS_CODE);
		final String fileId = getNonEmptyParameter(PARAMS_FILE_ID);

		// Get the data stored as extension in the temporary use code space.
		TemporaryCodeExtension tce = getTemporaryCodeExtension(spaceId, code);
		String userId = tce.getUserId();
		String tenantId = tce.getTenantId();
		
		// Get the list with FileResources from the json file in the user area.
		Credential tempAutCredential = createTempAuthCredential(tce.getTempAuthCode()); 
		TemporaryFile temporaryFile = createTemporaryFile(tempAutCredential, tenantId);
		String jsonFilename = String.format("%s.json", code);
		List<FileResource> fileResources = getFileResources(temporaryFile, jsonFilename, userId);

		// Download the file with the given ID, throw exception if ID is not valid.
		FileResource fr = fileResources.stream()
				.filter(r -> r.getFileId().equals(fileId)).findFirst()
				.orElseThrow(() -> createInternalError(String.format("The supplied file ID ('%s') is invalid", fileId)));
		File file = temporaryFile.downloadFileFromUserArea(userId, TEF_FOLDER_ID).downloadFile(fr.getTempName(), fr.getName());
		
		OperationOutput output = new OperationOutput(new ComponentJsonResult("{}"));			
		output.addBodyElement(new IOBodyElement(file)); 
		return output;
	}
	

}
