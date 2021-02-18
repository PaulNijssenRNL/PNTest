package com.ricoh.nsp.io.component.myprint.api.services.aut.model;

import java.util.Map;

import com.google.common.base.Strings;

import lombok.Getter;

@lombok.Generated
public class TemporaryCodeExtension {
	@Getter
	private String tenantId;
	@Getter
	private String userId;
	@Getter
	private String tempAuthCode;

	public static TemporaryCodeExtension fromMap(Map<String, Object> map) {
		TemporaryCodeExtension tce = new TemporaryCodeExtension();

		tce.tempAuthCode = map.getOrDefault("tempAuthCode", "").toString();
		tce.tenantId = map.getOrDefault("tenantId", "").toString();
		tce.userId = map.getOrDefault("userId", "").toString();

		if (Strings.isNullOrEmpty(tce.tempAuthCode) || Strings.isNullOrEmpty(tce.tenantId) || Strings.isNullOrEmpty(tce.userId))
			throw new RuntimeException("Data is missing in temporary code extension.");

		return tce;
	}
}
