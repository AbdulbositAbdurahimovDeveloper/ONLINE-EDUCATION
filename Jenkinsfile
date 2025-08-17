// ===================================================================
//              ENG ODDIY VA "OROMLAND"GA O'XSHASH JENKINSFILE
// ===================================================================

pipeline {
    agent any

    tools {
        // 'oromland' faylidagi kabi faqat 'docker-cli' ni qoldiramiz.
        dockerTool 'docker-cli'
    }

    environment {
        IMAGE_NAME = "onlineeducation/app:${env.BUILD_NUMBER}"
        LATEST_IMAGE = "onlineeducation/app:latest"
        CONTAINER_NAME = 'online-education-container'

        // Barcha maxfiy ma'lumotlar
        DB_URL                 = credentials('online-education-db-url')
        DB_USER                = credentials('postgres_username')
        DB_PASS                = credentials('postgres_password')
        // ... va boshqalar
    }

    stages {
        stage('1. Klonlash') {
            steps {
                cleanWs()
                git url: 'https://github.com/AbdulbositAbdurahimovDeveloper/ONLINE-EDUCATION.git', branch: 'main'
            }
        }

        // --- > YAGONA VA ENG MUHIM O'ZGARISH < ---
        stage('2. JAR Faylni Qurish') {
            steps {
                echo 'JAR fayl qurilmoqda (toza Docker konteyneri ichida)...'
                // Biz `agent { docker ... }` o'rniga to'g'ridan-to'g'ri `docker run` buyrug'ini ishlatamiz.
                // Bu 'oromland' ishlayotgan muhit bilan bir xil ishlaydi.
                sh """
                    docker run --rm -v "$(pwd)":/app -v "$HOME/.m2":/root/.m2 -w /app maven:3.8-openjdk-17 mvn clean package -DskipTests
                """
                echo 'JAR fayl muvaffaqiyatli qurildi.'
            }
        }

        stage('3. Docker Image Yaratish') {
            steps {
                echo "Docker image qurilmoqda: ${IMAGE_NAME}"
                sh "docker build -t ${IMAGE_NAME} -t ${LATEST_IMAGE} ."
                echo "Docker image muvaffaqiyatli qurildi."
            }
        }

        stage('4. Ilovani Deploy Qilish') {
            steps {
                echo "Container ishga tushirilmoqda: ${CONTAINER_NAME}"

                sh "docker rm -f ${CONTAINER_NAME} || true"

                sh """
                    docker run -d --restart always \\
                        --name ${CONTAINER_NAME} \\
                        -p 8888:8080 \\
                        --network oromland-network \\
                        -e SPRING_DATASOURCE_URL="${DB_URL}" \\
                        -e SPRING_DATASOURCE_USERNAME="${DB_USER}" \\
                        -e SPRING_DATASOURCE_PASSWORD="${DB_PASS}" \\
                        // ... boshqa barcha -e parametrlari ...
                        ${LATEST_IMAGE}
                """
                echo "Ilova http://176.57.188.165:8888 manzilida ishga tushdi."
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}