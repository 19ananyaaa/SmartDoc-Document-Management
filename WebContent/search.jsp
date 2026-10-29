<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<%@ page import="model.Document" %>
<%@ page import="model.Category" %>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<%
    User user = (User) session.getAttribute("user");
    if (user != null) {
        dao.CategoryDAO catDAO = new dao.CategoryDAO();
        if (request.getAttribute("categories") == null) {
            request.setAttribute("categories", catDAO.getAllCategories());
        }
        
        // Safe direct loading if not redirected from controller search action
        if (request.getAttribute("searchResults") == null) {
            dao.DocumentDAO docDAO = new dao.DocumentDAO();
            List<Document> userDocs = docDAO.getDocumentsByOwner(user.getId());
            request.setAttribute("searchResults", userDocs);
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
            <a class="sidebar-link active" href="document?action=search">
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
            <a href="auth?action=logout" class="sidebar-link text-danger">
                <i class="bi bi-box-arrow-left"></i>Sign Out
            </a>
        </div>
    </div>

    <!-- MAIN VIEWER CONTENT -->
    <div class="main-content">
        <header class="d-flex justify-content-between align-items-center mb-5">
            <div>
                <h1 class="h3 mb-0 fw-bold">Advanced Search Hub</h1>
                <p class="text-muted mb-0">Search metadata, categories, and OCR-captured text contents</p>
            </div>
            
            <div class="theme-toggle-btn" id="themeToggleBtn" title="Switch Theme">
                <i class="bi bi-moon-stars-fill text-primary"></i>
            </div>
        </header>

        <!-- SEARCH FILTERS CARD -->
        <div class="card glass-card border-0 p-4 mb-4">
            <form action="document" method="get" class="row g-3">
                <input type="hidden" name="action" value="search">
                
                <!-- Keyword search (matches document name and OCR text) -->
                <div class="col-md-4">
                    <label for="query" class="form-label small fw-bold">Keyword / OCR Text</label>
                    <div class="input-group">
                        <span class="input-group-text bg-transparent border-end-0"><i class="bi bi-search"></i></span>
                        <input type="text" class="form-control border-start-0" id="query" name="query" 
                               placeholder="Search title or text..." value="${searchQuery}">
                    </div>
                </div>

                <!-- Category -->
                <div class="col-md-3">
                    <label for="categoryId" class="form-label small fw-bold">Category Folder</label>
                    <select class="form-select" id="categoryId" name="categoryId">
                        <option value="">All Categories</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.id}" ${selectedCategory == cat.id ? 'selected' : ''}>${cat.name}</option>
                        </c:forEach>
                    </select>
                </div>

                <!-- Upload Date -->
                <div class="col-md-3">
                    <label for="uploadDate" class="form-label small fw-bold">Upload Date</label>
                    <input type="date" class="form-control" id="uploadDate" name="uploadDate" value="${selectedDate}">
                </div>

                <!-- Owner username (Visible to admin searches only) -->
                <div class="col-md-2">
                    <c:if test="${sessionScope.user.role == 'ADMIN'}">
                        <label for="owner" class="form-label small fw-bold">Owner Username</label>
                        <input type="text" class="form-control" id="owner" name="owner" placeholder="Username" value="${selectedOwner}">
                    </c:if>
                    <c:if test="${sessionScope.user.role != 'ADMIN'}">
                        <label class="form-label d-block">&nbsp;</label>
                        <!-- Spacer for user searches to align buttons -->
                    </c:if>
                </div>

                <!-- Search Buttons -->
                <div class="col-12 d-flex justify-content-end gap-2">
                    <a href="document?action=search" class="btn btn-light"><i class="bi bi-arrow-counterclockwise me-1"></i>Reset</a>
                    <button type="submit" class="btn btn-primary-gradient"><i class="bi bi-funnel-fill me-1"></i>Filter Search</button>
                </div>
            </form>
        </div>

        <!-- SEARCH RESULTS CONTAINER -->
        <div class="card glass-card border-0 p-4">
            <h3 class="h4 mb-4 fw-bold">Matching Records (${searchResults.size()})</h3>

            <c:if test="${empty searchResults}">
                <div class="text-center py-5 text-muted">
                    <i class="bi bi-clipboard-x display-4 text-secondary mb-3 d-block"></i>
                    No documents matched your search filter parameters.
                </div>
            </c:if>

            <c:if test="${not empty searchResults}">
                <div class="table-responsive">
                    <table class="table align-middle">
                        <thead>
                            <tr class="text-muted">
                                <th>Name</th>
                                <th>Category</th>
                                <th>Upload Date</th>
                                <th>Size</th>
                                <th>Owner</th>
                                <th class="text-end">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="doc" items="${searchResults}">
                                <tr class="document-row">
                                    <td>
                                        <div class="fw-bold text-break">
                                            <i class="bi bi-file-earmark-text-fill text-primary me-2 fs-5"></i>
                                            <a href="preview.jsp?id=${doc.id}" class="text-decoration-none text-reset">${doc.name}</a>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge bg-secondary-subtle text-secondary px-3 py-2 rounded-pill">
                                            <i class="bi bi-folder-fill me-1 text-warning"></i>
                                            <c:out value="${doc.categoryName != null ? doc.categoryName : 'Others'}" />
                                        </span>
                                    </td>
                                    <td class="text-muted small">${doc.uploadDate}</td>
                                    <td class="text-muted small">${doc.getFormattedFileSize()}</td>
                                    <td>
                                        <span class="badge bg-primary-subtle text-primary px-3 py-2 rounded-pill">
                                            <i class="bi bi-person-fill me-1"></i>${doc.ownerUsername}
                                        </span>
                                    </td>
                                    <td class="text-end">
                                        <div class="d-inline-flex gap-2">
                                            <a href="document?action=download&id=${doc.id}" class="btn btn-sm btn-outline-primary" title="Download">
                                                <i class="bi bi-download"></i>
                                            </a>
                                            <a href="preview.jsp?id=${doc.id}" class="btn btn-sm btn-outline-success" title="Preview and OCR OCR text view">
                                                <i class="bi bi-eye-fill"></i>
                                            </a>
                                        </div>
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

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
