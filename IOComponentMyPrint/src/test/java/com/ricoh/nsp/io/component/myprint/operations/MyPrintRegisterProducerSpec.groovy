package com.ricoh.nsp.io.component.myprint.operations

import org.junit.Rule
import com.ricoh.nsp.io.component.ComponentException
import com.ricoh.nsp.io.component.Job
import com.ricoh.nsp.io.component.RouteSpec
import com.ricoh.nsp.io.component.myprint.api.services.aut.Authentication
import com.ricoh.nsp.io.component.myprint.api.services.aut.Authentication.FetchTemporaryCodeSpace
import com.ricoh.nsp.io.component.myprint.api.services.aut.model.TemporaryCodeSpace
import com.ricoh.nsp.io.component.myprint.utils.Utils
import com.ricoh.nsp.io.util.exception.HttpExecFailureException

import mockit.Mock
import mockit.MockUp
import software.betamax.Configuration
import software.betamax.TapeMode
import software.betamax.junit.Betamax
import software.betamax.junit.RecorderRule
import spock.lang.IgnoreRest
import spock.lang.Shared

class MyPrintRegisterProducerSpec extends RouteSpec {
  @Shared def configuration = Configuration.builder().sslEnabled(true).build()
  @Rule RecorderRule recorder = new RecorderRule(configuration)
  MockUp<Utils> utilsMock
  
  String clientId = "70UOB0380zFk2GRKBiqSDklkpIYrzLmT"
  String clientSecret = "71iWt8uI1zyiYWfunnLsYJ713QSdAHBcYHGk4duicvUG0feZl548ltfia6JRuNvv"
  String accessToken = "82vu8w1FRixiQvePKeSvBMcqxcGeRoWl55nRtSFOpol1AsYFFD10BCueq604xaFb"
  String tenantId = "3044191822"
  //String tenantId = "ricoh-sdce"

  def setup() {
    init("io-myprint", "MyPrint", "register")
    utilsMock = createUtilsMock()
  }

  def cleanup() {
    utilsMock?.tearDown()
  }

  @Betamax(mode = TapeMode.READ_ONLY) // READ_ONLY -> READ_WRITE // See https://github.com/betamaxteam/betamax
  def "register succes non existing temporary code space"() {
    given:
    Job job = createJob()
    job.addParameter("clientId", clientId)
    job.addParameter("clientSecret", clientSecret)
    job.addParameter("accessToken", accessToken);
	job.addParameter("codeSpaceLength", 10);
    job.addFile("file1.pdf", new File("./src/test/resources/sample.pdf"))
    job.addFile("file2.pdf", new File("./src/test/resources/sample.pdf"))

    when:
    send(job)

    then:
	String code = job?.resultData?.data?.code
	println ("***** CODE = " + code + " *****")
    code?.length() == 10 || code?.length() == 8
    job?.resultData?.data?.files[0].fileId != null
    job?.resultData?.data?.files[0]?.name == "file1.pdf"
    job?.resultData?.data?.files[1].fileId != null
    job?.resultData?.data?.files[1]?.name == "file2.pdf"
  }
  
  @Betamax(mode = TapeMode.READ_ONLY) 
  def "register succes existing temporary code space"() {
	// This test is the same as the previous one, only another yaml file is used.
    given:
    Job job = createJob()
    job.addParameter("clientId", clientId)
    job.addParameter("clientSecret", clientSecret)
    job.addParameter("accessToken", accessToken);
	job.addParameter("tenantId", tenantId);
	job.addParameter("codeSpaceLength", 10);
    job.addFile("file1.pdf", new File("./src/test/resources/sample.pdf"))
    job.addFile("file2.pdf", new File("./src/test/resources/sample.pdf"))

    when:
    send(job)

    then:
	String code = job?.resultData?.data?.code  
	println ("***** CODE = " + code + " *****")
    code.length() == 10 || code.length() == 8 
    job?.resultData?.data?.files[0].fileId != null
    job?.resultData?.data?.files[0]?.name == "file1.pdf"
    job?.resultData?.data?.files[1].fileId != null
    job?.resultData?.data?.files[1]?.name == "file2.pdf"
  }
  
  def "register fail with no input files"() {
    given:
    Job job = createJob()
	job.addParameter("clientId", clientId)
	job.addParameter("clientSecret", clientSecret)
	job.addParameter("accessToken", accessToken);
	job.addParameter("tenantId", tenantId);

    when:
    send(job)

    then:
    ComponentException e = ComponentException.class.cast(job.getError())
    e.getErrorCode() == "io-myprint.no_input_files"
  }

  // This one is important when using the API; it mocks the right hostname. 
  def createUtilsMock() {
    return new MockUp<Utils>() {
          @Mock
          String getHostName() {
            return "deve"
          }
        }
  }
}