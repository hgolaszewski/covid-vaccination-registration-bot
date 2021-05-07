package dto;

import static lombok.AccessLevel.PRIVATE;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ServicePoint {

  @Include
  String addressText;
  String id;
  String mobility;
  String name;


}
