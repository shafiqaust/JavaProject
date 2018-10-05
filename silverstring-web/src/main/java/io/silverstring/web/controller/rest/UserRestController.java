package io.silverstring.web.controller.rest;

import io.silverstring.core.service.ActionLogService;
import io.silverstring.core.service.UserService;
import io.silverstring.domain.dto.Response;
import io.silverstring.domain.dto.UserDTO;
import io.silverstring.domain.enums.CodeEnum;
import io.silverstring.domain.hibernate.User;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserRestController {
    private UserService userService;
    private ActionLogService actionLogService;
    private ModelMapper modelMapper;

    @Autowired
    public UserRestController(UserService userService, ActionLogService actionLogService, ModelMapper modelMapper) {
        this.userService = userService;
        this.actionLogService = actionLogService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/changePassword")
    public Response<UserDTO.ResChangePassword> changePassword(@ModelAttribute User user, @Valid @RequestBody UserDTO.ReqChangePassword request) {
        return Response.<UserDTO.ResChangePassword>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(userService.changePassword(user, request))
                .build();
    }

    @PostMapping("/releaseMember")
    public Response<UserDTO.ResReleaseMember> releaseMember(@ModelAttribute User user, @Valid @RequestBody UserDTO.ReqReleaseMember request) {
        return Response.<UserDTO.ResReleaseMember>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(userService.releaseMember(user, request))
                .build();
    }

    @PostMapping("/getActionLogs")
    public Response<UserDTO.ResActionLogs> getActionLogs(@ModelAttribute User user, @RequestBody UserDTO.ReqActionLogs request) {
        return Response.<UserDTO.ResActionLogs>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(modelMapper.map(actionLogService.getActionLogs(user.getId(), request), UserDTO.ResActionLogs.class))
                .build();
    }

    @PostMapping("/releaseOtp")
    public Response<UserDTO.ResChangeOtpKey> releaseOtp(@ModelAttribute User user, @Valid @RequestBody UserDTO.ReqChangeOtpKey request) {
        return Response.<UserDTO.ResChangeOtpKey>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(userService.releaseOtp(user, request))
                .build();
    }

    @PostMapping("/confirmOtp")
    public Response<UserDTO.ResChangeOtpKey> confirmOtp(@ModelAttribute User user, @Valid @RequestBody UserDTO.ReqChangeOtpKey request) {
        return Response.<UserDTO.ResChangeOtpKey>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(userService.confirmOtp(user, request))
                .build();
    }

    @PostMapping("/changeOtpKey")
    public Response<UserDTO.ResChangeOtpKey> changeOtpKey(@ModelAttribute User user, @Valid @RequestBody UserDTO.ReqChangeOtpKey request) {
        return Response.<UserDTO.ResChangeOtpKey>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(userService.changeOtpKey(user, request))
                .build();
    }

    @PostMapping("/getOtpStatus")
    public Response<UserDTO.ResGetOtpStatus> getOtpStatus(@ModelAttribute User user, @Valid @RequestBody UserDTO.ReqGetOtpStatus request) {
        return Response.<UserDTO.ResGetOtpStatus>builder()
                .code(CodeEnum.SUCCESS.getCode())
                .msg(CodeEnum.SUCCESS.getMessage())
                .data(userService.getOtpStatus(user, request))
                .build();
    }
}
