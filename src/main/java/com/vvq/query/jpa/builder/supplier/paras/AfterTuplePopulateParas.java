package com.vvq.query.jpa.builder.supplier.paras;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AfterTuplePopulateParas extends WrapperTuple {
  private int rowIndex;

  private Object result;

  public <X> X getResult(Class<X> type) {
    if (result != null && !type.isInstance(result)) {
      throw new IllegalArgumentException(
          String.format(
              "Requested tuple value [value=%s] cannot be assigned to requested type [%s]",
              result, type.getName()));
    } else {
      return (X) result;
    }
  }
}
