package com.vvquan.query.jpa.builder.supplier;

import javax.persistence.Tuple;

@FunctionalInterface
public interface PersistableFunctionSupplier<T> {
  T getFromTuple(Tuple tuple);
}
