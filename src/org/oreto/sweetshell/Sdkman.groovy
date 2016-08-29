package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine


class Sdkman implements Shell {

    static String sdkmanName = 'sdk'

    Sdkman(SshOptions sshOptions) {
        this.engine = new SshDslEngine(sshOptions)
        homeDirName = fromHome('.sdkman')
    }

    protected sourceSdkman() {
        if(commands.isEmpty()) source(fromHome('bin', 'sdkman-init.sh'))
    }

    def Sdkman cli(cmd, Object... params) {
        sourceSdkman()
        prefixStack.push(sdkmanName)
        c(cmd, params)
        prefixStack.pop()
        this
    }

    def Sdkman selfInstall() {
        http("https://get.sdkman.io").pipe().c('bash')
        this
    }

    def Sdkman selfUpdate() {
        cli('selfupdate')
        this
    }

    def String version() {
        cli('version').text()
    }

    def Sdkman install(String candidate) {
        sourceSdkman()
        yes()
        cli('install', candidate)
    }

    def boolean upToDate(String candidate) {
        cli('outdated', candidate).text().contains('up-to-date')
    }

    def boolean outdated(String candidate) {
        !upToDate(candidate)
    }

    def String outdated() {
        cli('outdated').text()
    }

    def Sdkman use(String candidate) {
        sourceSdkman()
        yes()
        cli('use', candidate)
    }
}
