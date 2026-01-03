package org.httt2.hrms.storage.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PutPresignedResult {
  String url;
  String key;
}
