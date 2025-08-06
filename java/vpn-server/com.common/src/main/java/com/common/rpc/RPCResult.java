package com.common.rpc;

import com.common.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.List;

public class RPCResult<T> implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(RPCResult.class);
    private static final long serialVersionUID = 6028636097083630372L;
    private Integer totalCount;
    private Integer totalPage;
    private Integer pageSize;
    private Integer pageIndex;
    private Boolean success = false;
    private String resultCode;
    private String message;
    private Integer code;
    private String[] resultCodeParams;
    private T data;

    public T getData() {
        return this.data;
    }

    public void setData(T data) {
        this.setSuccess(true);
        if (this.data instanceof Page) {
            Page page = (Page) data;
            this.setTotalPage(page.getTotalPages());
            this.setPageSize(page.getSize());
            this.setTotalCount((int) page.getTotalElements());
            this.setPageIndex(page.getNumber());
            this.setData((T) page.getContent());
        } else {
            this.data = data;
        }
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public RPCResult() {
    }

    public Page<T> toPage() {
        Pageable pageable = null;
        List<T> list = (List) this.data;
        if (this.totalPage > 1) {
            pageable = PageRequest.of(this.pageIndex, this.pageSize);
            return new PageImpl(list, pageable, (long) this.getTotalCount());
        } else {
            return new PageImpl(list);
        }
    }

    public RPCResult(T data) {
        this.setSuccess(true);
        this.setData(data);
    }

    public RPCResult(Exception e) {
        if (e instanceof BizException) {
            this.setException((BizException) e);
        } else {
            log.error("unKnow.error", e);
            this.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            this.setMessage("执行业务失败");
        }

        this.setSuccess(false);
    }

    public RPCResult(Page page) {
        this.setTotalPage(page.getTotalPages());
        this.setPageSize(page.getSize());
        this.setTotalCount((int) page.getTotalElements());
        this.setPageIndex(page.getNumber());
        this.setData((T) page.getContent());
        this.setSuccess(true);
    }

    public RPCResult(BizException exception) {
        this.message = exception.getMessage();
        this.code = exception.getCode();
        this.setSuccess(false);
    }

    public void setException(BizException exception) {
        this.message = exception.getMessage();
        this.code = exception.getCode();
        this.setSuccess(false);
    }

    public Integer getPageIndex() {
        return this.pageIndex;
    }

    public void setPageIndex(Integer pageIndex) {
        this.pageIndex = pageIndex;
    }

    public Integer getTotalCount() {
        return this.totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public Integer getTotalPage() {
        return this.totalPage;
    }

    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }

    public Integer getCode() {
        return this.code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public Boolean isSuccess() {
        return this.success;
    }

    public String getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public void setResultCode(String resultCode, String... args) {
        this.resultCode = resultCode;
        this.resultCodeParams = args;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String[] getResultCodeParams() {
        return this.resultCodeParams;
    }

    public void setResultCodeParams(String[] resultCodeParams) {
        this.resultCodeParams = resultCodeParams;
    }
}