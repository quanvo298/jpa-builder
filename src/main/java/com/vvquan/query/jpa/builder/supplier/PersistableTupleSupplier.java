package com.vvquan.query.jpa.builder.supplier;

import javax.persistence.Tuple;

@FunctionalInterface
public interface PersistableTupleSupplier<T> {
  T getFromTuple(Tuple tuple);
}
