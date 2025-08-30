pipeline {
    // Pipeline asosiy Jenkins agent'ida ishlaydi
    agent any

    // 'Global Tool Configuration'dagi sozlamalarni chaqirish
    tools {
        // Bu nom 'Global Tool Configuration'dagi 'Name' maydoniga to'liq mos kelishi kerak
        maven 'maven'
        jdk 'JDK21'
        dockerTool 'docker-cli'
    }

    // Pipeline uchun o'zgaruvchilar
    environment {
        IMAGE_NAME = "online_education/app:${env.BUILD_NUMBER}" // Har bir build uchun unikal teg
        LATEST_IMAGE = "online_education/app:latest"         // Har doim eng so'nggi versiya uchun teg
        CONTAINER_NAME = 'online_education-container'       // Ishga tushiriladigan container nomi
    }

    stages {
        stage('1. Clone Repo') {
            steps {
                // Tozalik uchun avvalgi build qoldiqlarini o'chiramiz
                cleanWs()
                echo 'Klonlash boshlandi...'
                git url: 'https://github.com/AbdulbositAbdurahimovDeveloper/ONLINE-EDUCATION.git', branch: 'main'
                echo 'Repo muvaffaqiyatli olindi.'
            }
        }

        stage('2. Build JAR') {
            steps {
                echo 'JAR fayl qurilmoqda...'
                // 'mvn' buyrug'i endi `tools` bloki tufayli topiladi
                sh 'mvn clean package -DskipTests'
                echo 'JAR fayl muvaffaqiyatli qurildi.'
            }
        }

        stage('3. Build Docker Image') {
            steps {
                echo "Docker image qurilmoqda: ${IMAGE_NAME}"
                // JAR faylni joriy direktoriyaga nusxalash
                sh 'cp target/*.jar .'

                // Image'ni qurish va ikkita teg bilan belgilash
                sh "docker build -t ${IMAGE_NAME} -t ${LATEST_IMAGE} ."
                echo "Docker image muvaffaqiyatli qurildi: ${IMAGE_NAME} va ${LATEST_IMAGE}"
            }
        }

        stage('4. Deploy Application') {
            steps {
                echo "Container ishga tushirilmoqda: ${CONTAINER_NAME}"

                // Eski container'ni o'chirish (agar mavjud bo'lsa)
                sh "docker rm -f ${CONTAINER_NAME} || true"

                // Yangi container'ni ishga tushirish
                // --- > --network oromland-network < --- QO'SHILDI
                sh "docker run -d --name ${CONTAINER_NAME} -p 8888:8080 --network oromland-network ${LATEST_IMAGE}"

                echo "Ilova http://localhost:8081 manzilida ishga tushdi."
            }
        }
    } // stages bloki shu yerda yopiladi

    // Pipeline qanday tugashidan qat'iy nazar ishlaydigan blok
    post {
        always {
            echo 'Pipeline tugadi. Ish joyini tozalash...'
            // Build'dan qolgan fayllarni o'chirish
            cleanWs()
        }
    }
}