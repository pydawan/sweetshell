package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine


class Sdkman implements Cli<Sdkman> {

    String executable = 'sdk'
    SshOptions sshOptions

    Sdkman(SshOptions sshOptions, home = '') {
        this.sshOptions = sshOptions
        this.engine = new SshDslEngine(sshOptions)
        homeDirName = home ? objToPath(home) : fromHome('.sdkman')
    }

    protected sourceSdkman() {
        if(this.commandScripts.isEmpty()) source(fromHome('bin', 'sdkman-init.sh'))
    }

    def Sdkman cmd(cmd, Object... params) {
        sourceSdkman()
        Cli.super.cmd(cmd, params)
    }

    def Sdkman selfInstall() {
        http("https://get.sdkman.io").pipe().c('bash')
        this
    }

    def Sdkman selfUpdate() {
        cmd('selfupdate')
        this
    }

    def String version() {
        cmd('version').text()
    }

    def Sdkman install(String candidate) {
        sourceSdkman()
        yes()
        cmd('install', candidate)
    }

    def boolean isInstalled(String candidate) {
        cmd('default', candidate).text().contains('is not installed')
    }

    def Sdkman use(String candidate) {
        sourceSdkman()
        yes()
        cmd('use', candidate)
    }

    def boolean upToDate(String candidate) {
        cmd('outdated', candidate).text().contains('up-to-date')
    }

    def boolean outdated(String candidate) {
        !upToDate(candidate)
    }

    def String outdated() {
        cmd('outdated').text()
    }

    def Cli getCliFor(String candidate, String version = 'current') {
        switch (candidate) {
            case Activator.exe: getActivatorCli(version); break;
            case Vertx.exe: getVertxCli(version); break;
            default: this; break;
        }
    }

    def Activator getActivatorCli(String version = 'current') {
        new Activator(sshOptions,  candidateHome(Activator.exe, version))
    }

    def Vertx getVertxCli(String version = 'current') {
        new Vertx(sshOptions,  candidateHome(Vertx.exe, version))
    }

    def String candidateHome(String candidate, String version = 'current') {
        fromHome('candidates', candidate, version)
    }
}
