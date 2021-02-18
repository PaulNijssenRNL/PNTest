package com.ricoh.nsp.io.component.myprint

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint
import org.apache.camel.Processor

import spock.lang.Specification

class MyPrintComponentSpec extends Specification {
  def "Create component"() {
    given: CamelContext context = Mock(CamelContext)
    expect:
    new MyPrintComponent() != null
    new MyPrintComponent(context) != null
  }
}