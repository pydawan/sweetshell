
class SdkmanSpec extends BaseSpec {

    String groovyCandidate = 'groovy'

    def setupSpec() {}

    def "install sdkman"() {
        when:
        sdkman.selfInstall().run()

        then:
        sdkman.version().contains('SDKMAN')
    }

    def "install groovy"() {
        when:
        sdkman.install(groovyCandidate).run()

        then:
        !sdkman.outdated(groovyCandidate)
        sdkman.use(groovyCandidate).text().contains("Using $groovyCandidate")
    }

    def "update sdkman"() {
        when:
        sdkman.selfUpdate().run()

        then:
        sdkman.selfUpdate().text() == 'No update available at this time.'
    }
}
