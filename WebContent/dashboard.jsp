<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<%@ page import="model.Document" %>
<%@ page import="model.Category" %>
<%@ page import="model.Notification" %>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<%
    // Safe direct loading if not set by controller
    User user = (User) session.getAttribute("user");
    if (user != null) {
        dao.DocumentDAO docDAO = new dao.DocumentDAO();
        dao.CategoryDAO catDAO = new dao.CategoryDAO();
        dao.NotificationDAO notifDAO = new dao.NotificationDAO();
        dao.UserDAO userDAO = new dao.UserDAO();
        
        if (request.getAttribute("myDocuments") == null) {
            request.setAttribute("myDocuments", docDAO.getDocumentsByOwner(user.getId()));
        }
        if (request.getAttribute("sharedDocuments") == null) {
            request.setAttribute("sharedDocuments", docDAO.getSharedDocuments(user.getId()));
        }
        if (request.getAttribute("categories") == null) {
            request.setAttribute("categories", catDAO.getAllCategories());
        }
        if (request.getAttribute("notifications") == null) {
            request.setAttribute("notifications", notifDAO.getNotificationsByUser(user.getId()));
        }
        
        long usedStorage = userDAO.getUsedStorage(user.getId());
        request.setAttribute("usedStorage", usedStorage);
        
        // Count favorites
        int favCount = 0;
        List<Document> docList = (List<Document>) request.getAttribute("myDocuments");
        for (Document d : docList) {
            if (d.isFavorite()) favCount++;
        }
        request.setAttribute("favCount", favCount);
    }
%>

