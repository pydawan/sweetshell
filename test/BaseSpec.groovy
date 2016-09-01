import com.aestasit.infrastructure.ssh.SshOptions
import org.oreto.sweetshell.Sdkman
import org.oreto.sweetshell.ShellFactory
import org.oreto.sweetshell.SubversionOptions
import org.yaml.snakeyaml.Yaml
import spock.lang.Shared
import spock.lang.Specification

class BaseSpec extends Specification {
    @Shared SshOptions sshOptions
    @Shared SubversionOptions svnOptions
    @Shared def vagrantConf
    @Shared def svnConf
    @Shared Sdkman sdkman

    def testDir = ['tmp', 'test']

    def setupSpec() {
        Yaml yaml = new Yaml()
        def file = new File('host.yaml')
        def load = yaml.load(file.newInputStream()) as Map
        vagrantConf = load.get('vagrant') as Map
        svnConf = load.get('svn') as Map
        sshOptions = new SshOptions(defaultHost: vagrantConf.get('host'),
                defaultUser: vagrantConf.get('user'),
                defaultKeyFile: new File(vagrantConf.get("keyfile") as String))
        svnOptions = new SubversionOptions(repoUrl: svnConf.get('repo'),
                username: svnConf.get('username'),
                password: svnConf.get('password'))
        ShellFactory.defaultSshOptions(sshOptions, [svnConf.get('password')])
        sdkman = new Sdkman(sshOptions)
    }
}
