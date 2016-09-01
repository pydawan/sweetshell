package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions


class Activator implements Cli<Activator> {
    static String exe = 'activator'
    String executable = exe
    String javaHome
    def appDirectory
    List distDir = ['target', 'universal']

    Activator(SshOptions sshOptions, home, javaHome = '') {
        this.javaHome = javaHome ? "-java-home ${objToPath(javaHome)}" : javaHome
        initCli(sshOptions, objToPath(home))
    }

    String getAppDirectory() {
        objToPath(appDirectory)
    }

    @Override
    Activator cmd(cmd, Object[] params) {
        prefixStack.push(executable)
        if(javaHome) prefixStack.push(javaHome)
        if(appDirectory) prefixStack.push("$cdCmd ${getAppDirectory()} $andOp")
        c(cmd, params)
        prefixStack.pop()
        if(javaHome) prefixStack.pop()
        if(appDirectory) prefixStack.pop()
        this
    }

    String listTemplates() {
        cmd('list-templates').text()
    }

    Activator newApp(app, String template) {
        if(template) cmd('new', objToPath(app), template)
        else cmd('new', objToPath(app))
    }

    Activator test() {
        cmd('test')
    }

    Activator testOnly(String spec) {
        cmd('test-only', spec)
    }

    Activator dist() {
        cmd('universal:packageZipTarball')
    }

    boolean testsPass() {
        test().text().contains('All tests passed.')
    }

    boolean testsFail() {
        !testsPass()
    }
}
