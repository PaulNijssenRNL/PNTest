package com.ricoh.nsp.io.component.myprint

import org.apache.camel.Component;
import org.apache.camel.Endpoint
import org.apache.camel.Processor

import spock.lang.Specification

class MyPrintEndpointSpec extends Specification {
  def "Create Endpoint"() {
    given:
    MyPrintComponent component = new MyPrintComponent()

    expect: new MyPrintEndpoint("uri", component, "MyPrintOperation", [:]) != null
  }
}
