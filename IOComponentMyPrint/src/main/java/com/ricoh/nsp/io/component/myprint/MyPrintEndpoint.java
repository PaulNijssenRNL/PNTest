/*
 * Copyright(c) 2017 Ricoh Co., Ltd. All Rights Reserved.
 */

package com.ricoh.nsp.io.component.myprint;

import java.util.Map;

import org.apache.camel.Component;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;

import com.ricoh.nsp.io.component.framework.AbstractBaseEndpoint;

/**
 * Endpoint Class
 */
public final class MyPrintEndpoint extends AbstractBaseEndpoint {
  /**
   * Initialize endpoint
   *
   * @param uri
   *        Endpoint URI
   * @param component
   *        {@link Component}
   * @param operation
   *        Operation
   * @param params
   *        Parameters
   */
  public MyPrintEndpoint(final String uri, final Component component, final String operation, final Map<String, Object> params) {
    super(uri, component, operation, params);
  }

  @Override
  public Consumer createConsumer(final Processor processor) throws Exception {
    return new MyPrintConsumer(this, processor);
  }

  @Override
  public String getComponentName() {
    return "MyPrint";
  }
}