<div class="dashboard-wrapper">
    <!-- LEFT SIDEBAR -->
    <div class="sidebar">
        <div class="sidebar-logo">
            <i class="bi bi-shield-lock-fill me-2"></i>SMART DOC
        </div>
        
        <nav class="nav flex-column">
            <a class="sidebar-link active" href="dashboard.jsp">
                <i class="bi bi-grid-fill"></i>Dashboard
            </a>
            <a class="sidebar-link" href="upload.jsp">
                <i class="bi bi-cloud-arrow-up-fill"></i>Upload File
            </a>
            <a class="sidebar-link" href="document?action=search">
                <i class="bi bi-search"></i>Search Platform
            </a>
            <a class="sidebar-link" href="profile.jsp">
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
            <!-- Storage Usage Summary -->
            <div class="storage-box mb-4">
                <div class="d-flex justify-content-between mb-1 small">
                    <span>Used Storage</span>
                    <span>
                        <script>
                            // Render human readable bytes
                            function getReadableSize(bytes) {
                                if (bytes < 1024) return bytes + " B";
                                let exp = Math.floor(Math.log(bytes) / Math.log(1024));
                                let pre = "KMGTPE".charAt(exp - 1);
                                return (bytes / Math.pow(1024, exp)).toFixed(1) + " " + pre + "B";
                            }
                            document.write(getReadableSize(${usedStorage}));
                        </script>
                    </span>
                </div>
                <div class="progress mb-2" style="height: 6px;">
                    <div class="progress-bar bg-primary" role="progressbar" 
                         style="width: ${(usedStorage / sessionScope.user.storageLimit) * 100}%;" 
                         aria-valuenow="${usedStorage}" aria-valuemin="0" aria-valuemax="${sessionScope.user.storageLimit}"></div>
                </div>
                <div class="text-muted small text-center" style="font-size: 0.75rem;">
                    Limit: <script>document.write(getReadableSize(${sessionScope.user.storageLimit}));</script>
                </div>
            </div>
            
            <a href="auth?action=logout" class="sidebar-link text-danger">
                <i class="bi bi-box-arrow-left"></i>Sign Out
            </a>
        </div>
    </div>

    <!-- MAIN VIEWER CONTENT -->
    <div class="main-content">
        <!-- Dashboard Top Header Bar -->
        <header class="d-flex justify-content-between align-items-center mb-5">
            <div>
                <h1 class="h3 mb-0 fw-bold">Welcome, ${sessionScope.user.username}</h1>
                <p class="text-muted mb-0">Secure documents hub</p>
            </div>
            
            <div class="d-flex align-items-center gap-3">
                <!-- Theme Switcher -->
                <div class="theme-toggle-btn" id="themeToggleBtn" title="Switch Theme">
                    <i class="bi bi-moon-stars-fill text-primary"></i>
                </div>
                
                <!-- Notifications dropdown -->
                <div class="dropdown">
                    <button class="btn btn-light position-relative p-2 rounded-circle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <i class="bi bi-bell-fill fs-5"></i>
                        <c:if test="${not empty notifications}">
                            <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="font-size: 0.65rem;">
                                ${notifications.size()}
                            </span>
                        </c:if>
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end p-2 glass-card border-0 shadow-lg" style="width: 320px; max-height: 400px; overflow-y: auto;">
                        <li class="dropdown-header text-center fw-bold border-bottom pb-2">Notifications</li>
                        <c:if test="${empty notifications}">
                            <li class="text-muted text-center py-3">No unread alerts</li>
                        </c:if>
                        <c:forEach var="notif" items="${notifications}">
                            <li class="p-2 border-bottom">
                                <div class="small text-wrap text-break">${notif.message}</div>
                                <div class="text-muted small" style="font-size: 0.7rem;">${notif.createdAt}</div>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </header>

        <!-- Messages from actions -->
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
                        <i class="bi bi-file-earmark-code-fill fs-3"></i>
                    </div>
                    <div>
                        <h4 class="h2 mb-0 fw-bold">${myDocuments.size()}</h4>
                        <span class="text-muted small">My Documents</span>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card glass-card p-4 d-flex flex-row align-items-center gap-3">
                    <div class="bg-warning bg-opacity-10 p-3 rounded-3 text-warning">
                        <i class="bi bi-star-fill fs-3"></i>
                    </div>
                    <div>
                        <h4 class="h2 mb-0 fw-bold">${favCount}</h4>
                        <span class="text-muted small">Starred Items</span>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card glass-card p-4 d-flex flex-row align-items-center gap-3">
                    <div class="bg-success bg-opacity-10 p-3 rounded-3 text-success">
                        <i class="bi bi-people-fill fs-3"></i>
                    </div>
                    <div>
                        <h4 class="h2 mb-0 fw-bold">${sharedDocuments.size()}</h4>
                        <span class="text-muted small">Shared Files</span>
                    </div>
                </div>
            </div>
        </div>

        <!-- MY DOCUMENTS GRID / TABLE -->
        <div class="card glass-card border-0 p-4 mb-5">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h3 class="h4 mb-0 fw-bold">My Document Repositories</h3>
                <a href="upload.jsp" class="btn btn-primary-gradient"><i class="bi bi-plus-lg me-2"></i>Upload File</a>
            </div>

            <c:if test="${empty myDocuments}">
                <div class="text-center py-5 text-muted">
                    <i class="bi bi-folder2-open display-4 text-secondary mb-3 d-block"></i>
                    No documents found. Start by uploading one!
                </div>
            </c:if>

            <c:if test="${not empty myDocuments}">
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead>
                            <tr class="text-muted">
                                <th style="width: 40px;"></th>
                                <th>Name</th>
                                <th>Category</th>
                                <th>Upload Date</th>
                                <th>Size</th>
                                <th class="text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="doc" items="${myDocuments}">
                                <tr class="document-row">
                                    <!-- Favorite Star -->
                                    <td>
                                        <span class="fav-toggle-btn" data-doc-id="${doc.id}" data-is-fav="${doc.favorite}">
                                            <i class="bi ${doc.favorite ? 'bi-star-fill text-warning' : 'bi-star text-muted'}"></i>
                                        </span>
                                    </td>
                                    <!-- File Name & Icon -->
                                    <td>
                                        <div class="fw-medium text-break">
                                            <i class="bi bi-file-earmark-text-fill text-primary me-2 fs-5"></i>
                                            <a href="preview.jsp?id=${doc.id}" class="text-decoration-none text-reset fw-bold">${doc.name}</a>
                                        </div>
                                    </td>
                                    <!-- Category -->
                                    <td>
                                        <span class="badge bg-secondary-subtle text-secondary px-3 py-2 rounded-pill">
                                            <i class="bi bi-folder-fill me-1 text-warning"></i>
                                            <c:out value="${doc.categoryName != null ? doc.categoryName : 'Others'}" />
                                        </span>
                                    </td>
                                    <!-- Date -->
                                    <td class="text-muted small">${doc.uploadDate}</td>
                                    <!-- Size -->
                                    <td class="text-muted small">${doc.getFormattedFileSize()}</td>
                                    <!-- Operations Actions -->
                                    <td class="text-end">
                                        <div class="d-inline-flex gap-2">
                                            <a href="document?action=download&id=${doc.id}" class="btn btn-sm btn-outline-primary" title="Download">
                                                <i class="bi bi-download"></i>
                                            </a>
                                            <!-- Share modal trigger -->
                                            <button class="btn btn-sm btn-outline-success" title="Share" 
                                                    onclick="openShareModal(${doc.id}, '${doc.name}')">
                                                <i class="bi bi-share-fill"></i>
                                            </button>
                                            <!-- Operations Dropdown -->
                                            <div class="dropdown">
                                                <button class="btn btn-sm btn-outline-secondary p-1" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                                    <i class="bi bi-three-dots-vertical"></i>
                                                </button>
                                                <ul class="dropdown-menu dropdown-menu-end glass-card border-0 shadow">
                                                    <li>
                                                        <a class="dropdown-item" href="preview.jsp?id=${doc.id}"><i class="bi bi-eye-fill me-2"></i>Preview</a>
                                                    </li>
                                                    <li>
                                                        <button class="dropdown-item" onclick="openRenameModal(${doc.id}, '${doc.name}')">
                                                            <i class="bi bi-input-cursor-text me-2"></i>Rename
                                                        </button>
                                                    </li>
                                                    <li>
                                                        <button class="dropdown-item" onclick="openMoveModal(${doc.id}, ${doc.categoryId})">
                                                            <i class="bi bi-folder-symlink-fill me-2"></i>Move Category
                                                        </button>
                                                    </li>
                                                    <li><hr class="dropdown-divider"></li>
                                                    <li>
                                                        <form action="document?action=delete&id=${doc.id}" method="post" onsubmit="return confirm('Are you sure you want to delete this document?');">
                                                            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
                                                            <button type="submit" class="dropdown-item text-danger">
                                                                <i class="bi bi-trash-fill me-2"></i>Delete
                                                            </button>
                                                        </form>
                                                    </li>
                                                </ul>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
        </div>

        <!-- SHARED WITH ME TABLE -->
        <div class="card glass-card border-0 p-4">
            <h3 class="h4 mb-4 fw-bold">Documents Shared With Me</h3>

            <c:if test="${empty sharedDocuments}">
                <div class="text-center py-4 text-muted">
                    <i class="bi bi-people display-5 text-secondary mb-3 d-block"></i>
                    No documents shared with you yet.
                </div>
            </c:if>

            <c:if test="${not empty sharedDocuments}">
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead>
                            <tr class="text-muted">
                                <th>Name</th>
                                <th>Shared By</th>
                                <th>Size</th>
                                <th class="text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="sdoc" items="${sharedDocuments}">
                                <tr class="document-row">
                                    <td>
                                        <div class="fw-medium text-break">
                                            <i class="bi bi-file-earmark-lock-fill text-success me-2 fs-5"></i>
                                            <a href="preview.jsp?id=${sdoc.id}" class="text-decoration-none text-reset fw-bold">${sdoc.name}</a>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge bg-primary-subtle text-primary px-3 py-2 rounded-pill">
                                            <i class="bi bi-person-fill me-1"></i>${sdoc.ownerUsername}
                                        </span>
                                    </td>
                                    <td class="text-muted small">${sdoc.getFormattedFileSize()}</td>
                                    <td class="text-end">
                                        <a href="document?action=download&id=${sdoc.id}" class="btn btn-sm btn-outline-primary" title="Download">
                                            <i class="bi bi-download"></i>
                                        </a>
                                        <a href="preview.jsp?id=${sdoc.id}" class="btn btn-sm btn-outline-success" title="Preview">
                                            <i class="bi bi-eye-fill"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
        </div>
    </div>
