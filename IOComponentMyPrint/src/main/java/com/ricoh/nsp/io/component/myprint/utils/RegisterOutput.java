package com.ricoh.nsp.io.component.myprint.utils;

import java.beans.ConstructorProperties;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.FileResource;

import lombok.AllArgsConstructor;
import lombok.Value;

@lombok.Generated
@Value
@AllArgsConstructor(onConstructor = @__({@ConstructorProperties({"code", "codeSpaceId", "files"})}))
@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterOutput {
  String code;
  String codeSpaceId;
  List<FileResource> files;
}
