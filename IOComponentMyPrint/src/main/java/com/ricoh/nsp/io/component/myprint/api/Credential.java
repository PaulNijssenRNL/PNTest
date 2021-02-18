package com.ricoh.nsp.io.component.myprint.api;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

import lombok.Builder;
import lombok.Value;

@lombok.Generated
@Value
@Builder
public class Credential {
  @Nullable
  String clientId;
  @Nullable
  String clientSecret;
  @Nullable
  String tenantAccessKey;
  @Nullable
  String accessToken;
  @Nullable
  String tempAuthCode;

  public List<Pair<String, String>> toHeaders() {
    final List<Pair<String, String>> headers = new ArrayList<>();

    if (Objects.nonNull(clientId) && Objects.nonNull(clientSecret)) {
      final byte[] raw = String.format("%s:%s", clientId, clientSecret).getBytes();
      final String auth = Base64.getEncoder().encodeToString(raw);
      headers.add(Pair.of("Authorization", String.format("Basic %s", auth)));
    }
    if (Objects.nonNull(tenantAccessKey)) {
      headers.add(Pair.of("X-Tenant-Access-Key", tenantAccessKey));
    }
    if (Objects.nonNull(accessToken)) {
      headers.add(Pair.of("Authorization", String.format("Bearer %s", accessToken)));
    }
    if (Objects.nonNull(tempAuthCode)) {
      headers.add(Pair.of("X-Temporary-Authorization-Code", tempAuthCode));
    }
    return headers;
  }
}
