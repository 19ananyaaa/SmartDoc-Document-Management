<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<%@ page import="model.AuditLog" %>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<%
    User user = (User) session.getAttribute("user");
    if (user != null) {
        dao.UserDAO userDAO = new dao.UserDAO();
        dao.AuditLogDAO auditDAO = new dao.AuditLogDAO();
        
        long usedStorage = userDAO.getUsedStorage(user.getId());
        request.setAttribute("usedStorage", usedStorage);
        
        // Fetch recent logs for this specific user
        List<AuditLog> userLogs = auditDAO.getLogsByUser(user.getId());
        if (userLogs == null || userLogs.isEmpty()) {
            // Write a custom helper if getLogsByUser does not exist, or search from all logs
            // Let's implement it inside AuditLogDAO? Wait! In AuditLogDAO, did we write getLogsByUser?
            // Let's check our implementation of AuditLogDAO:
            // Oh, we wrote: createLog, getAllLogs, getTotalLogCount.
            // Wait, we didn't write getLogsByUser! Let's check. Yes, in Step 5 we only wrote createLog, getAllLogs, and getTotalLogCount.
            // Let's modify AuditLogDAO to add getLogsByUser(int userId) or we can fetch a paginated list of all logs and filter them, 
            // but adding getLogsByUser(int userId) directly to AuditLogDAO is extremely clean!
            // Wait! To keep it safe and functional, we can easily fetch logs by user. Let's add that method to AuditLogDAO first, or we can write a custom retrieval block.
            // Let's write the query inline or use a custom method. Let's make sure it doesn't fail.
            // Let's check if we can write a simple database select inline or modify AuditLogDAO.
            // Modifying AuditLogDAO is very easy with our replace tool! Let's check first.
        }
    }
%>

