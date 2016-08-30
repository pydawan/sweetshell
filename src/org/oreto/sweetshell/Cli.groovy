package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine

trait Cli<T extends Cli<T>> implements Shell {
    abstract String getExecutable()
    abstract void setExecutable(String executable)

    void initCli(SshOptions sshOptions, String home) {
        this.engine = new SshDslEngine(sshOptions)
        homeDirName = home
        executable = relativePath(home, 'bin', executable)
    }

    T cmd(cmd, Object... params) {
        prefixStack.push(executable)
        c(cmd, params)
        prefixStack.pop()
        this as T
    }

    Shell getShell() {
        this as Shell
    }
}