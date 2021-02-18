package com.ricoh.nsp.io.component.myprint.operations;

import java.util.List;
import com.ricoh.nsp.io.component.ComponentJsonResult;
import com.ricoh.nsp.io.component.framework.AbstractBaseEndpoint;
import com.ricoh.nsp.io.component.framework.OperationInput;
import com.ricoh.nsp.io.component.framework.OperationOutput;
import com.ricoh.nsp.io.component.myprint.api.Credential;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.FileResource;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.TemporaryCodeExtension;
import com.ricoh.nsp.io.component.myprint.api.services.tef.TemporaryFile;
import com.ricoh.nsp.io.component.myprint.api.services.tef.TemporaryFile.UploadFileToUserArea;
import com.ricoh.nsp.io.util.json.JsonConverter;

import static com.ricoh.nsp.io.component.myprint.operations.MyPrintConstants.*;

public class MyPrintDeleteProducer extends MyPrintAbstractBaseProducer {
	//
	//
	public MyPrintDeleteProducer(AbstractBaseEndpoint endpoint) {
		super(endpoint);
	}

	//
	//
	@Override
	protected OperationOutput operate(OperationInput arg0) {
		final String spaceId = getNonEmptyParameter(PARAMS_TENANT_ID);
		final String code = getNonEmptyParameter(PARAMS_CODE);
		final String fileId = getNonEmptyParameter(PARAMS_FILE_ID);

		// Get the data stored as extension in the temporary use code space.
		TemporaryCodeExtension tce = getTemporaryCodeExtension(spaceId, code);
		String userId = tce.getUserId();
		
		// Get the list with FileResources from the json file in the user area.
		Credential tempAutCredential = createTempAuthCredential(tce.getTempAuthCode()); 
		TemporaryFile temporaryFile = createTemporaryFile(tempAutCredential, tce.getTenantId());
		String jsonFilename = String.format("%s.json", code);
		List<FileResource> fileResources = getFileResources(temporaryFile, jsonFilename, userId);

		// Remove the file with the given ID, throw exception if ID is not valid.
		FileResource fr = fileResources.stream()
				.filter(r -> r.getFileId().equals(fileId)).findFirst()
				.orElseThrow(() -> createInternalError(String.format("The supplied file ID ('%s') is invalid", fileId)));
		temporaryFile.deleteFileFromUserArea(userId, TEF_FOLDER_ID).delete(fr.getTempName());

		fileResources.remove(fr);

		// Always delete the json file since the API does not overwrite an existing file (without any notification).
		// If there are PDF files left in the json file, create a new json file with the remaining PDF files.
		temporaryFile.deleteFileFromUserArea(userId, TEF_FOLDER_ID).delete(jsonFilename);
		if (!fileResources.isEmpty()) {
			String newJson = JsonConverter.createJson(fileResources);

			UploadFileToUserArea userAreaFileUploader = temporaryFile.new UploadFileToUserArea(userId, TEF_FOLDER_ID);
			userAreaFileUploader.uploadJson(jsonFilename, newJson);
		}
		
		String json = JsonConverter.createJson(fr);
		OperationOutput output = new OperationOutput(new ComponentJsonResult(json));
		return output;
	}
}
