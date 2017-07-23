node('general') {
    git branch: 'jenkins', url: 'https://github.com/calvinlfer/akka-cluster-sample'

    ansiColor('xterm') {
        sh 'sbt test'
    }
}