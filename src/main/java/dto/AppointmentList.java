package dto;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = PRIVATE)
public class AppointmentList {

  List<Appointment> list;

}
