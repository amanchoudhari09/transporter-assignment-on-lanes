package com.freightfox.transporter;

import java.util.List;

record Lane(long id, String origin, String destination) {}
record LaneQuote(long laneId, long quote) {}
record Transporter(long id, String name, List<LaneQuote> laneQuotes) { Transporter { laneQuotes = List.copyOf(laneQuotes); } }
record TransporterAssignment(long laneId, long transporterId) {}
record AssignmentResult(long totalCost, List<TransporterAssignment> assignments, List<Long> selectedTransporters) {
  AssignmentResult { assignments = List.copyOf(assignments); selectedTransporters = List.copyOf(selectedTransporters); }
}
