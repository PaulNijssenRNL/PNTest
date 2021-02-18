package com.ricoh.nsp.io.component.myprint.operations

import org.junit.Rule

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

class MyPrintGetEntryListProducerSpec extends RouteSpec {
	@Shared def configuration = Configuration.builder().sslEnabled(true).build()
	@Rule RecorderRule recorder = new RecorderRule(configuration)
	MockUp<Utils> mock
	
	String tenantId = "ricoh-sdce"
	String clientId = "70UOB0380zFk2GRKBiqSDklkpIYrzLmT"
	String clientSecret = "71iWt8uI1zyiYWfunnLsYJ713QSdAHBcYHGk4duicvUG0feZl548ltfia6JRuNvv"

	def setup() {
		init("io-myprint", "MyPrint", "_getEntryList")
		mock = createMock()
	}

	def cleanup() {
		mock?.tearDown()
	}

	@Betamax(mode = TapeMode.READ_ONLY)
	def "get entrylist succes"() {
		given:
		
		//String code = "6936374013" 	// RSI API
		String code = "12345678" 	// Betamax; deze code zit ook in de yaml file.  
		
		Job job = createJob()
		job.addParameter("folderId", "TEST")
		job.addParameter("credential", "NONE")
		job.addParameter("option", [
			"code": code,	// code == PIN 
			"tenantId": tenantId,
			"clientId": clientId,
			"clientSecret": clientSecret])

		when:
		send(job)

		then:
		println("***** Folder ID: " + job?.resultData?.data?.folderName)
		job?.resultData?.data?.entries.each { 
			println("***** " + it)
		}
		
		job?.resultData?.data?.entries[0].entryId == "0"
		job?.resultData?.data?.entries[0].entryName == "file1.pdf"
		job?.resultData?.data?.entries[1].entryId == "1"
		job?.resultData?.data?.entries[1].entryName == "file2.pdf"
	}

	@Betamax(mode = TapeMode.READ_ONLY)
	def "get entrylist invalid code"() {
		given:
		String invalidCodeMessage = "Invalid PIN"
		Job job = createJob()
		job.addParameter("folderId", "TEST")
		job.addParameter("credential", "NONE")
		job.addParameter("option", [
			"code": "99999999",	// code == PIN
			"invalidCodeMessage": invalidCodeMessage,
			"tenantId": tenantId,
			"clientId": clientId,
			"clientSecret": clientSecret])

		when:
		send(job)

		then:
		println ("***** Folder name: " + job?.resultData?.data.folderName)
		
		job?.resultData?.data.folderName == invalidCodeMessage
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