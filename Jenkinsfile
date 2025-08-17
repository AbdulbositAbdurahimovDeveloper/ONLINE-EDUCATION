// ===================================================================
//              ENG ISHONCHLI VA BARQAROR JENKINSFILE
//      (Build jarayoni to'liq Docker konteyneri ichida bajariladi)
// ===================================================================
pipeline {
    agent any // Asosiy pipeline istalgan agentda ishlaydi

    // Jenkins sozlamalaridagi asboblarni chaqirish (faqat Docker uchun)
    tools {
        dockerTool 'docker-cli'
    }

    // Pipeline davomida ishlatiladigan o'zgaruvchilar
    environment {
        // ... avvalgidek barcha environment o'zgaruvchilaringiz ...
        IMAGE_NAME = "onlineeducation/app:${env.BUILD_NUMBER}"
        LATEST_IMAGE = "onlineeducation/app:latest"
        CONTAINER_NAME = 'online-education-container'
        DB_URL = credentials('online-education-db-url')
        DB_USER = credentials('postgres_username')
        DB_PASS = credentials('postgres_password')
        // ... va boshqalar
    }

    stages {
        stage('1. Klonlash') {
            steps {
                cleanWs()
                echo 'GitHub\'dan kodlar yuklanmoqda...'
                git url: 'https://github.com/AbdulbositAbdurahimovDeveloper/ONLINE-EDUCATION.git', branch: 'main'
                echo 'Kodlar muvaffaqiyatli yuklandi.'
            }
        }

        // --- > ASOSIY O'ZGARISH SHU BOSQICHDA < ---
        stage('2. JAR Faylni Docker Ichida Qurish') {
            agent {
                // Jenkins'ga aytamiz: "Maven 3.8 va JDK 17 o'rnatilgan rasmiy Docker image'ni ishlat"
                docker {
                    image 'maven:3.8-openjdk-17'
                    args '-v $HOME/.m2:/root/.m2' // Maven keshini saqlab qolish uchun
                }
            }
            steps {
                echo 'Maven yordamida loyiha qurilmoqda (Docker konteyneri ichida)...'
                // Endi bu buyruq "maven:3.8-openjdk-17" konteynerining ichida bajariladi
                sh 'mvn clean package -DskipTests'
                echo 'JAR fayl muvaffaqiyatli qurildi.'
            }
        }

        stage('3. Docker Image Yaratish') {
            steps {
                echo "Docker image yaratilmoqda: ${LATEST_IMAGE}"
                // JAR fayl endi workspace'ning `target` papkasida bo'ladi
                sh "docker build -t ${IMAGE_NAME} -t ${LATEST_IMAGE} ."
                echo 'Docker image muvaffaqiyatli yaratildi.'
            }
        }

        stage('4. Ilovani Deploy Qilish') {
            steps {
                // Bu qism o'zgarishsiz qoladi
                echo "Konteyner ishga tushirilmoqda: ${CONTAINER_NAME}"
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
                echo "Ilova muvaffaqiyatli ishga tushirildi!"
            }
        }
    }

    post {
        always {
            echo 'Pipeline yakunlandi. Ish joyi tozalanmoqda...'
            cleanWs()
        }
    }
}