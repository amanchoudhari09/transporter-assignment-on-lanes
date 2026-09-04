package com.freightfox.transporter;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AssignmentOptimizerTest {
  private final AssignmentOptimizer optimizer = new AssignmentOptimizer();
  @Test void maximizesTransporterCountBeforeCost(){
    InputData data=new InputData(List.of(new Lane(1,"a","b"),new Lane(2,"b","c")),List.of(
      new Transporter(1,"one",List.of(new LaneQuote(1,1),new LaneQuote(2,1))),
      new Transporter(2,"two",List.of(new LaneQuote(1,2))),
      new Transporter(3,"three",List.of(new LaneQuote(2,2)))));
    AssignmentResult r=optimizer.optimize(data,2);
    assertEquals(List.of(1L,2L),r.selectedTransporters()); assertEquals(2,r.totalCost());
  }
  @Test void choosesCheapestQuoteWithinSelectedSubset(){
    InputData data=new InputData(List.of(new Lane(1,"a","b")),List.of(new Transporter(1,"one",List.of(new LaneQuote(1,10))),new Transporter(2,"two",List.of(new LaneQuote(1,5)))));
    assertEquals(5,optimizer.optimize(data,1).totalCost());
  }
  @Test void rejectsImpossibleCoverage(){
    InputData data=new InputData(List.of(new Lane(1,"a","b")),List.of(new Transporter(1,"one",List.of())));
    assertThrows(ApiException.class,()->optimizer.optimize(data,1));
  }
}
