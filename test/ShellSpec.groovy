
import com.aestasit.infrastructure.ssh.SshOptions
import org.oreto.sweetshell.ShellFactory
import org.oreto.sweetshell.Shell
import org.yaml.snakeyaml.Yaml
import spock.lang.*
class ShellSpec extends Specification {
    SshOptions sshOptions
    Shell shell
    def testDir = ['tmp', 'test']
    def vertx = ['home', 'vagrant', '.sdkman', 'candidates', 'vertx', 'current', 'bin']
    def conf

    def setup() {
        Yaml yaml = new Yaml()
        def file = new File('host.yaml')
        conf = (yaml.load(file.newInputStream()) as Map).get('vagrant') as Map
        sshOptions = new SshOptions(defaultHost: conf.get('host'),
                defaultUser: conf.get('user'),
                defaultKeyFile: new File(conf.get("keyfile") as String))
        shell = ShellFactory.create(sshOptions)
    }

    def "run app in background"() {
        when:
        def output = shell.cd(vertx)
                .background {
            it.sudo {
                it.cexe('vertx', 'start', ['opt', 'apps', 'vertx-apps', 'hello.groovy'], '-id', 'hello')
            }

        }.run()
        Thread.sleep(2000)
        def response = shell.c('curl', '127.0.0.1:8080').text()

        then:
        output.exitStatus == 0
        output.output.contains('Starting vert.x application...')
        response == 'Hello from Vert.x!'
    }

    def "stop app"() {
        when:
        def output = shell.cd(vertx).sudo {
            it.cexe('vertx', 'stop', 'hello')
        }.text()

        then:
        output.contains('terminated with status 0')
    }

    def "make dir and files"() {
        expect:
        shell.mkdir(testDir).ok()
        shell.mk(testDir + 'test.txt').ok()
        shell.ls(testDir).ok()
    }

    def "remove files and dir"() {
        expect:
        shell.rm(testDir + 'test.txt').ok()
        shell.rm(testDir).ok()
    }

//    def "upload file"() {
//        when:
//        shell.upload(['c', 'windows', 'system.ini'], ['tmp'])
//        then:
//        true
//    }
}
