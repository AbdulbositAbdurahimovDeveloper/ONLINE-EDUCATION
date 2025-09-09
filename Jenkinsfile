pipeline {
    agent any

    options {
        skipDefaultCheckout()   // Jenkins default checkoutni o‘chirib tashlaymiz
    }

    tools {
        maven 'maven'           // Global Tool Configuration → "maven"
        jdk 'JDK21'             // Global Tool Configuration → "JDK21" (java-21 path)
        dockerTool 'docker-cli' // Global Tool Configuration → "docker-cli"
    }

    environment {
        IMAGE_NAME      = "online_education/app:${env.BUILD_NUMBER}" // unique build image
        LATEST_IMAGE    = "online_education/app:latest"              // always latest
        CONTAINER_NAME  = "online_education-container"
        APP_PORT        = "8888"   // serverda ochiladigan port
        CONTAINER_PORT  = "8080"   // Spring Boot ichki port
        DOCKER_NETWORK  = "oromland-network"

        JAVA_TOOL_OPTIONS = "-Dorg.jenkinsci.plugins.durabletask.BourneShellScript.HEARTBEAT_CHECK_INTERVAL=86400"
    }

    stages {
        stage('1. Clone Repo') {
            steps {
                cleanWs()
                echo '📥 Repo klonlanmoqda...'
                git branch: 'main', url: 'https://github.com/AbdulbositAbdurahimovDeveloper/ONLINE-EDUCATION.git'
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

                // Network mavjudligini tekshirish
                sh "docker network create ${DOCKER_NETWORK} || true"

                // Eski container bor bo‘lsa — tozalash
                sh "docker rm -f ${CONTAINER_NAME} || true"

                // Yangi container ishga tushirish
                sh """
                docker run -d \
                    --name ${CONTAINER_NAME} \
                    -p ${APP_PORT}:${CONTAINER_PORT} \
                    --network ${DOCKER_NETWORK} \
                    ${LATEST_IMAGE}
                """

                echo "✅ Ilova http://localhost:${APP_PORT} da ishga tushdi."
            }
        }

        stage('5. Verify Application') {
            steps {
                echo "🔎 Container loglari:"
                sh "docker logs --tail=50 ${CONTAINER_NAME}"
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
