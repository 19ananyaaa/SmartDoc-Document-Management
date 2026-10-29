<%@ page import="model.User" %>
<%@ page import="model.Document" %>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<%
    User user = (User) session.getAttribute("user");
    Document doc = null;
    
    if (user != null) {
        String idStr = request.getParameter("id");
        if (idStr != null && !idStr.isEmpty()) {
            int docId = Integer.parseInt(idStr);
            dao.DocumentDAO docDAO = new dao.DocumentDAO();
            doc = docDAO.getDocumentById(docId);
            
            if (doc != null) {
                // Security Check: Enforce user download/read boundaries
                boolean isOwner = doc.getOwnerId() == user.getId();
                boolean isShared = docDAO.isDocumentSharedWithUser(docId, user.getId());
                
                if (!isOwner && !isShared && !user.isAdmin()) {
                    session.setAttribute("error", "Access Denied: Unauthorised access to preview target file.");
                    response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
                    return;
                }
                request.setAttribute("document", doc);
            } else {
                session.setAttribute("error", "Document not found.");
                response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
                return;
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard.jsp");
            return;
        }
    } else {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
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

    <!-- MAIN VIEWPORT -->
    <div class="main-content">
        <header class="d-flex justify-content-between align-items-center mb-4">
            <div class="d-flex align-items-center gap-3">
                <a href="javascript:history.back()" class="btn btn-light rounded-circle p-2" title="Go Back">
                    <i class="bi bi-arrow-left fs-5"></i>
                </a>
                <div>
                    <h1 class="h3 mb-0 fw-bold">Document Inspection</h1>
                    <p class="text-muted mb-0">${document.name}</p>
                </div>
            </div>
            
            <div class="d-flex gap-2">
                <div class="theme-toggle-btn me-2" id="themeToggleBtn" title="Switch Theme">
                    <i class="bi bi-moon-stars-fill text-primary"></i>
                </div>
                <a href="document?action=download&id=${document.id}" class="btn btn-primary-gradient">
                    <i class="bi bi-download me-2"></i>Download Original File
                </a>
            </div>
        </header>

        <!-- SPLIT LAYOUT PANEL -->
        <div class="row g-4" style="min-height: calc(100vh - 180px);">
            
            <!-- LEFT COLUMN: FILE PREVIEW DISPLAY -->
            <div class="col-lg-6">
                <div class="card glass-card border-0 p-3 h-100 d-flex flex-column">
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <span class="fw-bold"><i class="bi bi-file-earmark-image me-2"></i>Document Render Screen</span>
                        <span class="badge bg-secondary px-3 py-1 rounded-pill">${document.fileExtension.toUpperCase()}</span>
                    </div>

                    <div class="preview-viewer flex-grow-1 border rounded-3 p-2 bg-dark bg-opacity-25 d-flex align-items-center justify-content-center text-center overflow-auto" style="min-height: 400px;">
                        <c:choose>
                            <c:when test="${document.fileExtension == 'png' || document.fileExtension == 'jpg' || document.fileExtension == 'jpeg'}">
                                <img src="document?action=download&id=${document.id}" alt="Document Preview" class="img-fluid rounded shadow-sm" style="max-height: 500px; object-fit: contain;">
                            </c:when>
                            <c:when test="${document.fileExtension == 'pdf'}">
                                <iframe src="document?action=download&id=${document.id}#toolbar=0" class="w-100 h-100 rounded" style="border: none; min-height: 500px;"></iframe>
                            </c:when>
                            <c:otherwise>
                                <div class="text-muted p-4">
                                    <i class="bi bi-file-earmark-zip display-3 text-secondary mb-3 d-block"></i>
                                    <h5>Preview not supported for ${document.fileExtension.toUpperCase()} format.</h5>
                                    <p class="small text-muted mb-3">Use the download link to inspect the document locally.</p>
                                    <a href="document?action=download&id=${document.id}" class="btn btn-sm btn-outline-primary"><i class="bi bi-download me-1"></i>Download Now</a>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

            <!-- RIGHT COLUMN: METADATA & OCR TEXT -->
            <div class="col-lg-6">
                <div class="card glass-card border-0 p-4 h-100 d-flex flex-column">
                    
                    <!-- Metadata Box -->
                    <div class="mb-4">
                        <h4 class="fw-bold mb-3"><i class="bi bi-info-circle me-2 text-primary"></i>Metadata Details</h4>
                        <div class="row g-2 border rounded-3 p-3 bg-light bg-opacity-25">
                            <div class="col-6">
                                <span class="text-muted small d-block">Owner</span>
                                <span class="fw-medium">${document.ownerUsername}</span>
                            </div>
                            <div class="col-6">
                                <span class="text-muted small d-block">Folder Category</span>
                                <span class="fw-medium text-warning"><i class="bi bi-folder-fill me-1"></i>${document.categoryName != null ? document.categoryName : 'Others'}</span>
                            </div>
                            <div class="col-6 mt-2">
                                <span class="text-muted small d-block">Uploaded On</span>
                                <span class="fw-medium">${document.uploadDate}</span>
                            </div>
                            <div class="col-6 mt-2">
                                <span class="text-muted small d-block">File Size</span>
                                <span class="fw-medium">${document.getFormattedFileSize()}</span>
                            </div>
                        </div>
                    </div>

                    <!-- OCR text container -->
                    <div class="flex-grow-1 d-flex flex-column">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h4 class="fw-bold mb-0"><i class="bi bi-cpu me-2 text-success"></i>Captured Text (OCR)</h4>
                            <button class="btn btn-sm btn-outline-secondary" onclick="copyOcrText()">
                                <i class="bi bi-clipboard me-1"></i>Copy Text
                            </button>
                        </div>
                        
                        <div class="flex-grow-1 border rounded-3 p-3 bg-light bg-opacity-25 overflow-auto text-break text-wrap" 
                             style="max-height: 320px; font-family: monospace; font-size: 0.85rem;" id="ocrTextContainer">
                            <c:choose>
                                <c:when test="${not empty document.ocrText}">
                                    <c:out value="${document.ocrText}" escapeXml="true" />
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted italic">No captured text extracted or document has not been scanned.</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                </div>
            </div>

        </div>
    </div>
</div>

<script>
    function copyOcrText() {
        const text = document.getElementById('ocrTextContainer').innerText;
        navigator.clipboard.writeText(text).then(() => {
            alert('OCR text copied to clipboard!');
        }).catch(err => {
            console.error('Failed to copy text: ', err);
        });
    }
</script>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
