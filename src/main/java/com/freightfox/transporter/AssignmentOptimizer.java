package com.freightfox.transporter;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
class AssignmentOptimizer {
  AssignmentResult optimize(InputData input, int max) {
    List<Transporter> ts = input.transporters();
    if (max > ts.size()) throw new ApiException(400, "INVALID_MAX_TRANSPORTERS", "maxTransporters must not exceed the number of available transporters.");
    for (Lane lane : input.lanes()) if (ts.stream().noneMatch(t -> quote(t, lane.id()).isPresent())) throw new ApiException(422, "LANE_COVERAGE_NOT_POSSIBLE", "Unable to assign all lanes with the available transporter quotes.");
    Candidate best = null;
    for (int mask = 1; mask < (1 << ts.size()); mask++) {
      if (Integer.bitCount(mask) > max) continue;
      List<Long> ids = new ArrayList<>(); for (int i=0;i<ts.size();i++) if ((mask & (1<<i)) != 0) ids.add(ts.get(i).id());
      List<TransporterAssignment> assignments = new ArrayList<>(); long cost = 0; boolean feasible = true;
      for (Lane lane : input.lanes()) {
        Transporter chosen = null; long q = Long.MAX_VALUE;
        for (int i=0;i<ts.size();i++) if ((mask & (1<<i)) != 0) { Optional<Long> quote=quote(ts.get(i), lane.id()); if (quote.isPresent() && (quote.get()<q || (quote.get()==q && ts.get(i).id() < chosen.id()))) { chosen=ts.get(i); q=quote.get(); } }
        if (chosen==null) { feasible=false; break; } assignments.add(new TransporterAssignment(lane.id(), chosen.id())); cost=Math.addExact(cost,q);
      }
      if (feasible && (best==null || better(ids,cost,best.ids,best.cost))) best=new Candidate(ids,cost,assignments);
    }
    if (best==null) throw new ApiException(422, "LANE_COVERAGE_NOT_POSSIBLE", "Unable to cover every lane within maxTransporters.");
    return new AssignmentResult(best.cost,best.assignments,best.ids);
  }
  private Optional<Long> quote(Transporter t,long laneId){ return t.laneQuotes().stream().filter(q->q.laneId()==laneId).map(LaneQuote::quote).findFirst(); }
  private boolean better(List<Long> ids,long cost,List<Long> old,long oldCost){ int count=ids.size(); if(count!=old.size()) return count>old.size(); if(cost!=oldCost) return cost<oldCost; return lex(ids,old)<0; }
  private int lex(List<Long>a,List<Long>b){ for(int i=0;i<a.size();i++){int c=Long.compare(a.get(i),b.get(i));if(c!=0)return c;}return 0; }
  private record Candidate(List<Long> ids,long cost,List<TransporterAssignment> assignments) {}
}
