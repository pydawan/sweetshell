import org.oreto.sweetshell.Shell
import org.oreto.sweetshell.ShellFactory

class ShellSpec extends BaseSpec {

    Shell shell = ShellFactory.create(sshOptions)

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

    def "redirect"() {
        expect:
        shell.ls().redirect('test.out').commandString() == 'ls > test.out'
        shell.ls().redirect('test.out', true).commandString() == 'ls > test.out && ls >> test.out'
    }
}
