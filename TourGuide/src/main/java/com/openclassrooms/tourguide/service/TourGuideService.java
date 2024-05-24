package com.openclassrooms.tourguide.service;

import com.openclassrooms.tourguide.dto.AttractionDto;
import com.openclassrooms.tourguide.helper.InternalTestHelper;
import com.openclassrooms.tourguide.tracker.Tracker;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;

import rewardCentral.RewardCentral;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	public final Tracker tracker;
	boolean testMode = true;
	private  RewardCentral rewardsCentral;


	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService,  RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;
		this.rewardsCentral = rewardCentral;
		Locale.setDefault(Locale.US);

		if (testMode) {
			logger.info("TestMode enabled");
			logger.debug("Initializing users");
			initializeInternalUsers();
			logger.debug("Finished initializing users");
		}
		tracker = new Tracker(this);
		addShutDownHook();
	}


	public List<UserReward>  getUserRewards(User user) {
		List<User> users = getAllUsers();
		List<UserReward> userRewards = user.getUserRewards();
		List<UserReward> userRewardList = new ArrayList<>();
		final int[] cpt = {0};

		ExecutorService executorService = Executors.newFixedThreadPool(4);
		executorService.execute(() -> {
			for (int i=0; i< users.size(); i++) {
				if (users.get(i).getUserRewards() == user.getUserRewards()) {
					userRewardList.add(user.getUserRewards().get(i));
					cpt[0] = i;

					break;
				}
			}
			try {
				executorService.awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
			//UserReward userReward1 = new UserReward();
			//userReward1.setRewardPoints(cpt[0]);
			//userRewards.set(cpt[0], userReward1);
			executorService.shutdown();

		/*for(User userReward : users) {
			executorService.execute(() -> {
				if(userList1.contains(userReward)){
					userList1.add(userReward);
				//	userRewards.addAll(userList1);
				}
				executorService.shutdown();
				try {
					executorService.awaitTermination(5, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			});
		}*/
		return userRewardList;
	}


	public VisitedLocation getUserLocation(User user) {
		VisitedLocation visitedLocation = (user.getVisitedLocations().size() > 0) ? user.getLastVisitedLocation()
				: trackUserLocation(user);
		return visitedLocation;
	}

	public User getUser(String userName) {
		return internalUserMap.get(userName);
	}

	public List<User> getAllUsers() {
		return internalUserMap.values().stream().collect(Collectors.toList());
	}

	public void addUser(User user) {
		if (!internalUserMap.containsKey(user.getUserName())) {
			internalUserMap.put(user.getUserName(), user);
		}
	}

	public List<Provider> getTripDeals(User user) {
		int cumulatativeRewardPoints = user.getUserRewards().stream().mapToInt(i -> i.getRewardPoints()).sum();
		List<Provider> providers = tripPricer.getPrice(tripPricerApiKey, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(), user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(), cumulatativeRewardPoints);
		user.setTripDeals(providers);
		return providers;
	}

	public VisitedLocation trackUserLocation(User user) {
		VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
		user.addToVisitedLocations(visitedLocation);
		rewardsService.calculateRewards(user);
		return visitedLocation;
	}

	public List<AttractionDto> getNearByAttractions(VisitedLocation visitedLocation) {

		List<Attraction> attractions = gpsUtil.getAttractions();
		Map<Attraction, Double> attractionMap = new HashMap<>();

		for (Attraction attraction : attractions) {
		 	double	distanceAttraction = rewardsService.getDistance(visitedLocation.location, attraction);
			attractionMap.put(attraction, distanceAttraction);
		}

	List<Attraction> attractionList1 = attractionMap.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).toList().subList(0, 5);
		return  attractionList1.stream().map(attraction ->
			  new AttractionDto(
					attraction.attractionName,
					attraction.latitude,
					attraction.longitude,
					visitedLocation.location.latitude,
					visitedLocation.location.longitude,
					attractionMap.get(attraction),
					rewardsCentral.getAttractionRewardPoints(attraction.attractionId, visitedLocation.userId)
			)

		).toList();

	}

	private void addShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				tracker.stopTracking();
			}
		});
	}

	/**********************************************************************************
	 * 
	 * Methods Below: For Internal Testing
	 * 
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes
	// internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

	private void initializeInternalUsers() {
		IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
			String userName = "internalUser" + i;
			String phone = "000";
			String email = userName + "@tourGuide.com";
			User user = new User(UUID.randomUUID(), userName, phone, email);
			generateUserLocationHistory(user);

			internalUserMap.put(userName, user);
		});
		logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
	}

	private void generateUserLocationHistory(User user) {
		IntStream.range(0, 3).forEach(i -> {
			user.addToVisitedLocations(new VisitedLocation(user.getUserId(),
					new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
		});
	}

	private double generateRandomLongitude() {
		double leftLimit = -180;
		double rightLimit = 180;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private double generateRandomLatitude() {
		double leftLimit = -85.05112878;
		double rightLimit = 85.05112878;
		return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
	}

	private Date getRandomTime() {
		LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
		return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
	}

}
