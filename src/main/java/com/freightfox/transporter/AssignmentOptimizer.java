package com.freightfox.transporter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

@Component
class AssignmentOptimizer {
    AssignmentResult optimize(InputData input, int maxTransporters) {
        if (maxTransporters <= 0) {
            throw new ApiException(400, "INVALID_MAX_TRANSPORTERS", "maxTransporters must be positive.");
        }
        if (maxTransporters > input.transporters().size()) {
            throw new ApiException(400, "INVALID_MAX_TRANSPORTERS", "maxTransporters must not exceed the number of available transporters.");
        }
        for (Lane lane : input.lanes()) {
            if (input.transporters().stream().noneMatch(t -> quoteFor(t, lane.id()).isPresent())) {
                throw new ApiException(422, "LANE_COVERAGE_NOT_POSSIBLE", "Unable to cover every lane within maxTransporters.");
            }
        }

        Candidate best = null;
        List<Transporter> transporters = input.transporters();
        for (int mask = 1; mask < (1 << transporters.size()); mask++) {
            if (Integer.bitCount(mask) > maxTransporters) {
                continue;
            }
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < transporters.size(); i++) {
                if ((mask & (1 << i)) != 0) ids.add(transporters.get(i).id());
            }
            ids.sort(Long::compareTo);

            List<TransporterAssignment> assignments = new ArrayList<>();
            long totalCost = 0;
            boolean feasible = true;
            for (Lane lane : input.lanes().stream().sorted(Comparator.comparingLong(Lane::id)).toList()) {
                Transporter selected = null;
                long selectedQuote = Long.MAX_VALUE;
                for (int i = 0; i < transporters.size(); i++) {
                    if ((mask & (1 << i)) == 0) continue;
                    OptionalLong quote = quoteFor(transporters.get(i), lane.id());
                    if (quote.isPresent() && (quote.getAsLong() < selectedQuote
                            || (quote.getAsLong() == selectedQuote && (selected == null || transporters.get(i).id() < selected.id())))) {
                        selected = transporters.get(i);
                        selectedQuote = quote.getAsLong();
                    }
                }
                if (selected == null) {
                    feasible = false;
                    break;
                }
                assignments.add(new TransporterAssignment(lane.id(), selected.id()));
                try {
                    totalCost = Math.addExact(totalCost, selectedQuote);
                } catch (ArithmeticException exception) {
                    throw new ApiException(422, "TOTAL_COST_OVERFLOW", "The assignment total cost exceeds the supported range.");
                }
            }
            if (feasible && (best == null || isBetter(ids, totalCost, best))) {
                best = new Candidate(ids, totalCost, assignments);
            }
        }
        if (best == null) throw new ApiException(422, "LANE_COVERAGE_NOT_POSSIBLE", "Unable to cover every lane within maxTransporters.");
        return new AssignmentResult(best.cost(), best.assignments(), best.ids());
    }

    private OptionalLong quoteFor(Transporter transporter, long laneId) {
        return transporter.laneQuotes().stream().filter(q -> q.laneId() == laneId).mapToLong(LaneQuote::quote).findFirst();
    }

    private boolean isBetter(List<Long> ids, long cost, Candidate best) {
        if (ids.size() != best.ids().size()) return ids.size() > best.ids().size();
        if (cost != best.cost()) return cost < best.cost();
        for (int i = 0; i < ids.size(); i++) {
            int comparison = Long.compare(ids.get(i), best.ids().get(i));
            if (comparison != 0) return comparison < 0;
        }
        return false;
    }

    private record Candidate(List<Long> ids, long cost, List<TransporterAssignment> assignments) { }
}
