<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<%@ page import="model.Document" %>
<%@ page import="model.Category" %>
<%@ page import="model.AuditLog" %>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<%
    // Safe direct loading if not set by controller
    User user = (User) session.getAttribute("user");
    if (user != null && user.isAdmin()) {
        dao.UserDAO userDAO = new dao.UserDAO();
        dao.DocumentDAO docDAO = new dao.DocumentDAO();
        dao.CategoryDAO catDAO = new dao.CategoryDAO();
        dao.AuditLogDAO auditDAO = new dao.AuditLogDAO();
        
        if (request.getAttribute("usersList") == null) {
            request.setAttribute("usersList", userDAO.getAllUsers());
        }
        if (request.getAttribute("allDocs") == null) {
            request.setAttribute("allDocs", docDAO.getAllDocuments());
        }
        if (request.getAttribute("categories") == null) {
            request.setAttribute("categories", catDAO.getAllCategories());
        }
        
        // Paginated Audit Logs fallback
        if (request.getAttribute("auditLogs") == null) {
            int pageNum = 1;
            int limit = 15;
            String pStr = request.getParameter("page");
            if (pStr != null && !pStr.isEmpty()) {
                pageNum = Integer.parseInt(pStr);
            }
            int offset = (pageNum - 1) * limit;
            request.setAttribute("auditLogs", auditDAO.getAllLogs(limit, offset));
            int totalLogs = auditDAO.getTotalLogCount();
            request.setAttribute("currentPage", pageNum);
            request.setAttribute("totalPages", (int) Math.ceil((double) totalLogs / limit));
        }

        // Calculate total storage
        long totalStorageUsed = 0;
        List<Document> allDocs = (List<Document>) request.getAttribute("allDocs");
        for (Document d : allDocs) {
            totalStorageUsed += d.getFileSize();
        }
        request.setAttribute("totalStorageUsed", totalStorageUsed);
    }
%>

