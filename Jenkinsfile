// ===================================================================
//              SINTAKSIS XATOSI TUZATILGAN YAKUNIY JENKINSFILE
//
// Bu versiya ishlayotgan "oromland" andozasiga asoslangan bo'lib,
// JDK 17 muammosini va Groovy sintaksis xatosini hal qiladi.
// ===================================================================

pipeline {
    // Pipeline asosiy Jenkins agent'ida ishlaydi
    agent any

    // 'oromland' faylidagi kabi faqat 'docker-cli' ni ishlatamiz
    tools {
        dockerTool 'docker-cli'
    }

    // Pipeline davomida ishlatiladigan o'zgaruvchilar
    environment {
        // --- 1. UMUMIY O'ZGARUVCHILAR ---
        IMAGE_NAME = "onlineeducation/app:${env.BUILD_NUMBER}"
        LATEST_IMAGE = "onlineeducation/app:latest"
        CONTAINER_NAME = 'online-education-container'

        // --- 2. JENKINS CREDENTIALS'DAN MAXFIY MA'LUMOTLARNI O'QISH ---
        // DIQQAT: Bu yerdagi ID'lar Jenkins'dagi Credentials ID'lariga to'liq mos kelishi shart!

        // Database sozlamalari
        DB_URL                 = credentials('online-education-db-url')      // Qiymati: jdbc:postgresql://keycloak-db:5432/online_education
        DB_USER                = credentials('postgres_username')
        DB_PASS                = credentials('postgres_password')

        // E-mail sozlamalari
        MAIL_USER              = credentials('online-edu-mail-username')
        MAIL_PASS              = credentials('online-edu-mail-password')

        // JWT (Token) sozlamalari
        JWT_SECRET             = credentials('online-edu-jwt-secret')
        JWT_ACCESS_EXP         = '86400000'                                    // 1 kun
        JWT_REFRESH_EXP        = '604800000'                                   // 7 kun

        // MinIO (Fayl saqlash) sozlamalari
        MINIO_ENDPOINT         = credentials('online-edu-minio-endpoint')
        MINIO_ACCESS_KEY       = credentials('online-edu-minio-access-key')
        MINIO_SECRET_KEY       = credentials('online-edu-minio-secret-key')

        // Telegram Bot sozlamalari
        TELEGRAM_TOKEN         = credentials('online-edu-telegram-token')
        TELEGRAM_USERNAME      = credentials('online-edu-telegram-username')
        TELEGRAM_PATH          = "http://176.57.188.165:8888/api/v1/telegram-webhook"
        TELEGRAM_CHAT_ID       = credentials('online-edu-telegram-chat-id')
        TELEGRAM_CHANNEL_ID    = credentials('online-edu-telegram-channel-id')
    }

    stages {
        stage('1. Klonlash') {
            steps {
                cleanWs()
                git url: 'https://github.com/AbdulbositAbdurahimovDeveloper/ONLINE-EDUCATION.git', branch: 'main'
            }
        }

        stage('2. JAR Faylni Qurish') {
            steps {
                echo 'JAR fayl qurilmoqda (toza Docker konteyneri ichida)...'

                // Groovy sintaksis xatosining oldini olish uchun $ belgilari ekranlandi (\ belgisi bilan)
                sh """
                    docker run --rm -v "\\$(pwd)":/app -v "\\$HOME/.m2":/root/.m2 -w /app maven:3.8-openjdk-17 mvn clean package -DskipTests
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
                        -e SPRING_MAIL_USERNAME="${MAIL_USER}" \\
                        -e SPRING_MAIL_PASSWORD="${MAIL_PASS}" \\
                        -e APPLICATION_JWT_SECRET="${JWT_SECRET}" \\
                        -e APPLICATION_JWT_ACCESS_TOKEN_EXPIRATION="${JWT_ACCESS_EXP}" \\
                        -e APPLICATION_JWT_REFRESH_TOKEN_EXPIRATION="${JWT_REFRESH_EXP}" \\
                        -e APPLICATION_MINIO_ENDPOINT="${MINIO_ENDPOINT}" \\
                        -e APPLICATION_MINIO_ACCESS_KEY="${MINIO_ACCESS_KEY}" \\
                        -e APPLICATION_MINIO_SECRET_KEY="${MINIO_SECRET_KEY}" \\
                        -e APPLICATION_TELEGRAM_BOT_TOKEN="${TELEGRAM_TOKEN}" \\
                        -e APPLICATION_TELEGRAM_BOT_USERNAME="${TELEGRAM_USERNAME}" \\
                        -e APPLICATION_TELEGRAM_BOT_WEBHOOK_PATH="${TELEGRAM_PATH}" \\
                        -e APPLICATION_TELEGRAM_CHAT_ID="${TELEGRAM_CHAT_ID}" \\
                        -e APPLICATION_TELEGRAM_CHANNEL_ID="${TELEGRAM_CHANNEL_ID}" \\
                        ${LATEST_IMAGE}
                """
                echo "--------------------------------------------------------"
                echo "Ilova muvaffaqiyatli ishga tushirildi!"
                echo "Manzil: http://176.57.188.165:8888"
                echo "--------------------------------------------------------"
            }
        }
    }

    post {
        always {
            cleanWs()
        }
    }
}