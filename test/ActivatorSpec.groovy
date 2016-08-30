import org.oreto.sweetshell.Activator
import spock.lang.Shared

class ActivatorSpec extends BaseSpec {

    @Shared Activator activator

    def setupSpec() {
        activator = sdkman.getActivatorCli()
    }

    def "install and list templates"() {
        when:
        sdkman.install(Activator.exe).run()

        then:
        activator.listTemplates().size() > 50
    }
}
