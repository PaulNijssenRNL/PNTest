package com.ricoh.nsp.io.component.myprint.api.services.aut.model;

import java.beans.ConstructorProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Value;

@lombok.Generated
@Value
@AllArgsConstructor(onConstructor = @__({ @ConstructorProperties({ "fileId", "name", "tempName" }) }))
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileResource {
	String fileId;
	String name;
	String tempName;
}
