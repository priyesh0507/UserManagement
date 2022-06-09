pipeline {
    agent any
    tools { 
        maven "mvn" 
        jdk "jdk8"
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                ''' 
            }
        }

        stage ('Build') {
            steps {
                sh 'pwd'
                sh 'ls -al'
                echo 'This is a minimal pipeline.'
                sh 'mvn clean package'
            }
        }
        stage("Build image") {
            steps {
                script {
                    myapp = docker.build("otaideators/usermgmnt:${env.BUILD_ID}")
                }
            }
        }
        stage("Push image") {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', 'dockerhub') {
                            myapp.push("latest")
                            myapp.push("${env.BUILD_ID}")
                    }
                }
            }
        }
        
    }
}
