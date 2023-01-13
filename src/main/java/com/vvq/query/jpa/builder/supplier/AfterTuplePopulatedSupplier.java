package com.vvq.query.jpa.builder.supplier;

import com.vvq.query.jpa.builder.supplier.paras.AfterTuplePopulateParas;

@FunctionalInterface
public interface AfterTuplePopulatedSupplier<T> {
  void afterGetFromTuple(AfterTuplePopulateParas paras);
}
