package com.zivro.service;

import com.zivro.domain.ServiceCatalog;
import com.zivro.domain.UrgencyLevel;
import com.zivro.dto.PriceQuoteResponse;
import com.zivro.dto.ServiceCreateRequest;
import com.zivro.dto.ServiceResponse;
import com.zivro.dto.ServiceUpdateRequest;
import com.zivro.exception.ConflictException;
import com.zivro.exception.ResourceNotFoundException;
import com.zivro.repository.BookingRepository;
import com.zivro.repository.ServiceCatalogRepository;
import com.zivro.util.BookingMapper;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceCatalogRepository serviceCatalogRepository;
    private final BookingRepository bookingRepository;
    private final PricingService pricingService;

    @Transactional(readOnly = true)
    public List<ServiceResponse> listAll() {
        return serviceCatalogRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(BookingMapper::toServiceResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceResponse getById(Long id) {
        return serviceCatalogRepository
                .findById(id)
                .map(BookingMapper::toServiceResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found."));
    }

    @Transactional(readOnly = true)
    public PriceQuoteResponse quote(Long serviceId, UrgencyLevel urgency) {
        ServiceCatalog service =
                serviceCatalogRepository
                        .findById(serviceId)
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found."));
        Instant when = Instant.now();
        return PriceQuoteResponse.builder()
                .serviceId(service.getId())
                .urgencyLevel(urgency)
                .quotedPrice(pricingService.quote(service, urgency, when))
                .currency("INR")
                .build();
    }

    @Transactional
    public ServiceResponse create(ServiceCreateRequest request) {
        ServiceCatalog entity =
                ServiceCatalog.builder()
                        .name(request.getName().trim())
                        .description(request.getDescription() == null ? null : request.getDescription().trim())
                        .basePrice(request.getBasePrice())
                        .build();
        entity = serviceCatalogRepository.save(entity);
        return BookingMapper.toServiceResponse(entity);
    }

    @Transactional
    public ServiceResponse update(Long id, ServiceUpdateRequest request) {
        ServiceCatalog entity =
                serviceCatalogRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found."));
        if (request.getName() != null) {
            entity.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            entity.setDescription(request.getDescription().trim());
        }
        if (request.getBasePrice() != null) {
            entity.setBasePrice(request.getBasePrice());
        }
        return BookingMapper.toServiceResponse(serviceCatalogRepository.save(entity));
    }

    @Transactional
    public void delete(Long id) {
        ServiceCatalog entity =
                serviceCatalogRepository
                        .findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Service not found."));
        if (bookingRepository.countByService_Id(id) > 0) {
            throw new ConflictException("Cannot delete a service that has existing bookings.");
        }
        serviceCatalogRepository.delete(entity);
    }
}
