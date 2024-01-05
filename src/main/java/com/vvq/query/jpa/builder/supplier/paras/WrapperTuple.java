package com.vvq.query.jpa.builder.supplier.paras;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class WrapperTuple implements Tuple {
  private Tuple currentTuple;

  @Override
  public <X> X get(TupleElement<X> tupleElement) {
    return this.currentTuple.get(tupleElement);
  }

  @Override
  public <X> X get(String s, Class<X> aClass) {
    return this.currentTuple.get(s, aClass);
  }

  @Override
  public Object get(String s) {
    return this.currentTuple.get(s);
  }

  @Override
  public <X> X get(int i, Class<X> aClass) {
    return this.currentTuple.get(i, aClass);
  }

  @Override
  public Object get(int i) {
    return this.currentTuple.get(i);
  }

  @Override
  public Object[] toArray() {
    return this.currentTuple.toArray();
  }

  @Override
  public List<TupleElement<?>> getElements() {
    return this.currentTuple.getElements();
  }
}
