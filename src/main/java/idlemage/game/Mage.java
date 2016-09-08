package idlemage.game;

import static idlemage.game.GameResources.STARTING_BUILDING;
import static idlemage.game.GameResources.STARTING_MANA;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mage {
	private static final Timer DEFAULT_TIMER = () -> LocalDateTime.now(ZoneOffset.UTC);

	private final Timer timer;
	private final List<MageBuildings> buildings = new ArrayList<>(asList(new MageBuildings(STARTING_BUILDING)));
	private final Map<String, MageBuildings> buildingsMap = new HashMap<>(
			singletonMap(STARTING_BUILDING.getName(), buildings.get(0)));
	private double mana = STARTING_MANA;
	private LocalDateTime lastManaUpdate;

	public Mage() {
		this(DEFAULT_TIMER);
	}

	Mage(Timer timer) {
		this.timer = timer;
		lastManaUpdate = timer.now();
	}

	public double getMana() {
		return mana;
	}

	public double getIncome() {
		return buildings.stream().mapToDouble(MageBuildings::getIncome).sum();
	}

	public List<MageBuildings> getBuildings() {
		return buildings;
	}

	public Map<String, MageBuildings> getBuildingsMap() {
		return buildingsMap;
	}

	// Logic
	public synchronized Mage updateMana() {
		LocalDateTime now = timer.now();
		long dif = Duration.between(lastManaUpdate, now).getSeconds();
		mana += dif * getIncome();
		lastManaUpdate = now;
		return this;
	}

	public synchronized Mage buy(String buildingName) {
		updateMana();
		MageBuildings mageBuildings = buildingsMap.get(buildingName);
		if (mageBuildings == null) {
			throw new IllegalStateException("Building " + buildingName + " is not available");
		}
		if (mana < mageBuildings.getNextCost()) {
			throw new IllegalStateException("Insuffisient funds");
		}
		mana -= mageBuildings.getNextCost();
		mageBuildings.buy();
		if (mageBuildings.first()) {
			Building next = GameResources.NEXT_TYPES.get(buildingName);
			if (next != null) {
				MageBuildings nextBuildings = new MageBuildings(next);
				buildings.add(nextBuildings);
				buildingsMap.put(next.getName(), nextBuildings);
			}
		}
		return this;
	}

	public synchronized Mage upgrade(String buildingName) {
		updateMana();
		MageBuildings mageBuildings = buildingsMap.get(buildingName);
		if (mageBuildings == null) {
			throw new IllegalStateException("Upgrading " + buildingName + " is not available");
		}
		if (mana < mageBuildings.getUpgradeCost()) {
			throw new IllegalStateException("Insuffisient funds");
		}

		mana -= mageBuildings.getUpgradeCost();
		mageBuildings.upgrade();
		return this;
	}

	interface Timer {
		LocalDateTime now();
	}

}