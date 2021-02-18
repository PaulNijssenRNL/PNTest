/*
 * Copyright(c) 2019 Ricoh Co., Ltd. All Rights Reserved.
 */

package com.ricoh.nsp.io.component.myprint.operations

import org.junit.Rule
import com.ricoh.nsp.io.component.ComponentException
import com.ricoh.nsp.io.component.Job
import com.ricoh.nsp.io.component.RouteSpec
import com.ricoh.nsp.io.component.myprint.utils.Utils

import mockit.Mock
import mockit.MockUp
import software.betamax.Configuration
import software.betamax.TapeMode
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import spock.lang.Shared


class MyPrintReleaseProducerSpec extends RouteSpec {
  @Shared def configuration = Configuration.builder().sslEnabled(true).build()
  @Rule RecorderRule recorder = new RecorderRule(configuration)
  MockUp<Utils> mock
  
  //String tenantId = "3044191822"
  String tenantId = "ricoh-sdce"
  String tenantAccessKey = "kwNcsUUB9cOuCHhAMeXPeUSTecJ2dliUoZ6p3cJlQZBfuE4iqgxJT9Cndy6NtZL2"
  String clientId = "70UOB0380zFk2GRKBiqSDklkpIYrzLmT"
  String clientSecret = "71iWt8uI1zyiYWfunnLsYJ713QSdAHBcYHGk4duicvUG0feZl548ltfia6JRuNvv"

  def setup() {
    init("io-myprint", "MyPrint", "release")
    mock = createMock()
  }

  def cleanup() {
    mock?.tearDown()
  }

  @Betamax(mode = TapeMode.READ_ONLY)
  def "release succes"() {
    given:
    Job job = createJob()
    job.addParameter("tenantId", tenantId)
    job.addParameter("tenantAccessKey", tenantAccessKey)
    job.addParameter("clientId", clientId)
    job.addParameter("clientSecret", clientSecret)

    //job.addParameter("code", "49550667")	// RSI API
    job.addParameter("code", "12345678")  	// Betamax
    job.addParameter("fileId", "0");

    when:
    send(job)

    then:
	println "***** TEST COMPLETED"
	println "***** Files: " + job.getResultFileSize()
	println "***** First file: " + job.getFirstResultFile()
	job.getResultFile(0).name == "file1.pdf"
  }
  
  @Betamax(tape="release succes", mode = TapeMode.READ_ONLY)
  def "release with invalid code"() {
	given:
	Job job = createJob()
	job.addParameter("tenantId", tenantId)
	job.addParameter("tenantAccessKey", tenantAccessKey)
	job.addParameter("clientId", clientId)
	job.addParameter("clientSecret", clientSecret)

	job.addParameter("code", "99999999")
	job.addParameter("fileId", "999");

	when:
	send(job)

	then:
	ComponentException e = ComponentException.class.cast(job.getError())
	println "===> options: " + e.options
	e.getErrorCode() == "io-myprint.internal_error"
  }

  def createMock() {
    return new MockUp<Utils>() {
          @Mock
          String getHostName() {
            return "deve"
          }
        }
  }
}