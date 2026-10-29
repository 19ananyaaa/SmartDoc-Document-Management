<%@ include file="/WEB-INF/jsp/common/header.jspf" %>

<div class="container d-flex flex-column align-items-center justify-content-center" style="min-height: 90vh;">
    <div class="text-center p-5 glass-card border-0" style="max-width: 550px;">
        <h1 class="error-404-title mb-0">404</h1>
        <h2 class="h3 mb-3 fw-bold">Page Not Found</h2>
        <p class="text-muted mb-4">
            The resource you are looking for has been relocated, deleted, or does not exist on this platform.
        </p>
        <div class="d-flex justify-content-center gap-3">
            <a href="javascript:history.back()" class="btn btn-outline-secondary px-4 py-2">
                <i class="bi bi-arrow-left me-2"></i>Go Back
            </a>
            <a href="dashboard.jsp" class="btn btn-primary-gradient px-4 py-2">
                <i class="bi bi-house-door-fill me-2"></i>Home Dashboard
            </a>
        </div>
    </div>
</div>

<%@ include file="/WEB-INF/jsp/common/footer.jspf" %>
