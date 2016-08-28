package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions

class ShellFactory {
    static defaultSshOptions(SshOptions sshOptions) {
        sshOptions.trustUnknownHosts = true
        sshOptions.reuseConnection = true
        sshOptions.defaultPort = 2222
    }

    static Shell create(SshOptions sshOptions) {
        defaultSshOptions(sshOptions)
        switch (Bash.detectShell(sshOptions)) {
            case Shells.BASH: new Bash(sshOptions); break;
            default: new Bash(sshOptions)
        }
    }
}
