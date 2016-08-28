package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine

class Bash implements Shell {
    def Bash(SshOptions sshOptions) {
        this.engine = new SshDslEngine(sshOptions)
    }
}
