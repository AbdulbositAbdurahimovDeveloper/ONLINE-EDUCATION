pipeline {
    agent any

    tools {
        maven 'maven'          // Global Tool Configuration → "maven"
        jdk 'JDK21'            // Global Tool Configuration → "JDK21" (java-21 yo‘li bilan)
        dockerTool 'docker-cli'
    }

    environment {
        IMAGE_NAME = "online_education/app:${env.BUILD_NUMBER}" // Unikal image
        LATEST_IMAGE = "online_education/app:latest"            // Always latest
        CONTAINER_NAME = "online_education-container"
        JAVA_TOOL_OPTIONS = "-Dorg.jenkinsci.plugins.durabletask.BourneShellScript.HEARTBEAT_CHECK_INTERVAL=86400"
    }

    stages {
        stage('1. Clone Repo') {
            steps {
                cleanWs()
                echo '📥 Repo klonlanmoqda...'
                git url: 'https://github.com/AbdulbositAbdurahimovDeveloper/ONLINE-EDUCATION.git', branch: 'main'
            }
        }

        stage('2. Build JAR') {
            steps {
                echo '📦 JAR fayl qurilmoqda...'
                sh 'mvn clean package -DskipTests'
                echo '✅ JAR fayl tayyor.'
            }
        }

        stage('3. Build Docker Image') {
            steps {
                echo "🐳 Docker image qurilmoqda: ${IMAGE_NAME}"
                sh "docker build -t ${IMAGE_NAME} -t ${LATEST_IMAGE} ."
                echo "✅ Docker image qurildi: ${IMAGE_NAME}, ${LATEST_IMAGE}"
            }
        }

        stage('4. Deploy Application') {
            steps {
                echo "🚀 Container ishga tushirilmoqda: ${CONTAINER_NAME}"

                // Eski container bor bo‘lsa — tozalash
                sh "docker rm -f ${CONTAINER_NAME} || true"

                // Yangi container ishga tushirish
                sh "docker run -d --name ${CONTAINER_NAME} -p 8888:8080 --network oromland-network ${LATEST_IMAGE}"

                echo "✅ Ilova http://localhost:8888 da ishga tushdi."
            }
        }
    }

    post {
        always {
            echo '🧹 Pipeline tugadi. Workspace tozalanmoqda...'
            cleanWs()
        }
    }
}
