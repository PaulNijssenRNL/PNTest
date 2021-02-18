package com.ricoh.nsp.io.component.myprint.api.services.aut.model;

import java.beans.ConstructorProperties;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Value;

@lombok.Generated
@Value
@AllArgsConstructor(onConstructor = @__({@ConstructorProperties({"tenantId", "userId"})}))
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
  String tenantId;
  String userId;
}
