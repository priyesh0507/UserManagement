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
        
    }
}
