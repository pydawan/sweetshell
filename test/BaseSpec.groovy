import com.aestasit.infrastructure.ssh.SshOptions
import org.oreto.sweetshell.Sdkman
import org.oreto.sweetshell.ShellFactory
import org.yaml.snakeyaml.Yaml
import spock.lang.Shared
import spock.lang.Specification

class BaseSpec extends Specification {
    @Shared SshOptions sshOptions
    @Shared def conf
    @Shared Sdkman sdkman

    def testDir = ['tmp', 'test']
    def vertx = ['home', 'vagrant', '.sdkman', 'candidates', 'vertx', 'current', 'bin']

    def setupSpec() {
        Yaml yaml = new Yaml()
        def file = new File('host.yaml')
        conf = (yaml.load(file.newInputStream()) as Map).get('vagrant') as Map
        sshOptions = new SshOptions(defaultHost: conf.get('host'),
                defaultUser: conf.get('user'),
                defaultKeyFile: new File(conf.get("keyfile") as String))
        ShellFactory.defaultSshOptions(sshOptions)
        sdkman = new Sdkman(sshOptions)
    }
}
