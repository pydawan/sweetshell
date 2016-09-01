import org.oreto.sweetshell.Activator
import spock.lang.Shared

class ActivatorSpec extends BaseSpec {

    @Shared Activator activator
    @Shared String testApp = 'test-app'
    @Shared List testAppDir = ['tmp', testApp]

    def setupSpec() {
        activator = sdkman.getActivatorCli()
    }

    def "install and list templates"() {
        when:
        sdkman.install(Activator.exe).run()

        then:
        activator.listTemplates().size() > 50
    }

    def "new app"() {
        when:
        activator.cd('tmp')
        activator.newApp(testApp, 'play-scala').run()

        then:
        activator.testDirExists(testAppDir).ok()
    }

    def "test app"() {
        when:
        activator.appDirectory = testAppDir

        then:
        activator.test().testsPass()
    }
}
