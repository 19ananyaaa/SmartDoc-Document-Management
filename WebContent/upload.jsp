<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<%@ page import="model.Category" %>
<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<%
    User user = (User) session.getAttribute("user");
    if (user != null) {
        dao.CategoryDAO catDAO = new dao.CategoryDAO();
        if (request.getAttribute("categories") == null) {
            request.setAttribute("categories", catDAO.getAllCategories());
        }
        
        long usedStorage = new dao.UserDAO().getUsedStorage(user.getId());
        request.setAttribute("usedStorage", usedStorage);
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
            <a class="sidebar-link active" href="upload.jsp">
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
            <a href="auth?action=logout" class="sidebar-link text-danger">
                <i class="bi bi-box-arrow-left"></i>Sign Out
            </a>
        </div>
    </div>

    <!-- MAIN VIEWER CONTENT -->
    <div class="main-content">
        <header class="d-flex justify-content-between align-items-center mb-5">
            <div>
                <h1 class="h3 mb-0 fw-bold">Smart Document Capture</h1>
                <p class="text-muted mb-0">Upload documents for automatic text capture and indexing</p>
            </div>
            
            <div class="theme-toggle-btn" id="themeToggleBtn" title="Switch Theme">
                <i class="bi bi-moon-stars-fill text-primary"></i>
            </div>
        </header>

        <div class="row justify-content-center">
            <div class="col-lg-8">
                
                <!-- Action notifications -->
                <%
                    String error = (String) session.getAttribute("error");
                    if (error != null) {
                        out.println("<div class='alert alert-danger alert-dismissible fade show' role='alert'>" + error + "<button type='button' class='btn-close' data-bs-dismiss='alert'></button></div>");
                        session.removeAttribute("error");
                    }
                %>

                <div class="card glass-card border-0 p-5">
                    <form action="document?action=upload" method="post" enctype="multipart/form-data" id="uploadForm" class="needs-validation" novalidate>
                        <!-- CSRF Token as query param or hidden input -->
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrf_token}">

                        <div class="mb-4">
                            <label for="categoryId" class="form-label fw-bold">1. Select Document Category</label>
                            <select class="form-select" name="categoryId" id="categoryId" required>
                                <option value="" disabled selected>Choose a category...</option>
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.id}">${cat.name}</option>
                                </c:forEach>
                            </select>
                            <div class="invalid-feedback">Please select a category folders.</div>
                            <span class="text-muted small mt-1 d-block">
                                The system uses specific category templates to optimize OCR extraction accuracy.
                            </span>
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold">2. Upload File Attachment</label>
                            
                            <!-- Dropzone UI -->
                            <div class="border border-2 border-dashed rounded-3 p-5 text-center bg-light bg-opacity-25" id="dropzone" style="cursor: pointer;">
                                <i class="bi bi-cloud-arrow-up text-primary display-3 mb-3 d-block"></i>
                                <span class="fw-bold d-block mb-1">Drag and drop file here</span>
                                <span class="text-muted small d-block mb-3">or click to browse local files</span>
                                <button type="button" class="btn btn-sm btn-outline-primary" onclick="document.getElementById('fileInput').click()">Select File</button>
                                
                                <input type="file" id="fileInput" name="file" class="d-none" required accept=".pdf,.docx,.png,.jpg,.jpeg">
                            </div>
                            <div class="invalid-feedback text-center mt-2 d-none" id="fileFeedback">File upload is required.</div>
                        </div>

                        <!-- Selected File Preview Block -->
                        <div class="card bg-light bg-opacity-25 border-0 p-3 mb-4 rounded-3 d-none" id="filePreviewBlock">
                            <div class="d-flex align-items-center justify-content-between">
                                <div class="d-flex align-items-center gap-3">
                                    <i class="bi bi-file-earmark-check-fill text-success fs-3"></i>
                                    <div>
                                        <div class="fw-bold" id="previewFileName">filename.pdf</div>
                                        <div class="text-muted small" id="previewFileSize">2.4 MB</div>
                                    </div>
                                </div>
                                <button type="button" class="btn-close" onclick="resetFile()"></button>
                            </div>
                        </div>

                        <div class="mb-4">
                            <div class="alert alert-warning py-2 mb-0 d-flex align-items-start gap-2" style="font-size: 0.85rem;">
                                <i class="bi bi-exclamation-triangle-fill mt-1 flex-shrink-0"></i>
                                <div>
                                    <strong>Rules & Validation:</strong> Max file size is <strong>10 MB</strong>. <br>
                                    Supported types: <strong>PDF, DOCX, PNG, JPG, JPEG</strong>. <br>
                                    Files containing scanned text will be processed automatically using Tess4J OCR.
                                </div>
                            </div>
                        </div>

                        <button type="submit" class="btn btn-primary-gradient w-100 p-3 fs-5" id="submitBtn">
                            <i class="bi bi-cloud-upload-fill me-2"></i>Analyze & Store Document
                        </button>
                    </form>
                </div>

            </div>
        </div>
    </div>
