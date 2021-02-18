package com.ricoh.nsp.io.component.myprint.api.services.tef;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.ricoh.nsp.io.component.myprint.api.Credential;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.TemporaryCode;
import com.ricoh.nsp.io.component.myprint.utils.Utils;
import com.ricoh.nsp.io.util.CharSetConstants;
import com.ricoh.nsp.io.util.FileUtil;
import com.ricoh.nsp.io.util.exception.HttpExecFailureException;
import com.ricoh.nsp.io.util.http.HttpConnectionClient;
import com.ricoh.nsp.io.util.http.HttpConnectionClient.Method;
import com.ricoh.nsp.io.util.http.entity.FileDataEntity;
import com.ricoh.nsp.io.util.http.entity.StringDataEntity;
import com.ricoh.nsp.io.util.http.request.RequestContent;
import com.ricoh.nsp.io.util.http.response.ResponseContent;
import com.ricoh.nsp.io.util.http.util.ContentTypeUtil;
import com.ricoh.nsp.io.util.json.JsonConverter;

import lombok.Value;

@lombok.Generated
@Value
public class TemporaryFile {
	Credential credential;
	HttpConnectionClient client;
	String tenantId;

	private static String baseUrl = Utils.getEndpoint();

	public FetchUserAreaAccessKey fetchUserAreaAccessKey(final String userId) {
		return new FetchUserAreaAccessKey(userId);
	}

	public DownloadFileFromUserArea downloadFileFromUserArea(final String userId, final String folderId) {
		return new DownloadFileFromUserArea(userId, folderId);
	}

	public DeleteFileFromUserArea deleteFileFromUserArea(final String userId, final String folderId) {
		return new DeleteFileFromUserArea(userId, folderId);
	}

	@lombok.Generated
	@Value
	public class FetchUserAreaAccessKey {
		String userId;

		public String fetchAccessKey(String accessUrl) {
			final String url = String.format("%s/v1/tmpfiles/tenants/%s/users/%s/temporaryCode/issue", baseUrl, tenantId, userId);
			final RequestContent request = new RequestContent(url, Method.POST);
			credential.toHeaders().forEach(kv -> request.addHeader(kv.getKey(), kv.getValue()));

			Pair<String, String> pair = Pair.of("access_url", accessUrl);
			String json = JsonConverter.createJson(pair);
			request.setEntity(new StringDataEntity(json, ContentTypeUtil.getJsonType(), CharSetConstants.UTF_8));

			final ResponseContent response = client.sendRequest(request);
			TemporaryCode temporaryCode = JsonConverter.readJson(response.getBody(), TemporaryCode.class);
			return temporaryCode.getCode();
		}
	}

	@lombok.Generated
	@Value
	public class UploadFileToUserArea {
		String userId;
		String folderId;

		public String getFolderUrl() {
			final String url = String.format("%s/v1/tmpfiles/tenants/%s/users/%s/folders/%s", baseUrl, tenantId, userId, folderId);
			return url;
		}

		public void uploadFile(String fileName, File file) {
			String url = String.format("%s/files/%s", getFolderUrl(), fileName);
			RequestContent request = new RequestContent(url, Method.PUT);
			credential.toHeaders().forEach(kv -> request.addHeader(kv.getKey(), kv.getValue()));
			request.addHeader("Content-Type", "application/octet-stream");
			request.setEntity(new FileDataEntity(file));
			client.sendRequest(request);
		}

		public void uploadJson(String fileName, String json) {
			// For now we create a local temp-file and send this file to temp storage. Might be changed so the json is send directly.  
			File tempFile = createTempFile(json);

			uploadFile(fileName, tempFile);

			tempFile.delete();
		}

		private File createTempFile(String json) {
			try {
				File tempFile = File.createTempFile("myPrint_", ".json");
				tempFile.deleteOnExit();

				BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
				bw.write(json);
				bw.close();

				return tempFile;
			} catch (IOException e) {
				throw new RuntimeException("Save json to temp file has failed.", e);
			}
		}
	}

	@lombok.Generated
	@Value
	public class DownloadFileFromUserArea {
		String userId;
		String folderId;

		public File downloadFile(String sourceFileName, String destinationFileName) {
			final File tmpFile = downloadTempFile(sourceFileName);
			final String parentPath = FileUtil.getParentPath(tmpFile.getAbsolutePath());
			final String newFilePath = new StringBuilder(parentPath).append("/").append(destinationFileName).toString();
			final File newFile = new File(newFilePath);
			tmpFile.renameTo(newFile);
			return newFile;
		}

		public String downloadJson(String fileName) {
			File tmpFile = null;
			try {
				tmpFile = downloadTempFile(fileName);
			} catch(Exception e) {
				// If HTTP 404 (Not Found) is returned the JSON file is not available
				// or no longer available (because all files have been printed). 
				if (e.getCause() instanceof HttpExecFailureException) {
					HttpExecFailureException ex = (HttpExecFailureException) e.getCause();
					if (Objects.equals(ex.getStatusCode(), Integer.valueOf(404))) {
						return "";
					}
				} 
				throw e;
			}

			try (Stream<String> stream = Files.lines(tmpFile.toPath(), StandardCharsets.UTF_8)) {
				StringBuilder content = new StringBuilder();
				stream.forEach(s -> content.append(s).append("\n"));
				return content.toString();
			} catch (IOException e) {
				throw new RuntimeException("Read json from temp file has failed.", e);
			}
		}

		private File downloadTempFile(String fileName) {
			final String url = String.format("%s/v1/tmpfiles/tenants/%s/users/%s/folders/%s/files/%s", baseUrl, tenantId, userId, folderId, fileName);
			final RequestContent request = new RequestContent(url, Method.GET);
			credential.toHeaders().forEach(kv -> request.addHeader(kv.getKey(), kv.getValue()));
			request.addHeader("Content-Type", "application/octet-stream");
			final File tmpFile = FileUtil.createTempFile();
			tmpFile.deleteOnExit();
			client.sendRequest(request, null, tmpFile.getAbsolutePath());
			return tmpFile;
		}
	}

	@lombok.Generated
	@Value
	public class DeleteFileFromUserArea {
		String userId;
		String folderId;

		public void delete(String fileName) {
			final String url = String.format("%s/v1/tmpfiles/tenants/%s/users/%s/folders/%s/files/%s", baseUrl, tenantId, userId, folderId, fileName);
			final RequestContent request = new RequestContent(url, Method.DELETE);
			credential.toHeaders().forEach(kv -> request.addHeader(kv.getKey(), kv.getValue()));
			client.sendRequest(request);
		}
	}
}
