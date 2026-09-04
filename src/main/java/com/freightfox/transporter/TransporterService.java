package com.freightfox.transporter;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
class TransporterService {
  private final InputRepository repository; private final AssignmentOptimizer optimizer;
  TransporterService(InputRepository repository, AssignmentOptimizer optimizer){this.repository=repository;this.optimizer=optimizer;}
  void save(InputRequest request){
    Set<Long> laneIds=new HashSet<>(); request.lanes().forEach(l->{if(!laneIds.add(l.id())) throw new ApiException(400,"DUPLICATE_LANE_ID","Duplicate lane id: "+l.id());});
    Set<Long> transporterIds=new HashSet<>(); List<Lane> lanes=request.lanes().stream().map(l->new Lane(l.id(),l.origin().trim(),l.destination().trim())).toList();
    List<Transporter> ts=new ArrayList<>(); for(TransporterRequest t:request.transporters()){if(!transporterIds.add(t.id())) throw new ApiException(400,"DUPLICATE_TRANSPORTER_ID","Duplicate transporter id: "+t.id()); Set<Long> qids=new HashSet<>(); List<LaneQuote> qs=t.laneQuotes().stream().map(q->{if(!qids.add(q.laneId())) throw new ApiException(400,"DUPLICATE_LANE_QUOTE","Duplicate lane quote for lane: "+q.laneId()); if(!laneIds.contains(q.laneId())) throw new ApiException(400,"UNKNOWN_LANE","Quote references unknown lane: "+q.laneId()); return new LaneQuote(q.laneId(),q.quote);}).toList(); ts.add(new Transporter(t.id(),t.name().trim(),qs));}
    repository.save(lanes,ts);
  }
  AssignmentResult assign(int max){return optimizer.optimize(repository.get(),max);}
}