</div>

<script>
    const dropzone = document.getElementById('dropzone');
    const fileInput = document.getElementById('fileInput');
    const filePreviewBlock = document.getElementById('filePreviewBlock');
    const previewFileName = document.getElementById('previewFileName');
    const previewFileSize = document.getElementById('previewFileSize');
    const fileFeedback = document.getElementById('fileFeedback');
    const allowedExtensions = ['pdf', 'docx', 'png', 'jpg', 'jpeg'];
    const maxSizeBytes = 10 * 1024 * 1024; // 10MB

    // Handle clicks inside dropzone
    dropzone.addEventListener('click', () => {
        if (fileInput.files.length === 0) {
            fileInput.click();
        }
    });

    // Drag-over styling
    ['dragenter', 'dragover'].forEach(eventName => {
        dropzone.addEventListener(eventName, (e) => {
            e.preventDefault();
            dropzone.classList.add('bg-primary', 'bg-opacity-10', 'border-primary');
        }, false);
    });

    ['dragleave', 'drop'].forEach(eventName => {
        dropzone.addEventListener(eventName, (e) => {
            e.preventDefault();
            dropzone.classList.remove('bg-primary', 'bg-opacity-10', 'border-primary');
        }, false);
    });

    // Handle drop
    dropzone.addEventListener('drop', (e) => {
        const dt = e.dataTransfer;
        const files = dt.files;
        if (files.length > 0) {
            validateAndSetFile(files[0]);
        }
    });

    // Handle browse selector change
    fileInput.addEventListener('change', () => {
        if (fileInput.files.length > 0) {
            validateAndSetFile(fileInput.files[0]);
        }
    });

    function validateAndSetFile(file) {
        const name = file.name;
        const size = file.size;
        const ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();

        if (!allowedExtensions.includes(ext)) {
            alert('Invalid file format. Please upload PDF, DOCX, PNG, JPG, or JPEG.');
            resetFile();
            return;
        }

        if (size > maxSizeBytes) {
            alert('File size exceeds the 10 MB limit.');
            resetFile();
            return;
        }

        // Setup File Transfer
        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(file);
        fileInput.files = dataTransfer.files;

        // Render preview info card
        previewFileName.textContent = name;
        previewFileSize.textContent = (size / (1024 * 1024)).toFixed(2) + " MB";
        
        dropzone.classList.add('d-none');
        filePreviewBlock.classList.remove('d-none');
        fileFeedback.classList.add('d-none');
    }

    function resetFile() {
        fileInput.value = '';
        filePreviewBlock.classList.add('d-none');
        dropzone.classList.remove('d-none');
    }

    // Submit Validation Check
    document.getElementById('uploadForm').addEventListener('submit', function (event) {
        if (fileInput.files.length === 0) {
            event.preventDefault();
            event.stopPropagation();
            fileFeedback.classList.remove('d-none');
        }
        
        if (!this.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
        }
        this.classList.add('was-validated');
    });
</script>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
