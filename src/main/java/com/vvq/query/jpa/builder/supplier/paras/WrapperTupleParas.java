package com.vvq.query.jpa.builder.supplier.paras;

import java.util.Collections;
import java.util.List;
import javax.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class WrapperTupleParas extends WrapperTuple {
  private List<Tuple> tuples = Collections.emptyList();

  public WrapperTupleParas(Tuple currentTuple, List<Tuple> tuples) {
    super(currentTuple);
    this.tuples = tuples;
  }

  public int getRowIndex() {
    return tuples.indexOf(getCurrentTuple());
  }
}
