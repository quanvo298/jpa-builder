package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.supplier.paras.WrapperTupleParas;

@FunctionalInterface
public interface PersistableTupleSupplier<T> {
  T getFromTuple(WrapperTupleParas paras);
}
