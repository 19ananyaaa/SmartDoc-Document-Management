<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="container d-flex align-items-center justify-content-center" style="min-height: 80vh;">
    <div class="card glass-card auth-container w-100 p-4">
        <div class="text-center mb-4">
            <h2 class="auth-header">SMART DOCUMENT</h2>
            <p class="text-muted">Capture and Secure Platform</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger d-flex align-items-center" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <div>${error}</div>
            </div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-success d-flex align-items-center" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>
                <div>${success}</div>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth?action=login" method="post" class="needs-validation" novalidate>
            <!-- CSRF Token -->
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">

            <div class="mb-3">
                <label for="username" class="form-label">Username</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-person-fill"></i></span>
                    <input type="text" class="form-control" id="username" name="username" placeholder="Enter username" required autocomplete="username">
                </div>
            </div>

            <div class="mb-3">
                <div class="d-flex justify-content-between">
                    <label for="password" class="form-label">Password</label>
                    <a href="forgot_password.jsp" class="text-decoration-none small text-primary">Forgot Password?</a>
                </div>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                    <input type="password" class="form-control" id="password" name="password" placeholder="Enter password" required autocomplete="current-password">
                </div>
            </div>

            <div class="mb-3 form-check d-flex justify-content-between align-items-center">
                <div>
                    <input type="checkbox" class="form-check-input" id="rememberMe" name="rememberMe">
                    <label class="form-check-label text-muted small" for="rememberMe">Remember Me</label>
                </div>
                <!-- Theme Switcher Inside Card for Login -->
                <div class="theme-toggle-btn" id="themeToggleBtn" title="Switch Theme">
                    <i class="bi bi-moon-stars-fill text-primary"></i>
                </div>
            </div>

            <button type="submit" class="btn btn-primary-gradient w-100 mb-3">Sign In</button>
        </form>

        <div class="text-center text-muted small">
            Don't have an account? <a href="register.jsp" class="text-decoration-none text-primary fw-bold">Sign Up</a>
        </div>
    </div>
</div>

<script>
    // Client-side Validation helper
    (function () {
        'use strict'
        var forms = document.querySelectorAll('.needs-validation')
        Array.prototype.slice.call(forms)
            .forEach(function (form) {
                form.addEventListener('submit', function (event) {
                    if (!form.checkValidity()) {
                        event.preventDefault()
                        event.stopPropagation()
                    }
                    form.classList.add('was-validated')
                }, false)
            })
    })()
</script>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
