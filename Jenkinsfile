// ===================================================================
//              "OROMLAND" ANDOZASI ASOSIDA YARATILGAN YAKUNIY JENKINSFILE
// ===================================================================

pipeline {
    // Pipeline asosiy Jenkins agent'ida ishlaydi (oromland kabi)
    agent any

    // 'Global Tool Configuration'dagi sozlamalarni chaqirish
    tools {
        // Maven va JDK'ni bu yerdan olib tashladik, chunki buildni Dockerda qilamiz.
        // docker-cli sizning oromland proyektingizda ishlagan, demak bu yerda ham ishlashi kerak.
        dockerTool 'docker-cli'
    }

    // Pipeline uchun o'zgaruvchilar (yangi proyektga moslandi)
    environment {
        IMAGE_NAME = "onlineeducation/app:${env.BUILD_NUMBER}"
        LATEST_IMAGE = "onlineeducation/app:latest"
        CONTAINER_NAME = 'online-education-container'

        // Barcha maxfiy ma'lumotlar avvalgidek Credentials'dan olinadi
        DB_URL                 = credentials('online-education-db-url')
        DB_USER                = credentials('postgres_username')
        DB_PASS                = credentials('postgres_password')
        // ... va boshqa barcha maxfiy ma'lumotlaringiz
    }

    stages {
        stage('1. Klonlash') { // Bu bosqich oromland bilan bir xil
            steps {
                cleanWs()
                echo 'Klonlash boshlandi...'
                git url: 'https://github.com/AbdulbositAbdurahimovDeveloper/ONLINE-EDUCATION.git', branch: 'main'
                echo 'Repo muvaffaqiyatli olindi.'
            }
        }

        // --- > YAGONA VA ENG MUHIM O'ZGARISH SHU YERDA < ---
        stage('2. JAR Faylni Docker Ichida Qurish') {
            // Jenkins'dagi "buzilgan" JDK 17 o'rniga toza va barqaror Docker muhitini ishlatamiz
            agent {
                docker {
                    image 'maven:3.8-openjdk-17'
                    args '-v $HOME/.m2:/root/.m2'
                }
            }
            steps {
                echo 'Maven yordamida loyiha qurilmoqda (toza Docker konteyneri ichida)...'
                sh 'mvn clean package -DskipTests'
                echo 'JAR fayl muvaffaqiyatli qurildi.'
            }
        }

        stage('3. Docker Image Yaratish') { // Bu bosqich oromland bilan bir xil
            steps {
                echo "Docker image qurilmoqda: ${IMAGE_NAME}"
                // 'docker' buyrug'i endi `tools` blokidagi 'docker-cli' tufayli topilishi kerak
                sh "docker build -t ${IMAGE_NAME} -t ${LATEST_IMAGE} ."
                echo "Docker image muvaffaqiyatli qurildi."
            }
        }

        stage('4. Ilovani Deploy Qilish') { // Bu bosqich oromland bilan bir xil
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

    post { // Bu blok ham oromland bilan bir xil
        always {
            echo 'Pipeline tugadi. Ish joyini tozalash...'
            cleanWs()
        }
    }
}