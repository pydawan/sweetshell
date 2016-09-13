package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions


class Vertx implements Cli<Vertx> {
    static String exe = 'vertx'
    String executable = exe

    Vertx(SshOptions sshOptions, home) {
        initCli(sshOptions, objToPath(home))
    }

    String list() {
        cmd('list').text()
    }

    Vertx start(Object app, String id = null) {
        shell.sudo {
            it.background {
                if(id) cmd('start', app, '-id', id)
                else cmd('start', app)
            }
        }
        this
    }

    Vertx stop(String app) {
        shell.sudo {
            cmd('stop', app)
        }
        this
    }
}
