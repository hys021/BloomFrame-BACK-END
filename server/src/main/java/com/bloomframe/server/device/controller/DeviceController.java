package com.bloomframe.server.device.controller;

import com.bloomframe.server.common.security.CustomUserPrincipal;
import com.bloomframe.server.device.dto.request.DeviceRegisterRequest;
import com.bloomframe.server.device.dto.request.DeviceRenameRequest;
import com.bloomframe.server.device.dto.response.DeviceResponse;
import com.bloomframe.server.device.service.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    public ResponseEntity<DeviceResponse> register(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                     @Valid @RequestBody DeviceRegisterRequest request) {
        DeviceResponse response = deviceService.register(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<DeviceResponse> getDevices(@AuthenticationPrincipal CustomUserPrincipal principal) {
        return deviceService.getDevices(principal.getUserId());
    }

    @PatchMapping("/{deviceUuid}")
    public DeviceResponse rename(@AuthenticationPrincipal CustomUserPrincipal principal,
                                  @PathVariable String deviceUuid,
                                  @Valid @RequestBody DeviceRenameRequest request) {
        return deviceService.rename(principal.getUserId(), deviceUuid, request);
    }

    @PatchMapping("/{deviceUuid}/connect")
    public DeviceResponse connect(@AuthenticationPrincipal CustomUserPrincipal principal,
                                   @PathVariable String deviceUuid) {
        return deviceService.connect(principal.getUserId(), deviceUuid);
    }

    // 명세엔 없었지만, 삭제 전 연결 해제를 강제하는 정책을 위해 추가한 엔드포인트
    @PatchMapping("/{deviceUuid}/disconnect")
    public DeviceResponse disconnect(@AuthenticationPrincipal CustomUserPrincipal principal,
                                      @PathVariable String deviceUuid) {
        return deviceService.disconnect(principal.getUserId(), deviceUuid);
    }

    @DeleteMapping("/{deviceUuid}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal CustomUserPrincipal principal,
                                        @PathVariable String deviceUuid) {
        deviceService.delete(principal.getUserId(), deviceUuid);
        return ResponseEntity.noContent().build();
    }
}
