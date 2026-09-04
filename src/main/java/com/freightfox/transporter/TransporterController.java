package com.freightfox.transporter;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/transporters")
class TransporterController {
  private final TransporterService service;
  TransporterController(TransporterService service){this.service=service;}
  @PostMapping("/input") ResponseEntity<InputResponse> input(@Valid @RequestBody InputRequest request){service.save(request);return ResponseEntity.ok(new InputResponse("success","Input data saved successfully."));}
  @PostMapping("/assignment") ResponseEntity<AssignmentResponse> assignment(@Valid @RequestBody AssignmentRequest request){AssignmentResult r=service.assign(request.maxTransporters());return ResponseEntity.ok(new AssignmentResponse("success",r.totalCost(),r.assignments().stream().map(a->new AssignmentDto(a.laneId(),a.transporterId())).toList(),r.selectedTransporters()));}
}
