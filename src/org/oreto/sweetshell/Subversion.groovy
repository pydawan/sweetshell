package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions


class Subversion implements Cli<Subversion> {
    static String exe = 'svn'
    static String HEAD = 'head'
    String executable = exe
    String trunkUri = '/trunk'
    String tagUri = '/tag'
    String branchUri = '/branch'
    SubversionOptions options

    Subversion(SshOptions sshOptions, SubversionOptions options, home = '') {
        this.options = options
        initCli(sshOptions, objToPath(home))
    }

    Subversion checkout(path = '', RepoType type = RepoType.TRUNK, String rev = HEAD) {
        cmd('checkout', "--username=${options.username}", "--password=${options.password}", '-r',
                rev, urlFor(options.repoUrl, type), path)
    }

    String urlFor(String url, RepoType type) {
        switch (type) {
            case RepoType.TRUNK: "$url$trunkUri"; break;
            case RepoType.BRANCH: "$url$branchUri"; break;
            case RepoType.TAG: "$url$tagUri"; break;
            default: url
        }
    }

    enum RepoType{
        TRUNK, BRANCH, TAG, NONE
    }
}

class SubversionOptions {
    String repoUrl
    String username
    String password
}


