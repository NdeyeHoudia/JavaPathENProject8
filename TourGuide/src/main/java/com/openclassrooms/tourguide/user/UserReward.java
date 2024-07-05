package com.openclassrooms.tourguide.user;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;

public class UserReward {

	public    VisitedLocation visitedLocation;
	public    Attraction attraction;
	private int rewardPoints;

	public void setVisitedLocation(VisitedLocation visitedLocation) {
		this.visitedLocation = visitedLocation;
	}

	public void setAttraction(Attraction attraction) {
		this.attraction = attraction;
	}

	public UserReward(VisitedLocation visitedLocation, Attraction attraction, int rewardPoints) {
		this.visitedLocation = visitedLocation;
		this.attraction = attraction;
		this.rewardPoints = rewardPoints;
	}
	
	public UserReward(VisitedLocation visitedLocation, Attraction attraction) {
		this.visitedLocation = visitedLocation;
		this.attraction = attraction;
	}

	public UserReward() {
		visitedLocation = null;
		attraction = null;
	}

	public void setRewardPoints(int rewardPoints) {
		this.rewardPoints = rewardPoints;
	}
	
	public int getRewardPoints() {
		return rewardPoints;
	}

	public VisitedLocation getVisitedLocation() {
		return visitedLocation;
	}

	public Attraction getAttraction() {
		return attraction;
	}
}
