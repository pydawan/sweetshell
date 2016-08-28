package org.oreto.sweetshell

trait ShellPath {

    String separator = '/'
    String localSeparator = File.separator

    def String path(Collection names, String... dir) {
        names.addAll(dir)
        separator + names.join(separator)
    }
    def String path(String... names) {
        path(names.collect())
    }

    def String relativePath(Collection names, String... dir) {
        names.addAll(dir)
        names.join(separator)
    }
    def String relativePath(String... names) {
        relativePath(names.collect())
    }

    def String localPath(Collection names, String... dir) {
        names.addAll(dir)
        localSeparator + names.join(localSeparator)
    }
    def String localPath(String... names) {
        localPath(names.collect())
    }

    def String objToPath(o) {
        o instanceof Collection<String> ? path(o as Collection<String>) : o.toString()
    }

    def List<String> objsToParams(Object... objs) {
        def paths = []
        objs?.each { paths.add(objToPath(it))}
        paths
    }
}