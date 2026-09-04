package com.freightfox.transporter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
class TransporterService {
    private final InputRepository repository;
    private final AssignmentOptimizer optimizer;

    TransporterService(InputRepository repository, AssignmentOptimizer optimizer) {
        this.repository = repository;
        this.optimizer = optimizer;
    }

    void save(InputRequest request) {
        Set<Long> laneIds = new HashSet<>();
        List<Lane> lanes = request.lanes().stream().map(lane -> {
            if (!laneIds.add(lane.id())) {
                throw new ApiException(400, "DUPLICATE_LANE_ID", "Duplicate lane id: " + lane.id());
            }
            return new Lane(lane.id(), lane.origin().trim(), lane.destination().trim());
        }).toList();

        Set<Long> transporterIds = new HashSet<>();
        List<Transporter> transporters = new ArrayList<>();
        for (TransporterRequest requestTransporter : request.transporters()) {
            if (!transporterIds.add(requestTransporter.id())) {
                throw new ApiException(400, "DUPLICATE_TRANSPORTER_ID", "Duplicate transporter id: " + requestTransporter.id());
            }
            Set<Long> quoteLaneIds = new HashSet<>();
            List<LaneQuote> quotes = requestTransporter.laneQuotes().stream().map(quote -> {
                if (!quoteLaneIds.add(quote.laneId())) {
                    throw new ApiException(400, "DUPLICATE_LANE_QUOTE", "Duplicate lane quote for lane: " + quote.laneId());
                }
                if (!laneIds.contains(quote.laneId())) {
                    throw new ApiException(400, "UNKNOWN_LANE", "Quote references unknown lane: " + quote.laneId());
                }
                return new LaneQuote(quote.laneId(), quote.quote());
            }).toList();
            transporters.add(new Transporter(requestTransporter.id(), requestTransporter.name().trim(), quotes));
        }
        repository.save(lanes, transporters);
    }

    AssignmentResult assign(int maxTransporters) {
        return optimizer.optimize(repository.get(), maxTransporters);
    }
}
