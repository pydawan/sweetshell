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
    String copyCmd = 'cp'
    String showCmd = 'cat'
    String makeCmd = 'touch'
    String makeDirCmd = 'mkdir'
    String removeCmd = 'rm'
    String httpCmd = 'curl'
    String sourceCmd = 'source'
    String testCmd = 'test'

    def upload(from, to) {
        engine.remoteSession {
            scp objToPath(from), objToPath(to)
        }
    }

    def Shell cd(dir) {
        c(cdCmd, objToPath(dir))
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

    def Shell fileExists(file) {
        c(testCmd, '-f', file)
    }

    def Shell dirExists(file) {
        c(testCmd, '-d', file)
    }

    def Shell mk(file) {
        c(makeCmd, objToPath(file))
    }

    def Shell mkdir(dir) {
        c(makeDirCmd, objToPath(dir))
    }

    def Shell mkIf(file) {
        not().fileExists(file).mk(file)
    }

    def Shell mkdirIf(dir) {
        not().dirExists(dir).mkdir(dir)
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

    def Shell cp(source, dest) {
        c(copyCmd, objToPath(source), objToPath(dest))
    }

    def Shell cpIf(source, dest) {
        not().fileExists(dest).c(copyCmd, objToPath(source), objToPath(dest))
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

    def sudo = { Closure closure ->
        prefixStack.push(sudoCmd)
        closure(this)
        prefixStack.pop()
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

    def Shell workingDir() {
        c(workingDirCmd)
    }

    def Shell ls() {
        c(listCmd)
    }

    def Shell ls(dir) {
        c(listCmd, objToPath(dir))
    }

    def Shell yes() {
        c('yes').pipe()
    }
}