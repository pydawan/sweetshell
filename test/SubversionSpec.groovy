import org.oreto.sweetshell.Subversion
import spock.lang.Shared

class SubversionSpec extends BaseSpec {

    @Shared Subversion svn
    @Shared svnProjectName

    def setupSpec() {
        svnProjectName = svnConf.get('name')
        svn = new Subversion(sshOptions, svnOptions)
    }

    def "checkout repo"() {
        when:
        def checkoutDir = ['tmp', svnProjectName, 'trunk']
        svn.checkout(checkoutDir).run()

        then:
        svn.testDirExists(checkoutDir).ok()
    }
}
