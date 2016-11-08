package idlezoo.game.services.inmemory;

import idlezoo.game.domain.Building;
import idlezoo.game.domain.ZooBuildings;

public class InMemoryZooBuildings {
  private final String name;
  private final Building building;
  private int level = 0;
  private int number = 0;

  public InMemoryZooBuildings(Building building) {
    this.name = building.getName();
    this.building = building;
  }

  public ZooBuildings toDTO() {
    return new ZooBuildings(building, level, number);
  }

  public String getName() {
    return name;
  }

  public double getIncome() {
    return building.income(level) * number;
  }

  public double getNextCost() {
    return building.buildCost(number);
  }

  public double getUpgradeCost() {
    return building.upgradeCost(level);
  }

  public int getLevel() {
    return level;
  }

  public int getNumber() {
    return number;
  }

  // Game logic
  public void upgrade() {
    this.level++;
  }

  public void buy() {
    this.number++;
  }

  public boolean first() {
    return number == 1;
  }

  public Building getBuilding() {
    return building;
  }

  public void setLevel(int level) {
    this.level = level;
  }

  public void setNumber(int number) {
    this.number = number;
  }

}