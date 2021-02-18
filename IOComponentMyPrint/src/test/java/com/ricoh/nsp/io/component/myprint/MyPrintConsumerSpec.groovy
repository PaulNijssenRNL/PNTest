package com.ricoh.nsp.io.component.myprint

import org.apache.camel.Endpoint
import org.apache.camel.Processor

import spock.lang.Specification

class MyPrintConsumerSpec extends Specification {
  def "Create consumer"() {
    given:
    final Endpoint endpoint = Mock(Endpoint)
    final Processor processor = Mock(Processor)

    expect: new MyPrintConsumer(endpoint, processor) != null
  }
}