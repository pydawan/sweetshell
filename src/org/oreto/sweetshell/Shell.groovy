package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions
import com.aestasit.infrastructure.ssh.dsl.CommandOutput
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine

import static com.aestasit.infrastructure.ssh.DefaultSsh.remoteSession
import static com.aestasit.infrastructure.ssh.DefaultSsh.options

trait Shell implements ShellPath {
    static String helpCmd = 'help'

    String andOp = '&&'
    String orOp = '||'
    String pipeOp = '|'
    String groupOpenOp = '('
    String groupCloseOp = ')'
    String combineOpenOp = '{'
    String combineCloseOp = '}'
    String combineOp = ';'
    String cdCmd = 'cd'
    String parentDirName = '..'
    String currentDirName = '.'
    String workingDirCmd = 'pwd'
    String listCmd = 'ls'
    String homeDirName = '~'
    String grepCmd = 'grep'
    String sudoCmd = 'sudo'
    String backgroundCmd = 'setsid'
    String redirectCmd = '>'
    String mvCmd = 'mv'
    String showCmd = 'cat'
    String makeCmd = 'touch'
    String makeDirCmd = 'mkdir'
    String removeCmd = 'rm'

    String defaultOp = andOp
    Stack<String> opStack = new Stack<String>()
    Stack<String> prefixStack = new Stack<String>()
    ArrayList<Command> commands = [].withDefault { [:] }
    StringBuffer commandBuilder = new StringBuffer()
    SshDslEngine engine

    static def Shells detectShell(SshOptions sshOptions) {
        options = sshOptions
        CommandOutput commandOutput = null
        remoteSession {
            commandOutput = exec(command: helpCmd, showOutput: false)
        }
        def help = commandOutput?.output
        if(help?.contains('Windows environment variables')) Shells.CMD
        else if(help?.contains('GNU bash')) Shells.BASH
        else Shells.OTHER
    }

    def CommandOutput runCommand(String cmd, boolean show = true) {
        CommandOutput commandOutput = null
        engine.remoteSession {
            commandOutput = exec(command: cmd, showOutput: show)
        }
        if(commandOutput.getExitStatus() != 0 && commandOutput.exception != null)
            throw commandOutput.exception
        commandOutput
    }

    def CommandOutput run(boolean show = true) {
        def commandOutput = runCommand(commandString(), show)
        commands.clear()
        commandBuilder.delete(0, commandBuilder.size())
        commandOutput
    }

    def Shell c(cmd, List<String> params) {
        cmd = objToPath(cmd)
        def resolvedParams = params.join(' ')
        def prefix = prefixStack.join(' ')
        if(params) cmd = prefix ? "$prefix $cmd $resolvedParams" : "$cmd $resolvedParams"
        def op = opStack.isEmpty() ? defaultOp : opStack.peek()
        def command = new Command(string: cmd, type: CommandType.COMMAND)
        addOperator(op)
        commands.add(command)
        this
    }

    def Shell c(cmd, Object... params) {
        c(cmd, objsToParams(params))
    }

    def Shell c(Object... cmd) {
        cmd.each { c(objToPath(it)) }
        this
    }

    def Shell cexe(String exe, Object... params) {
        c(relativePath(currentDirName, exe), params)
    }

    def upload(from, to) {
        engine.remoteSession {
            scp objToPath(from), objToPath(to)
        }
    }

    def text(boolean show = true) {
        run(show).output.trim()
    }

    def ok(boolean show = true) {
        run(show).exitStatus == 0
    }

    def notOk(boolean show = true) {
        !ok(show)
    }

    def String commandString() {
        commands.eachWithIndex { c, i ->
            def s = c.string
            if(c.type == CommandType.OPERATOR) {
                if(i == 0 || (i > 0 && commands[i - 1].type == CommandType.OPERATOR)) s = "$s "
                else if(i > 0 && commands[i - 1].type == CommandType.COMMAND) s = " $s "
            }
            commandBuilder.append(s)
        }
        commandBuilder.toString().trim()
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

    def Shell mk(file) {
        c(makeCmd, objToPath(file))
    }

    def Shell mkdir(dir) {
        c(makeDirCmd, objToPath(dir))
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

    def Shell pipe() {
        addOperator(pipeOp)
    }

    def Shell grep(String text, Object... files) {
        def fileList = objsToParams(files)
        c(grepCmd, text, fileList.join(' '))
    }

    def Shell grep(String text) {
        c(grepCmd, text)
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

    def Shell workingDir() {
        c(workingDirCmd)
    }

    def Shell ls() {
        c(listCmd)
    }

    def Shell ls(dir) {
        c(listCmd, objToPath(dir))
    }

    def Shell addOperator(String op) {
        def lastCommand = commands.isEmpty() ? null : commands.last()
        if( (op == groupOpenOp || op == combineOpenOp)
                && (lastCommand == null || lastCommand?.type == CommandType.OPERATOR)
                || lastCommand?.type == CommandType.COMMAND
                || lastCommand?.string == groupCloseOp
                || lastCommand?.string == combineCloseOp
                || lastCommand?.string == combineOp && op != andOp && op != orOp) {
            def command = new Command(string: op, type: CommandType.OPERATOR)
            commands.add(command)
        }
        this
    }
}

class Command {
    CommandType type
    String string
}

enum CommandType {
    COMMAND, OPERATOR
}

enum Shells {
    CMD, BASH, OTHER
}