/*
 * Copyright(c) 2017 Ricoh Co., Ltd. All Rights Reserved.
 */

package com.ricoh.nsp.io.component.myprint;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;

import com.ricoh.nsp.io.component.framework.AbstractBaseComponent;

/**
 * Component class.
 */
public final class MyPrintComponent extends AbstractBaseComponent {
  /**
   * Initialize component.
   */
  public MyPrintComponent() {
    super();
  }

  /**
   * Initialize component.
   *
   * @param context
   *        {@link CamelContext}
   */
  public MyPrintComponent(final CamelContext context) {
    super(context);
  }

  @Override
  protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters) throws Exception {
    return new MyPrintEndpoint(uri, this, remaining, parameters);
  }
}