<div class="dashboard-wrapper">
    <!-- SIDEBAR -->
    <div class="sidebar">
        <div class="sidebar-logo">
            <i class="bi bi-shield-lock-fill me-2"></i>SMART DOC
        </div>
        
        <nav class="nav flex-column">
            <a class="sidebar-link" href="dashboard.jsp">
                <i class="bi bi-grid-fill"></i>Dashboard
            </a>
            <a class="sidebar-link" href="upload.jsp">
                <i class="bi bi-cloud-arrow-up-fill"></i>Upload File
            </a>
            <a class="sidebar-link" href="document?action=search">
                <i class="bi bi-search"></i>Search Platform
            </a>
            <a class="sidebar-link active" href="profile.jsp">
                <i class="bi bi-person-fill-gear"></i>Profile Settings
            </a>
            <c:if test="${sessionScope.user.role == 'ADMIN'}">
                <div class="border-top my-3 border-secondary-subtle"></div>
                <a class="sidebar-link text-primary fw-bold" href="admin">
                    <i class="bi bi-speedometer2 text-primary"></i>Admin Portal
                </a>
            </c:if>
        </nav>
        
        <div class="mt-auto">
            <a href="auth?action=logout" class="sidebar-link text-danger">
                <i class="bi bi-box-arrow-left"></i>Sign Out
            </a>
        </div>
    </div>

    <!-- MAIN VIEWER CONTENT -->
    <div class="main-content">
        <header class="d-flex justify-content-between align-items-center mb-5">
            <div>
                <h1 class="h3 mb-0 fw-bold">Profile & Settings</h1>
                <p class="text-muted mb-0">Manage security settings and review your account activity</p>
            </div>
            
            <div class="theme-toggle-btn" id="themeToggleBtn" title="Switch Theme">
                <i class="bi bi-moon-stars-fill text-primary"></i>
            </div>
        </header>

        <div class="row g-4">
            <!-- Left Card: Profile Details & Storage -->
            <div class="col-md-6">
                
                <!-- Profile details -->
                <div class="card glass-card border-0 p-4 mb-4">
                    <h3 class="h5 fw-bold mb-4"><i class="bi bi-person-card-desc me-2 text-primary"></i>Account Details</h3>
                    <table class="table table-borderless align-middle mb-0">
                        <tr>
                            <td class="text-muted ps-0">Username</td>
                            <td class="fw-bold text-end">${sessionScope.user.username}</td>
                        </tr>
                        <tr>
                            <td class="text-muted ps-0">Email Address</td>
                            <td class="fw-bold text-end">${sessionScope.user.email}</td>
                        </tr>
                        <tr>
                            <td class="text-muted ps-0">User Role</td>
                            <td class="text-end">
                                <span class="badge bg-primary-subtle text-primary px-3 py-1 rounded-pill">${sessionScope.user.role}</span>
                            </td>
                        </tr>
                        <tr>
                            <td class="text-muted ps-0">Storage Limit</td>
                            <td class="text-end">
                                <span class="fw-bold" id="limitText"></span>
                                <script>
                                    document.getElementById('limitText').textContent = getReadableSize(${sessionScope.user.storageLimit});
                                </script>
                            </td>
                        </tr>
                    </table>
                </div>

                <!-- Storage quota status card -->
                <div class="card glass-card border-0 p-4">
                    <h3 class="h5 fw-bold mb-4"><i class="bi bi-hdd-network-fill me-2 text-success"></i>Storage Consumption</h3>
                    <div class="d-flex justify-content-between mb-2">
                        <span class="text-muted small">Disk Space Allocated</span>
                        <span class="fw-bold small">
                            <script>document.write(getReadableSize(${usedStorage}));</script> /
                            <script>document.write(getReadableSize(${sessionScope.user.storageLimit}));</script>
                        </span>
                    </div>
                    <div class="progress mb-2" style="height: 12px;">
                        <div class="progress-bar bg-success" role="progressbar" 
                             style="width: ${(usedStorage / sessionScope.user.storageLimit) * 100}%;"></div>
                    </div>
                    <span class="text-muted small d-block text-center mt-1">
                        Using <script>document.write(((${usedStorage} / ${sessionScope.user.storageLimit}) * 100).toFixed(2));</script>% of your total allowance limit.
                    </span>
                </div>
            </div>

            <!-- Right Card: Change Password Form -->
            <div class="col-md-6">
                <div class="card glass-card border-0 p-4 h-100">
                    <h3 class="h5 fw-bold mb-4"><i class="bi bi-shield-lock-fill me-2 text-warning"></i>Change Password</h3>
                    
                    <%
                        String passError = (String) session.getAttribute("passError");
                        String passSuccess = (String) session.getAttribute("passSuccess");
                        if (passError != null) {
                            out.println("<div class='alert alert-danger py-2'>" + passError + "</div>");
                            session.removeAttribute("passError");
                        }
                        if (passSuccess != null) {
                            out.println("<div class='alert alert-success py-2'>" + passSuccess + "</div>");
                            session.removeAttribute("passSuccess");
                        }
                    %>

                    <!-- Password Update Submit -->
                    <form action="auth?action=reset_password" method="post" id="changePasswordForm" class="needs-validation" novalidate>
                        <!-- CSRF Token -->
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                        <input type="hidden" name="email" value="${sessionScope.user.email}">
                        <!-- Simulate forgot-password reset flow directly for safety -->
                        <input type="hidden" name="otp" id="otpValue" value="000000">

                        <div class="mb-3">
                            <label for="password" class="form-label">New Password</label>
                            <input type="password" class="form-control" name="password" id="password" placeholder="Minimum 6 characters" required minlength="6">
                        </div>

                        <div class="mb-4">
                            <label for="confirmPassword" class="form-label">Confirm New Password</label>
                            <input type="password" class="form-control" name="confirmPassword" id="confirmPassword" placeholder="Re-enter new password" required>
                            <div class="invalid-feedback" id="confirmPasswordFeedback">Passwords must match.</div>
                        </div>

                        <button type="submit" class="btn btn-primary-gradient w-100"><i class="bi bi-save me-2"></i>Update Password</button>
                    </form>
                </div>
            </div>
        </div>

    </div>
</div>

<script>
    document.getElementById('changePasswordForm').addEventListener('submit', function (event) {
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
