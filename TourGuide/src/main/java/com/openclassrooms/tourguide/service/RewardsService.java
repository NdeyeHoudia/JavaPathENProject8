package com.openclassrooms.tourguide.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;


@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;
	
	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}
	
	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	public void parallelSum100(User user) throws InterruptedException {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();
		ExecutorService executorService = Executors.newFixedThreadPool(4);
		for (VisitedLocation visitedLocation : userLocations) {
			for (Attraction attraction : attractions) {
				executorService.execute(() -> {
					if (Arrays.stream(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).toArray()).count() == 0) {
						if (nearAttraction(visitedLocation, attraction)) {
							user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
						}
					}

				});
			}
			executorService.shutdown();
			executorService.awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	public CompletableFuture<Void> calculateRewards(User user){
		return CompletableFuture.runAsync(() ->{
			List<Attraction> attractions = gpsUtil.getAttractions();
			List<VisitedLocation> userLocations = new CopyOnWriteArrayList<>(user.getVisitedLocations());
			for(VisitedLocation visitedLocation : userLocations){
				List<Attraction> nearAttractionFirstTime = attractions.stream()
						.filter(a ->
								(nearAttraction(visitedLocation,a))
								&& (user.getUserRewards().stream()
										.anyMatch(r -> r.attraction.attractionName.equals(a.attractionName))))
						.toList();
				nearAttractionFirstTime.forEach(a -> user.addUserReward(new UserReward(visitedLocation, a, getRewardPoints(a,user))));
			}
		}, Executors.newSingleThreadExecutor());
	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	public boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

}