</div>

<!-- ==========================================
     MODALS
     ========================================== -->

<!-- SHARE MODAL -->
<div class="modal fade" id="shareModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <form action="document?action=share" method="post" class="modal-content glass-card border-0 p-3">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="id" id="shareDocId">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold">Share Document</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <p class="text-muted small">Share file: <strong id="shareDocName"></strong></p>
                <div class="mb-3">
                    <label for="shareWithUsername" class="form-label">Recipient Username</label>
                    <input type="text" class="form-control" name="shareWithUsername" id="shareWithUsername" placeholder="Enter username" required>
                </div>
            </div>
            <div class="modal-footer border-0">
                <button type="button" class="btn btn-light" data-bs-dismiss="modal">Close</button>
                <button type="submit" class="btn btn-primary-gradient">Share Access</button>
            </div>
        </form>
    </div>
</div>

<!-- RENAME MODAL -->
<div class="modal fade" id="renameModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <form action="document?action=rename" method="post" class="modal-content glass-card border-0 p-3">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="id" id="renameDocId">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold">Rename Document</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3">
                    <label for="renameDocName" class="form-label">Document Name</label>
                    <input type="text" class="form-control" name="name" id="renameDocName" required>
                </div>
            </div>
            <div class="modal-footer border-0">
                <button type="button" class="btn btn-light" data-bs-dismiss="modal">Close</button>
                <button type="submit" class="btn btn-primary-gradient">Save Name</button>
            </div>
        </form>
    </div>
