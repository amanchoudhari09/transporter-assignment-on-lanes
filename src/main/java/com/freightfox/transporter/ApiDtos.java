package com.freightfox.transporter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

record InputRequest(@NotEmpty List<@Valid LaneRequest> lanes, @NotEmpty List<@Valid TransporterRequest> transporters) {}
record LaneRequest(@Positive long id, @NotBlank String origin, @NotBlank String destination) {}
record TransporterRequest(@Positive long id, @NotBlank String name, @NotEmpty List<@Valid LaneQuoteRequest> laneQuotes) {}
record LaneQuoteRequest(@Positive long laneId, @Positive long quote) {}
record AssignmentRequest(@Positive int maxTransporters) {}
record InputResponse(String status, String message) {}
record AssignmentDto(long laneId, long transporterId) {}
record AssignmentResponse(String status, long totalCost, List<AssignmentDto> assignments, List<Long> selectedTransporters) {}
record ErrorResponse(String status, String code, String message) {}
