document.addEventListener("DOMContentLoaded", function() {

    // 1. Elementlarni tanlab olamiz
    const tg = window.Telegram.WebApp;
    const loginView = document.getElementById('login-view');
    const registerView = document.getElementById('register-view');
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const showRegisterLink = document.getElementById('show-register-link');
    const showLoginLink = document.getElementById('show-login-link');
    const errorMessage = document.getElementById('error-message');

    let currentView = 'login'; // Hozirgi holatni saqlaymiz

    // 2. Ilovani tayyorlaymiz
    tg.ready();
    tg.expand();
    tg.BackButton.hide(); // Dastlab orqaga qaytish tugmasi kerak emas

    // 3. Ko'rinishlarni almashtiruvchi funksiyalar
    function showLoginView() {
        loginView.classList.remove('hidden');
        registerView.classList.add('hidden');
        tg.MainButton.setText("Kirish");
        tg.BackButton.hide();
        currentView = 'login';
        validateInputs(); // Yangi oyna uchun validatsiya
    }

    function showRegisterView() {
        loginView.classList.add('hidden');
        registerView.classList.remove('hidden');
        tg.MainButton.setText("Ro'yxatdan o'tish");
        tg.BackButton.show(); // Register oynasida orqaga qaytish mumkin
        currentView = 'register';
        validateInputs(); // Yangi oyna uchun validatsiya
    }

    // Linklar bosilishini kuzatamiz
    showRegisterLink.addEventListener('click', (e) => { e.preventDefault(); showRegisterView(); });
    showLoginLink.addEventListener('click', (e) => { e.preventDefault(); showLoginView(); });
    tg.BackButton.onClick(() => showLoginView());

    // 4. Inputlarni validatsiya qiluvchi funksiya
    function validateInputs() {
        let isValid = false;
        if (currentView === 'login') {
            const inputs = loginForm.querySelectorAll('input');
            isValid = [...inputs].every(input => input.value.trim() !== '');
        } else {
            const inputs = registerForm.querySelectorAll('input');
            isValid = [...inputs].every(input => input.value.trim() !== '');
        }

        if (isValid) {
            tg.MainButton.enable();
        } else {
            tg.MainButton.disable();
        }
    }

    // Har bir inputga yozilganda validatsiyani chaqiramiz
    document.querySelectorAll('input').forEach(input => {
        input.addEventListener('input', validateInputs);
    });

    // 5. Asosiy tugma bosilganda
    tg.MainButton.onClick(async () => {
        hideError();
        tg.MainButton.showProgress(true).disable();

        try {
            if (currentView === 'login') {
                await handleLogin();
            } else {
                await handleRegister();
            }
        } catch (error) {
            showError(error.message);
        } finally {
            tg.MainButton.hideProgress().enable();
        }
    });

    // 6. API bilan ishlaydigan funksiyalar
    async function handleLogin() {
        const username = document.getElementById('login-username').value;
        const password = document.getElementById('login-password').value;

        const response = await fetch('/api/admin/telegram/login', { // LOGIN ENDPOINT
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password, initData: tg.initData })
        });

        const result = await response.json();
        if (!response.ok || !result.success) {
            throw new Error(result.message || 'Login yoki parol xato.');
        }

        tg.showPopup({ title: 'Xush kelibsiz!', message: 'Tizimga muvaffaqiyatli kirdingiz.' }, () => tg.close());
    }

    async function handleRegister() {
        const name = document.getElementById('register-name').value;
        const phone = document.getElementById('register-phone').value;
        const username = document.getElementById('register-username').value;
        const password = document.getElementById('register-password').value;

        const response = await fetch('/api/admin/telegram/register', { // REGISTER ENDPOINT
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, phone, username, password, initData: tg.initData })
        });

        const result = await response.json();
        if (!response.ok || !result.success) {
            throw new Error(result.message || 'Ro\'yxatdan o\'tishda xatolik yuz berdi.');
        }

        tg.showPopup({ title: 'Muvaffaqiyatli!', message: 'Ro\'yxatdan o\'tdingiz. Endi tizimga kirishingiz mumkin.' }, () => showLoginView());
    }

    // 7. Yordamchi funksiyalar
    function showError(message) {
        errorMessage.textContent = message;
        errorMessage.style.display = 'block';
    }
    function hideError() {
        errorMessage.style.display = 'none';
    }

    // Dastlabki sozlashlar
    showLoginView(); // Boshida Login oynasini ko'rsatish
    tg.MainButton.show(); // Asosiy tugmani ko'rsatish
});