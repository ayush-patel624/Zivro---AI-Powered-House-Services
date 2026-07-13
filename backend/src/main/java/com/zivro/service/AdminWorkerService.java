package com.zivro.service;

import com.zivro.domain.Worker;
import com.zivro.dto.AdminWorkerRowResponse;
import com.zivro.exception.ResourceNotFoundException;
import com.zivro.repository.WorkerRepository;
import com.zivro.util.WorkerAdminMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminWorkerService {

    private final WorkerRepository workerRepository;

    @Transactional(readOnly = true)
    public List<AdminWorkerRowResponse> listWorkers() {
        return workerRepository.findAllByOrderByIdAsc().stream().map(WorkerAdminMapper::toAdminRow).toList();
    }

    @Transactional
    public void setVerified(Long workerId, boolean verified) {
        Worker w = workerRepository.findById(workerId).orElseThrow(() -> new ResourceNotFoundException("Worker not found."));
        w.setVerified(verified);
        workerRepository.save(w);
    }
}
