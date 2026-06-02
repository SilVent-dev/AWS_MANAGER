package com.awsmanager.controller;

import com.awsmanager.dto.ApiResponse;
import com.awsmanager.service.Ec2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para operações EC2.
 *
 * Endpoints:
 *   GET    /api/ec2/instances           - Listar instâncias
 *   GET    /api/ec2/instances/{id}      - Detalhes de instância
 *   POST   /api/ec2/instances/{id}/start    - Iniciar instância
 *   POST   /api/ec2/instances/{id}/stop     - Parar instância
 *   POST   /api/ec2/instances/{id}/reboot   - Reiniciar instância
 *   GET    /api/ec2/stats               - Estatísticas de instâncias
 *   GET    /api/ec2/logs                - Logs de operações (paginado)
 */
@RestController
@RequestMapping("/ec2")
@RequiredArgsConstructor
public class Ec2Controller {

    private final Ec2Service ec2Service;

    @GetMapping("/instances")
    public ResponseEntity<ApiResponse<?>> listInstances() {
        var instances = ec2Service.listInstances();
        return ResponseEntity.ok(ApiResponse.ok(instances));
    }

    @GetMapping("/instances/{instanceId}")
    public ResponseEntity<ApiResponse<?>> getInstanceDetails(@PathVariable String instanceId) {
        var instance = ec2Service.getInstanceDetails(instanceId);
        return ResponseEntity.ok(ApiResponse.ok(instance));
    }

    @PostMapping("/instances/{instanceId}/start")
    public ResponseEntity<ApiResponse<?>> startInstance(@PathVariable String instanceId) {
        var result = ec2Service.startInstance(instanceId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Instância iniciando..."));
    }

    @PostMapping("/instances/{instanceId}/stop")
    public ResponseEntity<ApiResponse<?>> stopInstance(@PathVariable String instanceId) {
        var result = ec2Service.stopInstance(instanceId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Instância parando..."));
    }

    @PostMapping("/instances/{instanceId}/reboot")
    public ResponseEntity<ApiResponse<?>> rebootInstance(@PathVariable String instanceId) {
        var result = ec2Service.rebootInstance(instanceId);
        return ResponseEntity.ok(ApiResponse.ok(result, "Reinicialização iniciada."));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getStats() {
        var stats = ec2Service.getStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<?>> getLogs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        var logs = ec2Service.getLogs(page, size);
        return ResponseEntity.ok(ApiResponse.ok(logs));
    }
}
