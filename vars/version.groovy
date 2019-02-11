import java.text.SimpleDateFormat

def call() {
    if(!env.GIT_COMMIT) {
        error 'environment variable GIT_COMMIT is required'
    }
    return version()
}

def version() {
    return timestamp() + "." + commitHashShort()
}

def version(commitHashLong) {
    return timestamp() + "." + commitHashShort(commitHashLong)
}

def commitHashShort() {
    return commitHashShort(env.GIT_COMMIT)
}

def commitHashShort(commitHashLong) {
    return commitHashLong.substring(0, 8)
}

static def timestamp() {
    return new SimpleDateFormat("yyyyMMdd.HHmm").format(new Date())
}

return this;
