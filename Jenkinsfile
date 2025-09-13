pipeline {
    agent any

    tools {
        maven 'maven'
        jdk 'JDK17'
    }

    environment {
        IMAGE_NAME = "online_education/app:${env.BUILD_NUMBER}"
        LATEST_IMAGE = "online_education/app:latest"
        CONTAINER_NAME = 'online_education-container'
    }

    stages {
        stage('1. Clone Repo') {
            steps {
                cleanWs()
                echo 'Klonlash boshlandi...'
                git url: 'https://github.com/AbdulbositAbdurahimovDeveloper/ONLINE-EDUCATION.git', branch: 'main'
                echo 'Repo muvaffaqiyatli olindi.'
            }
        }

        stage('2. Build JAR') {
            steps {
                echo 'JAR fayl qurilmoqda...'
                sh 'mvn clean package -DskipTests'
                echo 'JAR fayl muvaffaqiyatli qurildi.'
            }
        }

        stage('3. Build Docker Image') {
            steps {
                echo "Docker image qurilmoqda: ${IMAGE_NAME}"
                sh 'cp target/*.jar .'
                sh "docker build -t ${IMAGE_NAME} -t ${LATEST_IMAGE} ."
                echo "Docker image muvaffaqiyatli qurildi: ${IMAGE_NAME} va ${LATEST_IMAGE}"
            }
        }

        stage('4. Deploy Application') {
            steps {
                echo "Container ishga tushirilmoqda: ${CONTAINER_NAME}"
                sh "docker rm -f ${CONTAINER_NAME} || true"
                sh "docker run -d --name ${CONTAINER_NAME} -p 8809:8080 --network app-network ${LATEST_IMAGE}"
                echo "Ilova http://localhost:8808 manzilida ishga tushdi."
            }
        }
    }

    post {
        always {
            echo 'Pipeline tugadi. Ish joyini tozalash...'
            cleanWs()
        }
    }
}
