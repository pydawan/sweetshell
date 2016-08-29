package org.oreto.sweetshell

import com.aestasit.infrastructure.ssh.SshOptions
import com.aestasit.infrastructure.ssh.dsl.CommandOutput
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine

import static com.aestasit.infrastructure.ssh.DefaultSsh.options
import static com.aestasit.infrastructure.ssh.DefaultSsh.remoteSession

trait ShellCommand<T extends ShellCommand<T>> implements ShellPath {

    String andOp = '&&'
    String orOp = '||'
    String defaultOp = andOp
    Stack<String> opStack = new Stack<String>()
    Stack<String> prefixStack = new Stack<String>()
    String runPrefix = ''
    ArrayList<CommandScript> commands = [].withDefault { [:] }
    StringBuffer commandBuilder = new StringBuffer()
    SshDslEngine engine

    String groupOpenOp = '('
    String groupCloseOp = ')'
    String combineOpenOp = '{'
    String combineCloseOp = '}'
    String combineOp = ';'

    def CommandOutput runCommand(String cmd, boolean show = true) {
        CommandOutput commandOutput = null
        if(runPrefix) cmd = "$runPrefix $cmd"
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
        commandOutput
    }

    def T c(cmd, List<String> params, usePrefix = true) {
        cmd = objToPath(cmd)
        def resolvedParams = params.join(' ')
        def prefix = prefixStack.join(' ')
        if(params) cmd = prefix && usePrefix ? "$prefix $cmd $resolvedParams" : "$cmd $resolvedParams"
        else if(prefix) cmd = "$prefix $cmd"
        def op = opStack.isEmpty() ? defaultOp : opStack.peek()
        def command = new Command(script: cmd)
        addOperator(op)
        commands.add(command)
        this as T
    }

    def T c(cmd, Object... params) {
        c(cmd, objsToParams(params))
    }

    def T c(Object... cmd) {
        cmd.each { c(objToPath(it)) }
        this as T
    }

    def T cexe(String exe, Object... params) {
        c(relativePath(currentDirName, exe), params)
    }

    def String commandString() {
        commands.eachWithIndex { c, i ->
            def s = c.script
            if(c instanceof Operator) {
                if(i == 0 || (i > 0 && commands[i - 1] instanceof Operator)) s = "$s "
                else if(i > 0 && commands[i - 1] instanceof Command) s = " $s "
            }
            commandBuilder.append(s)
        }
        def commandString = commandBuilder.toString().trim()
        commandBuilder.delete(0, commandBuilder.size())
        commandString
    }

    def String text(boolean show = true) {
        run(show).output.trim()
    }

    def boolean ok(boolean show = true) {
        run(show).exitStatus == 0
    }

    def boolean notOk(boolean show = true) {
        !ok(show)
    }

    def T addOperator(String op) {
        def lastCommand = commands.isEmpty() ? null : commands.last()
        if( (op == groupOpenOp || op == combineOpenOp)
                && (lastCommand == null || lastCommand instanceof Operator)
                || lastCommand instanceof Command
                || lastCommand?.script == groupCloseOp
                || lastCommand?.script == combineCloseOp
                || lastCommand?.script == combineOp && op != andOp && op != orOp) {
            def command = new Operator(script: op)
            commands.add(command)
        }
        this as T
    }
}

class CommandScript {
    static String helpCmd = 'help'
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

    String script
}

class Command extends CommandScript {}
class Operator extends CommandScript {}

enum Shells {
    CMD, BASH, OTHER
}