package com.zivro.service;

import com.zivro.domain.Booking;
import com.zivro.domain.Worker;
import com.zivro.dto.NearbyWorkerResponse;
import com.zivro.exception.BadRequestException;
import com.zivro.exception.ResourceNotFoundException;
import com.zivro.repository.BookingRepository;
import com.zivro.repository.WorkerRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NearbyWorkerService {

    private static final String[] SYNTHETIC_NAMES = {
        "Arjun M.", "Priya S.", "Rahul K.", "Meera D.", "Vikram P.", "Ananya R.", "Karan T.", "Sneha L."
    };
    private static final String[] SYNTHETIC_CATEGORIES = {
        "Home cleaning", "Kitchen help", "Appliance care", "Deep cleaning", "Home keeping"
    };

    private final BookingRepository bookingRepository;
    private final WorkerRepository workerRepository;

    @Transactional(readOnly = true)
    public List<NearbyWorkerResponse> nearbyForBooking(Long bookingId) {
        Booking booking =
                bookingRepository
                        .findDetailById(bookingId)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found."));
        if (booking.getLatitude() == null || booking.getLongitude() == null) {
            throw new BadRequestException("Booking has no service location.");
        }
        double lat = booking.getLatitude().doubleValue();
        double lng = booking.getLongitude().doubleValue();
        long seed = bookingId * 31L + System.currentTimeMillis() / 4000L;
        Random random = new Random(seed);

        List<Worker> verified = workerRepository.findByVerifiedTrueOrderByRatingDesc();
        List<NearbyWorkerResponse> results = new ArrayList<>();
        for (Worker worker : verified) {
            if (booking.getWorker() != null && worker.getId().equals(booking.getWorker().getId())) {
                continue;
            }
            double[] coords = resolveWorkerCoords(worker, lat, lng, random);
            double distance = haversineKm(lat, lng, coords[0], coords[1]);
            results.add(
                    NearbyWorkerResponse.builder()
                            .workerId(worker.getId())
                            .employeeId(worker.getEmployeeId())
                            .name(worker.getUser().getName())
                            .category(worker.getCategory() != null ? worker.getCategory() : "General")
                            .rating(worker.getRating())
                            .verified(worker.isVerified())
                            .distanceKm(round(distance, 1))
                            .etaMinutes(etaMinutes(distance, random))
                            .latitude(coords[0])
                            .longitude(coords[1])
                            .build());
        }

        if (results.size() < 4) {
            results.addAll(syntheticNearby(lat, lng, random, 6 - results.size()));
        }

        results.sort(Comparator.comparingDouble(NearbyWorkerResponse::getDistanceKm));
        if (results.size() > 6) {
            results = new ArrayList<>(results.subList(0, 6));
        }
        java.util.Collections.shuffle(results, random);
        return results;
    }

    private List<NearbyWorkerResponse> syntheticNearby(double lat, double lng, Random random, int count) {
        List<NearbyWorkerResponse> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double[] coords = offset(lat, lng, 0.4 + random.nextDouble() * 3.5, random);
            double distance = haversineKm(lat, lng, coords[0], coords[1]);
            list.add(
                    NearbyWorkerResponse.builder()
                            .workerId(-1000L - i)
                            .employeeId("ZIV-N" + (100 + i))
                            .name(SYNTHETIC_NAMES[random.nextInt(SYNTHETIC_NAMES.length)])
                            .category(SYNTHETIC_CATEGORIES[random.nextInt(SYNTHETIC_CATEGORIES.length)])
                            .rating(BigDecimal.valueOf(3.8 + random.nextDouble() * 1.1).setScale(1, RoundingMode.HALF_UP))
                            .verified(true)
                            .distanceKm(round(distance, 1))
                            .etaMinutes(etaMinutes(distance, random))
                            .latitude(coords[0])
                            .longitude(coords[1])
                            .build());
        }
        return list;
    }

    private double[] resolveWorkerCoords(Worker worker, double bookingLat, double bookingLng, Random random) {
        if (worker.getLatitude() != null && worker.getLongitude() != null) {
            return new double[] {worker.getLatitude().doubleValue(), worker.getLongitude().doubleValue()};
        }
        return offset(bookingLat, bookingLng, 0.8 + (worker.getId() % 5) * 0.6, random);
    }

    private double[] offset(double lat, double lng, double km, Random random) {
        double bearing = random.nextDouble() * 2 * Math.PI;
        double dx = km * Math.cos(bearing);
        double dy = km * Math.sin(bearing);
        double newLat = lat + (dy / 111.0);
        double newLng = lng + (dx / (111.0 * Math.cos(Math.toRadians(lat))));
        return new double[] {newLat, newLng};
    }

    private int etaMinutes(double distanceKm, Random random) {
        return (int) Math.max(3, Math.round(distanceKm * 4.5 + 2 + random.nextInt(3)));
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a =
                Math.sin(dLat / 2) * Math.sin(dLat / 2)
                        + Math.cos(Math.toRadians(lat1))
                                * Math.cos(Math.toRadians(lat2))
                                * Math.sin(dLon / 2)
                                * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private double round(double value, int places) {
        return BigDecimal.valueOf(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
    }
}
