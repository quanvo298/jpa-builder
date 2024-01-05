package com.vvq.query.jpa.builder;

public interface JpaQueryConstant {
  static Junction getStaticGlobalJunction() {
    return Junction.And;
  }

  enum Junction {
    And,
    Or
  }
}