<div class="dashboard-wrapper">
    <!-- LEFT SIDEBAR -->
    <div class="sidebar">
        <div class="sidebar-logo">
            <i class="bi bi-speedometer2 me-2"></i>ADMIN PORTAL
        </div>
        
        <nav class="nav flex-column">
            <a class="sidebar-link active" href="admin">
                <i class="bi bi-shield-fill-check"></i>Admin Dashboard
            </a>
            <a class="sidebar-link text-primary fw-bold" href="dashboard.jsp">
                <i class="bi bi-grid-fill"></i>User Dashboard
            </a>
            <a class="sidebar-link" href="document?action=search">
                <i class="bi bi-search"></i>Search Platform
            </a>
            <a class="sidebar-link" href="profile.jsp">
                <i class="bi bi-person-fill-gear"></i>Profile Settings
            </a>
        </nav>
        
        <div class="mt-auto">
            <a href="auth?action=logout" class="sidebar-link text-danger">
                <i class="bi bi-box-arrow-left"></i>Sign Out
            </a>
        </div>
    </div>

    <!-- MAIN VIEWER CONTENT -->
    <div class="main-content">
        <!-- Top bar -->
        <header class="d-flex justify-content-between align-items-center mb-5">
            <div>
                <h1 class="h3 mb-0 fw-bold">System Administration</h1>
                <p class="text-muted mb-0">Platform health and audit panels</p>
            </div>
            
            <div class="d-flex align-items-center gap-3">
                <div class="theme-toggle-btn" id="themeToggleBtn" title="Switch Theme">
                    <i class="bi bi-moon-stars-fill text-primary"></i>
                </div>
                <span class="badge bg-danger px-3 py-2 rounded-pill">
                    <i class="bi bi-shield-fill me-1"></i>ADMIN
                </span>
            </div>
        </header>

        <!-- Alerts -->
        <%
            String success = (String) session.getAttribute("success");
            String error = (String) session.getAttribute("error");
            if (success != null) {
                out.println("<div class='alert alert-success alert-dismissible fade show' role='alert'>" + success + "<button type='button' class='btn-close' data-bs-dismiss='alert'></button></div>");
                session.removeAttribute("success");
            }
            if (error != null) {
                out.println("<div class='alert alert-danger alert-dismissible fade show' role='alert'>" + error + "<button type='button' class='btn-close' data-bs-dismiss='alert'></button></div>");
                session.removeAttribute("error");
            }
        %>

        <!-- STAT CARDS -->
        <div class="row g-4 mb-5">
            <div class="col-md-4">
                <div class="card glass-card p-4 d-flex flex-row align-items-center gap-3">
                    <div class="bg-primary bg-opacity-10 p-3 rounded-3 text-primary">
                        <i class="bi bi-people-fill fs-3"></i>
                    </div>
                    <div>
                        <h4 class="h2 mb-0 fw-bold">${usersList.size()}</h4>
                        <span class="text-muted small">Total Members</span>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card glass-card p-4 d-flex flex-row align-items-center gap-3">
                    <div class="bg-success bg-opacity-10 p-3 rounded-3 text-success">
                        <i class="bi bi-file-earmark-pdf-fill fs-3"></i>
                    </div>
                    <div>
                        <h4 class="h2 mb-0 fw-bold">${allDocs.size()}</h4>
                        <span class="text-muted small">Total Uploads</span>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card glass-card p-4 d-flex flex-row align-items-center gap-3">
                    <div class="bg-info bg-opacity-10 p-3 rounded-3 text-info">
                        <i class="bi bi-hdd-network-fill fs-3"></i>
                    </div>
                    <div>
                        <h4 class="h3 mb-0 fw-bold">
                            <script>
                                function getReadableSize(bytes) {
                                    if (bytes < 1024) return bytes + " B";
                                    let exp = Math.floor(Math.log(bytes) / Math.log(1024));
                                    let pre = "KMGTPE".charAt(exp - 1);
                                    return (bytes / Math.pow(1024, exp)).toFixed(1) + " " + pre + "B";
                                }
                                document.write(getReadableSize(${totalStorageUsed}));
                            </script>
                        </h4>
                        <span class="text-muted small">Platform Storage Used</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- TABS NAV -->
        <ul class="nav nav-pills mb-4 gap-2" id="adminTabs" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active glass-card border-0" id="users-tab" data-bs-toggle="tab" data-bs-target="#users" type="button" role="tab">
                    <i class="bi bi-people-fill me-2"></i>Users
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link glass-card border-0" id="categories-tab" data-bs-toggle="tab" data-bs-target="#categories" type="button" role="tab">
                    <i class="bi bi-tags-fill me-2"></i>Categories
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link glass-card border-0" id="audits-tab" data-bs-toggle="tab" data-bs-target="#audits" type="button" role="tab">
                    <i class="bi bi-journal-code me-2"></i>Audit Logs
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link glass-card border-0" id="reports-tab" data-bs-toggle="tab" data-bs-target="#reports" type="button" role="tab">
                    <i class="bi bi-file-earmark-bar-graph-fill me-2"></i>Reports
                </button>
            </li>
        </ul>

        <!-- TABS CONTENT -->
        <div class="tab-content" id="adminTabsContent">
            
            <!-- 1. USERS DIRECTORY -->
            <div class="tab-pane fade show active" id="users" role="tabpanel" aria-labelledby="users-tab">
                <div class="card glass-card border-0 p-4">
                    <h3 class="h4 mb-4 fw-bold">Users Directory</h3>
                    <div class="table-responsive">
                        <table class="table align-middle">
                            <thead>
                                <tr class="text-muted">
                                    <th>ID</th>
                                    <th>Username</th>
                                    <th>Email</th>
                                    <th>Role</th>
                                    <th>Status</th>
                                    <th>Limit</th>
                                    <th class="text-end">Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="u" items="${usersList}">
                                    <tr>
                                        <td>${u.id}</td>
                                        <td class="fw-bold">${u.username}</td>
                                        <td>${u.email}</td>
                                        <td>
                                            <span class="badge ${u.role == 'ADMIN' ? 'bg-danger-subtle text-danger' : 'bg-primary-subtle text-primary'} px-3 py-2 rounded-pill">
                                                ${u.role}
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge ${u.status == 'ACTIVE' ? 'bg-success' : 'bg-warning'} px-3 py-2 rounded-pill">
                                                ${u.status}
                                            </span>
                                        </td>
                                        <td><script>document.write(getReadableSize(${u.storageLimit}));</script></td>
                                        <td class="text-end">
                                            <div class="d-inline-flex gap-2">
                                                <!-- Activate/Deactivate -->
                                                <form action="admin?action=toggleStatus&userId=${u.id}" method="post">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                                                    <button type="submit" class="btn btn-sm ${u.status == 'ACTIVE' ? 'btn-outline-warning' : 'btn-outline-success'}">
                                                        ${u.status == 'ACTIVE' ? 'Suspend' : 'Activate'}
                                                    </button>
                                                </form>
                                                <!-- Delete -->
                                                <form action="admin?action=deleteUser&userId=${u.id}" method="post" onsubmit="return confirm('Are you sure you want to permanently delete this user and all their files?');">
                                                    <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                                                    <button type="submit" class="btn btn-sm btn-outline-danger">
                                                        Delete
                                                    </button>
                                                </form>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- 2. CATEGORIES MANAGEMENT -->
            <div class="tab-pane fade" id="categories" role="tabpanel" aria-labelledby="categories-tab">
                <div class="row g-4">
                    <div class="col-md-4">
                        <div class="card glass-card border-0 p-4">
                            <h4 class="fw-bold mb-4">Add New Category</h4>
                            <form action="admin?action=addCategory" method="post" class="needs-validation" novalidate>
                                <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                                <div class="mb-3">
                                    <label for="catName" class="form-label">Category Name</label>
                                    <input type="text" class="form-control" name="name" id="catName" placeholder="e.g. Invoices" required>
                                </div>
                                <button type="submit" class="btn btn-primary-gradient w-100">Create Folder</button>
                            </form>
                        </div>
                    </div>
                    <div class="col-md-8">
                        <div class="card glass-card border-0 p-4">
                            <h4 class="fw-bold mb-4">Active Categories</h4>
                            <div class="table-responsive">
                                <table class="table align-middle">
                                    <thead>
                                        <tr class="text-muted">
                                            <th>ID</th>
                                            <th>Folder Category</th>
                                            <th class="text-end">Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="c" items="${categories}">
                                            <tr>
                                                <td>${c.id}</td>
                                                <td class="fw-bold"><i class="bi bi-folder-fill me-2 text-warning"></i>${c.name}</td>
                                                <td class="text-end">
                                                    <form action="admin?action=deleteCategory&categoryId=${c.id}" method="post" onsubmit="return confirm('Deleting this category will set associated files category to Others. Continue?');">
                                                        <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                                                        <button type="submit" class="btn btn-sm btn-outline-danger">
                                                            <i class="bi bi-trash"></i>
                                                        </button>
                                                    </form>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 3. AUDIT LOGS -->
            <div class="tab-pane fade" id="audits" role="tabpanel" aria-labelledby="audits-tab">
                <div class="card glass-card border-0 p-4">
                    <h3 class="h4 mb-4 fw-bold">Platform Activity Logs</h3>
                    <div class="table-responsive mb-3">
                        <table class="table align-middle table-sm" style="font-size: 0.9rem;">
                            <thead>
                                <tr class="text-muted">
                                    <th>Timestamp</th>
                                    <th>User</th>
                                    <th>Action</th>
                                    <th>Details</th>
                                    <th>IP Address</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="l" items="${auditLogs}">
                                    <tr>
                                        <td class="text-muted small">${l.timestamp}</td>
                                        <td class="fw-bold">${l.username}</td>
                                        <td>
                                            <span class="badge bg-secondary px-2 py-1 rounded">
                                                ${l.action}
                                            </span>
                                        </td>
                                        <td class="text-wrap" style="max-width: 300px;">${l.details}</td>
                                        <td class="text-muted small">${l.ipAddress}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    
                    <!-- Pagination -->
                    <c:if test="${totalPages > 1}">
                        <nav aria-label="Audit pagination">
                            <ul class="pagination justify-content-center">
                                <li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
                                    <a class="page-link" href="admin?page=${currentPage - 1}#audits">Previous</a>
                                </li>
                                <c:forEach var="i" begin="1" end="${totalPages}">
                                    <li class="page-item ${currentPage == i ? 'active' : ''}">
                                        <a class="page-link" href="admin?page=${i}#audits">${i}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
                                    <a class="page-link" href="admin?page=${currentPage + 1}#audits">Next</a>
                                </li>
                            </ul>
                        </nav>
                    </c:if>
                </div>
            </div>

            <!-- 4. SYSTEM REPORTS -->
            <div class="tab-pane fade" id="reports" role="tabpanel" aria-labelledby="reports-tab">
                <div class="card glass-card border-0 p-4">
                    <h3 class="h4 mb-4 fw-bold">Compliance Reports Generation</h3>
                    <p class="text-muted">Download platform snapshots for users, storage quotas, and security audit logs.</p>
                    
                    <div class="row g-4 mt-2">
                        <!-- User Report -->
                        <div class="col-md-4">
                            <div class="border rounded-3 p-3 text-center">
                                <i class="bi bi-people-fill text-primary display-4 mb-2 d-block"></i>
                                <h5 class="fw-bold">Users Directory</h5>
                                <p class="text-muted small">Registered emails, active status state, role configurations.</p>
                                <div class="d-flex justify-content-center gap-2">
                                    <a href="admin?action=report&type=user&format=pdf" class="btn btn-sm btn-primary-gradient"><i class="bi bi-file-pdf me-1"></i>PDF</a>
                                    <a href="admin?action=report&type=user&format=csv" class="btn btn-sm btn-outline-secondary"><i class="bi bi-filetype-csv me-1"></i>CSV</a>
                                </div>
                            </div>
                        </div>
                        <!-- Storage Usage -->
                        <div class="col-md-4">
                            <div class="border rounded-3 p-3 text-center">
                                <i class="bi bi-hdd-network-fill text-success display-4 mb-2 d-block"></i>
                                <h5 class="fw-bold">Storage Consumption</h5>
                                <p class="text-muted small">Disk storage volume used per account, storage percentage indices.</p>
                                <div class="d-flex justify-content-center gap-2">
                                    <a href="admin?action=report&type=storage&format=pdf" class="btn btn-sm btn-primary-gradient"><i class="bi bi-file-pdf me-1"></i>PDF</a>
                                    <a href="admin?action=report&type=storage&format=csv" class="btn btn-sm btn-outline-secondary"><i class="bi bi-filetype-csv me-1"></i>CSV</a>
                                </div>
                            </div>
                        </div>
                        <!-- Upload Activity -->
                        <div class="col-md-4">
                            <div class="border rounded-3 p-3 text-center">
                                <i class="bi bi-cloud-arrow-up-fill text-info display-4 mb-2 d-block"></i>
                                <h5 class="fw-bold">Upload Logs Summary</h5>
                                <p class="text-muted small">Global system document lists, extensions, upload metadata.</p>
                                <div class="d-flex justify-content-center gap-2">
                                    <a href="admin?action=report&type=upload&format=pdf" class="btn btn-sm btn-primary-gradient"><i class="bi bi-file-pdf me-1"></i>PDF</a>
                                    <a href="admin?action=report&type=upload&format=csv" class="btn btn-sm btn-outline-secondary"><i class="bi bi-filetype-csv me-1"></i>CSV</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            
        </div>
    </div>
</div>

<script>
    // Keep active tab on refresh matching URL hashes
    document.addEventListener("DOMContentLoaded", function() {
        var hash = window.location.hash;
        if (hash) {
            var triggerEl = document.querySelector('button[data-bs-target="' + hash + '"]');
            if (triggerEl) {
                bootstrap.Tab.getInstance(triggerEl)?.show() || new bootstrap.Tab(triggerEl).show();
            }
        }
        
        // Update hash in URL on tab switch
        var tabButtons = document.querySelectorAll('button[data-bs-toggle="tab"]');
        tabButtons.forEach(btn => {
            btn.addEventListener('shown.bs.tab', function(e) {
                window.location.hash = e.target.getAttribute("data-bs-target");
            });
        });
    });
</script>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