</div>

<!-- MOVE CATEGORY MODAL -->
<div class="modal fade" id="moveModal" tabindex="-1">
    <div class="modal-dialog modal-dialog-centered">
        <form action="document?action=move" method="post" class="modal-content glass-card border-0 p-3">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">
            <input type="hidden" name="id" id="moveDocId">
            <div class="modal-header border-0 pb-0">
                <h5 class="modal-title fw-bold">Change Folder/Category</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <div class="mb-3">
                    <label for="moveCategoryId" class="form-label">Select Category</label>
                    <select class="form-select" name="categoryId" id="moveCategoryId" required>
                        <option value="0">Others</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.id}">${cat.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="modal-footer border-0">
                <button type="button" class="btn btn-light" data-bs-dismiss="modal">Close</button>
                <button type="submit" class="btn btn-primary-gradient">Move File</button>
            </div>
        </form>
    </div>
</div>

<!-- Scripts to initialize Modals -->
<script>
    function openShareModal(id, name) {
        document.getElementById('shareDocId').value = id;
        document.getElementById('shareDocName').textContent = name;
        new bootstrap.Modal(document.getElementById('shareModal')).show();
    }
    
    function openRenameModal(id, currentName) {
        document.getElementById('renameDocId').value = id;
        document.getElementById('renameDocName').value = currentName;
        new bootstrap.Modal(document.getElementById('renameModal')).show();
    }
    
    function openMoveModal(id, currentCatId) {
        document.getElementById('moveDocId').value = id;
        document.getElementById('moveCategoryId').value = currentCatId;
        new bootstrap.Modal(document.getElementById('moveModal')).show();
    }
</script>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
