package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions

class ShellFactory {
    static defaultSshOptions(SshOptions sshOptions) {
        sshOptions.trustUnknownHosts = true
        sshOptions.reuseConnection = true
        sshOptions.defaultPort = 2222
        sshOptions.execOptions.failOnError = false
    }

    static Shell create(SshOptions sshOptions) {
        switch (CommandScript.detectShell(sshOptions)) {
            case Shells.BASH: new Bash(sshOptions); break;
            default: new Bash(sshOptions)
        }
    }
}
