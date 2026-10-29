<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="container d-flex align-items-center justify-content-center" style="min-height: 80vh;">
    <div class="card glass-card auth-container w-100 p-4">
        <div class="text-center mb-4">
            <h2 class="auth-header">CREATE ACCOUNT</h2>
            <p class="text-muted">Register to start managing your documents securely</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger d-flex align-items-center" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <div>${error}</div>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/auth?action=register" method="post" class="needs-validation" id="registerForm" novalidate>
            <!-- CSRF Token -->
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">

            <div class="mb-3">
                <label for="username" class="form-label">Username</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-person-fill"></i></span>
                    <input type="text" class="form-control" id="username" name="username" placeholder="Alphanumeric, 3-20 chars" required pattern="^[a-zA-Z0-9_]{3,20}$">
                    <div class="invalid-feedback">Username must be alphanumeric and between 3-20 characters.</div>
                </div>
            </div>

            <div class="mb-3">
                <label for="email" class="form-label">Email Address</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-envelope-fill"></i></span>
                    <input type="email" class="form-control" id="email" name="email" placeholder="name@example.com" required>
                    <div class="invalid-feedback">Please enter a valid email address.</div>
                </div>
            </div>

            <div class="mb-3">
                <label for="password" class="form-label">Password</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                    <input type="password" class="form-control" id="password" name="password" placeholder="Min 6 characters" required minlength="6">
                    <div class="invalid-feedback">Password must be at least 6 characters long.</div>
                </div>
            </div>

            <div class="mb-3">
                <label for="confirmPassword" class="form-label">Confirm Password</label>
                <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-shield-lock-fill"></i></span>
                    <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" placeholder="Re-enter password" required>
                    <div class="invalid-feedback" id="confirmPasswordFeedback">Please confirm your password.</div>
                </div>
            </div>

            <div class="mb-3 d-flex justify-content-between align-items-center">
                <div class="small text-muted">
                    By signing up, you agree to storage and security policies.
                </div>
                <!-- Theme Switcher -->
                <div class="theme-toggle-btn" id="themeToggleBtn" title="Switch Theme">
                    <i class="bi bi-moon-stars-fill text-primary"></i>
                </div>
            </div>

            <button type="submit" class="btn btn-primary-gradient w-100 mb-3">Sign Up</button>
        </form>

        <div class="text-center text-muted small">
            Already have an account? <a href="login.jsp" class="text-decoration-none text-primary fw-bold">Sign In</a>
        </div>
    </div>
</div>

<script>
    document.getElementById('registerForm').addEventListener('submit', function (event) {
        const password = document.getElementById('password').value;
        const confirm = document.getElementById('confirmPassword').value;
        const confirmFeedback = document.getElementById('confirmPasswordFeedback');
        
        if (password !== confirm) {
            event.preventDefault();
            event.stopPropagation();
            document.getElementById('confirmPassword').setCustomValidity("Passwords do not match");
            confirmFeedback.textContent = "Passwords do not match.";
        } else {
            document.getElementById('confirmPassword').setCustomValidity("");
        }

        if (!this.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
        }
        this.classList.add('was-validated');
    });
</script>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
