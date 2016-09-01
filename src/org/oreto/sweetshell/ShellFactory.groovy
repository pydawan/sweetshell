package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions

class ShellFactory {
    static defaultSshOptions(SshOptions sshOptions, List secrets = []) {
        sshOptions.trustUnknownHosts = true
        sshOptions.reuseConnection = true
        sshOptions.defaultPort = 2222
        sshOptions.execOptions.failOnError = false
        sshOptions.execOptions.secrets = secrets
    }

    static Shell create(SshOptions sshOptions) {
        switch (CommandScript.detectShell(sshOptions)) {
            case Shells.BASH: new Bash(sshOptions); break;
            default: new Bash(sshOptions)
        }
    }
}
