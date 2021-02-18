package com.ricoh.nsp.io.component.myprint.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.FileResource;

public class Utils {
	private static String endpoint = null;

	//
	//
	public static List<FileResource> jsonToFileResourceList(String json) {
		List<FileResource> files = null;
		try {
			ObjectMapper mapper = new ObjectMapper();
			files = new ArrayList<>(Arrays.asList(mapper.readValue(json, FileResource[].class)));
			return files;
		} catch (IOException  ex) {
			throw new RuntimeException("Error converting json to FileResource list.", ex);
		}
	}

	//
	//
	public static String getEndpoint() {
		if (Strings.isNullOrEmpty(Utils.endpoint)) {
			try {
				final String hostname = getHostName();
				final String[] names = hostname.split("\\.");
				final String env = names[names.length - 1];
				Utils.endpoint = String.format("https://api.%s.smart-integration.ricoh.com", env);
			} catch (final UnknownHostException e) {
				throw new RuntimeException("Error determining the base URL for the end point", e);
			}
		}
		return Utils.endpoint;
	}

	//
	//
	private static String getHostName() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostName();
	}
}
