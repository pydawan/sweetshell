package org.oreto.sweetshell

trait Shell implements ShellCommand<Shell> {

    String pipeOp = '|'
    String redirectOp = '>'
    String redirectErrOp = '2>'
    String appendOp = '>>'
    String cdCmd = 'cd'
    String workingDirCmd = 'pwd'
    String listCmd = 'ls'
    String grepCmd = 'grep'
    String sudoCmd = 'sudo'
    String backgroundCmd = 'setsid'
    String mvCmd = 'mv'
    String renameCmd = 'mv'
    String copyCmd = 'cp'
    String showCmd = 'cat'
    String makeCmd = 'touch'
    String makeDirCmd = 'mkdir'
    String removeCmd = 'rm'
    String httpCmd = 'curl'
    String sourceCmd = 'source'
    String testCmd = 'test'
    String echoCmd = 'echo'
    String unzipCmd = 'tar'

    boolean enableSudo = true

    def upload(from, to) {
        engine.remoteSession {
            scp objToPath(from), objToPath(to)
        }
    }

    def Shell cd(dir) {
        if(dir instanceof String) dir = path(dir)
        else dir = objToPath(dir)
        c(cdCmd, dir)
    }

    def Shell cd() {
        cd(currentDirName)
    }

    def Shell home() {
        cd(homeDirName)
    }

    def Shell show(file) {
        c(showCmd, objToPath(file))
    }

    def Shell echo(String s) {
        c(echoCmd, s)
    }

    def Shell test(String flag, file) {
        c(testCmd, flag, file)
    }

    def Shell testFileExists(file) {
        test('-e', file)
    }

    def Shell testDirExists(file) {
        test('-d', file)
    }

    def Shell mk(file) {
        c(makeCmd, objToPath(file))
    }

    def Shell mkdir(dir) {
        c(makeDirCmd, objToPath(dir))
    }

    def Shell mkIf(file) {
        not().testFileExists(file).mk(file)
    }

    def Shell mkdirIf(dir) {
        not().testDirExists(dir).mkdir(dir)
    }

    def Shell rm(Object... dir) {
        c(removeCmd, '-rf', objsToParams(dir).join(' '))
    }

    def Shell mv(source, dest, String... options) {
        def params = options.toList()
        params.add(objToPath(source))
        params.add(objToPath(dest))
        c(mvCmd, params)
    }

    def Shell rename(source, dest) {
        c(renameCmd, source, dest)
    }

    def Shell cp(source, dest) {
        c(copyCmd, objToPath(source), objToPath(dest))
    }

    def Shell cpIf(source, dest) {
        not().testFileExists(dest).c(copyCmd, objToPath(source), objToPath(dest))
    }

    def Shell not() {
        addOperator(notOp)
    }

    def Shell pipe() {
        addOperator(pipeOp)
    }

    def Shell redirect(file, boolean append = false) {
        String op = append ? appendOp : redirectOp
        CommandScript command = this.commandScripts?.last()
        if(command instanceof Command) {
            command.script += " $op ${objToPath(file)}"
        }
        this
    }

    def Shell redirectErr(file) {
        CommandScript command = this.commandScripts?.last()
        if(command instanceof Command) {
            command.script += " $redirectErrOp ${objToPath(file)}"
        }
        this
    }

    def Shell grep(String text, Object... files) {
        def fileList = objsToParams(files)
        c(grepCmd, text, fileList.join(' '))
    }

    def Shell grep(String text) {
        c(grepCmd, text)
    }

    def Shell http(String url) {
        c(httpCmd, '-s', url)
    }

    def Shell source(file) {
        c(sourceCmd, objToPath(file))
    }

    def Shell workingDir() {
        c(workingDirCmd)
    }

    def Shell ls() {
        c(listCmd)
    }

    def Shell ls(dir) {
        c(listCmd, objToPath(dir))
    }

    def Shell yes(String text = '') {
        if(text) c('yes', text).pipe()
        else c('yes').pipe()
    }

    def Shell unzip(file, dest = '', String flags = '-zxvf') {
        if(dest) c(unzipCmd, '-C', dest, flags, file)
        else c(unzipCmd, flags, file)
    }

    def sudo = { Closure closure ->
        if(enableSudo) prefixStack.push(sudoCmd)
        closure(this)
        if(enableSudo) prefixStack.pop()
        this
    }

    def background = { Closure closure ->
        prefixStack.push(backgroundCmd)
        closure(this)
        prefixStack.pop()
        this
    }

    def seq = { Closure closure ->
        opStack.push(combineOp)
        closure(this)
        opStack.pop()
        this
    }

    def or = { Closure closure ->
        addOperator(orOp)
        opStack.push(orOp)
        closure(this)
        opStack.pop()
        this
    }

    def and = { Closure closure ->
        addOperator(andOp)
        opStack.push(andOp)
        closure(this)
        opStack.pop()
        this
    }

    def combine = { Closure closure ->
        addOperator(combineOpenOp)
        opStack.push(combineOp)
        closure(this)
        opStack.pop()
        addOperator(combineOp)
        addOperator(combineCloseOp)
        this
    }

    def group = { Closure closure ->
        addOperator(groupOpenOp)
        closure(this)
        addOperator(groupCloseOp)
        this
    }

    def test = { Closure closure ->
        addOperator(testGroupOpenOp)
        closure(this)
        addOperator(testGroupCloseOp)
        this
    }
}