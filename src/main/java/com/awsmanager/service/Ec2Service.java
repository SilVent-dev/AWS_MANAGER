package com.awsmanager.service;

import com.awsmanager.dto.*;
import com.awsmanager.exception.Ec2OperationException;
import com.awsmanager.model.Ec2OperationLog;
import com.awsmanager.model.Ec2OperationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class Ec2Service {

    private final Ec2Client ec2Client;
    private final Ec2OperationLogRepository logRepository;

    // ======================== LISTAR INSTÂNCIAS ========================

    public List<Ec2InstanceDto> listInstances() {
        try {
            DescribeInstancesResponse response = ec2Client.describeInstances();

            return response.reservations().stream()
                .flatMap(r -> r.instances().stream())
                .filter(i -> !i.state().nameAsString().equals("terminated"))
                .map(this::toInstanceDto)
                .collect(Collectors.toList());

        } catch (Ec2Exception e) {
            log.error("Erro ao listar instâncias EC2: {}", e.getMessage());
            throw new Ec2OperationException("Erro ao listar instâncias: " + e.awsErrorDetails().errorMessage());
        }
    }

    // ======================== DETALHES DE UMA INSTÂNCIA ========================

    public Ec2InstanceDto getInstanceDetails(String instanceId) {
        try {
            DescribeInstancesResponse response = ec2Client.describeInstances(
                DescribeInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build()
            );

            return response.reservations().stream()
                .flatMap(r -> r.instances().stream())
                .findFirst()
                .map(this::toInstanceDto)
                .orElseThrow(() -> new Ec2OperationException("Instância não encontrada: " + instanceId));

        } catch (Ec2Exception e) {
            throw new Ec2OperationException("Erro ao buscar instância: " + e.awsErrorDetails().errorMessage());
        }
    }

    // ======================== INICIAR INSTÂNCIA ========================

    public Map<String, String> startInstance(String instanceId) {
        try {
            StartInstancesResponse response = ec2Client.startInstances(
                StartInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build()
            );

            String prevState = response.startingInstances().get(0).previousState().nameAsString();
            String currState = response.startingInstances().get(0).currentState().nameAsString();

            saveLog(instanceId, "START", "SUCCESS", Map.of("previousState", prevState), null);
            log.info("Instância {} iniciada: {} -> {}", instanceId, prevState, currState);

            return Map.of(
                "instanceId", instanceId,
                "previousState", prevState,
                "currentState", currState
            );

        } catch (Ec2Exception e) {
            saveLog(instanceId, "START", "FAILED", null, e.getMessage());
            throw new Ec2OperationException("Erro ao iniciar instância: " + e.awsErrorDetails().errorMessage());
        }
    }

    // ======================== PARAR INSTÂNCIA ========================

    public Map<String, String> stopInstance(String instanceId) {
        try {
            StopInstancesResponse response = ec2Client.stopInstances(
                StopInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build()
            );

            String prevState = response.stoppingInstances().get(0).previousState().nameAsString();
            String currState = response.stoppingInstances().get(0).currentState().nameAsString();

            saveLog(instanceId, "STOP", "SUCCESS", Map.of("previousState", prevState), null);
            log.info("Instância {} parada: {} -> {}", instanceId, prevState, currState);

            return Map.of(
                "instanceId", instanceId,
                "previousState", prevState,
                "currentState", currState
            );

        } catch (Ec2Exception e) {
            saveLog(instanceId, "STOP", "FAILED", null, e.getMessage());
            throw new Ec2OperationException("Erro ao parar instância: " + e.awsErrorDetails().errorMessage());
        }
    }

    // ======================== REINICIAR INSTÂNCIA ========================

    public Map<String, String> rebootInstance(String instanceId) {
        try {
            ec2Client.rebootInstances(
                RebootInstancesRequest.builder()
                    .instanceIds(instanceId)
                    .build()
            );

            saveLog(instanceId, "REBOOT", "SUCCESS", null, null);
            return Map.of("instanceId", instanceId, "status", "reboot_initiated");

        } catch (Ec2Exception e) {
            saveLog(instanceId, "REBOOT", "FAILED", null, e.getMessage());
            throw new Ec2OperationException("Erro ao reiniciar instância: " + e.awsErrorDetails().errorMessage());
        }
    }

    // ======================== ESTATÍSTICAS ========================

    public Ec2StatsDto getStats() {
        List<Ec2InstanceDto> instances = listInstances();

        long running = instances.stream().filter(i -> "running".equals(i.getStateName())).count();
        long stopped = instances.stream().filter(i -> "stopped".equals(i.getStateName())).count();
        long pending = instances.stream().filter(i -> "pending".equals(i.getStateName())).count();
        long terminated = instances.stream().filter(i -> "terminated".equals(i.getStateName())).count();

        return new Ec2StatsDto(running, stopped, pending, terminated, instances.size());
    }

    // ======================== LOGS ========================

    public PagedResponse<Ec2OperationLogDto> getLogs(int page, int size) {
        Page<Ec2OperationLog> logsPage = logRepository.findAllByOrderByExecutedAtDesc(
            PageRequest.of(page, size)
        );

        List<Ec2OperationLogDto> content = logsPage.getContent().stream()
            .map(this::toLogDto)
            .toList();

        return new PagedResponse<>(content, page, size,
            logsPage.getTotalElements(), logsPage.getTotalPages(), logsPage.isLast());
    }

    // ======================== HELPERS ========================

    private void saveLog(String instanceId, String operation, String status,
                         Map<String, Object> requestPayload, String errorMessage) {
        try {
            Ec2OperationLog log = Ec2OperationLog.builder()
                .instanceId(instanceId)
                .operation(operation)
                .status(status)
                .requestPayload(requestPayload)
                .errorMessage(errorMessage)
                .build();
            logRepository.save(log);
        } catch (Exception e) {
            Ec2Service.log.warn("Falha ao salvar log EC2: {}", e.getMessage());
        }
    }

    private Ec2InstanceDto toInstanceDto(Instance i) {
        Map<String, String> tags = i.tags().stream()
            .collect(Collectors.toMap(Tag::key, Tag::value, (a, b) -> a));

        List<String> sgNames = i.securityGroups().stream()
            .map(GroupIdentifier::groupName)
            .toList();

        return Ec2InstanceDto.builder()
            .instanceId(i.instanceId())
            .instanceType(i.instanceTypeAsString())
            .state(String.valueOf(i.state().code()))
            .stateName(i.state().nameAsString())
            .publicIpAddress(i.publicIpAddress())
            .privateIpAddress(i.privateIpAddress())
            .publicDnsName(i.publicDnsName())
            .launchTime(i.launchTime() != null ? i.launchTime().toString() : null)
            .platform(i.platformAsString())
            .architecture(i.architectureAsString())
            .securityGroups(sgNames)
            .tags(tags)
            .availabilityZone(i.placement() != null ? i.placement().availabilityZone() : null)
            .imageId(i.imageId())
            .build();
    }

    private Ec2OperationLogDto toLogDto(Ec2OperationLog l) {
        return new Ec2OperationLogDto(
            l.getId(), l.getInstanceId(), l.getOperation(),
            l.getStatus(), l.getErrorMessage(), l.getExecutedAt()
        );
    }
}
