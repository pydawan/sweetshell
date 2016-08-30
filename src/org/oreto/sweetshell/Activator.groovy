package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions


class Activator implements Cli<Activator> {
    static String exe = 'activator'
    String executable = exe

    Activator(SshOptions sshOptions, String home) {
        initCli(sshOptions, home)
    }

    String listTemplates() {
        cmd('list-templates').text()
    }
}
