package com.vvq.query.jpa.builder;

public interface BaseQueryConst {
  enum Junction {
    And,
    Or
  }

  enum Operator {
    Equal,
    Greater,
    GreaterAndEqual,
    Less,
    LessAndEqual,
    Like,
    StartLike,
    EndLike,
    In
  }
}
