<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="container d-flex align-items-center justify-content-center" style="min-height: 80vh;">
    <div class="card glass-card auth-container w-100 p-4">
        <div class="text-center mb-4">
            <h2 class="auth-header">PASSWORD RECOVERY</h2>
            <p class="text-muted">Recover your password via simulated secure verification</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger d-flex align-items-center" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>
                <div>${error}</div>
            </div>
        </c:if>
        <c:if test="${not empty success}">
            <div class="alert alert-info d-flex align-items-center" role="alert">
                <i class="bi bi-info-circle-fill me-2"></i>
                <div class="small">${success}</div>
            </div>
        </c:if>

        <!-- STEP 1: REQUEST OTP (Visible if 'email' is not in model request scope) -->
        <c:if test="${empty email}">
            <form action="${pageContext.request.contextPath}/auth?action=forgot_password" method="post" class="needs-validation" novalidate>
                <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                
                <div class="mb-4 text-center text-muted small">
                    Enter your registered email address. We will generate a simulated OTP code for you.
                </div>

                <div class="mb-3">
                    <label for="email" class="form-label">Email Address</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-envelope-fill"></i></span>
                        <input type="email" class="form-control" id="email" name="email" placeholder="Enter your email" required>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary-gradient w-100 mb-3">Send Reset Code</button>
            </form>
        </c:if>

        <!-- STEP 2: VERIFY OTP AND RESET PASSWORD (Visible when 'email' is populated) -->
        <c:if test="${not empty email}">
            <form action="${pageContext.request.contextPath}/auth?action=reset_password" method="post" id="resetForm" class="needs-validation" novalidate>
                <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                <input type="hidden" name="email" value="${email}">

                <div class="mb-3">
                    <label for="otp" class="form-label">Verification Code (OTP)</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-shield-check"></i></span>
                        <input type="text" class="form-control" id="otp" name="otp" placeholder="6-digit OTP code" required pattern="^[0-9]{6}$" maxlength="6">
                        <div class="invalid-feedback">Enter the 6-digit verification code.</div>
                    </div>
                </div>

                <div class="mb-3">
                    <label for="password" class="form-label">New Password</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-lock-fill"></i></span>
                        <input type="password" class="form-control" id="password" name="password" placeholder="Minimum 6 characters" required minlength="6">
                    </div>
                </div>

                <div class="mb-3">
                    <label for="confirmPassword" class="form-label">Confirm New Password</label>
                    <div class="input-group">
                        <span class="input-group-text"><i class="bi bi-shield-lock-fill"></i></span>
                        <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" placeholder="Re-enter password" required>
                        <div class="invalid-feedback" id="confirmPasswordFeedback">Passwords must match.</div>
                    </div>
                </div>

                <button type="submit" class="btn btn-accent-gradient w-100 mb-3">Reset Password</button>
            </form>
        </c:if>

        <div class="text-center text-muted small">
            Remembered your password? <a href="login.jsp" class="text-decoration-none text-primary fw-bold">Sign In</a>
        </div>
    </div>
</div>

<script>
    const resetForm = document.getElementById('resetForm');
    if (resetForm) {
        resetForm.addEventListener('submit', function (event) {
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
    }

    (function () {
        'use strict';
        var forms = document.querySelectorAll('.needs-validation');
        Array.prototype.slice.call(forms)
            .forEach(function (form) {
                form.addEventListener('submit', function (event) {
                    if (!form.checkValidity()) {
                        event.preventDefault();
                        event.stopPropagation();
                    }
                    form.classList.add('was-validated');
                }, false);
            });
    })();
</script>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
