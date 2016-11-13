package idlezoo.game.services.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import idlezoo.game.domain.Zoo;
import idlezoo.game.services.FightService;
import idlezoo.game.services.GameService;
import idlezoo.game.services.ResourcesService;
import idlezoo.security.UsersService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FightServiceInMemoryTest {
  private static final String ZOO1 = "1";
  private static final String ZOO2 = "2";

  @Autowired
  private FightService fightService;

  @Autowired
  private GameService gameService;

  @Autowired
  private UsersService usersService;

  @Autowired
  private ResourcesService resourcesService;

  @Autowired
  private Storage storage;

  @Before
  public void setup() {
    assertTrue(usersService.addUser(ZOO1, ""));
    assertTrue(usersService.addUser(ZOO2, ""));
    storage.getZoo(ZOO1).setMoney(1000000);
    storage.getZoo(ZOO2).setMoney(1000000);
  }

  @After
  public void tearDown() {
    storage.getZoos().clear();
  }

  @Test
  public void test1vs0() {
    gameService.buy(ZOO1, resourcesService.firstName());
    fightService.fight(ZOO1);
    fightService.fight(ZOO2);

    Zoo zoo1 = gameService.getZoo(ZOO1);
    assertEquals(1, zoo1.getFightWins());
    assertEquals(1, zoo1.getBuildings().get(0).getNumber());

    Zoo zoo2 = gameService.getZoo(ZOO2);
    assertEquals(0, zoo2.getFightWins());
  }

  @Test
  public void test1vs1() {
    gameService.buy(ZOO1, resourcesService.firstName());
    gameService.buy(ZOO2, resourcesService.firstName());

    fightService.fight(ZOO1);
    fightService.fight(ZOO2);

    Zoo zoo1 = gameService.getZoo(ZOO1);
    assertEquals(1, zoo1.getFightWins());
    assertEquals(0, zoo1.getBuildings().get(0).getNumber());

    Zoo zoo2 = gameService.getZoo(ZOO2);
    assertEquals(0, zoo2.getFightWins());
  }

  @Test
  public void test0vs1() {
    gameService.buy(ZOO2, resourcesService.firstName());
    fightService.fight(ZOO1);
    fightService.fight(ZOO2);

    Zoo zoo1 = gameService.getZoo(ZOO1);
    assertEquals(0, zoo1.getFightWins());

    Zoo zoo2 = gameService.getZoo(ZOO2);
    assertEquals(1, zoo2.getBuildings().get(0).getNumber());
    assertEquals(1, zoo2.getFightWins());
  }

  @Test
  public void test1vs2() {
    gameService.buy(ZOO1, resourcesService.firstName());
    gameService.buy(ZOO2, resourcesService.firstName());
    gameService.buy(ZOO2, resourcesService.firstName());

    fightService.fight(ZOO1);
    fightService.fight(ZOO2);

    Zoo zoo1 = gameService.getZoo(ZOO1);
    assertEquals(0, zoo1.getFightWins());

    Zoo zoo2 = gameService.getZoo(ZOO2);
    assertEquals(1, zoo2.getBuildings().get(0).getNumber());
    assertEquals(1, zoo2.getFightWins());
  }

  @Test
  public void test1and1vs1and2() {
    gameService.buy(ZOO1, resourcesService.firstName());
    gameService.buy(ZOO1, resourcesService.secondName());

    gameService.buy(ZOO2, resourcesService.firstName());
    gameService.buy(ZOO2, resourcesService.secondName());
    gameService.buy(ZOO2, resourcesService.secondName());

    fightService.fight(ZOO1);
    fightService.fight(ZOO2);

    Zoo zoo1 = gameService.getZoo(ZOO1);
    assertEquals(1, zoo1.getFightWins());
    assertEquals(0, zoo1.getBuildings().get(0).getNumber());
    assertEquals(0, zoo1.getBuildings().get(1).getNumber());

    Zoo zoo2 = gameService.getZoo(ZOO2);
    assertEquals(0, zoo2.getBuildings().get(0).getNumber());
    assertEquals(1, zoo2.getBuildings().get(1).getNumber());
    assertEquals(0, zoo2.getFightWins());
  }

}