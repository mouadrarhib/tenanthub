package com.tenanthub.tenant.service;

import com.tenanthub.tenant.entity.Plan;
import com.tenanthub.tenant.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlanService {

    private final PlanRepository planRepository;

    public List<Plan> listPlans() {
        return planRepository.findAll();
    }
}
