package com.example.demo.controller.branch;

import com.example.demo.domain.dto.req.CreateBranchReq;
import com.example.demo.domain.dto.req.UpdateBranchReq;
import com.example.demo.domain.dto.res.BranchResponse;
import com.example.demo.service.branchService.IBranchService;
import com.example.demo.share.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/branches")
public class BranchController {

    private final IBranchService branchService;
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<BranchResponse> createBranch(
            @Valid @RequestBody CreateBranchReq req,
            HttpServletRequest request
    ) {
        var created = branchService.createBranch(req);
        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "BRANCH.CREATE_SUCCESS",
                "Create branch successfully",
                created,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<BranchResponse>> getAllBranches(HttpServletRequest request) {
        var list = branchService.getAllBranches();
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "BRANCH.GET_ALL_SUCCESS",
                "Get all branches successfully",
                list,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ApiResponse<List<BranchResponse>> search(@RequestParam(required = false) String q, HttpServletRequest request) {
        var list = branchService.searchBranches(q);
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "BRANCH.SEARCH_SUCCESS",
                "Search branches successfully",
                list,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResponse<BranchResponse> getById(@PathVariable String id, HttpServletRequest request) {
        var opt = branchService.getBranchById(id);
        if (opt.isEmpty()) {
            return ApiResponse.success(
                    HttpStatus.NOT_FOUND.value(),
                    "BRANCH.NOT_FOUND",
                    "Branch not found",
                    null,
                    request.getRequestURI(),
                    MDC.get("traceId")
            );
        }
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "BRANCH.GET_SUCCESS",
                "Get branch successfully",
                opt.get(),
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<BranchResponse> update(@PathVariable String id, @RequestBody UpdateBranchReq req, HttpServletRequest request) {
        var updated = branchService.updateBranch(id, req);
        if (updated == null) {
            return ApiResponse.success(
                    HttpStatus.NOT_FOUND.value(),
                    "BRANCH.NOT_FOUND",
                    "Branch not found",
                    null,
                    request.getRequestURI(),
                    MDC.get("traceId")
            );
        }
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "BRANCH.UPDATE_SUCCESS",
                "Update branch successfully",
                updated,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id, HttpServletRequest request) {
        branchService.deleteBranch(id);
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "BRANCH.DELETE_SUCCESS",
                "Delete branch successfully",
                null,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

}
