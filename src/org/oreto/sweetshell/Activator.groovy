package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions


class Activator implements Cli<Activator> {
    static String exe = 'activator'
    String executable = exe

    Activator(SshOptions sshOptions, home) {
        initCli(sshOptions, objToPath(home))
    }

    String listTemplates() {
        cmd('list-templates').text()
    }
}
