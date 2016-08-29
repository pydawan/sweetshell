import org.oreto.sweetshell.Shell
import org.oreto.sweetshell.ShellFactory

class ShellSpec extends BaseSpec {

    Shell shell = ShellFactory.create(sshOptions)

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

    def "redirect"() {
        expect:
        shell.ls().redirect('test.out').commandString() == 'ls > test.out'
        shell.ls().redirect('test.out', true).commandString() == 'ls > test.out && ls >> test.out'
    }

//    def "upload file"() {
//        when:
//        shell.upload(['c', 'windows', 'system.ini'], ['tmp'])
//        then:
//        true
//    }
}
