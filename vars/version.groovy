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

def commitHashShort() {
    return env.GIT_COMMIT.substring(0, 8)
}

static def timestamp() {
    return new SimpleDateFormat("yyyyMMdd.HHmm").format(new Date())
}