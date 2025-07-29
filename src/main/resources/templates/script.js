document.addEventListener('DOMContentLoaded', () => {

    // --- NAMUNAVIY MA'LUMOTLAR (BACKENDDAN KELADIGAN JSON) ---
    const sampleApiResponse = {
        "success": true,
        "data": {
            "content": [
                { "id": 129, "title": "Principal Planner", "description": "...", "slug": "principal-planner", "instructorId": 5, "categoryId": 5, "modulesCount": 5, "success": false, "reviewSummary": { "count": 4, "averageRating": 3.5 }, "createdAt": 1753646514141, "updatedAt": 1753646514141 },
                { "id": 168, "title": "Manufacturing Facilitator", "description": "...", "slug": "manufacturing-facilitator", "instructorId": 5, "categoryId": 6, "modulesCount": 5, "success": false, "reviewSummary": { "count": 2, "averageRating": 1.0 }, "createdAt": 1753646540487, "updatedAt": 1753646540487 },
                { "id": 147, "title": "Product Designer", "description": "...", "slug": "product-designer", "instructorId": 5, "categoryId": 3, "modulesCount": 5, "success": true, "reviewSummary": { "count": 0, "averageRating": 0.0 }, "createdAt": 1753646525332, "updatedAt": 1753646525332 },
                { "id": 41, "title": "Direct Sales Director", "description": "...", "slug": "direct-sales-director", "instructorId": 9, "categoryId": 1, "modulesCount": 5, "success": true, "reviewSummary": { "count": 1, "averageRating": 5.0 }, "createdAt": 1753646476300, "updatedAt": 1753646476300 },
                { "id": 30, "title": "Dynamic Mining Technician", "description": "...", "slug": "dynamic-mining-technician", "instructorId": 4, "categoryId": 7, "modulesCount": 5, "success": true, "reviewSummary": { "count": 1, "averageRating": 4.2 }, "createdAt": 1753646473398, "updatedAt": 1753646473398 }
            ],
            "pageNumber": 0, "pageSize": 5, "totalElements": 25, "totalPages": 5, "last": false, "first": true, "numberOfElements": 5, "empty": false
        }
    };

    const courseListContainer = document.getElementById('course-list');
    const paginationControls = document.getElementById('pagination-controls');

    // Asosiy funksiya: ma'lumotlarni olib, sahifani yangilaydi
    function displayCourses(apiResponse) {
        renderCourseCards(apiResponse.data.content);
        renderPagination(apiResponse.data);
    }

    // Kurs kartochkalarini chizadigan funksiya
    function renderCourseCards(courses) {
        courseListContainer.innerHTML = ''; // Avvalgi ma'lumotlarni tozalash

        if (courses.length === 0) {
            courseListContainer.innerHTML = '<p>No courses found.</p>';
            return;
        }

        courses.forEach(course => {
            const courseCard = `
                <div class="course-card">
                    <div class="card-image">
                        <img src="https://picsum.photos/400/200?random=${course.id}" alt="${course.title}">
                        <span class="category-tag">Category ${course.categoryId}</span>
                    </div>
                    <div class="card-content">
                        <div class="instructor-info">By Instructor ${course.instructorId}</div>
                        <h3><a href="/courses/${course.slug}">${course.title}</a></h3>
                        <div class="course-meta">
                            <span><i class="fas fa-book-open"></i> ${course.modulesCount} Modules</span>
                            <span class="stars" title="${course.reviewSummary.averageRating.toFixed(1)}">
                                ${generateStars(course.reviewSummary.averageRating)}
                                (${course.reviewSummary.count})
                            </span>
                        </div>
                    </div>
                    <div class="card-footer">
                        <span class="price">Free</span> 
                        <a href="/courses/${course.slug}" class="view-more-btn">View More <i class="fas fa-arrow-right"></i></a>
                    </div>
                </div>
            `;
            courseListContainer.innerHTML += courseCard;
        });
    }

    // Yulduzchalarni (reyting) generatsiya qiladigan yordamchi funksiya
    function generateStars(rating) {
        let starsHTML = '';
        const fullStars = Math.floor(rating);
        const halfStar = rating % 1 >= 0.5 ? 1 : 0;
        const emptyStars = 5 - fullStars - halfStar;

        for (let i = 0; i < fullStars; i++) starsHTML += '<i class="fas fa-star"></i>';
        if (halfStar) starsHTML += '<i class="fas fa-star-half-alt"></i>';
        for (let i = 0; i < emptyStars; i++) starsHTML += '<i class="far fa-star"></i>';

        return starsHTML;
    }

    // Paginatsiyani chizadigan funksiya
    function renderPagination(pageData) {
        paginationControls.innerHTML = '';
        const { pageNumber, totalPages, first, last } = pageData;

        // "Previous" tugmasi
        const prevButton = document.createElement('button');
        prevButton.innerHTML = '<i class="fas fa-chevron-left"></i>';
        if (first) prevButton.disabled = true;
        paginationControls.appendChild(prevButton);

        // Sahifa raqamlari
        for (let i = 0; i < totalPages; i++) {
            const pageButton = document.createElement('button');
            pageButton.innerText = i + 1;
            if (i === pageNumber) {
                pageButton.classList.add('active');
            }
            // Bu yerga event listener qo'shib, backendga so'rov jo'natish mumkin
            paginationControls.appendChild(pageButton);
        }

        // "Next" tugmasi
        const nextButton = document.createElement('button');
        nextButton.innerHTML = '<i class="fas fa-chevron-right"></i>';
        if (last) nextButton.disabled = true;
        paginationControls.appendChild(nextButton);
    }

    // Sahifa birinchi marta yuklanganda ma'lumotlarni chizish
    displayCourses(sampleApiResponse);
});