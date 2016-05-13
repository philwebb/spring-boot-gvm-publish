package io.spring.gvmpublish

@Grab("spring-web")

import org.springframework.boot.*
import org.springframework.util.*
import org.springframework.web.client.*
import org.springframework.http.*

@Component
class GvmPublish implements ApplicationRunner {

	@Value('${consumer-key}')
	String consumerKey

	@Value('${consumer-token}') 
	String consumerToken

	void run(ApplicationArguments args) {
		Assert.notNull(consumerKey, 'No consumer key set (have you added application-secrets.properties')
		Assert.notNull(consumerKey, 'No consumer token set (have you added application-secrets.properties')
		Assert.isTrue(args.nonOptionArgs.size() == 1, 'No version provided, use spring run gvmpublish.groovy <version released>')
		def version = args.nonOptionArgs.get(0)
		println "About to call GVM API for release ${version}. Press enter to continue"
		System.in.newReader().readLine() 

		def url = 'https://vendors.sdkman.io'
		def repo = (version.endsWith('RELEASE') ? 'libs-release-local' : 'libs-milestone-local')
		def downloadUrl = "http://repo.spring.io/simple/${repo}/org/springframework/boot/spring-boot-cli/" +
			"${version}/spring-boot-cli-${version}-bin.zip"

		def rest = new RestTemplate();

		def releaseRequest = RequestEntity.post(new URI(url+'/release'))
			.header('consumer_key', consumerKey)
			.header('consumer_token', consumerToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.body('{"candidate": "springboot", "version": "' + version + '", "url": "' + downloadUrl + '"}')
		rest.exchange(releaseRequest, String.class)

		if (version.endsWith("RELEASE")) {
			def makeDefaultRequest = RequestEntity.put(new URI(url+'/default'))
				.header('consumer_key', consumerKey)
				.header('consumer_token', consumerToken)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.body('{"candidate": "springboot", "default": "' + version + '"}')
			rest.exchange(makeDefaultRequest, String.class)
		}

		def broadcastRequest = RequestEntity.post(new URI(url+'/announce/struct'))
			.header('consumer_key', consumerKey)
			.header('consumer_token', consumerToken)
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
				.body('{"candidate": "springboot", "version": "'+version+'", "hashtag": "springboot"}')
		rest.exchange(broadcastRequest, String.class)
	}

}
