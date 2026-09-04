package com.freightfox.transporter;

import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Repository
class InputRepository {
  private final AtomicReference<InputData> data = new AtomicReference<>();
  void save(List<Lane> lanes, List<Transporter> transporters) { data.set(new InputData(List.copyOf(lanes), List.copyOf(transporters))); }
  InputData get() { return Optional.ofNullable(data.get()).orElseThrow(() -> new ApiException(404, "INPUT_NOT_FOUND", "No transporter input data has been submitted.")); }
}
record InputData(List<Lane> lanes, List<Transporter> transporters) {}
