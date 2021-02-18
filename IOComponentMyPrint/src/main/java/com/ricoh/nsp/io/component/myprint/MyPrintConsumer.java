/*
 * Copyright(c) 2017 Ricoh Co., Ltd. All Rights Reserved.
 */

package com.ricoh.nsp.io.component.myprint;

import org.apache.camel.Endpoint;
import org.apache.camel.Processor;

import com.ricoh.nsp.io.component.framework.AbstractBaseConsumer;

/**
 * Consumer class.
 */
public class MyPrintConsumer extends AbstractBaseConsumer {
  /**
   * Initialize consumer.
   *
   * @param endpoint
   *        the endpoint
   * @param processor
   *        the processor
   */
  public MyPrintConsumer(final Endpoint endpoint, final Processor processor) {
    super(endpoint, processor);
  }

}
