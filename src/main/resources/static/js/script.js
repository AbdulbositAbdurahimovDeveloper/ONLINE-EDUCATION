document.addEventListener('DOMContentLoaded', function() {
    // Kerakli DOM elementlarini topib olish
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const showRegisterLink = document.getElementById('showRegisterLink');
    const showLoginLink = document.getElementById('showLoginLink');
    const messageBox = document.getElementById('message-box');

    // 1. URL'dan 'chat_id' parametrini olish
    const urlParams = new URLSearchParams(window.location.search);
    const chatId = urlParams.get('chat_id');

    // Agar chat_id topilmasa, xatolik ko'rsatib, ishni to'xtatish
    if (!chatId) {
        document.querySelector('.auth-container').innerHTML = '<h1>Xatolik</h1><p>Iltimos, Telegram botidagi to‘g‘ri havola orqali kiring.</p>';
        return;
    }

    // 2. Olingan chat_id'ni yashirin inputlarga joylashtirish
    document.getElementById('login_chat_id').value = chatId;
    document.getElementById('register_chat_id').value = chatId;

    // 3. Formalarni almashtirish logikasi
    showRegisterLink.addEventListener('click', function(event) {
        event.preventDefault();
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        messageBox.style.display = 'none';
    });

    showLoginLink.addEventListener('click', function(event) {
        event.preventDefault();
        registerForm.style.display = 'none';
        loginForm.style.display = 'block';
        messageBox.style.display = 'none';
    });

    // 4. Xabar ko'rsatuvchi yordamchi funksiya
    function showMessage(message, isSuccess) {
        messageBox.textContent = message;
        messageBox.className = 'message-box';
        messageBox.classList.add(isSuccess ? 'success' : 'error');
    }

    // 5. Formalarni serverga yuborish uchun umumiy funksiya
    async function handleFormSubmit(event) {
        event.preventDefault();

        const form = event.target;
        const button = form.querySelector('button');
        const formData = new FormData(form);

        button.disabled = true;
        button.textContent = 'Yuborilmoqda...';

        try {
            const response = await fetch(form.action, {
                method: 'POST',
                body: new URLSearchParams(formData)
            });

            const data = await response.json();

            if (data.success) {
                showMessage(data.data, true);

                // ❗️❗️❗️ ASOSIY O'ZGARISH SHU YERDA ❗️❗️❗️
                setTimeout(() => {
                    // Botga oddiy link o'rniga, "/start" buyrug'ini yuboradigan link bilan yo'naltirish
                    // BU YERGA O'Z BOTINGIZNING USERNAME'INI YOZING!
                    window.location.href = `https://t.me/onlieducation_bot?start`;
                }, 2000);

            } else {
                showMessage('Xatolik: ' + data.error.message, false);
            }
        } catch (error) {
            console.error('Fetch Error:', error);
            showMessage('Server bilan bog‘lanishda kutilmagan xatolik yuz berdi.', false);
        } finally {
            button.disabled = false;
            button.textContent = form.id === 'loginForm' ? 'Kirish va Bog\'lash' : 'Ro\'yxatdan o\'tish va Bog\'lash';
        }
    }

    loginForm.addEventListener('submit', handleFormSubmit);
    registerForm.addEventListener('submit', handleFormSubmit);
});