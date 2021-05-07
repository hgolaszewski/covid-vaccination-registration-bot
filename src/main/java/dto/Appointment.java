package dto;

import static lombok.AccessLevel.PRIVATE;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Appointment {

  String id;
  @Include
  String vaccineType;
  Integer dose;
  Integer duration;
  String mobility;
  @Include
  ServicePoint servicePoint;
  @Include
  Date startAt;
  String status;

  @Override
  public String toString() {
    return String.format(
        "Vaccine = %s | Date = %s | Location = %s | Name = %s | Id = %s",
        vaccineType, startAt, servicePoint.getAddressText(), servicePoint.getName(), id
    );
  }

}
