
import org.oreto.sweetshell.Vertx
import spock.lang.Shared

class VertxSpec extends BaseSpec {
    @Shared Vertx vertx
    @Shared String appName = 'hello'
    @Shared String appFile = "${appName}.groovy"
    @Shared def appsDir = ['opt', 'apps', 'vertx-apps']

    def setupSpec() {
        vertx = sdkman.getVertxCli()
        def localFile = vertx.relativePath('test', appFile)
        new File(localFile) << """
vertx.createHttpServer().requestHandler({ req ->
  req.response()
    .putHeader("content-type", "text/plain")
    .end("Hello from Vert.x!")
}).listen(8080)
"""
        vertx.upload(localFile, ['tmp'])
    }

    def "make apps dir"() {
        when:
        vertx.shell.sudo {
            it.mkdirIf(appsDir.take(2)).mkdirIf(appsDir)
        }.run()

        then:
        vertx.testDirExists(appsDir)
    }

    def "copy source to apps"() {
        when:
        vertx.shell.sudo {
            it.cpIf(['tmp', appFile], appsDir)
        }.run()

        then:
        vertx.testFileExists(appsDir + appFile)
    }

    def "install and list apps"() {
        when:
        sdkman.install(Vertx.exe).run()

        then:
        vertx.list().contains("Listing vert.x applications")
    }

    def "run app in background"() {
        when:
        def output = vertx.start(appsDir + appFile, appName).run()
        Thread.sleep(3000)
        def response = vertx.http('localhost:8080').text()

        then:
        output.exitStatus == 0
        output.output.contains('Starting vert.x application...')
        response == 'Hello from Vert.x!'
    }

    def "stop app"() {
        when:
        def output = vertx.stop(appName).text()

        then:
        output.contains('terminated with status 0')
    }
}
