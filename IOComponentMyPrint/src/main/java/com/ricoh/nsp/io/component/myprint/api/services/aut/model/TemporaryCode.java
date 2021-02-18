package com.ricoh.nsp.io.component.myprint.api.services.aut.model;

import java.beans.ConstructorProperties;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Value;

@lombok.Generated
@Value
@AllArgsConstructor(onConstructor = @__({@ConstructorProperties({"code", "extension"})}))
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemporaryCode {
  String code;
  Map<String, Object> extension;
}
